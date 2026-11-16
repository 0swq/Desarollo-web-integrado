import { CommonModule } from '@angular/common';
import { Component, ElementRef, inject, OnInit, ViewChild } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { finalize } from 'rxjs';
import { toast } from 'ngx-sonner';
import { AdminCatalogoService } from '../../../_services/admin-catalogo.service';
import { Proveedor } from '../../../models/proveedor.interface';
@Component({selector:'app-proveedores-page',standalone:true,imports:[CommonModule,FormsModule],templateUrl:'./proveedores.html',styleUrl:'./proveedores.css'})
export class ProveedoresPage implements OnInit {
 readonly catalogo=inject(AdminCatalogoService);
 proveedores:Proveedor[]=[]; busqueda=''; cargando=true; error=''; guardando=false; errorFormulario=''; editando:Proveedor|null=null;
 formulario={ruc:'',razonSocial:'',contactoNombre:'',telefono:'',correo:'',direccion:'',activo:true};
 @ViewChild('form') form?: NgForm;
 @ViewChild('editor') dialog?:ElementRef<HTMLDialogElement>;
 ngOnInit(){this.cargar();}
 get visibles(){const q=this.busqueda.trim().toLocaleLowerCase();return this.proveedores.filter(p=>[p.razonSocial,p.ruc,p.contactoNombre,p.correo,p.telefono].join(' ').toLocaleLowerCase().includes(q));}
 get activos(){return this.proveedores.filter(p=>p.activo).length;}
 cargar(){this.cargando=true;this.error='';this.catalogo.listar<Proveedor>('proveedores').pipe(finalize(()=>this.cargando=false)).subscribe({next:p=>this.proveedores=p,error:e=>this.error=e.error?.message||'No pudimos cargar los proveedores.'});}
 nuevo(){this.editando=null;this.formulario={ruc:'',razonSocial:'',contactoNombre:'',telefono:'',correo:'',direccion:'',activo:true};this.errorFormulario='';this.form?.resetForm(this.formulario);this.dialog?.nativeElement.showModal();}
 editar(p:Proveedor){this.editando=p;this.formulario={ruc:p.ruc,razonSocial:p.razonSocial,contactoNombre:p.contactoNombre||'',telefono:p.telefono||'',correo:p.correo||'',direccion:p.direccion||'',activo:p.activo};this.errorFormulario='';this.form?.resetForm(this.formulario);this.dialog?.nativeElement.showModal();}
 validar(campo:string):string {
 const f=this.formulario; const v=(f[campo as keyof typeof f] as string).trim();
 if(campo==='razonSocial' && (v.length<3 || v.length>255)) return 'Ingresa una razón social de 3 a 255 caracteres.';
 if(campo==='ruc') {if(!/^\d{11}$/.test(v))return 'El RUC debe tener exactamente 11 dígitos.';if(this.proveedores.some(p=>p.ruc===v&&p.id!==this.editando?.id))return 'Este RUC ya está registrado.';}
 if(campo==='contactoNombre' && v && (v.length<3 || !/^[\p{L}\p{M} .'-]+$/u.test(v)))return 'Ingresa un nombre válido, sin números ni símbolos.';
 if(campo==='telefono' && v && (!/^[0-9+()\s-]+$/.test(v)||v.replace(/\D/g,'').length<7||v.replace(/\D/g,'').length>15||v.length>20))return 'Ingresa un teléfono con 7 a 15 dígitos.';
 if(campo==='correo' && v && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v))return 'Ingresa un correo válido, por ejemplo contacto@empresa.com.';
 if(campo==='direccion' && v && v.length<5)return 'La dirección debe tener al menos 5 caracteres.';
 if(v.length>255)return 'Máximo 255 caracteres.';
 return '';
 } guardar(){if(this.guardando)return;this.form?.form.markAllAsTouched();const campos=['razonSocial','ruc','contactoNombre','telefono','correo','direccion'];if(campos.some(c=>this.validar(c))){this.errorFormulario='Corrige los campos indicados.';this.dialog?.nativeElement.querySelector<HTMLElement>('.field-invalid')?.focus();return;}const f=this.formulario; if(!/^\d{11}$/.test(f.ruc.trim())||!f.razonSocial.trim()){this.errorFormulario='Ingresa un RUC de 11 dígitos y la razón social.';return;}if(this.proveedores.some(p=>p.ruc===f.ruc.trim()&&p.id!==this.editando?.id)){this.errorFormulario='Ya existe un proveedor con este RUC.';return;}this.guardando=true;this.errorFormulario='';const datos={...f,ruc:f.ruc.trim(),razonSocial:f.razonSocial.trim(),contactoNombre:f.contactoNombre.trim(),telefono:f.telefono.trim(),correo:f.correo.trim(),direccion:f.direccion.trim()};this.catalogo.guardar<Proveedor>('proveedores',datos,this.editando?.id).pipe(finalize(()=>this.guardando=false)).subscribe({next:p=>{this.proveedores=this.editando?this.proveedores.map(x=>x.id===p.id?p:x):[p,...this.proveedores];this.dialog?.nativeElement.close();toast.success('Proveedor guardado.');},error:e=>this.errorFormulario=e.error?.message||'No se pudo guardar el proveedor.'});}
 cambiarEstado(p:Proveedor){this.catalogo.cambiarEstado<Proveedor>('proveedores',p.id).subscribe({next:x=>{this.proveedores=this.proveedores.map(item=>item.id===x.id?x:item);toast.success(x.activo?'Proveedor activado.':'Proveedor desactivado.');},error:e=>toast.error(e.error?.message||'No se pudo cambiar el estado.')});}
}