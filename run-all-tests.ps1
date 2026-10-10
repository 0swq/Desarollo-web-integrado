param(
    [string]$BaseUrl = "http://localhost:8080/api",
    [switch]$KeepServer,
    [switch]$SkipCleanup
)

$ErrorActionPreference = "Continue"

$Raiz     = Split-Path -Parent $MyInvocation.MyCommand.Path
$Backend  = Join-Path $Raiz "autopartes-backend"
$Maven    = Join-Path $Raiz ".tools\apache-maven-3.9.9\bin\mvn.cmd"
$Runner   = Join-Path $Backend "api-test\run-tests.ps1"
$TempDir  = Join-Path $env:TEMP "autopartes-tests"
$LogFile  = Join-Path $TempDir "servidor.log"

if (-not (Test-Path $Maven)) {
    Write-Host "No se encontro Maven en: $Maven" -ForegroundColor Red
    Write-Host "Ajusta la variable `$Maven en este script." -ForegroundColor Yellow
    exit 1
}

New-Item -ItemType Directory -Force -Path $TempDir | Out-Null
Remove-Item $LogFile -ErrorAction SilentlyContinue

function Detener-Java {
    $procs = Get-Process java -ErrorAction SilentlyContinue
    if ($procs) {
        foreach ($p in $procs) {
            try { Stop-Process -Id $p.Id -Force -ErrorAction Stop } catch { }
        }
        Start-Sleep -Seconds 3
    }
}

Write-Host ""
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host "  AUTOPARTES - EJECUCION COMPLETA DE TESTS" -ForegroundColor Cyan
Write-Host "==============================================" -ForegroundColor Cyan

# --- 1. Compilar -----------------------------------------------------------
Write-Host ""
Write-Host "[1/4] Compilando..." -ForegroundColor Yellow
& $Maven -f (Join-Path $Backend "pom.xml") clean compile -q 2>&1 |
    Select-String -Pattern "ERROR|BUILD FAILURE" | Select-Object -First 10
if ($LASTEXITCODE -ne 0) {
    Write-Host "La compilacion fallo. Revisa el pom." -ForegroundColor Red
    exit 1
}
Write-Host "      OK - compilacion exitosa" -ForegroundColor Green

# --- 2. Levantar el servidor ---------------------------------------------
Write-Host ""
Write-Host "[2/4] Levantando el servidor..." -ForegroundColor Yellow
Detener-Java

$null = Start-Process -FilePath $Maven `
    -ArgumentList "spring-boot:run" `
    -WorkingDirectory $Backend `
    -RedirectStandardOutput $LogFile `
    -RedirectStandardError (Join-Path $TempDir "servidor-err.log") `
    -PassThru -WindowStyle Hidden

Write-Host "      Esperando que la API responda..."
$listo = $false
for ($i = 0; $i -lt 60; $i++) {
    Start-Sleep -Seconds 2
    if (Select-String -Path $LogFile -Pattern "Started AutopartesApplication" -ErrorAction SilentlyContinue) {
        $listo = $true
        break
    }
    if (Select-String -Path $LogFile -Pattern "APPLICATION FAILED TO START|Application run failed" -ErrorAction SilentlyContinue) {
        break
    }
}

if (-not $listo) {
    Write-Host "      El servidor no arranco. Ultimas lineas:" -ForegroundColor Red
    Get-Content $LogFile -Tail 25 | ForEach-Object { Write-Host "      $_" -ForegroundColor DarkRed }
    Detener-Java
    exit 1
}
Write-Host "      OK - servidor arriba" -ForegroundColor Green

# --- 3. Correr los tests ---------------------------------------------------
Write-Host ""
Write-Host "[3/4] Corriendo los tests de endpoints..." -ForegroundColor Yellow

# hashtable (no array) para que PowerShell interprete los nombres de parametro
$suiteArgs = @{ BaseUrl = $BaseUrl }
if ($SkipCleanup) { $suiteArgs["SkipCleanup"] = $true }
& $Runner @suiteArgs
$codigo = $LASTEXITCODE

# --- 4. Cerrar -------------------------------------------------------------
if (-not $KeepServer) {
    Write-Host ""
    Write-Host "[4/4] Deteniendo el servidor..." -ForegroundColor Yellow
    Detener-Java
    Write-Host "      OK" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "[4/4] Servidor queda corriendo (se detuvo el paso de cierre)" -ForegroundColor Yellow
}

Write-Host ""
if ($codigo -eq 0) {
    Write-Host ">>> RESULTADO: TODOS LOS TESTS PASARON" -ForegroundColor Green
} else {
    Write-Host ">>> RESULTADO: HAY TESTS FALLIDOS (revisa el detalle arriba)" -ForegroundColor Red
}
Write-Host ""

exit $codigo