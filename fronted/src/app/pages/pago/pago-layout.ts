import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
@Component({selector:'app-pago-layout',standalone:true,imports:[RouterLink,RouterOutlet],templateUrl:'./pago-layout.html',styleUrl:'./pago-layout.css'})
export class PagoLayout { private router=inject(Router); get paso(){ return this.router.url.includes('/confirmacion') ? 3 : this.router.url.includes('/pago/pago') ? 2 : 1; } }
