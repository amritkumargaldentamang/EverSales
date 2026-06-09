package com.eversales.controller;

import java.io.IOException;

import com.eversales.model.User;
import com.eversales.service.AuthService;
import com.eversales.util.ResponseUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Authentication servlet - handles user registration, login, and logout.
 * Endpoints:
 * - POST /api/auth/register
 * - POST /api/auth/login
 * - POST /api/auth/logout
 * - GET /api/auth/me (get current user info)
 */
public class AuthServlet extends HttpServlet {
    private AuthService authService = new AuthService();
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        
        String pathInfo = request.getPathInfo();
        
        if ("/register".equals(pathInfo)) {
            handleRegister(request, response);
        } else if ("/login".equals(pathInfo)) {
            handleLogin(request, response);
        } else if ("/logout".equals(pathInfo)) {
            handleLogout(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(ResponseUtil.notFound("endpoint"));
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        
        String pathInfo = request.getPathInfo();
        
        if ("/me".equals(pathInfo)) {
            handleGetCurrentUser(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(ResponseUtil.notFound("endpoint"));
        }
    }
    
    private void handleRegister(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String fullName = request.getParameter("fullName");
            String email = request.getParameter("email");
            String password = request.getParameter("password");
            String confirmPassword = request.getParameter("confirmPassword");
            String phoneNumber = request.getParameter("phoneNumber");
            
            // Validation
            if (fullName == null || fullName.trim().isEmpty() ||
                email == null || email.trim().isEmpty() ||
                password == null || password.trim().isEmpty() ||
                !password.equals(confirmPassword)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(ResponseUtil.error("Invalid input data"));
                return;
            }
            
            // Register user
            User user = authService.register(fullName, email, password, phoneNumber);
            
            if (user != null) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.getWriter().write(ResponseUtil.success("id", String.valueOf(user.getId())));
            } else {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                response.getWriter().write(ResponseUtil.error("Registration failed. Email may already exist."));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(ResponseUtil.error("Server error: " + e.getMessage()));
        }
    }
    
    private void handleLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String email = request.getParameter("email");
            String password = request.getParameter("password");
            
            if (email == null || email.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(ResponseUtil.error("Email and password required"));
                return;
            }
            
            User user = authService.login(email, password);
            
            if (user != null) {
                // Create session and store user info
                HttpSession session = request.getSession(true);
                session.setAttribute("userId", user.getId());
                session.setAttribute("user", user);
                
                String userData = "{\"id\":" + user.getId() + 
                                 ",\"fullName\":\"" + user.getFullName() + 
                                 "\",\"email\":\"" + user.getEmail() + 
                                 "\",\"role\":\"" + user.getRole() + "\"}";
                
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(ResponseUtil.success(userData));
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(ResponseUtil.error("Invalid email or password"));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(ResponseUtil.error("Server error: " + e.getMessage()));
        }
    }
    
    private void handleLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(ResponseUtil.success("message", "Logged out successfully"));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(ResponseUtil.error("Server error: " + e.getMessage()));
        }
    }
    
    private void handleGetCurrentUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            HttpSession session = request.getSession(false);
            
            if (session == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(ResponseUtil.unauthorized());
                return;
            }
            
            User user = (User) session.getAttribute("user");
            if (user == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(ResponseUtil.unauthorized());
                return;
            }
            
            // Refresh user data from database
            user = authService.getUserById(user.getId());
            session.setAttribute("user", user);
            
            String userData = "{\"id\":" + user.getId() + 
                             ",\"fullName\":\"" + user.getFullName() + 
                             "\",\"email\":\"" + user.getEmail() + 
                             "\",\"role\":\"" + user.getRole() + "\"}";
            
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(ResponseUtil.success(userData));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(ResponseUtil.error("Server error: " + e.getMessage()));
        }
    }
}
