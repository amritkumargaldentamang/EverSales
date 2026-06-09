package com.eversales.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.eversales.model.CartItem;
import com.eversales.model.Order;
import com.eversales.model.OrderItem;
import com.eversales.service.OrderService;
import com.eversales.util.ResponseUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Order servlet - handles order operations.
 * Endpoints:
 * - POST /api/orders (create order from cart)
 * - GET /api/orders (get user's orders)
 * - GET /api/orders/{id} (get order details)
 * - POST /api/orders/{id}/cancel (cancel order)
 */
public class OrderServlet extends HttpServlet {
    private OrderService orderService = new OrderService();
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
            
            int userId = (Integer) session.getAttribute("userId");
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/orders - get user's order history
                List<Order> orders = orderService.getUserOrders(userId);
                String ordersJson = buildOrdersListJson(orders);
                response.getWriter().write(ResponseUtil.success(ordersJson));
            } else {
                // GET /api/orders/{id} - get specific order
                int orderId = Integer.parseInt(pathInfo.substring(1));
                Order order = orderService.getOrderById(orderId);
                
                if (order != null && order.getUserId() == userId) {
                    String orderJson = buildOrderJson(order);
                    response.getWriter().write(ResponseUtil.success(orderJson));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().write(ResponseUtil.notFound("Order"));
                }
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(ResponseUtil.error("Invalid order ID"));
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
            
            int userId = (Integer) session.getAttribute("userId");
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                // POST /api/orders - create order from cart
                handleCreateOrder(request, response, session, userId);
            } else if (pathInfo.contains("/cancel")) {
                // POST /api/orders/{id}/cancel - cancel order
                handleCancelOrder(request, response, userId, pathInfo);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write(ResponseUtil.notFound("endpoint"));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(ResponseUtil.error("Server error: " + e.getMessage()));
        }
    }
    
    @SuppressWarnings("unchecked")
    private void handleCreateOrder(HttpServletRequest request, HttpServletResponse response, 
                                   HttpSession session, int userId) throws IOException {
        Map<Integer, CartItem> cart = (Map<Integer, CartItem>) session.getAttribute(CART_ATTR);
        
        if (cart == null || cart.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(ResponseUtil.error("Cart is empty"));
            return;
        }
        
        int orderId = orderService.createOrderFromCart(userId, cart);
        
        if (orderId > 0) {
            // Clear cart after successful order
            cart.clear();
            
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write(ResponseUtil.success("orderId", String.valueOf(orderId)));
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(ResponseUtil.error("Failed to create order"));
        }
    }
    
    private void handleCancelOrder(HttpServletRequest request, HttpServletResponse response, 
                                   int userId, String pathInfo) throws IOException {
        try {
            // Extract order ID from pathInfo like "/123/cancel"
            String[] parts = pathInfo.split("/");
            int orderId = Integer.parseInt(parts[1]);
            
            Order order = orderService.getOrderById(orderId);
            if (order == null || order.getUserId() != userId) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write(ResponseUtil.notFound("Order"));
                return;
            }
            
            if (orderService.cancelOrder(orderId)) {
                response.getWriter().write(ResponseUtil.success("message", "Order cancelled successfully"));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(ResponseUtil.error("Cannot cancel this order"));
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(ResponseUtil.error("Invalid order ID"));
        }
    }
    
    private String buildOrderJson(Order order) {
        StringBuilder json = new StringBuilder("{\"orderId\":" + order.getOrderId() +
                ",\"userId\":" + order.getUserId() +
                ",\"totalAmount\":" + order.getTotalAmount() +
                ",\"status\":\"" + order.getStatus() +
                "\",\"items\":[");
        
        List<OrderItem> items = order.getItems();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) json.append(",");
            OrderItem item = items.get(i);
            json.append("{\"productId\":").append(item.getProductId())
                .append(",\"quantity\":").append(item.getQuantity())
                .append(",\"price\":").append(item.getPrice()).append("}");
        }
        
        json.append("]}");
        return json.toString();
    }
    
    private String buildOrdersListJson(List<Order> orders) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < orders.size(); i++) {
            if (i > 0) json.append(",");
            json.append(buildOrderJson(orders.get(i)));
        }
        json.append("]");
        return json.toString();
    }
}
