package com.autopartes.service;

import com.autopartes.dto.cart.AddToCartRequest;
import com.autopartes.dto.cart.CartResponse;
import com.autopartes.dto.cart.UpdateCartItemRequest;

public interface CartService {
    CartResponse getCartByUserEmail(String userEmail);
    CartResponse addItemToCart(String userEmail, AddToCartRequest request);
    CartResponse updateCartItem(String userEmail, Long itemId, UpdateCartItemRequest request);
    CartResponse removeItemFromCart(String userEmail, Long itemId);
    void clearCart(String userEmail);
}
