import type { ProductoCategoriaId } from './producto-categoria-id.interface';

                                                                                         
export interface ProductoCategoria {
  id: ProductoCategoriaId;
  productoId?: string | null;
  categoriaId?: string | null;
}
