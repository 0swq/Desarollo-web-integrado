$base = "http://localhost:8080/api/api"
$script:pass = 0
$script:fail = 0

function Ok($name, $cond) {
  if ($cond) { $script:pass++; Write-Output "PASS: $name" }
  else { $script:fail++; Write-Output "FAIL: $name" }
}

function Call($method, $url, $body = $null, $token = $null) {
  $h = @{ "Content-Type" = "application/json" }
  if ($token) { $h["Authorization"] = "Bearer $token" }
  $p = @{ Uri = $url; Method = $method; Headers = $h }
  if ($body) { $p["Body"] = ($body | ConvertTo-Json -Depth 5) }
  return Invoke-RestMethod @p
}

function CallStatus($method, $url, $body = $null, $token = $null) {
  try { $null = Call $method $url $body $token; return 200 }
  catch { return [int]$_.Exception.Response.StatusCode }
}

# ---------- AUTH ----------
$r = Call "Post" "$base/auth/register" @{ correo = "cliente@test.com"; contrasena = "123456"; nombre = "Juan"; apellido = "Perez"; telefono = "999888777"; direccion = "Av. Lima 123"; rol = "CLIENTE" }
Ok "register" ($r.success -eq $true)
$usuarioId = $r.data.id

Ok "register duplicado -> 400" ((CallStatus "Post" "$base/auth/register" @{ correo = "cliente@test.com"; contrasena = "123456"; nombre = "X"; apellido = "Y" }) -eq 400)

$r = Call "Post" "$base/auth/login" @{ correo = "cliente@test.com"; contrasena = "123456" }
Ok "login" ($r.success -eq $true -and $r.data.token.Length -gt 20)
$token = $r.data.token

Ok "login clave mala -> 401" ((CallStatus "Post" "$base/auth/login" @{ correo = "cliente@test.com"; contrasena = "mala" }) -eq 401)
Ok "register sin correo -> 400" ((CallStatus "Post" "$base/auth/register" @{ contrasena = "123456"; nombre = "X"; apellido = "Y" }) -eq 400)

# ---------- USUARIO ----------
$r = Call "Get" "$base/usuarios"
Ok "listar usuarios" ($r.success -and $r.data.Count -ge 1)
$r = Call "Get" "$base/usuarios/$usuarioId"
Ok "obtener usuario" ($r.data.correo -eq "cliente@test.com")
$r = Call "Put" "$base/usuarios/$usuarioId" @{ correo = "cliente@test.com"; contrasena = "123456"; nombre = "Juan Carlos"; apellido = "Perez"; telefono = "999888777"; direccion = "Av. Lima 123"; rol = "CLIENTE" }
Ok "actualizar usuario" ($r.data.nombre -eq "Juan Carlos")
$r = Call "Patch" "$base/usuarios/$usuarioId/password" @{ contrasenaActual = "123456"; contrasenaNueva = "654321" }
Ok "cambiar password" ($r.success -eq $true)
$r = Call "Post" "$base/auth/login" @{ correo = "cliente@test.com"; contrasena = "654321" }
Ok "login con nueva password" ($r.success -eq $true)

# ---------- CATEGORIA ----------
$r = Call "Post" "$base/categorias" @{ nombre = "Frenos"; descripcion = "Sistema de frenos"; imagenUrl = $null; activo = $true }
$catA = $r.data.id
Ok "crear categoria A" ($r.success -eq $true)
$r = Call "Post" "$base/categorias" @{ nombre = "Filtros"; descripcion = "Filtros"; imagenUrl = $null; activo = $true }
$catB = $r.data.id
Ok "crear categoria B" ($r.success -eq $true)
Ok "categoria duplicada -> 400" ((CallStatus "Post" "$base/categorias" @{ nombre = "Frenos" }) -eq 400)
$r = Call "Get" "$base/categorias"
Ok "listar categorias" ($r.data.Count -ge 2)
$r = Call "Get" "$base/categorias/$catB"
Ok "obtener categoria" ($r.data.nombre -eq "Filtros")
$r = Call "Put" "$base/categorias/$catB" @{ nombre = "Filtros"; descripcion = "Filtros actualizados"; imagenUrl = $null; activo = $true }
Ok "actualizar categoria" ($r.data.descripcion -eq "Filtros actualizados")
Call "Patch" "$base/categorias/$catB/toggle-activo" | Out-Null
Call "Patch" "$base/categorias/$catB/toggle-activo" | Out-Null
$r = Call "Get" "$base/categorias/$catB"
Ok "toggle-activo restaura" ($r.data.activo -eq $true)
Call "Delete" "$base/categorias/$catB" | Out-Null
Ok "eliminar categoria -> 404" ((CallStatus "Get" "$base/categorias/$catB") -eq 404)

# ---------- PROVEEDOR ----------
$r = Call "Post" "$base/proveedores" @{ ruc = "20123456789"; razonSocial = "Repuestos del Sur S.A.C."; contactoNombre = "Luis"; telefono = "014445566"; correo = "ventas@repsur.com"; direccion = "Av. Industrial 789"; activo = $true }
$provA = $r.data.id
Ok "crear proveedor A" ($r.success -eq $true)
$r = Call "Post" "$base/proveedores" @{ ruc = "20987654321"; razonSocial = "Spare S.A.C."; contactoNombre = $null; telefono = $null; correo = $null; direccion = $null; activo = $true }
$provB = $r.data.id
Ok "crear proveedor B" ($r.success -eq $true)
Ok "ruc invalido -> 400" ((CallStatus "Post" "$base/proveedores" @{ ruc = "123"; razonSocial = "X" }) -eq 400)
$r = Call "Get" "$base/proveedores"
Ok "listar proveedores" ($r.data.Count -ge 2)
$r = Call "Get" "$base/proveedores/$provB"
Ok "obtener proveedor" ($r.data.ruc -eq "20987654321")
$r = Call "Put" "$base/proveedores/$provB" @{ ruc = "20987654321"; razonSocial = "Spare Actualizado S.A.C."; contactoNombre = $null; telefono = $null; correo = $null; direccion = $null; activo = $true }
Ok "actualizar proveedor" ($r.data.razonSocial -eq "Spare Actualizado S.A.C.")
Call "Patch" "$base/proveedores/$provB/toggle-activo" | Out-Null
Call "Patch" "$base/proveedores/$provB/toggle-activo" | Out-Null
Call "Delete" "$base/proveedores/$provB" | Out-Null
Ok "eliminar proveedor -> 404" ((CallStatus "Get" "$base/proveedores/$provB") -eq 404)

# ---------- PRODUCTO ----------
$r = Call "Post" "$base/productos" @{ sku = "PAST-TOY-001"; nombre = "Pastilla de freno Toyota"; descripcion = "Delantera ceramica"; precio = 120.50; precioPromocional = $null; imagenUrl = $null; categoriaId = $catA; proveedorId = $provA; stockInicial = 50; stockMinimo = 5; ubicacionAlmacen = "A-01"; activo = $true }
$prodId = $r.data.id
Ok "crear producto" ($r.success -eq $true)
Ok "sku duplicado -> 400" ((CallStatus "Post" "$base/productos" @{ sku = "PAST-TOY-001"; nombre = "X"; precio = 10.0; categoriaId = $catA }) -eq 400)
Ok "producto sin precio -> 400" ((CallStatus "Post" "$base/productos" @{ sku = "X-001"; nombre = "X"; categoriaId = $catA }) -eq 400)
$r = Call "Get" "$base/productos"
Ok "listar productos" ($r.data.Count -ge 1)
$r = Call "Get" "$base/productos/$prodId"
Ok "obtener producto" ($r.data.sku -eq "PAST-TOY-001")
$r = Call "Get" "$base/productos/sku/PAST-TOY-001"
Ok "obtener por sku" ($r.data.id -eq $prodId)
$r = Call "Get" "$base/productos/buscar?q=pastilla"
Ok "buscar productos" ($r.data.Count -ge 1)
$r = Call "Get" "$base/productos/proveedor/$provA"
Ok "productos por proveedor" ($r.data.Count -ge 1)
$r = Call "Put" "$base/productos/$prodId" @{ sku = "PAST-TOY-001"; nombre = "Pastilla de freno Toyota"; descripcion = "Premium"; precio = 130.00; precioPromocional = $null; imagenUrl = $null; categoriaId = $catA; proveedorId = $provA; activo = $true }
Ok "actualizar producto" ([decimal]$r.data.precio -eq 130.00)
Ok "producto inexistente -> 404" ((CallStatus "Get" "$base/productos/$([guid]::NewGuid())") -eq 404)

# ---------- PRODUCTO-CATEGORIA ----------
$r = Call "Post" "$base/categorias" @{ nombre = "Motor"; descripcion = "Partes de motor"; imagenUrl = $null; activo = $true }
$catC = $r.data.id
$r = Call "Post" "$base/producto-categorias" @{ productoId = $prodId; categoriaId = $catC }
Ok "asociar producto-categoria" ($r.success -eq $true)
$r = Call "Get" "$base/producto-categorias/producto/$prodId"
Ok "categorias del producto" ($r.data.Count -ge 2)
$r = Call "Get" "$base/producto-categorias/categoria/$catA"
Ok "productos de la categoria" ($r.data.Count -ge 1)
Call "Delete" "$base/producto-categorias/producto/$prodId/categoria/$catC" | Out-Null
$r = Call "Get" "$base/producto-categorias/producto/$prodId"
Ok "eliminar asociacion" ($r.data.Count -eq 1)
Call "Delete" "$base/categorias/$catC" | Out-Null

# ---------- MARCA / MODELO / COMPATIBILIDAD ----------
$r = Call "Post" "$base/marcas-vehiculos" @{ nombre = "Toyota"; paisOrigen = "Japon"; activo = $true }
$marcaA = $r.data.id
Ok "crear marca A" ($r.success -eq $true)
$r = Call "Post" "$base/marcas-vehiculos" @{ nombre = "Nissan"; paisOrigen = "Japon"; activo = $true }
$marcaB = $r.data.id
Ok "marca duplicada -> 400" ((CallStatus "Post" "$base/marcas-vehiculos" @{ nombre = "Toyota" }) -eq 400)
$r = Call "Get" "$base/marcas-vehiculos"
Ok "listar marcas" ($r.data.Count -ge 2)
$r = Call "Post" "$base/modelos-vehiculos" @{ nombre = "Corolla"; marcaId = $marcaA; tipoVehiculo = "Sedan"; activo = $true }
$modeloA = $r.data.id
Ok "crear modelo" ($r.success -eq $true)
$r = Call "Post" "$base/modelos-vehiculos" @{ nombre = "Sentra"; marcaId = $marcaB; tipoVehiculo = "Sedan"; activo = $true }
$modeloB = $r.data.id
$r = Call "Get" "$base/modelos-vehiculos/marca/$marcaA"
Ok "modelos por marca" ($r.data.Count -ge 1)
$r = Call "Get" "$base/modelos-vehiculos/$modeloA"
Ok "obtener modelo" ($r.data.nombre -eq "Corolla")
$r = Call "Put" "$base/modelos-vehiculos/$modeloA" @{ nombre = "Corolla"; marcaId = $marcaA; tipoVehiculo = "Sedan"; activo = $true }
Ok "actualizar modelo" ($r.success -eq $true)
Call "Patch" "$base/modelos-vehiculos/$modeloA/toggle-activo" | Out-Null
Call "Patch" "$base/modelos-vehiculos/$modeloA/toggle-activo" | Out-Null
$r = Call "Get" "$base/modelos-vehiculos/$modeloA"
Ok "toggle modelo restaura" ($r.data.activo -eq $true)
Call "Delete" "$base/modelos-vehiculos/$modeloB" | Out-Null
Call "Delete" "$base/marcas-vehiculos/$marcaB" | Out-Null
Ok "eliminar marca -> 404" ((CallStatus "Get" "$base/marcas-vehiculos/$marcaB") -eq 404)

$r = Call "Post" "$base/compatibilidades-vehiculos" @{ productoId = $prodId; modeloVehiculoId = $modeloA; anioInicio = 2015; anioFin = 2022; motor = "1.8L"; notas = "Solo full" }
$compId = $r.data.id
Ok "crear compatibilidad" ($r.success -eq $true)
Ok "anio invertido -> 400" ((CallStatus "Post" "$base/compatibilidades-vehiculos" @{ productoId = $prodId; modeloVehiculoId = $modeloA; anioInicio = 2022; anioFin = 2015 }) -eq 400)
$r = Call "Get" "$base/compatibilidades-vehiculos/producto/$prodId"
Ok "compat por producto" ($r.data.Count -ge 1)
$r = Call "Get" "$base/compatibilidades-vehiculos/modelo/$modeloA"
Ok "compat por modelo" ($r.data.Count -ge 1)
$r = Call "Get" "$base/compatibilidades-vehiculos/compatibles?modeloId=$modeloA&anio=2020"
Ok "compatibles por anio" ($r.data.Count -ge 1)
$r = Call "Get" "$base/compatibilidades-vehiculos/compatibles?modeloId=$modeloA&anio=1990"
Ok "sin compatibles fuera de rango" ($r.data.Count -eq 0)
Call "Delete" "$base/compatibilidades-vehiculos/$compId" | Out-Null
Ok "eliminar compatibilidad -> lista vacia" ((Call "Get" "$base/compatibilidades-vehiculos/producto/$prodId").data.Count -eq 0)

# ---------- STOCK ----------
$r = Call "Get" "$base/stocks"
Ok "listar stocks" ($r.data.Count -ge 1)
$r = Call "Get" "$base/stocks/producto/$prodId"
Ok "stock por producto = 50" ($r.data.cantidad -eq 50)
$r = Call "Get" "$base/stocks/bajo-stock"
Ok "bajo-stock vacio" ($r.data.Count -eq 0)
$r = Call "Put" "$base/stocks/producto/$prodId" @{ stockMinimo = 10; ubicacionAlmacen = "B-02" }
Ok "actualizar stock config" ($r.data.stockMinimo -eq 10 -and $r.data.ubicacionAlmacen -eq "B-02")

# ---------- CARRITO + ITEMS ----------
$r = Call "Get" "$base/carritos/usuario/$usuarioId" -token $token
$carritoId = $r.data.id
Ok "obtener carrito" ($r.success -eq $true)
Ok "carrito ajeno -> 403" ((CallStatus "Get" "$base/carritos/usuario/$([guid]::NewGuid())" -token $token) -eq 403)
$r = Call "Post" "$base/carritos/$carritoId/items" @{ productoId = $prodId; cantidad = 2 }
$itemId = $r.data.id
Ok "agregar item" ($r.success -eq $true)
Ok "stock insuficiente -> 400" ((CallStatus "Post" "$base/carritos/$carritoId/items" @{ productoId = $prodId; cantidad = 9999 }) -eq 400)
$r = Call "Get" "$base/carritos/$carritoId/items"
Ok "listar items" ($r.data.Count -eq 1)
$r = Call "Put" "$base/carritos/$carritoId/items/$itemId" @{ cantidad = 5 }
Ok "actualizar cantidad" ($r.data.cantidad -eq 5)

# ---------- ORDEN (checkout) ----------
$r = Call "Post" "$base/ordenes/usuario/$usuarioId" @{ direccionEntrega = "Av. Lima 123"; telefonoContacto = "999888777"; notas = "Oficina"; metodoPago = "MERCADO_PAGO" } -token $token
$ordenId = $r.data.id
$numeroOrden = $r.data.numeroOrden
Ok "checkout crea orden" ($r.success -eq $true -and [decimal]$r.data.total -gt 0)
$r = Call "Get" "$base/carritos/$carritoId/items"
Ok "carrito vacio tras checkout" ($r.data.Count -eq 0)
$r = Call "Get" "$base/stocks/producto/$prodId"
Ok "stock descontado 50->45" ($r.data.cantidad -eq 45)
$r = Call "Get" "$base/ordenes/$ordenId"
Ok "obtener orden" ($r.data.numeroOrden -eq $numeroOrden)
$r = Call "Get" "$base/ordenes/numero/$numeroOrden"
Ok "orden por numero" ($r.data.id -eq $ordenId)
$r = Call "Get" "$base/ordenes/usuario/$usuarioId" -token $token
Ok "ordenes del usuario" ($r.data.Count -ge 1)
$r = Call "Get" "$base/ordenes/estado/PENDIENTE"
Ok "orden en PENDIENTE" ($r.data.Count -ge 1)
Ok "checkout carrito vacio -> 400" ((CallStatus "Post" "$base/ordenes/usuario/$usuarioId" @{ direccionEntrega = "X"; telefonoContacto = "Y" } -token $token) -eq 400)
$r = Call "Patch" "$base/ordenes/$ordenId/estado" @{ estado = "PAGADO" }
Ok "orden a PAGADO" ($r.data.estado -eq "PAGADO")

# ---------- PAGO ----------
$r = Call "Get" "$base/pagos/orden/$ordenId"
$pagoId = $r.data.id
Ok "pago auto-creado en checkout" ($r.success -eq $true -and $r.data.estado -eq "PENDIENTE")
$r = Call "Get" "$base/pagos"
Ok "listar pagos" ($r.data.Count -ge 1)
$r = Call "Get" "$base/pagos/estado/PENDIENTE"
Ok "pagos pendientes" ($r.data.Count -ge 1)
$r = Call "Patch" "$base/pagos/$pagoId/estado" @{ estado = "APROBADO" }
Ok "pago a APROBADO" ($r.data.estado -eq "APROBADO")
$r = Call "Patch" "$base/pagos/$pagoId/mercado-pago" @{ mercadoPagoPagoId = "123456789"; mercadoPagoPreferenciaId = "pref-abc-123" }
Ok "datos mercado-pago (solo guarda IDs, sin llamada externa)" ($r.data.mercadoPagoPagoId -eq "123456789")

Write-Output ""
Write-Output "RESULTADO: $script:pass PASS, $script:fail FAIL"
