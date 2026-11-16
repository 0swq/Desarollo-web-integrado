import { Rol } from '../models/rol';
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse, UsuarioResponse } from '../models/auth.interface';
@Injectable({providedIn:'root'})
export class UsuariosService {
 private http=inject(HttpClient);private base=`${environment.apiUrl}/usuarios`;
 private datos<T>(r:ApiResponse<T>):T{if(!r.success || r.data===null)throw new Error(r.message || 'No se pudieron consultar los usuarios.');return r.data;}
 listar():Observable<UsuarioResponse[]>{return this.http.get<ApiResponse<UsuarioResponse[]>>(this.base).pipe(map(r=>this.datos(r)));}
 cambiarRol(id:string,rol:Rol):Observable<UsuarioResponse>{return this.http.patch<ApiResponse<UsuarioResponse>>(`${this.base}/${encodeURIComponent(id)}/rol`,{rol}).pipe(map(r=>this.datos(r)));}
 cambiarEstado(id:string):Observable<UsuarioResponse>{return this.http.patch<ApiResponse<UsuarioResponse>>(`${this.base}/${encodeURIComponent(id)}/toggle-activo`,{}).pipe(map(r=>this.datos(r)));}
}
