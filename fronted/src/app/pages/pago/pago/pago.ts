import { PerfilModalService } from '../../../_services/perfil-modal.service';
import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { switchMap, of, finalize } from 'rxjs';
import { CheckoutService, CompraResumen, VistaCarrito, Entrega } from '../../../_services/checkout.service';
import { ResumenCompra } from '../resumen-compra/resumen-compra';
@Component({selector:'app-pago-page',standalone:true,imports:[CommonModule,FormsModule,RouterLink,ResumenCompra],templateUrl:'./pago.html',styleUrl:'./pago.css'})
export class PagoPage implements OnInit {
 readonly perfil=inject(PerfilModalService);
 private checkout=inject(CheckoutService);private route=inject(ActivatedRoute);private router=inject(Router);private destroy=inject(DestroyRef);
 cargando=true;procesando=false;error='';vista:VistaCarrito|null=null;compra:CompraResumen|null=null;entrega:Entrega|null=null;aceptado=false;
 ngOnInit(){this.cargar();}
 get lineas(){return this.compra?.items || this.vista?.items || [];}
 get importes(){return this.compra?.orden || this.vista;}
 cargar(){this.error='';this.cargando=true;const id=this.route.snapshot.queryParamMap.get('pedido');
  if(id){this.checkout.obtener(id).pipe(finalize(()=>this.cargando=false),takeUntilDestroyed(this.destroy)).subscribe({next:c=>{this.compra=c;this.entrega={direccionEntrega:c.orden.direccionEntrega || '',telefonoContacto:c.orden.telefonoContacto || '',notas:c.orden.notas || ''};if(c.pago.estado==='APROBADO')void this.router.navigate(['/pago/confirmacion'],{queryParams:{pedido:c.orden.id},replaceUrl:true});},error:e=>this.error=e.error?.message || 'No pudimos cargar este pedido.'});return;}
  this.entrega=this.checkout.entrega();if(!this.entrega){this.cargando=false;void this.router.navigate(['/pago/llenar-datos'],{replaceUrl:true});return;}
  this.checkout.carrito().pipe(finalize(()=>this.cargando=false),takeUntilDestroyed(this.destroy)).subscribe({next:v=>{this.vista=v;if(!v.items.length)void this.router.navigate(['/pago/llenar-datos'],{replaceUrl:true});},error:e=>this.error=e.error?.message || e.message || 'No pudimos preparar el pago.'});
 }
 pagar(){if(this.procesando || !this.aceptado || !this.entrega || !this.lineas.length)return;this.procesando=true;this.error='';
  (this.compra ? of(this.compra) : this.checkout.crear(this.entrega)).pipe(switchMap(c=>{this.compra=c;void this.router.navigate([],{relativeTo:this.route,queryParams:{pedido:c.orden.id},replaceUrl:true});return this.checkout.simular(c.orden.id,'APROBADO');}),finalize(()=>this.procesando=false),takeUntilDestroyed(this.destroy)).subscribe({next:c=>{this.compra=c;void this.router.navigate(['/pago/confirmacion'],{queryParams:{pedido:c.orden.id}});},error:e=>this.error=e.error?.message || e.message || 'No se pudo completar la operación. Puedes reintentar sin crear otro pedido.'});
 }
}
