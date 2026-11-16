import { CommonModule } from '@angular/common';
import { Component, Input, OnChanges } from '@angular/core';
import { CompatibilidadVehiculo } from '../../../models/compatibilidad-vehiculo.interface';
import { ModeloVehiculo } from '../../../models/modelo-vehiculo.interface';

@Component({
  selector: 'app-compatibility-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './compatibility-card.html',
  styleUrl: './compatibility-card.css',
})
export class CompatibilityCard implements OnChanges {
  @Input({ required: true }) compatibilidad!: CompatibilidadVehiculo;
  @Input() modelo?: ModeloVehiculo;
  @Input() marca = 'Sin especificar';
  @Input() anios = 'Sin especificar';
  imagenFallida = false;
  ngOnChanges(): void { this.imagenFallida = false; }
}
