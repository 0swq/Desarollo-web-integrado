import { ConfirmarSalidaService } from '../../../_services/confirmar-salida.service';
import { ParametrosService } from '../../../_services/parametros.service';
import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError, of } from 'rxjs';
import { StorageService } from '../../../_services/storage.service';
import { AuthService } from '../../../_services/auth.service';
import { PerfilService } from '../../../_services/perfil.service';
import { PerfilModalService } from '../../../_services/perfil-modal.service';
import { ROLES } from '../../../models/rol';
@Component({selector:'app-sidebar-admin',standalone:true,imports:[RouterLink,RouterLinkActive],templateUrl:'./sidebar-admin.html',styleUrl:'./sidebar-admin.css'})
export class SidebarAdmin {
 private readonly confirmarSalida=inject(ConfirmarSalidaService);
  readonly parametros=inject(ParametrosService);
 readonly storage=inject(StorageService);readonly perfil=inject(PerfilModalService);private auth=inject(AuthService);private router=inject(Router);
 abierto=false;
 readonly gestion=[{ruta:'productos',nombre:'Productos',icono:'bi-box-seam'},{ruta:'stock',nombre:'Stock',icono:'bi-boxes'},{ruta:'categorias',nombre:'Categorías',icono:'bi-tags'},{ruta:'marcas',nombre:'Marcas',icono:'bi-bookmark'},{ruta:'modelos',nombre:'Modelos',icono:'bi-car-front'},{ruta:'compatibilidades',nombre:'Compatibilidades',icono:'bi-intersect'},{ruta:'proveedores',nombre:'Proveedores',icono:'bi-truck'},{ruta:'pedidos',nombre:'Pedidos',icono:'bi-receipt'},{ruta:'pagos',nombre:'Pagos',icono:'bi-credit-card'}];
 readonly administracion=[{ruta:'usuarios',nombre:'Usuarios',icono:'bi-people'},{ruta:'reportes',nombre:'Reportes',icono:'bi-bar-chart'},{ruta:'parametros',nombre:'Parámetros',icono:'bi-sliders'}];
 get esAdmin(){return this.storage.obtenerRol()===ROLES.ADMIN;}
 constructor(){inject(PerfilService).obtener().pipe(catchError(()=>of(null)),takeUntilDestroyed()).subscribe();}
 cerrar(){this.abierto=false;}
 salir(){this.cerrar();this.confirmarSalida.solicitar(()=>{this.auth.logout();void this.router.navigate(['/iniciar-sesion']);});}
}
