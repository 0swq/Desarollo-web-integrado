import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, tap, Observable, timeout } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse } from '../models/auth.interface';
export interface ParametrosTienda { id:number; nombre:string; local:string; telefono:string; whatsapp:string; direccion:string; correoContacto:string; horario:string; latitud:number; longitud:number; mapsUrl:string; version:number; }
@Injectable({providedIn:'root'})
export class ParametrosService {
 private http=inject(HttpClient);private base=`${environment.apiUrl}/parametros`;
 readonly valores=signal<ParametrosTienda|null>(null);
 readonly contacto=computed(()=>{
  const p=this.valores();const coordenadas=p?`${p.latitud},${p.longitud}`:'';
  return {nombre:p?.nombre || 'Tienda',local:p?.local || '',telefono:p?.telefono || '',direccion:p?.direccion || '',correoContacto:p?.correoContacto || '',horario:p?.horario || '',
   whatsappUrl:p?`https://wa.me/${p.whatsapp}`:null,
   mapsUrl:p?(p.mapsUrl || `https://www.google.com/maps?q=${encodeURIComponent(coordenadas)}`):null,
   indicacionesUrl:p?`https://www.google.com/maps/dir/?api=1&destination=${encodeURIComponent(coordenadas)}`:null,
   mapaUrl:p?`https://www.google.com/maps?q=${encodeURIComponent(coordenadas)}&z=17&output=embed`:null};
 });
 private datos(r:ApiResponse<ParametrosTienda>):ParametrosTienda{if(!r.success || !r.data)throw new Error(r.message || 'No se pudieron cargar los parámetros.');return r.data;}
 cargar():Observable<ParametrosTienda>{return this.http.get<ApiResponse<ParametrosTienda>>(this.base).pipe(timeout(10000),map(r=>this.datos(r)),tap(p=>this.valores.set(p)));}
 guardar(datos:Omit<ParametrosTienda,'id'>):Observable<ParametrosTienda>{return this.http.put<ApiResponse<ParametrosTienda>>(this.base,datos).pipe(map(r=>this.datos(r)),tap(p=>this.valores.set(p)));}
 whatsapp(mensaje:string):string|null{const url=this.contacto().whatsappUrl;return url?`${url}?text=${encodeURIComponent(mensaje)}`:null;}
}
