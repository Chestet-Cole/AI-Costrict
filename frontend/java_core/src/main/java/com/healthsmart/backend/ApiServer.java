package com.healthsmart.backend;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.healthsmart.model.*;
import com.healthsmart.service.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * REST API 服务器 - 前端后端对接核心组件
 * REST API Server for Frontend-Backend Integration
 */
public class ApiServer {
    
    private static final int PORT = 8080;
    private static final Gson gson = new Gson();
    private static HttpServer server;
    private static UserService userService;
    private static HealthReportService healthReportService;
    private static PlanGenerationService planService;
    private static AnalyticsService analyticsService;
    
    public static void main(String[] args) throws IOException {
        start();
    }
    
    public static void start() throws IOException {
        // 初始化服务
        userService = new UserService();
        healthReportService = new HealthReportService();
        planService = new PlanGenerationService();
        analyticsService = new AnalyticsService();
        
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
            
            Map<String, Object> response = new HashMap<>();
            
            try {
                switch (action) {
                    case "login":
                        response = handleLogin(body);
                        break;
                    case "register":
                        response = handleRegister(body);
                        break;
                    case "profile":
                        response = handleProfile(body);
                        break;
                    case "update":
                        response = handleUpdate(body);
                        break;
                    default:
                        response.put("success", false);
                        response.put("message", "未知的操作类型");
                }
            } catch (Exception e) {
                response.put("success", false);
                response.put("message", e.getMessage());
            }
            
            sendJsonResponse(exchange, 200, gson.toJson(response));
        }
    }
    
    private static Map<String, Object> handleLogin(String body) {
        Map<String, Object> data = gson.fromJson(body, Map.class);
        Map<String, Object> response = new HashMap<>();
        
        try {
            String username = (String) data.get("username");
            String password = (String) data.get("password");
            
            User user = userService.login(username, password);
            
            response.put("success", true);
            Map<String, Object> userData = new HashMap<>();
            userData.put("userId", user.getUserId());
            userData.put("username", user.getUsername());
            userData.put("nickname", user.getNickname());
            userData.put("email", user.getEmail());
            response.put("data", userData);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    private static Map<String, Object> handleRegister(String body) {
        Map<String, Object> data = gson.fromJson(body, Map.class);
        Map<String, Object> response = new HashMap<>();
        
        try {
            String username = (String) data.get("username");
            String password = (String) data.get("password");
            String email = (String) data.get("email");
            String nickname = (String) data.get("nickname");
            
            User user = userService.register(username, password, email, nickname);
            
            response.put("success", true);
            Map<String, Object> userData = new HashMap<>();
            userData.put("userId", user.getUserId());
            userData.put("username", user.getUsername());
            userData.put("nickname", user.getNickname());
            userData.put("email", user.getEmail());
            response.put("data", userData);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    private static Map<String, Object> handleProfile(String body) {
        Map<String, Object> data = gson.fromJson(body, Map.class);
        Map<String, Object> response = new HashMap<>();
        
        try {
            Integer userId = ((Number) data.get("userId")).intValue();
            User user = userService.getUserById(userId);
            
            response.put("success", true);
            Map<String, Object> userData = new HashMap<>();
            userData.put("userId", user.getUserId());
            userData.put("username", user.getUsername());
            userData.put("nickname", user.getNickname());
            userData.put("email", user.getEmail());
            userData.put("phone", user.getPhone());
            userData.put("gender", user.getGender());
            response.put("data", userData);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    private static Map<String, Object> handleUpdate(String body) {
        Map<String, Object> data = gson.fromJson(body, Map.class);
        Map<String, Object> response = new HashMap<>();
        
        try {
            Integer userId = ((Number) data.get("userId")).intValue();
            User user = userService.getUserById(userId);
            
            if (data.containsKey("nickname")) {
                user.setNickname((String) data.get("nickname"));
            }
            if (data.containsKey("email")) {
                user.setEmail((String) data.get("email"));
            }
            
            boolean success = userService.updateUser(user);
            response.put("success", success);
            response.put("message", success ? "更新成功" : "更新失败");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 健康报告API处理器
     */
    static class HealthReportHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String body = readRequestBody(exchange);
            String action = getAction(exchange.getRequestURI().getQuery());
            
            Map<String, Object> response = new HashMap<>();
            
            try {
                switch (action) {
                    case "submit":
                        response = handleSubmit(body);
                        break;
                    case "getLatest":
                        response = handleGetLatest(body);
                        break;
                    case "list":
                        response = handleList(body);
                        break;
                    default:
                        response.put("success", false);
                        response.put("message", "未知的操作类型");
                }
            } catch (Exception e) {
                response.put("success", false);
                response.put("message", e.getMessage());
            }
            
            sendJsonResponse(exchange, 200, gson.toJson(response));
        }
    }
    
    private static Map<String, Object> handleSubmit(String body) {
        Map<String, Object> data = gson.fromJson(body, Map.class);
        Map<String, Object> response = new HashMap<>();
        
        try {
            HealthReport report = parseHealthReport(data);
            report = healthReportService.submitReport(report);
            
            response.put("success", true);
            response.put("message", "健康报告提交成功");
            
            Map<String, Object> reportData = new HashMap<>();
            reportData.put("reportId", report.getReportId());
            reportData.put("reportMonth", report.getReportMonth());
            reportData.put("bmi", report.getBmi());
            response.put("data", reportData);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    private static Map<String, Object> handleGetLatest(String body) {
        Map<String, Object> data = gson.fromJson(body, Map.class);
        Map<String, Object> response = new HashMap<>();
        
        try {
            Integer userId = ((Number) data.get("userId")).intValue();
            HealthReport report = healthReportService.getLatestReport(userId);
            
            response.put("success", true);
            response.put("data", healthReportToMap(report));
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    private static Map<String, Object> handleList(String body) {
        Map<String, Object> data = gson.fromJson(body, Map.class);
        Map<String, Object> response = new HashMap<>();
        
        try {
            Integer userId = ((Number) data.get("userId")).intValue();
            List<HealthReport> reports = healthReportService.getReportsByUserId(userId);
            
            response.put("success", true);
            List<Map<String, Object>> reportList = new ArrayList<>();
            for (HealthReport report : reports) {
                reportList.add(healthReportToMap(report));
            }
            response.put("data", reportList);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    private static HealthReport parseHealthReport(Map<String, Object> data) {
        HealthReport report = new HealthReport();
        report.setUserId(((Number) data.get("userId")).intValue());
        report.setReportMonth((String) data.get("reportMonth"));
        
        if (data.containsKey("height")) {
            report.setHeight(((Number) data.get("height")).doubleValue());
        }
        if (data.containsKey("weight")) {
            report.setWeight(((Number) data.get("weight")).doubleValue());
        }
        if (data.containsKey("bodyFatRate")) {
            report.setBodyFatRate(((Number) data.get("bodyFatRate")).doubleValue());
        }
        if (data.containsKey("muscleMass")) {
            report.setMuscleMass(((Number) data.get("muscleMass")).doubleValue());
        }
        if (data.containsKey("systolicPressure")) {
            report.setSystolicPressure(((Number) data.get("systolicPressure")).intValue());
        }
        if (data.containsKey("diastolicPressure")) {
            report.setDiastolicPressure(((Number) data.get("diastolicPressure")).intValue());
        }
        if (data.containsKey("sleepHoursAvg")) {
            report.setSleepHoursAvg(((Number) data.get("sleepHoursAvg")).doubleValue());
        }
        if (data.containsKey("sleepQuality")) {
            report.setSleepQuality(((Number) data.get("sleepQuality")).intValue());
        }
        if (data.containsKey("smoking")) {
            report.setSmoking((Boolean) data.get("smoking"));
        }
        if (data.containsKey("drinking")) {
            report.setDrinking((Boolean) data.get("drinking"));
        }
        if (data.containsKey("exerciseFrequency")) {
            report.setExerciseFrequency(((Number) data.get("exerciseFrequency")).intValue());
        }
        if (data.containsKey("dietaryPreferences")) {
            report.setDietaryPreferences((String) data.get("dietaryPreferences"));
        }
        if (data.containsKey("healthGoal")) {
            report.setHealthGoal((String) data.get("healthGoal"));
        }
        if (data.containsKey("targetWeight")) {
            report.setTargetWeight(((Number) data.get("targetWeight")).doubleValue());
        }
        if (data.containsKey("stressLevel")) {
            report.setStressLevel(((Number) data.get("stressLevel")).intValue());
        }
        if (data.containsKey("energyLevel")) {
            report.setEnergyLevel(((Number) data.get("energyLevel")).intValue());
        }
        
        return report;
    }
    
    private static Map<String, Object> healthReportToMap(HealthReport report) {
        Map<String, Object> map = new HashMap<>();
        if (report == null) return map;
        
        map.put("reportId", report.getReportId());
        map.put("userId", report.getUserId());
        map.put("reportMonth", report.getReportMonth());
        map.put("height", report.getHeight());
        map.put("weight", report.getWeight());
        map.put("bmi", report.getBmi());
        map.put("bodyFatRate", report.getBodyFatRate());
        map.put("muscleMass", report.getMuscleMass());
        map.put("sleepHoursAvg", report.getSleepHoursAvg());
        map.put("sleepQuality", report.getSleepQuality());
        map.put("exerciseFrequency", report.getExerciseFrequency());
        map.put("dietaryPreferences", report.getDietaryPreferences());
        map.put("healthGoal", report.getHealthGoal());
        map.put("targetWeight", report.getTargetWeight());
        
        return map;
    }
    
    /**
     * 计划API处理器
     */
    static class PlanHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String body = readRequestBody(exchange);
            String action = getAction(exchange.getRequestURI().getQuery());
            
            Map<String, Object> response = new HashMap<>();
            
            try {
                switch (action) {
                    case "generate":
                        response = handleGenerate(body);
                        break;
                    case "getTodayTask":
                        response = handleGetTodayTask(body);
                        break;
                    case "getTasks":
                        response = handleGetTasks(body);
                        break;
                    case "completeTask":
                        response = handleCompleteTask(body);
                        break;
                    default:
                        response.put("success", false);
                        response.put("message", "未知的操作类型");
                }
            } catch (Exception e) {
                response.put("success", false);
                response.put("message", e.getMessage());
            }
            
            sendJsonResponse(exchange, 200, gson.toJson(response));
        }
    }
    
    private static Map<String, Object> handleGenerate(String body) {
        Map<String, Object> data = gson.fromJson(body, Map.class);
        Map<String, Object> response = new HashMap<>();
        
        try {
            Integer userId = ((Number) data.get("userId")).intValue();
            
            HealthReport report = healthReportService.getLatestReport(userId);
            if (report == null) {
                response.put("success", false);
                response.put("message", "请先提交健康报告");
                return response;
            }
            
            PersonalizedPlan plan = planService.generatePlanFromReport(report);
            
            response.put("success", true);
            response.put("message", "计划生成成功");
            response.put("data", planToMap(plan));
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    private static Map<String, Object> handleGetTodayTask(String body) {
        Map<String, Object> data = gson.fromJson(body, Map.class);
        Map<String, Object> response = new HashMap<>();
        
        try {
            Integer userId = ((Number) data.get("userId")).intValue();
            DailyTask task = planService.getTodayTask(userId);
            
            response.put("success", true);
            if (task != null) {
                response.put("data", dailyTaskToMap(task));
            } else {
                response.put("data", null);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    private static Map<String, Object> handleGetTasks(String body) {
        Map<String, Object> data = gson.fromJson(body, Map.class);
        Map<String, Object> response = new HashMap<>();
        
        try {
            Integer userId = ((Number) data.get("userId")).intValue();
            List<DailyTask> tasks = planService.getTasksByUserId(userId);
            
            response.put("success", true);
            List<Map<String, Object>> taskList = new ArrayList<>();
            for (DailyTask task : tasks) {
                taskList.add(dailyTaskToMap(task));
            }
            response.put("data", taskList);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    private static Map<String, Object> handleCompleteTask(String body) {
        Map<String, Object> data = gson.fromJson(body, Map.class);
        Map<String, Object> response = new HashMap<>();
        
        try {
            Integer taskId = ((Number) data.get("taskId")).intValue();
            boolean completed = data.containsKey("completed") ? (Boolean) data.get("completed") : true;
            
            planService.markTaskCompleted(taskId, completed);
            
            response.put("success", true);
            response.put("message", "操作成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    private static Map<String, Object> planToMap(PersonalizedPlan plan) {
        Map<String, Object> map = new HashMap<>();
        if (plan == null) return map;
        
        map.put("planId", plan.getPlanId());
        map.put("userId", plan.getUserId());
        map.put("planMonth", plan.getPlanMonth());
        map.put("calorieTarget", plan.getCalorieTarget());
        map.put("proteinTarget", plan.getProteinTarget());
        map.put("exerciseSessionsPerWeek", plan.getExerciseSessionsPerWeek());
        
        return map;
    }
    
    private static Map<String, Object> dailyTaskToMap(DailyTask task) {
        Map<String, Object> map = new HashMap<>();
        if (task == null) return map;
        
        map.put("taskId", task.getTaskId());
        map.put("planId", task.getPlanId());
        map.put("taskDate", task.getTaskDate());
        map.put("dayNumber", task.getDayNumber());
        
        // 饮食
        map.put("mealBreakfast", task.getMealBreakfast());
        map.put("mealLunch", task.getMealLunch());
        map.put("mealDinner", task.getMealDinner());
        map.put("calorieGoal", task.getCalorieGoal());
        
        // 运动
        map.put("exerciseType", task.getExerciseType());
        map.put("exerciseDescription", task.getExerciseDescription());
        map.put("exerciseDuration", task.getExerciseDuration());
        
        // 健康指标
        map.put("waterIntakeGoal", task.getWaterIntakeGoal());
        map.put("sleepGoal", task.getSleepGoal());
        
        // 完成状态
        map.put("completed", task.isCompleted());
        
        return map;
    }
    
    /**
     * 数据分析API处理器
     */
    static class AnalyticsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String body = readRequestBody(exchange);
            String action = getAction(exchange.getRequestURI().getQuery());
            
            Map<String, Object> response = new HashMap<>();
            
            try {
                switch (action) {
                    case "dashboard":
                        response = handleDashboard(body);
                        break;
                    case "weekly":
                        response = handleWeekly(body);
                        break;
                    case "streak":
                        response = handleStreak(body);
                        break;
                    default:
                        response.put("success", false);
                        response.put("message", "未知的操作类型");
                }
            } catch (Exception e) {
                response.put("success", false);
                response.put("message", e.getMessage());
            }
            
            sendJsonResponse(exchange, 200, gson.toJson(response));
        }
    }
    
    private static Map<String, Object> handleDashboard(String body) {
        Map<String, Object> data = gson.fromJson(body, Map.class);
        Map<String, Object> response = new HashMap<>();
        
        try {
            Integer userId = ((Number) data.get("userId")).intValue();
            
            Map<String, Object> dashboard = new HashMap<>();
            
            // 今日任务
            DailyTask todayTask = planService.getTodayTask(userId);
            if (todayTask != null) {
                dashboard.put("todayTask", dailyTaskToMap(todayTask));
            }
            
            // 周统计
            AnalyticsService.WeeklyStatistics weekly = analyticsService.getWeeklyStatistics(userId);
            dashboard.put("weeklyStats", Map.of(
                "totalDays", weekly.getTotalDays(),
                "completedDays", weekly.getCompletedDays(),
                "completionRate", weekly.getOverallCompletionRate()
            ));
            
            // 连续打卡
            AnalyticsService.StreakStatistics streak = analyticsService.getStreakStatistics(userId);
            dashboard.put("streakStats", Map.of(
                "currentStreak", streak.getCurrentStreak(),
                "longestStreak", streak.getLongestStreak()
            ));
            
            response.put("success", true);
            response.put("data", dashboard);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    private static Map<String, Object> handleWeekly(String body) {
        Map<String, Object> data = gson.fromJson(body, Map.class);
        Map<String, Object> response = new HashMap<>();
        
        try {
            Integer userId = ((Number) data.get("userId")).intValue();
            AnalyticsService.WeeklyStatistics weekly = analyticsService.getWeeklyStatistics(userId);
            
            response.put("success", true);
            response.put("data", Map.of(
                "totalDays", weekly.getTotalDays(),
                "completedDays", weekly.getCompletedDays(),
                "completionRate", weekly.getOverallCompletionRate()
            ));
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    private static Map<String, Object> handleStreak(String body) {
        Map<String, Object> data = gson.fromJson(body, Map.class);
        Map<String, Object> response = new HashMap<>();
        
        try {
            Integer userId = ((Number) data.get("userId")).intValue();
            AnalyticsService.StreakStatistics streak = analyticsService.getStreakStatistics(userId);
            
            response.put("success", true);
            response.put("data", Map.of(
                "currentStreak", streak.getCurrentStreak(),
                "longestStreak", streak.getLongestStreak()
            ));
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 工具方法
     */
    private static String readRequestBody(HttpExchange exchange) throws IOException {
        InputStream inputStream = exchange.getRequestBody();
        return new String(inputStream.readAllBytes(), "UTF-8");
    }
    
    private static String getAction(String query) {
        if (query == null || query.isEmpty()) return "";
        String[] params = query.split("&");
        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2 && keyValue[0].equals("action")) {
                return keyValue[1];
            }
        }
        return "";
    }
    
    private static void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        
        byte[] responseBytes = json.getBytes("UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
    
    public static String getServerUrl() {
        return "http://localhost:" + PORT;
    }
}