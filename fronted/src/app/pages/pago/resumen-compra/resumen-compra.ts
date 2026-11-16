import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LineaCompra } from '../../../_services/checkout.service';
@Component({selector:'app-resumen-compra',standalone:true,imports:[CommonModule],templateUrl:'./resumen-compra.html',styleUrl:'./resumen-compra.css'})
export class ResumenCompra { items=input<LineaCompra[]>([]); subtotal=input<number|null|undefined>(0); igv=input<number|null|undefined>(0); total=input<number|null|undefined>(0); moneda=input<string|null|undefined>('PEN'); }
