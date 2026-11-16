import { ParametrosService } from '../../../_services/parametros.service';
import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CheckoutService, CompraResumen } from '../../../_services/checkout.service';
import { PerfilModalService } from '../../../_services/perfil-modal.service';
import { ResumenCompra } from '../resumen-compra/resumen-compra';

@Component({selector:'app-confirmacion-page',standalone:true,imports:[CommonModule,RouterLink,ResumenCompra],templateUrl:'./confirmacion.html',styleUrl:'./confirmacion.css'})
export class ConfirmacionPage implements OnInit {
 private parametros=inject(ParametrosService);
 private checkout=inject(CheckoutService);private route=inject(ActivatedRoute);private destroy=inject(DestroyRef);readonly perfil=inject(PerfilModalService);
 compra:CompraResumen|null=null;cargando=true;error='';
 get aprobada(){return this.compra?.pago.estado==='APROBADO';}
 get rechazada(){return this.compra?.pago.estado==='RECHAZADO';}
 get ayuda(){return this.parametros.whatsapp('Hola, tengo una consulta sobre mi pedido '+(this.compra?.orden.numeroOrden || '')+'.');}
 nombreEstado(estado:string):string{return ({PENDIENTE:'Pendiente',PAGADO:'Pagado',EN_PREPARACION:'En preparación',ENVIADO:'Enviado',ENTREGADO:'Entregado',CANCELADO:'Cancelado'} as Record<string,string>)[estado] || estado;}
 ngOnInit(){this.cargar();}
 cargar(){const id=this.route.snapshot.queryParamMap.get('pedido');this.error='';if(!id){this.cargando=false;this.error='Selecciona un pedido para consultar su confirmación.';return;}this.cargando=true;this.checkout.obtener(id).pipe(finalize(()=>this.cargando=false),takeUntilDestroyed(this.destroy)).subscribe({next:c=>this.compra=c,error:e=>this.error=e.error?.message || 'No pudimos consultar la confirmación del pedido.'});}
}
