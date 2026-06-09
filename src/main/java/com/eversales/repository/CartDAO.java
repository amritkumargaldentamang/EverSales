package com.eversales.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eversales.model.CartItem;

/**
 * Data Access Object for CartItem entity.
 * Handles session-based cart operations (no database persistence).
 * Cart items are stored in-memory for a user session.
 */
public class CartDAO {
    
    /**
     * Add an item to the cart.
     * If the item already exists, update its quantity.
     */
    public void addToCart(Map<Integer, CartItem> cart, CartItem item) {
        if (cart.containsKey(item.getProductId())) {
            // Update quantity if product already in cart
            CartItem existing = cart.get(item.getProductId());
            existing.setQuantity(existing.getQuantity() + item.getQuantity());
        } else {
            // Add new item to cart
            cart.put(item.getProductId(), item);
        }
    }
    
    /**
     * Remove an item from the cart by product ID
     */
    public void removeFromCart(Map<Integer, CartItem> cart, int productId) {
        cart.remove(productId);
    }
    
    /**
     * Update the quantity of an item in the cart
     */
    public void updateQuantity(Map<Integer, CartItem> cart, int productId, int quantity) {
        if (cart.containsKey(productId)) {
            if (quantity <= 0) {
                cart.remove(productId);
            } else {
                cart.get(productId).setQuantity(quantity);
            }
        }
    }
    
    /**
     * Get all items in the cart
     */
    public List<CartItem> getCartItems(Map<Integer, CartItem> cart) {
        return new ArrayList<>(cart.values());
    }
    
    /**
     * Get a specific cart item by product ID
     */
    public CartItem getCartItem(Map<Integer, CartItem> cart, int productId) {
        return cart.get(productId);
    }
    
    /**
     * Clear the entire cart
     */
    public void clearCart(Map<Integer, CartItem> cart) {
        cart.clear();
    }
    
    /**
     * Check if cart is empty
     */
    public boolean isCartEmpty(Map<Integer, CartItem> cart) {
        return cart.isEmpty();
    }
    
    /**
     * Get the total number of items in cart
     */
    public int getCartItemCount(Map<Integer, CartItem> cart) {
        return cart.values().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }
    
    /**
     * Create a new empty cart
     */
    public Map<Integer, CartItem> createNewCart() {
        return new HashMap<>();
    }
}
