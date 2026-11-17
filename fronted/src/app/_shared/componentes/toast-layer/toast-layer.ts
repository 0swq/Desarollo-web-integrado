import { AfterViewInit, Component, ElementRef, OnDestroy, PLATFORM_ID, inject, viewChild } from '@angular/core';
import { DOCUMENT, isPlatformBrowser } from '@angular/common';
import { NgxSonnerToaster } from 'ngx-sonner';

@Component({
  selector: 'app-toast-layer', standalone: true, imports: [NgxSonnerToaster],
  template: '<ngx-sonner-toaster #toaster position="top-right" [richColors]="true" [closeButton]="true" />',
  styles: [':host { display: contents; }'],
})
export class ToastLayer implements AfterViewInit, OnDestroy {
  private document = inject(DOCUMENT);
  private platform = inject(PLATFORM_ID);
  private host = inject(ElementRef<HTMLElement>);
  private toaster = viewChild.required('toaster', { read: ElementRef<HTMLElement> });
  private observer?: MutationObserver;
  private dialogs: HTMLDialogElement[] = [];
  ngAfterViewInit(): void {
    if (!isPlatformBrowser(this.platform)) return;
    this.dialogs = Array.from(this.document.querySelectorAll<HTMLDialogElement>('dialog[open]'));
    this.observer = new MutationObserver(records => {
      for (const record of records) {
        const target = record.target;
        if (!(target instanceof HTMLDialogElement)) continue;
        this.dialogs = this.dialogs.filter(dialog => dialog !== target);
        if (target.open) this.dialogs.push(target);
      }
      this.colocar();
    });
                                                                           
                                                                                     
    this.observer.observe(this.document.body, { subtree: true, attributes: true, attributeFilter: ['open'] });
    this.colocar();
  }
  private colocar(): void {
    this.dialogs = this.dialogs.filter(dialog => dialog.isConnected && dialog.open);
    const parent = this.dialogs.at(-1) || this.host.nativeElement;
    const toaster = this.toaster().nativeElement;
    if (toaster.parentElement !== parent) parent.appendChild(toaster);
  }
  ngOnDestroy(): void {
    this.observer?.disconnect();
    const toaster = this.toaster().nativeElement;
    if (toaster.parentElement !== this.host.nativeElement) this.host.nativeElement.appendChild(toaster);
  }
}
