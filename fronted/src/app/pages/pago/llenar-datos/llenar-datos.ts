import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { forkJoin, finalize } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CheckoutService, VistaCarrito } from '../../../_services/checkout.service';
import { PerfilService } from '../../../_services/perfil.service';
import { ResumenCompra } from '../resumen-compra/resumen-compra';
@Component({selector:'app-llenar-datos-page',standalone:true,imports:[ReactiveFormsModule,RouterLink,ResumenCompra],templateUrl:'./llenar-datos.html',styleUrl:'./llenar-datos.css'})
export class LlenarDatosPage implements OnInit {
 private checkout=inject(CheckoutService); private perfil=inject(PerfilService); private router=inject(Router); private fb=inject(FormBuilder); private destroy=inject(DestroyRef);
 cargando=true; guardando=false; intentoEnviar=false; error=''; vista:VistaCarrito|null=null; nombre=''; correo='';
 form=this.fb.nonNullable.group({direccionEntrega:['',[Validators.required,Validators.minLength(8),Validators.maxLength(255),Validators.pattern(/.*\S.*/)]],telefonoContacto:['',[Validators.required,Validators.maxLength(20),Validators.pattern(/^(?=(?:\D*\d){7,15}\D*$)\+?[0-9() -]+$/)]],notas:['',Validators.maxLength(500)]});
 ngOnInit(){this.cargar();}
 cargar(){this.cargando=true;this.error='';forkJoin({vista:this.checkout.carrito(),perfil:this.perfil.obtener()}).pipe(finalize(()=>this.cargando=false),takeUntilDestroyed(this.destroy)).subscribe({next:({vista,perfil})=>{this.vista=vista;this.nombre=`${perfil.nombre} ${perfil.apellido}`;this.correo=perfil.correo;this.form.reset(this.checkout.entrega() || {direccionEntrega:perfil.direccion || '',telefonoContacto:perfil.telefono || '',notas:''});},error:e=>this.error=e.error?.message || e.message || 'No pudimos consultar tu compra.'});}
 continuar(){this.intentoEnviar=true;this.form.markAllAsTouched();if(this.guardando || this.form.invalid || !this.vista?.items.length)return; const datos=this.form.getRawValue();datos.direccionEntrega=datos.direccionEntrega.trim();datos.telefonoContacto=datos.telefonoContacto.trim();datos.notas=datos.notas.trim();this.guardando=true;this.checkout.guardarEntrega(datos);void this.router.navigate(['/pago/pago']).finally(()=>this.guardando=false);}
}
