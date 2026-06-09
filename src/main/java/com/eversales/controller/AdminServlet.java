package com.eversales.controller;

import java.io.IOException;
import java.util.List;

import com.eversales.model.Order;
import com.eversales.model.User;
import com.eversales.repository.UserDAO;
import com.eversales.service.OrderService;
import com.eversales.service.ProductService;
import com.eversales.util.ResponseUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Admin servlet - handles admin dashboard operations.
 * Endpoints:
 * - GET /api/admin/users (list all users)
 * - GET /api/admin/orders (list all orders)
 * - GET /api/admin/stats (get dashboard statistics)
 */
public class AdminServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();
    private OrderService orderService = new OrderService();
    private ProductService productService = new ProductService();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        
        try {
            // Check if user is admin
            if (!isAdmin(request)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write(ResponseUtil.forbidden());
                return;
            }
            
            String pathInfo = request.getPathInfo();
            
            if ("/users".equals(pathInfo)) {
                handleListUsers(response);
            } else if ("/orders".equals(pathInfo)) {
                handleListOrders(response);
            } else if ("/stats".equals(pathInfo)) {
                handleGetStats(response);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write(ResponseUtil.notFound("endpoint"));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(ResponseUtil.error("Server error: " + e.getMessage()));
        }
    }
    
    private void handleListUsers(HttpServletResponse response) throws IOException {
        try {
            List<User> users = userDAO.getAllUsers();
            String usersJson = buildUsersListJson(users);
            response.getWriter().write(ResponseUtil.success(usersJson));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(ResponseUtil.error("Error retrieving users: " + e.getMessage()));
        }
    }
    
    private void handleListOrders(HttpServletResponse response) throws IOException {
        try {
            var orders = orderService.getAllOrders();
            String ordersJson = buildOrdersListJson(orders);
            response.getWriter().write(ResponseUtil.success(ordersJson));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(ResponseUtil.error("Error retrieving orders: " + e.getMessage()));
        }
    }
    
    private void handleGetStats(HttpServletResponse response) throws IOException {
        try {
            int userCount = userDAO.getAllUsers().size();
            var allOrders = orderService.getAllOrders();
            int orderCount = allOrders.size();
            var allProducts = productService.getAllProducts();
            int productCount = allProducts.size();
            
            String statsJson = "{\"userCount\":" + userCount +
                               ",\"orderCount\":" + orderCount +
                               ",\"productCount\":" + productCount + "}";
            
            response.getWriter().write(ResponseUtil.success(statsJson));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(ResponseUtil.error("Error retrieving statistics: " + e.getMessage()));
        }
    }
    
    private boolean isAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        Object userObj = session.getAttribute("user");
        if (userObj instanceof User) {
            User user = (User) userObj;
            return "admin".equals(user.getRole());
        }
        return false;
    }
    
    private String buildUsersListJson(List<User> users) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < users.size(); i++) {
            if (i > 0) json.append(",");
            User user = users.get(i);
            json.append("{\"id\":").append(user.getId())
                .append(",\"fullName\":\"").append(user.getFullName())
                .append("\",\"email\":\"").append(user.getEmail())
                .append("\",\"role\":\"").append(user.getRole())
                .append("\",\"phoneNumber\":\"").append(user.getPhoneNumber() != null ? user.getPhoneNumber() : "")
                .append("\"}");
        }
        json.append("]");
        return json.toString();
    }
    
    private String buildOrdersListJson(List<Order> orders) {
        StringBuilder json = new StringBuilder("[");
        int count = 0;
        for (Order order : orders) {
            if (count > 0) json.append(",");
            json.append("{\"orderId\":").append(order.getOrderId())
                .append(",\"userId\":").append(order.getUserId())
                .append(",\"totalAmount\":").append(order.getTotalAmount())
                .append(",\"status\":\"").append(order.getStatus())
                .append("\"}");
            count++;
        }
        json.append("]");
        return json.toString();
    }
}
