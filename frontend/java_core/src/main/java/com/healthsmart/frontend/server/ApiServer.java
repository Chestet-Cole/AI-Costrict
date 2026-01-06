package com.healthsmart.frontend.server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.healthsmart.frontend.controller.AnalyticsController;
import com.healthsmart.frontend.controller.HealthReportController;
import com.healthsmart.frontend.controller.PlanController;
import com.healthsmart.frontend.controller.UserController;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * REST API 服务器
 * REST API Server for Frontend-Backend Integration
 */
public class ApiServer {
    
    private static final int PORT = 8080;
    private static final Gson gson = new Gson();
    private static HttpServer server;
    
    public static void main(String[] args) throws IOException {
        start();
    }
    
    public static void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        
        // API 路由
        server.createContext("/api/user", new UserHandler());
        server.createContext("/api/health-report", new HealthReportHandler());
        server.createContext("/api/plan", new PlanHandler());
        server.createContext("/api/analytics", new AnalyticsHandler());
        
        // 健康检查
        server.createContext("/api/health", exchange -> {
            sendJsonResponse(exchange, 200, Map.of("status", "ok", "message", "服务器运行正常"));
        });
        
        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();
        
        System.out.println("========================================");
        System.out.println("   API 服务器已启动");
        System.out.println("   地址: http://localhost:" + PORT);
        System.out.println("========================================");
    }
    
    public static void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("API 服务器已停止");
        }
    }
    
    /**
     * 用户API处理器
     */
    static class UserHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String body = readRequestBody(exchange);
            String action = getAction(exchange.getRequestURI().getQuery());
            
            String response = UserController.handleRequest(action, body);
            sendJsonResponse(exchange, 200, response);
        }
    }
    
    /**
     * 健康报告API处理器
     */
    static class HealthReportHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String body = readRequestBody(exchange);
            String action = getAction(exchange.getRequestURI().getQuery());
            
            String response = HealthReportController.handleRequest(action, body);
            sendJsonResponse(exchange, 200, response);
        }
    }
    
    /**
     * 计划API处理器
     */
    static class PlanHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String body = readRequestBody(exchange);
            String action = getAction(exchange.getRequestURI().getQuery());
            
            String response = PlanController.handleRequest(action, body);
            sendJsonResponse(exchange, 200, response);
        }
    }
    
    /**
     * 数据分析API处理器
     */
    static class AnalyticsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String body = readRequestBody(exchange);
            String action = getAction(exchange.getRequestURI().getQuery());
            
            String response = AnalyticsController.handleRequest(action, body);
            sendJsonResponse(exchange, 200, response);
        }
    }
    
    /**
     * 读取请求体
     */
    private static String readRequestBody(HttpExchange exchange) throws IOException {
        InputStream inputStream = exchange.getRequestBody();
        return new String(inputStream.readAllBytes(), "UTF-8");
    }
    
    /**
     * 解析请求参数中的action
     */
    private static String getAction(String query) {
        if (query == null || query.isEmpty()) {
            return "";
        }
        String[] params = query.split("&");
        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2 && keyValue[0].equals("action")) {
                return keyValue[1];
            }
        }
        // 尝试从body中获取action
        return "";
    }
    
    /**
     * 发送JSON响应
     */
    private static void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        
        byte[] responseBytes = json.getBytes("UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
    
    /**
     * 发送JSON响应（Map转JSON）
     */
    private static void sendJsonResponse(HttpExchange exchange, int statusCode, Map<String, Object> data) throws IOException {
        String json = gson.toJson(data);
        sendJsonResponse(exchange, statusCode, json);
    }
    
    /**
     * 获取服务器地址
     */
    public static String getServerUrl() {
        return "http://localhost:" + PORT;
    }
}