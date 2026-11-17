import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AdminCatalogoService } from '../../_services/admin-catalogo.service';
import { Categoria } from '../../models/categoria.interface';
import { ParametrosService } from '../../_services/parametros.service';
import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-inicio',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './inicio.html',
  styleUrl: './inicio.css',
})
export class InicioComponent implements OnInit {
  readonly parametros = inject(ParametrosService);
  private readonly catalogo=inject(AdminCatalogoService);
  private readonly destroyRef=inject(DestroyRef);
  categorias:Categoria[]=[];cargandoCategorias=true;errorCategorias=false;
  readonly estilos=['categoria-motor','categoria-frenos','categoria-accesorios'];
  readonly iconos=['bi-gear-wide-connected','bi-disc','bi-lightning-charge'];
  ngOnInit(){this.cargarCategorias();}
  cargarCategorias(){this.cargandoCategorias=true;this.errorCategorias=false;this.catalogo.listar<Categoria>('categorias').pipe(takeUntilDestroyed(this.destroyRef)).subscribe({next:lista=>{this.categorias=lista.filter(c=>c.activo).sort((a,b)=>a.nombre.localeCompare(b.nombre,'es')).slice(0,3);this.cargandoCategorias=false;},error:()=>{this.errorCategorias=true;this.cargandoCategorias=false;}});}}
