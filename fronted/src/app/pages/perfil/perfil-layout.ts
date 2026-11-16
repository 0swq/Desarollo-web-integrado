import { Component, ElementRef, effect, inject, viewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { PerfilModalService } from '../../_services/perfil-modal.service';
import { StorageService } from '../../_services/storage.service';
import { MiPerfilPage } from './mi-perfil/mi-perfil';
import { MisPedidosPage } from './mis-pedidos/mis-pedidos';
import { SeguridadPage } from './seguridad/seguridad';
@Component({
  selector: 'app-perfil-layout', standalone: true,
  imports: [MiPerfilPage, MisPedidosPage, SeguridadPage],
  templateUrl: './perfil-layout.html', styleUrl: './perfil-layout.css',
})
export class PerfilLayout {
  readonly modal = inject(PerfilModalService);
  readonly storage = inject(StorageService);
  private dialog = viewChild<ElementRef<HTMLDialogElement>>('dialog');
  constructor() {
    effect(onCleanup => {
      const dialog = this.dialog()?.nativeElement;
      if (!dialog) return;
      if (this.modal.abierto()) {
        if (!dialog.open) dialog.showModal();
        const body = dialog.ownerDocument.body;
        const anterior = body.style.overflow; body.style.overflow = 'hidden';
        onCleanup(() => { body.style.overflow = anterior; if (dialog.open) dialog.close(); });
      } else if (dialog.open) dialog.close();
    });
    this.storage.cambios$.pipe(takeUntilDestroyed()).subscribe(() => {
      if (!this.storage.obtenerToken()) this.modal.cerrar();
    });
  }
  cerrarFuera(event: MouseEvent): void {
    const dialog = this.dialog()?.nativeElement;
    if (event.target !== dialog || !dialog) return;
    const rect = dialog.getBoundingClientRect();
    if (event.clientX < rect.left || event.clientX > rect.right || event.clientY < rect.top || event.clientY > rect.bottom) this.modal.cerrar();
  }
}
