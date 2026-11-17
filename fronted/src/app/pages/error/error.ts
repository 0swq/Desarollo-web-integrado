import { Component, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
@Component({selector:'app-error-page',standalone:true,imports:[RouterLink],templateUrl:'./error.html',styleUrl:'./error.css'})
export class ErrorPage {
 private route=inject(ActivatedRoute);
 get codigo(){const value=this.route.snapshot.paramMap.get('codigo')||this.route.snapshot.data['codigo']||'404';return ['403','404','405','500','503','offline'].includes(value)?value:'404';}
 get contenido(){return ({'403':{titulo:'Esta zona necesita permiso.',texto:'Tu cuenta no tiene acceso a esta página. Puedes volver a la tienda o ingresar con otra cuenta.',icono:'bi-shield-lock'},'404':{titulo:'Parece que esta pieza no encaja.',texto:'La página que buscas no existe, cambió de dirección o el enlace está incompleto.',icono:'bi-signpost-split'},'405':{titulo:'Esta acción no está disponible.',texto:'La solicitud no está permitida en esta dirección. Regresa a la tienda para continuar.',icono:'bi-slash-circle'},'500':{titulo:'Algo falló en el camino.',texto:'No pudimos completar la operación. Espera un momento e inténtalo otra vez.',icono:'bi-tools'},'503':{titulo:'Estamos haciendo una pausa.',texto:'El servicio no está disponible en este momento. Vuelve a intentarlo en unos minutos.',icono:'bi-cone-striped'},'offline':{titulo:'Nos quedamos sin conexión.',texto:'Comprueba tu conexión a Internet y vuelve a intentarlo.',icono:'bi-wifi-off'}} as Record<string,{titulo:string;texto:string;icono:string}>)[this.codigo];}
 recargar(){window.location.reload();}
}