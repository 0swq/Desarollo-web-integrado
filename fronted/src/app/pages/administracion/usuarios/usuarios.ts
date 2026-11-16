import { Rol, esRol } from '../../../models/rol';
import { Component, DestroyRef, ElementRef, ViewChild, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, debounceTime, distinctUntilChanged, finalize } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { toast } from 'ngx-sonner';
import { UsuariosService } from '../../../_services/usuarios.service';
import { StorageService } from '../../../_services/storage.service';
import { UsuarioResponse } from '../../../models/auth.interface';
@Component({selector:'app-usuarios-page',standalone:true,imports:[CommonModule,FormsModule],templateUrl:'./usuarios.html',styleUrl:'./usuarios.css'})
export class UsuariosPage implements OnInit {
 private service=inject(UsuariosService);private storage=inject(StorageService);private destroy=inject(DestroyRef);
 @ViewChild('detalle') private dialog?:ElementRef<HTMLDialogElement>;
 usuarios:UsuarioResponse[]=[];cargando=true;error='';busquedaInput='';busqueda='';rol='';estado='';orden='recientes';pagina=1;porPagina=10;
 seleccionado:UsuarioResponse|null=null;confirmando=false;confirmandoRol=false;rolNuevo:Rol='CLIENTE';guardando=false;errorAccion='';private overflowAnterior='';private buscando=new Subject<string>();readonly fotosFallidas=new Set<string>();
 constructor(){this.buscando.pipe(debounceTime(300),distinctUntilChanged(),takeUntilDestroyed(this.destroy)).subscribe(v=>{this.busqueda=v.trim().toLocaleLowerCase();this.pagina=1;});this.destroy.onDestroy(()=>{const dialog=this.dialog?.nativeElement;if(dialog?.open){dialog.close();dialog.ownerDocument.body.style.overflow=this.overflowAnterior;}});}
 ngOnInit(){this.cargar();}
 get activos(){return this.usuarios.filter(u=>u.activo).length;}
 get equipo(){return this.usuarios.filter(u=>u.rol==='ADMIN' || u.rol==='EMPLEADO').length;}
 get clientes(){return this.usuarios.filter(u=>u.rol==='CLIENTE').length;}
 get filtrados(){const q=this.busqueda;return this.usuarios.filter(u=>(!this.rol || u.rol===this.rol) && (!this.estado || u.activo===(this.estado==='activo')) && (!q || [u.nombre,u.apellido,u.correo,u.telefono].some(v=>v?.toLocaleLowerCase().includes(q)) || `${u.nombre} ${u.apellido || ''}`.toLocaleLowerCase().includes(q))).sort((a,b)=>this.orden==='nombre'?`${a.nombre} ${a.apellido || ''}`.localeCompare(`${b.nombre} ${b.apellido || ''}`,'es'):this.orden==='antiguos'?(a.fechaCreacion || '').localeCompare(b.fechaCreacion || ''):(b.fechaCreacion || '').localeCompare(a.fechaCreacion || ''));}
 get paginas(){return Math.max(1,Math.ceil(this.filtrados.length/this.porPagina));}
 get visibles(){return this.filtrados.slice((this.pagina-1)*this.porPagina,this.pagina*this.porPagina);}
 get desde(){return this.filtrados.length?(this.pagina-1)*this.porPagina+1:0;}
 get hasta(){return Math.min(this.pagina*this.porPagina,this.filtrados.length);}
 nombreRol(rol:string){return ({ADMIN:'Administrador',EMPLEADO:'Empleado',CLIENTE:'Cliente'} as Record<string,string>)[rol] || rol;}
 iniciales(u:UsuarioResponse){return (u.nombre.charAt(0)+(u.apellido || '').charAt(0)).toUpperCase();}
 esMiCuenta(u:UsuarioResponse){return u.id===this.storage.obtenerUsuarioId();}
 buscar(v:string){this.busquedaInput=v;this.buscando.next(v);}
 filtrar(){this.pagina=1;}
 limpiar(){this.busquedaInput='';this.busqueda='';this.buscando.next('');this.rol='';this.estado='';this.pagina=1;}
 cargar(){if(this.guardando)return;this.cargando=true;this.error='';this.service.listar().pipe(finalize(()=>this.cargando=false),takeUntilDestroyed(this.destroy)).subscribe({next:usuarios=>{this.usuarios=usuarios;this.pagina=1;},error:e=>this.error=e.status===403?'No tienes permiso para consultar usuarios.':e.error?.message || 'No pudimos cargar los usuarios. Vuelve a intentarlo.'});}
 ver(u:UsuarioResponse,confirmar=false){this.seleccionado=u;this.confirmando=confirmar;this.confirmandoRol=false;this.rolNuevo=u.rol;this.errorAccion='';const dialog=this.dialog?.nativeElement;if(dialog && !dialog.open){this.overflowAnterior=dialog.ownerDocument.body.style.overflow;dialog.showModal();dialog.ownerDocument.body.style.overflow='hidden';}}
 cerrar(){if(this.guardando)return;const dialog=this.dialog?.nativeElement;if(dialog?.open)dialog.close();if(dialog)dialog.ownerDocument.body.style.overflow=this.overflowAnterior;this.seleccionado=null;this.confirmando=false;this.confirmandoRol=false;}
 cancelar(event:Event){event.preventDefault();this.cerrar();}
 fuera(event:MouseEvent){const dialog=this.dialog?.nativeElement;if(!dialog || event.target!==dialog)return;const r=dialog.getBoundingClientRect();if(event.clientX<r.left || event.clientX>r.right || event.clientY<r.top || event.clientY>r.bottom)this.cerrar();}
 cambiarEstado(){const u=this.seleccionado;if(!u || !this.confirmando || this.guardando || this.esMiCuenta(u))return;this.guardando=true;this.errorAccion='';this.service.cambiarEstado(u.id).pipe(finalize(()=>this.guardando=false),takeUntilDestroyed(this.destroy)).subscribe({next:actualizado=>{this.usuarios=this.usuarios.map(item=>item.id===actualizado.id?actualizado:item);this.seleccionado=actualizado;this.confirmando=false;this.pagina=Math.min(this.pagina,this.paginas);toast.success(actualizado.activo?'Cuenta activada.':'Cuenta desactivada.');},error:e=>{this.errorAccion=e.error?.message || e.message || 'No se pudo actualizar la cuenta.';toast.error(this.errorAccion);}});}
 revisarRol(){const u=this.seleccionado;if(!u || this.guardando || this.esMiCuenta(u) || !esRol(this.rolNuevo) || this.rolNuevo===u.rol)return;this.confirmandoRol=true;this.errorAccion='';}
 guardarRol(){const u=this.seleccionado;if(!u || !this.confirmandoRol || this.guardando || this.esMiCuenta(u) || !esRol(this.rolNuevo) || this.rolNuevo===u.rol)return;this.guardando=true;this.errorAccion='';this.service.cambiarRol(u.id,this.rolNuevo).pipe(finalize(()=>this.guardando=false),takeUntilDestroyed(this.destroy)).subscribe({next:actualizado=>{this.usuarios=this.usuarios.map(item=>item.id===actualizado.id?actualizado:item);this.seleccionado=actualizado;this.rolNuevo=actualizado.rol;this.confirmandoRol=false;this.pagina=Math.min(this.pagina,this.paginas);toast.success('Rol actualizado a '+this.nombreRol(actualizado.rol)+'.');},error:e=>{this.errorAccion=e.error?.message || e.message || 'No se pudo cambiar el rol.';toast.error(this.errorAccion);}});}
 cancelarAccion(){if(this.guardando)return;if(this.confirmandoRol)this.confirmandoRol=false;else if(this.confirmando)this.confirmando=false;else this.cerrar();}
}
