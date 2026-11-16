import { ParametrosService } from '../../../_services/parametros.service';
import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({selector:'app-recuperar-contrasena',standalone:true,imports:[RouterLink],templateUrl:'./recuperar-contrasena.html',styleUrl:'./recuperar-contrasena.css'})
export class RecuperarContrasenaPage { private parametros=inject(ParametrosService); get contacto(){return this.parametros.contacto();} get ayuda(){return this.parametros.whatsapp('Hola, olvidé mi contraseña y necesito ayuda para recuperar el acceso a mi cuenta.');} }
