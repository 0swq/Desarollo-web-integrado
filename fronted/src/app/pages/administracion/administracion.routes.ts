import { Routes } from '@angular/router';
import { authGuard } from '../../_helpers/auth.guard';
import { roleGuard } from '../../_helpers/role.guard';
import { ROLES_GESTION, ROLES_USUARIOS } from '../../models/rol';

export const administracionRoutes: Routes = [{
  path: '',
  loadComponent: () => import('./panel/admin-layout').then(m => m.AdminLayout),
  canActivate: [authGuard, roleGuard],
  canActivateChild: [roleGuard],
  data: { roles: ROLES_GESTION },
  children: [
    { path: '', redirectTo: 'productos', pathMatch: 'full' },
    { path: 'reportes', loadComponent: () => import('./reportes/reportes').then(m => m.ReportesPage), data: { roles: ROLES_USUARIOS } },
    { path: 'parametros', loadComponent: () => import('./parametros/parametros').then(m => m.ParametrosPage), data: { roles: ROLES_USUARIOS } },
    { path: 'usuarios', loadComponent: () => import('./usuarios/usuarios').then(m => m.UsuariosPage), data: { roles: ROLES_USUARIOS } },
    { path: 'productos', loadComponent: () => import('./productos/manage-products').then(m => m.ManageProducts), data: { roles: ROLES_GESTION } },
    { path: 'stock', loadComponent: () => import('./stock/manage-stock').then(m => m.ManageStock), data: { roles: ROLES_GESTION } },
    { path: 'marcas', loadComponent: () => import('./marcas/marcas').then(m => m.MarcasPage), data: { roles: ROLES_GESTION } },
    { path: 'categorias', loadComponent: () => import('./categorias/categorias').then(m => m.CategoriasPage), data: { roles: ROLES_GESTION } },
    { path: 'modelos', loadComponent: () => import('./modelos/modelos').then(m => m.ModelosPage), data: { roles: ROLES_GESTION } },
    { path: 'compatibilidades', loadComponent: () => import('./compatibilidades/compatibilidades').then(m => m.CompatibilidadesPage), data: { roles: ROLES_GESTION } },
    { path: 'proveedores', loadComponent: () => import('./proveedores/proveedores').then(m => m.ProveedoresPage), data: { roles: ROLES_GESTION } },
    { path: 'pedidos', loadComponent: () => import('./pedidos/pedidos').then(m => m.PedidosAdminPage), data: { roles: ROLES_GESTION } },
    { path: 'pagos', loadComponent: () => import('./pagos/pagos').then(m => m.PagosAdminPage), data: { roles: ROLES_GESTION } },
  ],
}];
