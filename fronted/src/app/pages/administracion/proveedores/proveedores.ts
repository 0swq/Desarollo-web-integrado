import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, DestroyRef, ElementRef, inject, OnInit, signal, viewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { toast } from 'ngx-sonner';
import { finalize } from 'rxjs';
import { AdminCatalogoService } from '../../../_services/admin-catalogo.service';
import { Proveedor } from '../../../models/proveedor.interface';

type AccionProveedor = { tipo: 'crear' } | { tipo: 'editar' | 'eliminar'; proveedor: Proveedor };
const CAMPOS_PROVEEDOR = ['ruc', 'razonSocial', 'contactoNombre', 'telefono', 'correo', 'direccion'] as const;
type CampoProveedor = typeof CAMPOS_PROVEEDOR[number];

@Component({
  selector: 'app-proveedores-page',
  standalone: true,
  imports: [FormsModule, ReactiveFormsModule],
  templateUrl: './proveedores.html',
  styleUrl: './proveedores.css',
})
export class ProveedoresPage implements OnInit {
  private readonly catalogo = inject(AdminCatalogoService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly dialogo = viewChild.required<ElementRef<HTMLDialogElement>>('dialogo');

  protected readonly proveedores = signal<Proveedor[]>([]);
  protected readonly busqueda = signal('');
  protected readonly cargando = signal(false);
  protected readonly error = signal('');
  protected readonly accion = signal<AccionProveedor>({ tipo: 'crear' });
  protected readonly errorAccion = signal('');
  protected readonly procesando = signal(false);
  protected readonly formulario = inject(FormBuilder).nonNullable.group({
    ruc: '',
    razonSocial: '',
    contactoNombre: '',
    telefono: '',
    correo: '',
    direccion: '',
    activo: true,
  });

  protected readonly activos = computed(() => this.proveedores().filter(proveedor => proveedor.activo).length);
  protected readonly visibles = computed(() => {
    const termino = normalizar(this.busqueda().trim());
    const proveedores = this.proveedores();
    return termino ? proveedores.filter(proveedor => normalizar([
      proveedor.razonSocial, proveedor.ruc, proveedor.contactoNombre, proveedor.telefono, proveedor.correo,
    ].join(' ')).includes(termino)) : proveedores;
  });

  ngOnInit(): void {
    this.cargar();
  }

  protected cargar(): void {
    if (this.cargando() || this.procesando()) return;

    this.cargando.set(true);
    this.error.set('');
    this.catalogo.listar<Proveedor>('proveedores').pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.cargando.set(false)),
    ).subscribe({
      next: proveedores => this.proveedores.set(proveedores),
      error: error => this.error.set(mensajeError(error, 'No se pudo consultar el directorio de proveedores.')),
    });
  }

  protected abrirEditor(proveedor?: Proveedor): void {
    this.accion.set(proveedor ? { tipo: 'editar', proveedor } : { tipo: 'crear' });
    this.formulario.reset({
      ruc: proveedor?.ruc ?? '',
      razonSocial: proveedor?.razonSocial ?? '',
      contactoNombre: proveedor?.contactoNombre ?? '',
      telefono: proveedor?.telefono ?? '',
      correo: proveedor?.correo ?? '',
      direccion: proveedor?.direccion ?? '',
      activo: proveedor?.activo ?? true,
    });
    this.abrirDialogo();
  }

  protected solicitarEliminar(proveedor: Proveedor): void {
    this.accion.set({ tipo: 'eliminar', proveedor });
    this.abrirDialogo();
  }

  protected cerrarDialogo(evento?: Event): void {
    evento?.preventDefault();
    if (!this.procesando()) this.dialogo().nativeElement.close();
  }

  protected errorCampo(campo: CampoProveedor): string {
    const control = this.formulario.controls[campo];
    if (!control.dirty && !control.touched) return '';

    const valor = control.value.trim();
    if (valor.length > 255) return 'Usa como máximo 255 caracteres.';
    switch (campo) {
      case 'ruc': {
        if (!/^[0-9]{11}$/.test(valor)) return 'Ingresa un RUC de 11 dígitos.';
        const accion = this.accion();
        const id = accion.tipo === 'editar' ? accion.proveedor.id : undefined;
        return this.proveedores().some(proveedor => proveedor.ruc === valor && proveedor.id !== id)
          ? 'Este RUC ya está registrado.' : '';
      }
      case 'razonSocial':
        return valor.length < 3 || valor.length > 255 ? 'Ingresa una razón social de 3 a 255 caracteres.' : '';
      case 'contactoNombre':
        return valor && (valor.length < 3 || !/^[\p{L}\p{M} .'-]+$/u.test(valor))
          ? 'Ingresa un nombre válido, sin números ni símbolos.' : '';
      case 'telefono': {
        const digitos = valor.replace(/\D/g, '').length;
        return valor && (!/^[0-9+()\s-]+$/.test(valor) || digitos < 7 || digitos > 15 || valor.length > 20)
          ? 'Ingresa un teléfono con 7 a 15 dígitos.' : '';
      }
      case 'correo':
        return valor && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(valor)
          ? 'Ingresa un correo válido, por ejemplo contacto@empresa.com.' : '';
      case 'direccion':
        return valor && valor.length < 5 ? 'La dirección debe tener al menos 5 caracteres.' : '';
    }
  }

  protected guardar(): void {
    const accion = this.accion();
    if (this.procesando() || accion.tipo === 'eliminar') return;

    this.formulario.markAllAsTouched();
    const invalido = CAMPOS_PROVEEDOR.find(campo => this.errorCampo(campo) || this.formulario.controls[campo].invalid);
    if (invalido) {
      this.errorAccion.set('Corrige los campos indicados antes de guardar.');
      this.dialogo().nativeElement.querySelector<HTMLElement>(`[formcontrolname="${invalido}"]`)?.focus();
      return;
    }

    const datos = this.formulario.getRawValue();
    const id = accion.tipo === 'editar' ? accion.proveedor.id : undefined;
    this.procesando.set(true);
    this.errorAccion.set('');
    this.catalogo.guardar<Proveedor>('proveedores', {
      ruc: datos.ruc.trim(),
      razonSocial: datos.razonSocial.trim(),
      contactoNombre: datos.contactoNombre.trim() || null,
      telefono: datos.telefono.trim() || null,
      correo: datos.correo.trim() || null,
      direccion: datos.direccion.trim() || null,
      activo: datos.activo,
    }, id).pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.procesando.set(false)),
    ).subscribe({
      next: proveedor => {
        this.proveedores.update(proveedores => id
          ? proveedores.map(item => item.id === id ? proveedor : item)
          : [proveedor, ...proveedores]);
        this.dialogo().nativeElement.close();
        toast.success(id ? 'Proveedor actualizado.' : 'Proveedor creado.');
      },
      error: error => this.errorAccion.set(mensajeError(error, 'No se pudo guardar el proveedor.')),
    });
  }

  protected eliminar(): void {
    const accion = this.accion();
    if (this.procesando() || accion.tipo !== 'eliminar') return;

    const { id } = accion.proveedor;
    this.procesando.set(true);
    this.errorAccion.set('');
    this.catalogo.eliminar('proveedores', id).pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.procesando.set(false)),
    ).subscribe({
      next: () => {
        this.proveedores.update(proveedores => proveedores.map(proveedor =>
          proveedor.id === id ? { ...proveedor, activo: false } : proveedor,
        ));
        this.dialogo().nativeElement.close();
        toast.success('Proveedor desactivado.');
      },
      error: error => this.errorAccion.set(mensajeError(error, 'No se pudo eliminar el proveedor.')),
    });
  }

  protected reactivar(proveedor: Proveedor): void {
    if (proveedor.activo || this.procesando()) return;

    this.procesando.set(true);
    this.catalogo.cambiarEstado<Proveedor>('proveedores', proveedor.id).pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.procesando.set(false)),
    ).subscribe({
      next: actualizado => {
        this.proveedores.update(proveedores => proveedores.map(item => item.id === actualizado.id ? actualizado : item));
        toast.success(actualizado.activo ? 'Proveedor activado.' : 'Proveedor desactivado.');
      },
      error: error => {
        toast.error(mensajeError(error, 'No se pudo cambiar el estado del proveedor.'));
        this.procesando.set(false);
        this.cargar();
      },
    });
  }

  private abrirDialogo(): void {
    this.errorAccion.set('');
    this.dialogo().nativeElement.showModal();
  }
}

function normalizar(texto: string): string {
  return texto.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLocaleLowerCase();
}

function mensajeError(error: unknown, mensaje: string): string {
  if (error instanceof HttpErrorResponse) {
    if (typeof error.error?.message === 'string') return error.error.message;
    if (error.status === 403) return 'Tu cuenta no tiene permiso para realizar esta acción.';
    return mensaje;
  }
  return error instanceof Error ? error.message : mensaje;
}
