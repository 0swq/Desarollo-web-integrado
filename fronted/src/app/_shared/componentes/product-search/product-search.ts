import { Component, EventEmitter, Output, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-product-search',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './product-search.html',
  styleUrl: './product-search.css',
})
export class ProductSearchComponent {
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  @Output() searched = new EventEmitter<void>();
  termino = '';

  constructor() {
    this.route.queryParamMap.pipe(takeUntilDestroyed()).subscribe(params => {
      this.termino = params.get('q') || '';
    });
  }

  buscar(): void {
    void this.router.navigate(['/catalogo'], {
      queryParams: { q: this.termino.trim() || null },
    });
    this.searched.emit();
  }
}
