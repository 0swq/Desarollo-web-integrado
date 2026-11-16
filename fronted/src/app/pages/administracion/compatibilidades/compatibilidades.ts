import { CommonModule } from '@angular/common';
import { Component, ElementRef, ViewChild, inject, OnInit } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { forkJoin, finalize } from 'rxjs';
import { toast } from 'ngx-sonner';
import { environment } from '../../../../environments/environment';
import { ApiResponse } from '../../../models/auth.interface';
import { Producto } from '../../../models/producto.interface';
import { ModeloVehiculo } from '../../../models/modelo-vehiculo.interface';
import { MarcaVehiculo } from '../../../models/marca-vehiculo.interface';
import { CompatibilidadVehiculo } from '../../../models/compatibilidad-vehiculo.interface';
import { AdminCatalogoService } from '../../../_services/admin-catalogo.service';
@Component({selector:'app-compatibilidades-page',standalone:true,imports:[CommonModule,FormsModule],templateUrl:'./compatibilidades.html',styleUrl:'./compatibilidades.css'})
export class CompatibilidadesPage implements OnInit {
 private http=inject(HttpClient); readonly catalogo=inject(AdminCatalogoService); private url=environment.apiUrl;
 productos:Producto[]=[]; modelos:ModeloVehiculo[]=[]; marcas:MarcaVehiculo[]=[]; relaciones:CompatibilidadVehiculo[]=[];
 seleccionado:Producto|null=null; busqueda=''; cargando=true; error=''; guardando=false; errorFormulario=''; intento=false; marcaId=''; eliminando=false; pendiente:CompatibilidadVehiculo|null=null;
 datos={modeloVehiculoId:'',anioInicio:null as number|null,anioFin:null as number|null,motor:'',notas:''};
 @ViewChild('selector') selector?:ElementRef<HTMLDialogElement>; @ViewChild('editor') editor?:ElementRef<HTMLDialogElement>; @ViewChild('confirmacion') confirmacion?:ElementRef<HTMLDialogElement>; @ViewChild('form') form?:NgForm;
 ngOnInit(){this.cargar();}
 cargar(){this.cargando=true;this.error='';forkJoin({productos:this.http.get<ApiResponse<Producto[]>>(`${this.url}/productos`),modelos:this.http.get<ApiResponse<ModeloVehiculo[]>>(`${this.url}/modelos-vehiculos`),marcas:this.http.get<ApiResponse<MarcaVehiculo[]>>(`${this.url}/marcas-vehiculos`),relaciones:this.http.get<ApiResponse<CompatibilidadVehiculo[]>>(`${this.url}/compatibilidades-vehiculos`)}).pipe(finalize(()=>this.cargando=false)).subscribe({next:r=>{if(Object.values(r).some(x=>!x.success||!Array.isArray(x.data))){this.error='No se pudo leer el catálogo completo.';return;}this.productos=r.productos.data!;this.modelos=r.modelos.data!;this.marcas=r.marcas.data!;this.relaciones=r.relaciones.data!;if(this.seleccionado)this.seleccionado=this.productos.find(p=>p.id===this.seleccionado?.id)||null;},error:()=>this.error='No pudimos cargar los datos. Comprueba tu conexión e inténtalo de nuevo.'});}
 get productosVisibles(){const q=this.busqueda.trim().toLowerCase();return this.productos.filter(p=>`${p.nombre} ${p.sku}`.toLowerCase().includes(q));}
 get asociadas(){return this.relaciones.filter(r=>r.productoId===this.seleccionado?.id);}
 get modelosDisponibles(){return this.modelos.filter(m=>(m.marcaId||m.marca?.id)===this.marcaId&&m.activo);}
 modelo(id?:string|null){return this.modelos.find(m=>m.id===id);}
 marca(id?:string|null){const m=this.modelo(id);return this.marcas.find(x=>x.id===(m?.marcaId||m?.marca?.id))?.nombre||m?.marca?.nombre||'Marca no disponible';}
 seleccionar(p:Producto){this.seleccionado=p;this.selector?.nativeElement.close();}
 abrirSelector(){this.busqueda='';this.selector?.nativeElement.showModal();}
 editando:CompatibilidadVehiculo|null=null;
 editar(r:CompatibilidadVehiculo){this.editando=r;const m=this.modelo(r.modeloVehiculoId);this.marcaId=m?.marcaId||m?.marca?.id||'';this.datos={modeloVehiculoId:r.modeloVehiculoId||'',anioInicio:r.anioInicio??null,anioFin:r.anioFin??null,motor:r.motor||'',notas:r.notas||''};this.intento=false;this.errorFormulario='';this.form?.resetForm({...this.datos,marca:this.marcaId});this.editor?.nativeElement.showModal();}
 nuevo(){this.editando=null;this.marcaId='';this.datos={modeloVehiculoId:'',anioInicio:null,anioFin:null,motor:'',notas:''};this.intento=false;this.errorFormulario='';this.form?.resetForm(this.datos);this.editor?.nativeElement.showModal();}
 validar(campo:string){const d=this.datos;if(campo==='marca'&&!this.marcaId)return 'Selecciona una marca.';if(campo==='modeloVehiculoId'&&!this.modelosDisponibles.some(m=>m.id===d.modeloVehiculoId))return 'Selecciona un modelo de esta marca.';if(campo==='anioInicio'||campo==='anioFin'){const v=d[campo];if(v===null||!Number.isInteger(v)||v<1900||v>2100)return 'Ingresa un año entero entre 1900 y 2100.';if(campo==='anioFin'&&d.anioInicio!==null&&v<d.anioInicio)return 'El año final debe ser igual o posterior al inicial.';}if(campo==='motor'&&d.motor.trim().length>100)return 'Máximo 100 caracteres.';if(campo==='notas'&&d.notas.trim().length>500)return 'Máximo 500 caracteres.';return '';}
 guardar(){if(this.guardando||!this.seleccionado)return;this.intento=true;this.form?.form.markAllAsTouched();if(['marca','modeloVehiculoId','anioInicio','anioFin','motor','notas'].some(c=>this.validar(c))){this.errorFormulario='Corrige los campos indicados antes de guardar.';return;}const d=this.datos;if(this.asociadas.some(r=>r.id!==this.editando?.id&&r.modeloVehiculoId===d.modeloVehiculoId&&r.anioInicio===d.anioInicio&&r.anioFin===d.anioFin&&(r.motor||'').trim().toLowerCase()===d.motor.trim().toLowerCase())){this.errorFormulario='Esta compatibilidad ya está registrada para el producto.';return;}this.guardando=true;this.errorFormulario='';this.http.request<ApiResponse<CompatibilidadVehiculo>>(this.editando ? 'PUT' : 'POST', `${this.url}/compatibilidades-vehiculos${this.editando ? '/'+encodeURIComponent(this.editando.id) : ''}`,{body:{...d,productoId:this.seleccionado.id,motor:d.motor.trim(),notas:d.notas.trim()}}).pipe(finalize(()=>this.guardando=false)).subscribe({next:r=>{if(!r.success||!r.data){this.errorFormulario=r.message||'No se pudo guardar.';return;}this.relaciones=this.editando?this.relaciones.map(x=>x.id===r.data!.id?r.data!:x):[...this.relaciones,r.data];this.editor?.nativeElement.close();toast.success(this.editando?'Compatibilidad actualizada.':'Compatibilidad agregada.');},error:e=>this.errorFormulario=e.error?.message||'No se pudo guardar. Inténtalo de nuevo.'});}
 pedirEliminar(r:CompatibilidadVehiculo){this.pendiente=r;this.errorFormulario='';this.confirmacion?.nativeElement.showModal();}
 eliminar(){if(!this.pendiente||this.eliminando)return;const id=this.pendiente.id;this.eliminando=true;this.http.delete(`${this.url}/compatibilidades-vehiculos/${encodeURIComponent(id)}`).pipe(finalize(()=>this.eliminando=false)).subscribe({next:()=>{this.relaciones=this.relaciones.filter(r=>r.id!==id);this.confirmacion?.nativeElement.close();this.pendiente=null;toast.success('Compatibilidad eliminada.');},error:()=>this.errorFormulario='No se pudo eliminar. Inténtalo de nuevo.'});}
 imagenFallida(event:Event){(event.target as HTMLImageElement).style.display='none';}
}