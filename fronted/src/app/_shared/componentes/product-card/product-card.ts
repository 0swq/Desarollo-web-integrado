import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Producto } from '../../../models/producto.interface';

@Component({
  selector: 'app-product-card',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './product-card.html',
  styleUrl: './product-card.css',
})
export class ProductCard {
  @Input({ required: true }) producto!: Producto;
  @Input() categoria = '';
  @Input() stock: number | null = null;
  @Input() compatibilidad = '';
  imagenFallida = false;
  get precio(): number { return this.enOferta ? this.producto.precioPromocional! : this.producto.precio; }
  get enOferta(): boolean {
    return this.producto.precioPromocional != null && this.producto.precioPromocional < this.producto.precio;
  }
}
