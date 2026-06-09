package com.eversales.service;

import java.math.BigDecimal;
import java.util.List;

import com.eversales.model.Product;
import com.eversales.repository.ProductDAO;

/**
 * Service layer for product operations.
 * Handles product retrieval, search, and admin CRUD operations.
 */
public class ProductService {
    private ProductDAO productDAO = new ProductDAO();
    
    /**
     * Get all products
     */
    public List<Product> getAllProducts() {
        return productDAO.getAllProducts();
    }
    
    /**
     * Get a specific product by ID
     */
    public Product getProductById(int productId) {
        return productDAO.getProductById(productId);
    }
    
    /**
     * Get all products in stock (stock > 0)
     */
    public List<Product> getProductsInStock() {
        return productDAO.getProductsInStock();
    }
    
    /**
     * Search products by keyword (name or description)
     */
    public List<Product> searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllProducts();
        }
        return productDAO.searchProducts(keyword.trim());
    }
    
    /**
     * Create a new product (admin only)
     */
    public boolean createProduct(String name, String description, BigDecimal price, int stock) {
        if (name == null || name.trim().isEmpty() || price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            System.err.println("Invalid product data");
            return false;
        }
        
        Product product = new Product(name, description, price, stock);
        return productDAO.createProduct(product);
    }
    
    /**
     * Update an existing product (admin only)
     */
    public boolean updateProduct(int productId, String name, String description, 
                                 BigDecimal price, int stock) {
        Product product = productDAO.getProductById(productId);
        if (product == null) {
            return false;
        }
        
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStock(stock);
        return productDAO.updateProduct(product);
    }
    
    /**
     * Delete a product (admin only)
     */
    public boolean deleteProduct(int productId) {
        return productDAO.deleteProduct(productId);
    }
    
    /**
     * Reduce product stock (after order is placed)
     */
    public boolean reduceStock(int productId, int quantity) {
        Product product = productDAO.getProductById(productId);
        if (product == null || product.getStock() < quantity) {
            return false;
        }
        
        product.setStock(product.getStock() - quantity);
        return productDAO.updateProduct(product);
    }
    
    /**
     * Restore product stock (if order is cancelled)
     */
    public boolean restoreStock(int productId, int quantity) {
        Product product = productDAO.getProductById(productId);
        if (product == null) {
            return false;
        }
        
        product.setStock(product.getStock() + quantity);
        return productDAO.updateProduct(product);
    }
}
