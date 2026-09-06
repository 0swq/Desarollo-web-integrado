package com.autopartes.service.impl;

import com.autopartes.dto.cart.AddToCartRequest;
import com.autopartes.dto.cart.CartItemResponse;
import com.autopartes.dto.cart.CartResponse;
import com.autopartes.dto.cart.UpdateCartItemRequest;
import com.autopartes.exception.BusinessException;
import com.autopartes.exception.ResourceNotFoundException;
import com.autopartes.model.*;
import com.autopartes.repository.*;
import com.autopartes.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final StockRepository stockRepository;

    @Override
    @Transactional
    public CartResponse getCartByUserEmail(String userEmail) {
        Cart cart = getOrCreateCart(userEmail);
        return mapToCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addItemToCart(String userEmail, AddToCartRequest request) {
        Cart cart = getOrCreateCart(userEmail);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", request.getProductId()));

        if (!product.getActivo()) {
            throw new BusinessException("El producto seleccionado no se encuentra disponible.");
        }

        // Verificar disponibilidad en stock
        Stock stock = stockRepository.findByProductId(product.getId()).orElse(null);
        int stockDisponible = stock != null ? stock.getCantidad() : 0;

        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();

        int cantidadFinal = request.getCantidad();
        if (existingItemOpt.isPresent()) {
            cantidadFinal += existingItemOpt.get().getCantidad();
        }

        if (stockDisponible < cantidadFinal) {
            throw new BusinessException(String.format("Stock insuficiente para %s. Disponible: %d, solicitado: %d",
                    product.getNombre(), stockDisponible, cantidadFinal));
        }

        BigDecimal precioAplicado = product.getPrecioPromocional() != null && product.getPrecioPromocional().compareTo(BigDecimal.ZERO) > 0
                ? product.getPrecioPromocional()
                : product.getPrecio();

        if (existingItemOpt.isPresent()) {
            CartItem item = existingItemOpt.get();
            item.setCantidad(cantidadFinal);
            item.setPrecioUnitario(precioAplicado);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .cantidad(request.getCantidad())
                    .precioUnitario(precioAplicado)
                    .build();
            cart.getItems().add(newItem);
        }

        Cart saved = cartRepository.save(cart);
        return mapToCartResponse(saved);
    }

    @Override
    @Transactional
    public CartResponse updateCartItem(String userEmail, Long itemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCart(userEmail);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item de carrito", "id", itemId));

        Stock stock = stockRepository.findByProductId(item.getProduct().getId()).orElse(null);
        int stockDisponible = stock != null ? stock.getCantidad() : 0;

        if (stockDisponible < request.getCantidad()) {
            throw new BusinessException(String.format("Stock insuficiente. Disponible: %d", stockDisponible));
        }

        item.setCantidad(request.getCantidad());
        Cart saved = cartRepository.save(cart);
        return mapToCartResponse(saved);
    }

    @Override
    @Transactional
    public CartResponse removeItemFromCart(String userEmail, Long itemId) {
        Cart cart = getOrCreateCart(userEmail);
        cart.getItems().removeIf(item -> item.getId().equals(itemId));
        Cart saved = cartRepository.save(cart);
        return mapToCartResponse(saved);
    }

    @Override
    @Transactional
    public void clearCart(String userEmail) {
        Cart cart = getOrCreateCart(userEmail);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private Cart getOrCreateCart(String userEmail) {
        return cartRepository.findByUserEmail(userEmail)
                .orElseGet(() -> {
                    User user = userRepository.findByEmail(userEmail)
                            .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", userEmail));
                    return cartRepository.save(Cart.builder()
                            .user(user)
                            .items(new ArrayList<>())
                            .build());
                });
    }

    private CartResponse mapToCartResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(item -> CartItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productSku(item.getProduct().getSku())
                        .productNombre(item.getProduct().getNombre())
                        .productImagenUrl(item.getProduct().getImagenUrl())
                        .cantidad(item.getCantidad())
                        .precioUnitario(item.getPrecioUnitario())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        BigDecimal total = itemResponses.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalItems = itemResponses.stream()
                .mapToInt(CartItemResponse::getCantidad)
                .sum();

        return CartResponse.builder()
                .id(cart.getId())
                .items(itemResponses)
                .totalItems(totalItems)
                .total(total)
                .moneda("PEN")
                .build();
    }
}
