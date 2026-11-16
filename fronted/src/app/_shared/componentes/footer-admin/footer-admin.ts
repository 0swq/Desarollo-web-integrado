import { ParametrosService } from '../../../_services/parametros.service';
import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
@Component({selector:'app-footer-admin',standalone:true,imports:[RouterLink],templateUrl:'./footer-admin.html',styleUrl:'./footer-admin.css'})
export class FooterAdmin { readonly parametros=inject(ParametrosService); readonly anio=new Date().getFullYear(); }
