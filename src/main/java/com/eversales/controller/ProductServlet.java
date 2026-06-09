package com.eversales.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import com.eversales.model.Product;
import com.eversales.service.ProductService;
import com.eversales.util.ResponseUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Product servlet - handles product browsing and admin operations.
 * Endpoints:
 * - GET /api/products (list all products)
 * - GET /api/products?search=keyword (search products)
 * - GET /api/products/{id} (get product details)
 * - POST /api/products (create product - admin only)
 * - PUT /api/products/{id} (update product - admin only)
 * - DELETE /api/products/{id} (delete product - admin only)
 */
public class ProductServlet extends HttpServlet {
    private ProductService productService = new ProductService();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        
        try {
            String pathInfo = request.getPathInfo();
            String searchKeyword = request.getParameter("search");
            
            // Extract product ID if present
            if (pathInfo != null && !pathInfo.equals("/")) {
                // GET /api/products/{id}
                int productId = Integer.parseInt(pathInfo.substring(1));
                Product product = productService.getProductById(productId);
                
                if (product != null) {
                    String productJson = buildProductJson(product);
                    response.getWriter().write(ResponseUtil.success(productJson));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().write(ResponseUtil.notFound("Product"));
                }
            } else if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
                // GET /api/products?search=keyword
                List<Product> products = productService.searchProducts(searchKeyword);
                String productsJson = buildProductsListJson(products);
                response.getWriter().write(ResponseUtil.success(productsJson));
            } else {
                // GET /api/products (list all)
                List<Product> products = productService.getAllProducts();
                String productsJson = buildProductsListJson(products);
                response.getWriter().write(ResponseUtil.success(productsJson));
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(ResponseUtil.error("Invalid product ID"));
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
            // Check if user is admin
            if (!isAdmin(request)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write(ResponseUtil.forbidden());
                return;
            }
            
            String name = request.getParameter("name");
            String description = request.getParameter("description");
            String priceStr = request.getParameter("price");
            String stockStr = request.getParameter("stock");
            
            if (name == null || priceStr == null || stockStr == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(ResponseUtil.error("Missing required fields"));
                return;
            }
            
            BigDecimal price = new BigDecimal(priceStr);
            int stock = Integer.parseInt(stockStr);
            
            if (productService.createProduct(name, description, price, stock)) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.getWriter().write(ResponseUtil.success("message", "Product created successfully"));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(ResponseUtil.error("Failed to create product"));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(ResponseUtil.error("Server error: " + e.getMessage()));
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
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
            if (pathInfo == null || pathInfo.equals("/")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(ResponseUtil.error("Product ID required"));
                return;
            }
            
            int productId = Integer.parseInt(pathInfo.substring(1));
            String name = request.getParameter("name");
            String description = request.getParameter("description");
            String priceStr = request.getParameter("price");
            String stockStr = request.getParameter("stock");
            
            if (name == null || priceStr == null || stockStr == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(ResponseUtil.error("Missing required fields"));
                return;
            }
            
            BigDecimal price = new BigDecimal(priceStr);
            int stock = Integer.parseInt(stockStr);
            
            if (productService.updateProduct(productId, name, description, price, stock)) {
                response.getWriter().write(ResponseUtil.success("message", "Product updated successfully"));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write(ResponseUtil.notFound("Product"));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(ResponseUtil.error("Server error: " + e.getMessage()));
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
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
            if (pathInfo == null || pathInfo.equals("/")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(ResponseUtil.error("Product ID required"));
                return;
            }
            
            int productId = Integer.parseInt(pathInfo.substring(1));
            
            if (productService.deleteProduct(productId)) {
                response.getWriter().write(ResponseUtil.success("message", "Product deleted successfully"));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write(ResponseUtil.notFound("Product"));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(ResponseUtil.error("Server error: " + e.getMessage()));
        }
    }
    
    private boolean isAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        Object userObj = session.getAttribute("user");
        if (userObj instanceof com.eversales.model.User) {
            com.eversales.model.User user = (com.eversales.model.User) userObj;
            return "admin".equals(user.getRole());
        }
        return false;
    }
    
    private String buildProductJson(Product product) {
        return "{\"productId\":" + product.getProductId() +
               ",\"name\":\"" + product.getName() +
               "\",\"description\":\"" + (product.getDescription() != null ? product.getDescription() : "") +
               "\",\"price\":" + product.getPrice() +
               ",\"stock\":" + product.getStock() + "}";
    }
    
    private String buildProductsListJson(List<Product> products) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < products.size(); i++) {
            if (i > 0) json.append(",");
            json.append(buildProductJson(products.get(i)));
        }
        json.append("]");
        return json.toString();
    }
}
