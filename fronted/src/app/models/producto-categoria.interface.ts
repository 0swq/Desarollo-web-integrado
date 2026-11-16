import type { ProductoCategoriaId } from './producto-categoria-id.interface';

// Modelo ProductoCategoria.java. Las relaciones pueden omitirse en respuestas resumidas.
export interface ProductoCategoria {
  id: ProductoCategoriaId;
  productoId?: string | null;
  categoriaId?: string | null;
}
