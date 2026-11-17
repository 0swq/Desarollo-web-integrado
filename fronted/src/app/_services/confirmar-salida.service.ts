import { Injectable, signal } from '@angular/core';
@Injectable({providedIn:'root'})
export class ConfirmarSalidaService {
 readonly pendiente=signal<(()=>void)|null>(null);
 solicitar(accion:()=>void){this.pendiente.set(accion);}
 cancelar(){this.pendiente.set(null);}
 confirmar(){const accion=this.pendiente();this.pendiente.set(null);accion?.();}
}