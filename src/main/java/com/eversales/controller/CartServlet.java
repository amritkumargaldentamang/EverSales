package com.eversales.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.eversales.model.CartItem;
import com.eversales.service.CartService;
import com.eversales.util.ResponseUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Cart servlet - handles shopping cart operations.
 * Endpoints:
 * - GET /api/cart (get cart items)
 * - POST /api/cart/add (add item to cart)
 * - POST /api/cart/remove (remove item from cart)
 * - POST /api/cart/update (update item quantity)
 * - POST /api/cart/clear (clear cart)
 */
public class CartServlet extends HttpServlet {
    private CartService cartService = new CartService();
    private static final String CART_ATTR = "cart";
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        
        try {
            // Check authentication
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("userId") == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(ResponseUtil.unauthorized());
                return;
            }
            
            Map<Integer, CartItem> cart = getOrCreateCart(session);
            
            String cartJson = buildCartJson(cart);
            response.getWriter().write(ResponseUtil.success(cartJson));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(ResponseUtil.error("Server error: " + e.getMessage()));
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        
        try {
            // Check authentication
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("userId") == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(ResponseUtil.unauthorized());
                return;
            }
            
            String pathInfo = request.getPathInfo();
            
            if ("/add".equals(pathInfo)) {
                handleAddToCart(request, response, session);
            } else if ("/remove".equals(pathInfo)) {
                handleRemoveFromCart(request, response, session);
            } else if ("/update".equals(pathInfo)) {
                handleUpdateQuantity(request, response, session);
            } else if ("/clear".equals(pathInfo)) {
                handleClearCart(request, response, session);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write(ResponseUtil.notFound("endpoint"));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(ResponseUtil.error("Server error: " + e.getMessage()));
        }
    }
    
    private void handleAddToCart(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException {
        try {
            String productIdStr = request.getParameter("productId");
            String quantityStr = request.getParameter("quantity");
            
            if (productIdStr == null || quantityStr == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(ResponseUtil.error("productId and quantity required"));
                return;
            }
            
            int productId = Integer.parseInt(productIdStr);
            int quantity = Integer.parseInt(quantityStr);
            
            Map<Integer, CartItem> cart = getOrCreateCart(session);
            
            if (cartService.addToCart(cart, productId, quantity)) {
                response.getWriter().write(ResponseUtil.success("message", "Item added to cart"));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(ResponseUtil.error("Cannot add item to cart"));
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(ResponseUtil.error("Invalid productId or quantity"));
        }
    }
    
    private void handleRemoveFromCart(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException {
        try {
            String productIdStr = request.getParameter("productId");
            
            if (productIdStr == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(ResponseUtil.error("productId required"));
                return;
            }
            
            int productId = Integer.parseInt(productIdStr);
            Map<Integer, CartItem> cart = getOrCreateCart(session);
            
            cartService.removeFromCart(cart, productId);
            response.getWriter().write(ResponseUtil.success("message", "Item removed from cart"));
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(ResponseUtil.error("Invalid productId"));
        }
    }
    
    private void handleUpdateQuantity(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException {
        try {
            String productIdStr = request.getParameter("productId");
            String quantityStr = request.getParameter("quantity");
            
            if (productIdStr == null || quantityStr == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(ResponseUtil.error("productId and quantity required"));
                return;
            }
            
            int productId = Integer.parseInt(productIdStr);
            int quantity = Integer.parseInt(quantityStr);
            
            Map<Integer, CartItem> cart = getOrCreateCart(session);
            
            if (cartService.updateQuantity(cart, productId, quantity)) {
                response.getWriter().write(ResponseUtil.success("message", "Quantity updated"));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(ResponseUtil.error("Cannot update quantity"));
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(ResponseUtil.error("Invalid productId or quantity"));
        }
    }
    
    private void handleClearCart(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException {
        Map<Integer, CartItem> cart = getOrCreateCart(session);
        cartService.clearCart(cart);
        response.getWriter().write(ResponseUtil.success("message", "Cart cleared"));
    }
    
    @SuppressWarnings("unchecked")
    private Map<Integer, CartItem> getOrCreateCart(HttpSession session) {
        Map<Integer, CartItem> cart = (Map<Integer, CartItem>) session.getAttribute(CART_ATTR);
        if (cart == null) {
            cart = cartService.createNewCart();
            session.setAttribute(CART_ATTR, cart);
        }
        return cart;
    }
    
    private String buildCartJson(Map<Integer, CartItem> cart) {
        StringBuilder json = new StringBuilder("{\"items\":[");
        List<CartItem> items = cartService.getCartItems(cart);
        
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) json.append(",");
            CartItem item = items.get(i);
            json.append("{\"productId\":").append(item.getProductId())
                .append(",\"productName\":\"").append(item.getProductName())
                .append("\",\"price\":").append(item.getPrice())
                .append(",\"quantity\":").append(item.getQuantity())
                .append(",\"total\":").append(item.getTotal()).append("}");
        }
        
        BigDecimal total = cartService.getCartTotal(cart);
        json.append("],\"cartTotal\":").append(total).append("}");
        return json.toString();
    }
}
