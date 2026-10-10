param(
    [string]$BaseUrl = "http://localhost:8080/api",
    [switch]$SkipCleanup
)

$ErrorActionPreference = "Continue"
$Dir = Split-Path -Parent $MyInvocation.MyCommand.Path

$RunId = [DateTimeOffset]::Now.ToUnixTimeSeconds()
$Vars = @{
    baseUrl           = $BaseUrl
    correoCliente     = "cliente.$RunId@test.com"
    correoEmpleado    = "empleado.$RunId@test.com"
    contrasenaCliente = "123456"
    contrasenaActual  = "123456"
    contrasenaNueva   = "654321"
    usuarioId         = ""
    empleadoId        = ""
    token             = ""
    carritoId         = ""
    categoriaId       = ""
    proveedorId       = ""
    productoId        = ""
    stockId           = ""
    marcaId           = ""
    modeloId          = ""
    compatibilidadId  = ""
    itemId            = ""
    ordenId           = ""
    numeroOrden       = ""
    ordenSinPago      = ""
    pagoId            = ""
    totalOrden        = "0"
    # valores unicos por ejecucion: RUC, SKU y nombres tienen unicidad en la BD
    ruc               = "20" + ([string]$RunId).PadLeft(9, '0').Substring(0, 9)
    sku               = "PAST-$RunId"
    nombreMarca       = "Toyota$RunId"
    nombreModelo      = "Corolla$RunId"
}

$Results = New-Object System.Collections.ArrayList

function Parse-Requests {
    param([string]$Path)
    $lines  = [System.IO.File]::ReadAllLines($Path, [System.Text.Encoding]::UTF8)
    $reqs   = New-Object System.Collections.ArrayList
    $cur    = $null
    $hdrs   = New-Object System.Collections.ArrayList
    $body   = New-Object System.Collections.ArrayList
    $inVars = $true
    $seenGap = $false

    foreach ($line in $lines) {
        if ($line -match '^\s*###+\s*$') {
            if ($null -ne $cur) {
                $cur.Headers = @($hdrs); $cur.Body = (($body -join "`n")).Trim()
                [void]$reqs.Add([pscustomobject]$cur)
            }
            $cur = [ordered]@{ Method = ""; Path = ""; Headers = @(); Body = ""; Capture = @(); Expect = 0 }
            $hdrs = New-Object System.Collections.ArrayList
            $body = New-Object System.Collections.ArrayList
            $inVars = $false; $seenGap = $false
            continue
        }
        if ($inVars -or $null -eq $cur) { continue }

        if ($line -match '^\s*#\s*@capture\s+(.+)$') {
            $cur.Capture = @($matches[1].Trim() -split '\s+'); continue
        }
        if ($line -match '^\s*#\s*@expect\s+(\d{3})\s*$') {
            $cur.Expect = [int]$matches[1]; continue
        }
        if ([string]::IsNullOrWhiteSpace($cur.Path) -and
            $line -match '^(GET|POST|PUT|PATCH|DELETE)\s+(\S+)') {
            $cur.Method = $matches[1]; $cur.Path = $matches[2]; continue
        }
        if ($cur.Path -eq "") { continue }
        if (-not $seenGap) {
            if ([string]::IsNullOrWhiteSpace($line)) { $seenGap = $true; continue }
            if ($line -match '^([A-Za-z\-]+)\s*:\s*(.*)$') {
                [void]$hdrs.Add(@{ Name = $matches[1]; Value = $matches[2] }); continue
            }
        }
        [void]$body.Add($line)
    }
    if ($null -ne $cur) {
        $cur.Headers = @($hdrs); $cur.Body = (($body -join "`n")).Trim()
        [void]$reqs.Add([pscustomobject]$cur)
    }
    return $reqs
}

function Resolve-Tpl {
    param([string]$Text)
    if ($null -eq $Text) { return "" }
    $out = $Text
    foreach ($k in $Vars.Keys) { $out = $out.Replace("{{$k}}", [string]$Vars[$k]) }
    return $out
}

function Invoke-Req {
    param([string]$Url, [string]$Method, [string]$BodyText, $Headers)
    $h = @{}
    if ($Headers) {
        foreach ($hd in $Headers) {
            # las cabeceras tambien admiten {{variable}}
            $h[$hd.Name] = Resolve-Tpl $hd.Value
        }
    }
    try {
        $p = @{ Uri = $Url; Method = $Method; Headers = $h; TimeoutSec = 30
                UseBasicParsing = $true; ErrorAction = "Stop" }
        if ($BodyText -ne "") { $p.Body = $BodyText; $h["Content-Type"] = "application/json" }
        $resp = Invoke-WebRequest @p
        return @{ Code = [int]$resp.StatusCode; Body = $resp.Content }
    } catch [System.Net.WebException] {
        $r = $_.Exception.Response
        if ($null -eq $r) { return @{ Code = 0; Body = $_.Exception.Message } }
        $code = [int]$r.StatusCode; $txt = ""
        try {
            $sr = New-Object System.IO.StreamReader($r.GetResponseStream())
            $txt = $sr.ReadToEnd(); $sr.Close()
        } catch { }
        return @{ Code = $code; Body = $txt }
    } catch {
        return @{ Code = 0; Body = $_.Exception.Message }
    }
}

function Apply-Captures {
    param($Json, $Specs)
    if ($null -eq $Json -or -not $Specs) { return }
    $dp = $Json.PSObject.Properties["data"]
    if ($null -eq $dp) { return }
    $d = $dp.Value
    if ($d -is [System.Array]) { return }   # nunca capturar desde listas
    foreach ($spec in $Specs) {
        # formato: variable=campoJson   (si no hay '=', se usa el mismo nombre)
        $varName  = $spec
        $field    = $spec
        if ($spec -match '^([A-Za-z0-9_]+)=(.+)$') { $varName = $matches[1]; $field = $matches[2] }
        $prop = $d.PSObject.Properties[$field]
        if ($null -ne $prop -and $null -ne $prop.Value) {
            $Vars[$varName] = [string]$prop.Value
        }
    }
}

function Run-File {
    param(
        [string]$File,
        [string[]]$Only = $null,
        [switch]$SkipDeletes
    )
    $reqs = Parse-Requests (Join-Path $Dir $File)
    Write-Host ""
    Write-Host ("--- " + $File) -ForegroundColor Cyan

    foreach ($r in $reqs) {
        if ($Only -and ($Only -notcontains $r.Method)) { continue }
        if ($SkipDeletes -and $r.Method -eq "DELETE") { continue }

        $url  = Resolve-Tpl $r.Path
        $body = Resolve-Tpl $r.Body
        $res  = Invoke-Req $url $r.Method $body $r.Headers

        $exp = if ($r.Expect -gt 0) { $r.Expect } else { 0 }
        $ok = if ($exp -gt 0) { $res.Code -eq $exp } else { ($res.Code -ge 200 -and $res.Code -lt 300) }
        $tag = if ($ok) { "PASS" } else { "FAIL" }
        $color = if ($ok) { "Green" } else { "Red" }
        $expTxt = if ($exp -gt 0) { " (esperado $exp)" } else { "" }
        $short  = $url.Replace($BaseUrl, "")
        Write-Host ("  [{0}] {1,-6} {2,-56} -> {3}{4}" -f $tag, $r.Method, $short, $res.Code, $expTxt) -ForegroundColor $color

        [void]$Results.Add([pscustomobject]@{ Method = $r.Method; Url = $short; Code = $res.Code; Ok = $ok; Body = $res.Body })

        if ($ok) {
            $json = $null
            try { $json = $res.Body | ConvertFrom-Json } catch { }
            Apply-Captures $json $r.Capture
        } else {
            $msg = "$($res.Body)"
            if ($msg.Length -gt 220) { $msg = $msg.Substring(0, 220) + "..." }
            Write-Host ("        " + $msg) -ForegroundColor DarkYellow
        }
    }
}

function Run-Raw {
    param([string]$Label, [string]$Method, [string]$Path, $Headers = @(), [int]$ExpectCode)
    $url = Resolve-Tpl $Path
    $res = Invoke-Req $url $Method "" $Headers
    $ok  = ($res.Code -eq $ExpectCode)
    $tag = if ($ok) { "PASS" } else { "FAIL" }
    $color = if ($ok) { "Green" } else { "Red" }
    $short = $url.Replace($BaseUrl, "") + "  [" + $Label + "]"
    Write-Host ("  [{0}] {1,-6} {2,-56} -> {3} (esperado {4})" -f $tag, $Method, $short, $res.Code, $ExpectCode) -ForegroundColor $color
    [void]$Results.Add([pscustomobject]@{ Method = $Method; Url = $short; Code = $res.Code; Ok = $ok; Body = $res.Body })
}

# ------------------------------------------------------------------ inicio
Write-Host ""
Write-Host "=== SUITE DE ENDPOINTS - Autopartes ===" -ForegroundColor White
Write-Host ("Base: " + $BaseUrl + "   RunId: " + $RunId)

try {
    $null = Invoke-WebRequest -Uri "$BaseUrl/api/usuarios" -UseBasicParsing -TimeoutSec 15 -ErrorAction Stop
} catch {
    Write-Host ("No se pudo contactar la API: " + $_.Exception.Message) -ForegroundColor Red
    exit 1
}

Run-File "auth.http"
if ([string]::IsNullOrWhiteSpace($Vars.token)) {
    Write-Host "No se obtuvo token. Abortando." -ForegroundColor Red
    exit 1
}
Write-Host ("   [info] token capturado, usuarioId=" + $Vars.usuarioId) -ForegroundColor DarkGray

Run-File "categoria.http"            -SkipDeletes
Run-File "proveedor.http"            -SkipDeletes
Run-File "producto.http"             -SkipDeletes
Run-File "stock.http"                -SkipDeletes
Run-File "marca-vehiculo.http"       -SkipDeletes
Run-File "modelo-vehiculo.http"      -SkipDeletes
Run-File "compatibilidad-vehiculo.http" -SkipDeletes
Run-File "producto-categoria.http"   -SkipDeletes
Run-File "usuario.http"              -SkipDeletes
Run-File "carrito.http"              -SkipDeletes
Run-File "item-carrito.http"         -SkipDeletes
Run-File "orden.http"

# Segunda orden: reponer el carrito y facturar de nuevo (la necesita pago.http)
Write-Host ""
Write-Host "--- Reposicion para la orden del pago" -ForegroundColor Cyan
Run-File "item-carrito.http" -Only @("POST")
Run-File "orden.http"        -Only @("POST")
$Vars.ordenSinPago = $Vars.ordenId
$Vars.totalOrden   = "150.75"
Write-Host ("   [info] ordenSinPago=" + $Vars.ordenSinPago) -ForegroundColor DarkGray

Run-File "pago.http"

# Seguridad
Write-Host ""
Write-Host "--- Seguridad / tokens" -ForegroundColor Cyan
Run-Raw -Label "sin header" -Method "GET" -Path "{{baseUrl}}/api/carritos/usuario/{{usuarioId}}" -Headers @() -ExpectCode 401
Run-Raw -Label "token invalido" -Method "GET" -Path "{{baseUrl}}/api/carritos/usuario/{{usuarioId}}" -Headers @(@{Name="Authorization";Value="Bearer no.es.valido"}) -ExpectCode 401
Run-Raw -Label "sin prefijo Bearer" -Method "GET" -Path "{{baseUrl}}/api/carritos/usuario/{{usuarioId}}" -Headers @(@{Name="Authorization";Value={{token}}}) -ExpectCode 401
Run-Raw -Label "token de otro usuario" -Method "GET" -Path "{{baseUrl}}/api/carritos/usuario/{{empleadoId}}" -Headers @(@{Name="Authorization";Value="Bearer $($Vars.token)"}) -ExpectCode 403
Run-Raw -Label "token valido" -Method "GET" -Path "{{baseUrl}}/api/carritos/usuario/{{usuarioId}}" -Headers @(@{Name="Authorization";Value="Bearer $($Vars.token)"}) -ExpectCode 200

if (-not $SkipCleanup) {
    Write-Host ""
    Write-Host "=== Limpieza ===" -ForegroundColor Cyan
    Run-File "producto-categoria.http"      -Only @("DELETE")
    # el checkout ya vacio el carrito: se repone un item para poder borrarlo
    Run-File "item-carrito.http"            -Only @("POST")
    Run-File "item-carrito.http"            -Only @("DELETE")
    Run-File "compatibilidad-vehiculo.http" -Only @("DELETE")
    Run-File "modelo-vehiculo.http"         -Only @("DELETE")
    Run-File "marca-vehiculo.http"          -Only @("DELETE")
    Run-File "producto.http"                -Only @("DELETE")
    Run-File "categoria.http"               -Only @("DELETE")
    Run-File "proveedor.http"               -Only @("DELETE")
    Run-File "usuario.http"                 -Only @("DELETE")
    Run-File "carrito.http"                 -Only @("DELETE")
}

$pass = @($Results | Where-Object { $_.Ok }).Count
$fail = @($Results | Where-Object { -not $_.Ok }).Count

Write-Host ""
Write-Host "=== RESUMEN ===" -ForegroundColor White
Write-Host ("  Total : " + $Results.Count)
Write-Host ("  PASS  : " + $pass) -ForegroundColor Green
Write-Host ("  FAIL  : " + $fail) -ForegroundColor $(if ($fail -gt 0) { "Red" } else { "Green" })

if ($fail -gt 0) {
    Write-Host ""
    Write-Host "--- Fallos ---" -ForegroundColor Red
    foreach ($r in ($Results | Where-Object { -not $_.Ok })) {
        $msg = "$($r.Body)"
        if ($msg.Length -gt 300) { $msg = $msg.Substring(0, 300) + "..." }
        Write-Host ("  " + $r.Method + " " + $r.Url + "  ->  " + $r.Code) -ForegroundColor Red
        Write-Host ("     " + $msg) -ForegroundColor DarkYellow
    }
}

exit $(if ($fail -gt 0) { 1 } else { 0 })