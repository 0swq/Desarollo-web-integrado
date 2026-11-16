import { DomSanitizer } from '@angular/platform-browser';
import { computed } from '@angular/core';
import { ParametrosService } from '../../_services/parametros.service';
import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';


@Component({
  selector: 'app-nosotros', standalone: true, imports: [RouterLink],
  templateUrl: './nosotros.html', styleUrl: './nosotros.css',
})
export class NosotrosPage { private parametros = inject(ParametrosService);
  get contacto() { return this.parametros.contacto(); }
  private sanitizer=inject(DomSanitizer);
  readonly mapa=computed(()=>{const url=this.parametros.contacto().mapaUrl;return url?this.sanitizer.bypassSecurityTrustResourceUrl(url):null;}); }
