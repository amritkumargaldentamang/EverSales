package com.eversales.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.eversales.config.AppConfig;
import com.eversales.model.CartItem;
import com.eversales.model.Order;
import com.eversales.model.OrderItem;
import com.eversales.repository.OrderDAO;
import com.eversales.repository.OrderItemDAO;

/**
 * Service layer for order operations.
 * Handles order creation, retrieval, and status updates.
 */
public class OrderService {
    private OrderDAO orderDAO = new OrderDAO();
    private OrderItemDAO orderItemDAO = new OrderItemDAO();
    private ProductService productService = new ProductService();
    
    /**
     * Create an order from cart items
     * @return the order ID if successful, -1 otherwise
     */
    public int createOrderFromCart(int userId, Map<Integer, CartItem> cart) {
        if (cart.isEmpty()) {
            System.err.println("Cart is empty, cannot create order");
            return -1;
        }
        
        // Calculate total amount
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItem item : cart.values()) {
            totalAmount = totalAmount.add(item.getTotal());
        }
        
        // Create order
        Order order = new Order(userId, totalAmount, AppConfig.ORDER_STATUS_PENDING);
        int orderId = orderDAO.createOrder(order);
        
        if (orderId > 0) {
            // Create order items and reduce stock
            List<OrderItem> items = new ArrayList<>();
            for (CartItem cartItem : cart.values()) {
                OrderItem orderItem = new OrderItem(orderId, cartItem.getProductId(), 
                                                   cartItem.getQuantity(), cartItem.getPrice());
                items.add(orderItem);
                
                // Reduce product stock
                productService.reduceStock(cartItem.getProductId(), cartItem.getQuantity());
            }
            
            // Save order items to database
            orderItemDAO.createOrderItems(items);
            return orderId;
        }
        
        return -1;
    }
    
    /**
     * Get a specific order by ID
     */
    public Order getOrderById(int orderId) {
        Order order = orderDAO.getOrderById(orderId);
        if (order != null) {
            // Load order items
            List<OrderItem> items = orderItemDAO.getItemsByOrderId(orderId);
            order.setItems(items);
        }
        return order;
    }
    
    /**
     * Get all orders for a user
     */
    public List<Order> getUserOrders(int userId) {
        List<Order> orders = orderDAO.getUserOrders(userId);
        // Load order items for each order
        for (Order order : orders) {
            List<OrderItem> items = orderItemDAO.getItemsByOrderId(order.getOrderId());
            order.setItems(items);
        }
        return orders;
    }
    
    /**
     * Get all orders (admin only)
     */
    public List<Order> getAllOrders() {
        List<Order> orders = orderDAO.getAllOrders();
        // Load order items for each order
        for (Order order : orders) {
            List<OrderItem> items = orderItemDAO.getItemsByOrderId(order.getOrderId());
            order.setItems(items);
        }
        return orders;
    }
    
    /**
     * Update order status
     */
    public boolean updateOrderStatus(int orderId, String newStatus) {
        Order order = orderDAO.getOrderById(orderId);
        if (order == null) {
            return false;
        }
        
        return orderDAO.updateOrderStatus(orderId, newStatus);
    }
    
    /**
     * Cancel an order and restore stock
     */
    public boolean cancelOrder(int orderId) {
        Order order = orderDAO.getOrderById(orderId);
        if (order == null) {
            return false;
        }
        
        // Restore product stock
        List<OrderItem> items = orderItemDAO.getItemsByOrderId(orderId);
        for (OrderItem item : items) {
            productService.restoreStock(item.getProductId(), item.getQuantity());
        }
        
        // Update order status to cancelled
        return orderDAO.updateOrderStatus(orderId, AppConfig.ORDER_STATUS_CANCELLED);
    }
    
    /**
     * Delete an order (admin only)
     */
    public boolean deleteOrder(int orderId) {
        // Delete order items first
        orderItemDAO.deleteItemsByOrderId(orderId);
        // Then delete the order
        return orderDAO.deleteOrder(orderId);
    }
}
