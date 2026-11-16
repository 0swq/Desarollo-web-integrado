import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { finalize, forkJoin } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ApiResponse } from '../../../models/auth.interface';
import { Pago } from '../../../models/pago.interface';
import { Orden } from '../../../models/orden.interface';
@Component({selector:'app-pagos-page',standalone:true,imports:[CommonModule,FormsModule],templateUrl:'./pagos.html',styleUrl:'./pagos.css'})
export class PagosAdminPage implements OnInit {
 private http=inject(HttpClient);pagos:Pago[]=[];ordenes:Orden[]=[];cargando=true;error='';busqueda='';estado='';desde='';hasta='';pagina=1;readonly tamano=10;
 ngOnInit(){this.cargar();}
 cargar(){this.cargando=true;this.error='';forkJoin({pagos:this.http.get<ApiResponse<Pago[]>>(`${environment.apiUrl}/pagos`),ordenes:this.http.get<ApiResponse<Orden[]>>(`${environment.apiUrl}/ordenes`)}).pipe(finalize(()=>this.cargando=false)).subscribe({next:r=>{if(!r.pagos.success||!Array.isArray(r.pagos.data)||!r.ordenes.success||!Array.isArray(r.ordenes.data)){this.error='No se pudo leer el historial de pagos.';return;}this.pagos=[...r.pagos.data].sort((a,b)=>(b.fechaCreacion||'').localeCompare(a.fechaCreacion||''));this.ordenes=r.ordenes.data;this.pagina=1;},error:()=>this.error='No pudimos cargar los pagos. Comprueba tu conexión y tus permisos de acceso.'});}
 orden(p:Pago){return this.ordenes.find(o=>o.id===p.ordenId)?.numeroOrden||p.ordenId||'Sin pedido';}
 get errorFechas(){return this.desde&&this.hasta&&this.desde>this.hasta?'La fecha final debe ser igual o posterior a la inicial.':'';}
 get filtrados(){if(this.errorFechas)return [];const q=this.busqueda.trim().toLowerCase();return this.pagos.filter(p=>(!q||`${p.id} ${this.orden(p)} ${p.metodoPago||''}`.toLowerCase().includes(q))&&(!this.estado||p.estado===this.estado)&&(!this.desde||(p.fechaCreacion||'').slice(0,10)>=this.desde)&&(!this.hasta||!!p.fechaCreacion&&p.fechaCreacion.slice(0,10)<=this.hasta));}
 get paginas(){return Math.max(1,Math.ceil(this.filtrados.length/this.tamano));}
 get visibles(){return this.filtrados.slice((this.pagina-1)*this.tamano,this.pagina*this.tamano);}
 get aprobados(){return this.pagos.filter(p=>p.estado==='APROBADO').length;}
 get pendientes(){return this.pagos.filter(p=>p.estado==='PENDIENTE').length;}
 get totales(){const montos=new Map<string,number>();this.pagos.filter(p=>p.estado==='APROBADO').forEach(p=>{const moneda=p.moneda||'PEN';montos.set(moneda,(montos.get(moneda)||0)+Number(p.monto));});return Array.from(montos,([moneda,monto])=>({moneda,monto}));}
 limpiar(){this.busqueda='';this.estado='';this.desde='';this.hasta='';this.pagina=1;}
 etiqueta(estado:string){return ({APROBADO:'Aprobado',PENDIENTE:'Pendiente',RECHAZADO:'Rechazado',ANULADO:'Anulado',REEMBOLSADO:'Reembolsado'} as Record<string,string>)[estado]||estado;}
 dinero(p:Pago){return new Intl.NumberFormat('es-PE',{style:'currency',currency:/^[A-Z]{3}$/.test(p.moneda||'')?p.moneda!:'PEN'}).format(Number(p.monto));}
}