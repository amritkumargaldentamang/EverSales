package com.eversales.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.eversales.model.CartItem;
import com.eversales.model.Product;
import com.eversales.repository.CartDAO;

/**
 * Service layer for shopping cart operations.
 * Handles add/remove/update cart items and cart calculations.
 */
public class CartService {
    private CartDAO cartDAO = new CartDAO();
    private ProductService productService = new ProductService();
    
    /**
     * Add an item to the cart
     */
    public boolean addToCart(Map<Integer, CartItem> cart, int productId, int quantity) {
        if (quantity <= 0) {
            return false;
        }
        
        Product product = productService.getProductById(productId);
        if (product == null || product.getStock() < quantity) {
            return false;
        }
        
        CartItem item = new CartItem(productId, product.getName(), product.getPrice(), quantity);
        cartDAO.addToCart(cart, item);
        return true;
    }
    
    /**
     * Remove an item from the cart
     */
    public void removeFromCart(Map<Integer, CartItem> cart, int productId) {
        cartDAO.removeFromCart(cart, productId);
    }
    
    /**
     * Update quantity of an item in the cart
     */
    public boolean updateQuantity(Map<Integer, CartItem> cart, int productId, int quantity) {
        if (quantity < 0) {
            return false;
        }
        
        if (quantity > 0) {
            Product product = productService.getProductById(productId);
            if (product == null || product.getStock() < quantity) {
                return false;
            }
        }
        
        cartDAO.updateQuantity(cart, productId, quantity);
        return true;
    }
    
    /**
     * Get all items in the cart
     */
    public List<CartItem> getCartItems(Map<Integer, CartItem> cart) {
        return cartDAO.getCartItems(cart);
    }
    
    /**
     * Get the total price of the cart
     */
    public BigDecimal getCartTotal(Map<Integer, CartItem> cart) {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cart.values()) {
            total = total.add(item.getTotal());
        }
        return total;
    }
    
    /**
     * Get the total number of items in the cart
     */
    public int getCartItemCount(Map<Integer, CartItem> cart) {
        return cartDAO.getCartItemCount(cart);
    }
    
    /**
     * Clear the cart
     */
    public void clearCart(Map<Integer, CartItem> cart) {
        cartDAO.clearCart(cart);
    }
    
    /**
     * Check if cart is empty
     */
    public boolean isCartEmpty(Map<Integer, CartItem> cart) {
        return cartDAO.isCartEmpty(cart);
    }
    
    /**
     * Create a new empty cart (for initialization)
     */
    public Map<Integer, CartItem> createNewCart() {
        return cartDAO.createNewCart();
    }
}
