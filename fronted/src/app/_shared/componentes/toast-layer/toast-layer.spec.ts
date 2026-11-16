import { TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ToastLayer } from './toast-layer';
describe('Toast sobre diálogos', () => {
 beforeEach(() => { vi.stubGlobal('matchMedia', vi.fn(() => ({ matches: false, addEventListener: vi.fn(), removeEventListener: vi.fn() }))); });
 it('mueve el mismo toaster al modal y lo devuelve al cerrar', async () => {
  TestBed.configureTestingModule({ imports: [ToastLayer] });
  const fixture = TestBed.createComponent(ToastLayer); fixture.detectChanges();
  const toaster = fixture.nativeElement.querySelector('ngx-sonner-toaster');
  const dialog = document.createElement('dialog'); document.body.appendChild(dialog);
  dialog.setAttribute('open', ''); await Promise.resolve();
  expect(toaster.parentElement).toBe(dialog);
  dialog.removeAttribute('open'); await Promise.resolve();
  expect(toaster.parentElement).toBe(fixture.nativeElement);
  dialog.remove(); fixture.destroy();
 });
 it('sigue el modal más reciente y restaura el anterior', async () => {
  TestBed.configureTestingModule({ imports: [ToastLayer] });
  const fixture = TestBed.createComponent(ToastLayer); fixture.detectChanges();
  const toaster = fixture.nativeElement.querySelector('ngx-sonner-toaster');
  const primero = document.createElement('dialog'); const segundo = document.createElement('dialog');
  document.body.append(primero, segundo);
  primero.setAttribute('open', ''); await Promise.resolve();
  segundo.setAttribute('open', ''); await Promise.resolve(); expect(toaster.parentElement).toBe(segundo);
  segundo.removeAttribute('open'); await Promise.resolve(); expect(toaster.parentElement).toBe(primero);
  fixture.destroy(); primero.remove(); segundo.remove();
 });
});
