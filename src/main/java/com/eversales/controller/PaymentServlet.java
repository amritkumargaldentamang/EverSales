package com.eversales.controller;

import com.eversales.model.Payment;
import com.eversales.service.PaymentService;
import com.eversales.util.ResponseUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * Payment servlet - handles payment operations.
 * Endpoints:
 * - POST /api/payments/process (process payment for an order)
 * - GET /api/payments/{orderId} (get payment status)
 * - POST /api/payments/{paymentId}/refund (refund a payment)
 */
public class PaymentServlet extends HttpServlet {
    private PaymentService paymentService = new PaymentService();
    
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
            
            String pathInfo = request.getPathInfo();
            
            if (pathInfo != null && !pathInfo.equals("/")) {
                int orderId = Integer.parseInt(pathInfo.substring(1));
                Payment payment = paymentService.getPaymentByOrderId(orderId);
                
                if (payment != null) {
                    String paymentJson = buildPaymentJson(payment);
                    response.getWriter().write(ResponseUtil.success(paymentJson));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().write(ResponseUtil.notFound("Payment"));
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(ResponseUtil.error("Order ID required"));
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
            
            String pathInfo = request.getPathInfo();
            
            if ("/process".equals(pathInfo)) {
                handleProcessPayment(request, response);
            } else if (pathInfo != null && pathInfo.contains("/refund")) {
                handleRefundPayment(request, response, pathInfo);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write(ResponseUtil.notFound("endpoint"));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(ResponseUtil.error("Server error: " + e.getMessage()));
        }
    }
    
    private void handleProcessPayment(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String orderIdStr = request.getParameter("orderId");
            String amountStr = request.getParameter("amount");
            String paymentMethod = request.getParameter("paymentMethod");
            
            if (orderIdStr == null || amountStr == null || paymentMethod == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(ResponseUtil.error("orderId, amount, and paymentMethod required"));
                return;
            }
            
            int orderId = Integer.parseInt(orderIdStr);
            BigDecimal amount = new BigDecimal(amountStr);
            
            int paymentId = paymentService.processPayment(orderId, amount, paymentMethod);
            
            if (paymentId > 0) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.getWriter().write(ResponseUtil.success("paymentId", String.valueOf(paymentId)));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(ResponseUtil.error("Payment processing failed"));
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(ResponseUtil.error("Invalid orderId or amount"));
        }
    }
    
    private void handleRefundPayment(HttpServletRequest request, HttpServletResponse response, 
                                     String pathInfo) throws IOException {
        try {
            // Extract payment ID from pathInfo like "/123/refund"
            String[] parts = pathInfo.split("/");
            int paymentId = Integer.parseInt(parts[1]);
            
            if (paymentService.refundPayment(paymentId)) {
                response.getWriter().write(ResponseUtil.success("message", "Payment refunded successfully"));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(ResponseUtil.error("Cannot refund this payment"));
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(ResponseUtil.error("Invalid payment ID"));
        }
    }
    
    private String buildPaymentJson(Payment payment) {
        return "{\"paymentId\":" + payment.getPaymentId() +
               ",\"orderId\":" + payment.getOrderId() +
               ",\"amount\":" + payment.getAmount() +
               ",\"paymentMethod\":\"" + payment.getPaymentMethod() +
               "\",\"status\":\"" + payment.getStatus() + "\"}";
    }
}
