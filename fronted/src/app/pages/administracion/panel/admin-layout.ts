import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SidebarAdmin } from '../../../_shared/componentes/sidebar-admin/sidebar-admin';
import { FooterAdmin } from '../../../_shared/componentes/footer-admin/footer-admin';
@Component({selector:'app-admin-layout',standalone:true,imports:[RouterOutlet,SidebarAdmin,FooterAdmin],templateUrl:'./admin-layout.html',styleUrl:'./admin-layout.css'})
export class AdminLayout {}
