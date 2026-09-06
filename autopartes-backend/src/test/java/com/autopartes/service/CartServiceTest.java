package com.autopartes.service;

import com.autopartes.dto.cart.AddToCartRequest;
import com.autopartes.dto.cart.CartResponse;
import com.autopartes.dto.cart.UpdateCartItemRequest;
import com.autopartes.exception.BusinessException;
import com.autopartes.model.Cart;
import com.autopartes.model.CartItem;
import com.autopartes.model.Product;
import com.autopartes.model.Stock;
import com.autopartes.model.User;
import com.autopartes.repository.CartRepository;
import com.autopartes.repository.ProductRepository;
import com.autopartes.repository.StockRepository;
import com.autopartes.repository.UserRepository;
import com.autopartes.service.impl.CartServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private User testUser;
    private Cart testCart;
    private Product testProduct;
    private Stock testStock;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("cliente@test.com")
                .nombre("Juan")
                .apellido("Pérez")
                .build();

        testCart = Cart.builder()
                .id(10L)
                .user(testUser)
                .items(new ArrayList<>())
                .build();

        testProduct = Product.builder()
                .id(100L)
                .sku("BUJ-IRID-01")
                .nombre("Bujía Iridium")
                .precio(new BigDecimal("50.00"))
                .activo(true)
                .build();

        testStock = Stock.builder()
                .id(50L)
                .product(testProduct)
                .cantidad(8)
                .stockMinimo(2)
                .build();
    }

    @Test
    @DisplayName("Debe agregar producto al carrito cuando hay stock disponible")
    void shouldAddItemToCartSuccessfully() {
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(100L);
        request.setCantidad(2);

        when(cartRepository.findByUserEmail("cliente@test.com")).thenReturn(Optional.of(testCart));
        when(productRepository.findById(100L)).thenReturn(Optional.of(testProduct));
        when(stockRepository.findByProductId(100L)).thenReturn(Optional.of(testStock));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        CartResponse response = cartService.addItemToCart("cliente@test.com", request);

        assertThat(response).isNotNull();
        assertThat(testCart.getItems()).hasSize(1);
        assertThat(testCart.getItems().get(0).getCantidad()).isEqualTo(2);
        assertThat(testCart.getItems().get(0).getPrecioUnitario()).isEqualByComparingTo("50.00");
    }

    @Test
    @DisplayName("Debe lanzar BusinessException si la cantidad solicitada supera el stock disponible")
    void shouldThrowWhenAddingMoreThanAvailableStock() {
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(100L);
        request.setCantidad(15); // Solo hay 8 en stock

        when(cartRepository.findByUserEmail("cliente@test.com")).thenReturn(Optional.of(testCart));
        when(productRepository.findById(100L)).thenReturn(Optional.of(testProduct));
        when(stockRepository.findByProductId(100L)).thenReturn(Optional.of(testStock));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> cartService.addItemToCart("cliente@test.com", request));

        assertThat(ex.getMessage()).contains("Stock insuficiente");
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    @DisplayName("Debe actualizar la cantidad de un ítem en el carrito")
    void shouldUpdateCartItemQuantity() {
        CartItem item = CartItem.builder()
                .id(20L)
                .cart(testCart)
                .product(testProduct)
                .cantidad(2)
                .precioUnitario(new BigDecimal("50.00"))
                .build();
        testCart.getItems().add(item);

        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setCantidad(4);

        when(cartRepository.findByUserEmail("cliente@test.com")).thenReturn(Optional.of(testCart));
        when(stockRepository.findByProductId(100L)).thenReturn(Optional.of(testStock));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        CartResponse response = cartService.updateCartItem("cliente@test.com", 20L, request);

        assertThat(response).isNotNull();
        assertThat(item.getCantidad()).isEqualTo(4);
    }

    @Test
    @DisplayName("Debe remover un ítem del carrito")
    void shouldRemoveItemFromCart() {
        CartItem item = CartItem.builder()
                .id(20L)
                .cart(testCart)
                .product(testProduct)
                .cantidad(2)
                .precioUnitario(new BigDecimal("50.00"))
                .build();
        testCart.getItems().add(item);

        when(cartRepository.findByUserEmail("cliente@test.com")).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        CartResponse response = cartService.removeItemFromCart("cliente@test.com", 20L);

        assertThat(response.getItems()).isEmpty();
        assertThat(testCart.getItems()).isEmpty();
    }
}
