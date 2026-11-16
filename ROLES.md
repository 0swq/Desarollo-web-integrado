# Cambio de roles

PATCH `/api/usuarios/{id}/rol`, solo ADMIN. Cuerpo: `{ "rol": "CLIENTE" }`, EMPLEADO o ADMIN. El servicio valida que el actor sea administrador activo, impide cambiar el propio rol y modifica únicamente el rol y la fecha de actualización. No requiere reenviar contraseña, correo ni el perfil completo. Respuesta UsuarioResponse sin contraseña.

El registro público `/api/auth/register` fija el rol CLIENTE aunque el cliente intente enviar otro. La creación administrativa de usuarios sigue usando su endpoint protegido.

Pruebas: `mvn -Dtest=UsuarioRolControllerTest,UsuarioRolServiceTest,RegistroRolTest test`.
