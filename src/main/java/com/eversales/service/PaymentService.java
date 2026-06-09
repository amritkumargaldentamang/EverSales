package com.eversales.service;

import java.math.BigDecimal;

import com.eversales.config.AppConfig;
import com.eversales.model.Payment;
import com.eversales.repository.PaymentDAO;

/**
 * Service layer for payment operations.
 * Handles payment processing, status updates, and refunds.
 */
public class PaymentService {
    private PaymentDAO paymentDAO = new PaymentDAO();
    private OrderService orderService = new OrderService();
    
    /**
     * Process a payment for an order
     * In this mock implementation, all payments succeed
     * @return the payment ID if successful, -1 otherwise
     */
    public int processPayment(int orderId, BigDecimal amount, String paymentMethod) {
        // Validate payment method
        if (!isValidPaymentMethod(paymentMethod)) {
            System.err.println("Invalid payment method: " + paymentMethod);
            return -1;
        }
        
        // Create payment record
        Payment payment = new Payment(orderId, amount, paymentMethod, AppConfig.PAYMENT_STATUS_PENDING);
        int paymentId = paymentDAO.createPayment(payment);
        
        if (paymentId > 0) {
            // Mock: Always succeed with payment processing
            if (completePayment(paymentId)) {
                // Update order status to completed
                orderService.updateOrderStatus(orderId, AppConfig.ORDER_STATUS_COMPLETED);
                return paymentId;
            }
        }
        
        return -1;
    }
    
    /**
     * Complete a payment (set status to completed)
     */
    private boolean completePayment(int paymentId) {
        return paymentDAO.updatePaymentStatus(paymentId, AppConfig.PAYMENT_STATUS_COMPLETED);
    }
    
    /**
     * Get payment status
     */
    public String getPaymentStatus(int paymentId) {
        Payment payment = paymentDAO.getPaymentById(paymentId);
        if (payment != null) {
            return payment.getStatus();
        }
        return null;
    }
    
    /**
     * Get payment for an order
     */
    public Payment getPaymentByOrderId(int orderId) {
        return paymentDAO.getPaymentByOrderId(orderId);
    }
    
    /**
     * Refund a payment (sets status to failed)
     */
    public boolean refundPayment(int paymentId) {
        Payment payment = paymentDAO.getPaymentById(paymentId);
        if (payment == null) {
            return false;
        }
        
        // Mark payment as failed
        boolean result = paymentDAO.updatePaymentStatus(paymentId, AppConfig.PAYMENT_STATUS_FAILED);
        
        if (result) {
            // Revert order status to pending
            orderService.updateOrderStatus(payment.getOrderId(), AppConfig.ORDER_STATUS_PENDING);
        }
        
        return result;
    }
    
    /**
     * Check if payment method is valid
     */
    private boolean isValidPaymentMethod(String method) {
        return method.equals(AppConfig.PAYMENT_METHOD_CREDIT_CARD) ||
               method.equals(AppConfig.PAYMENT_METHOD_PAYPAL) ||
               method.equals(AppConfig.PAYMENT_METHOD_BANK_TRANSFER);
    }
}
