package main.java.com.healthsmart.frontend.controller;

import com.google.gson.Gson;
import com.healthsmart.service.AnalyticsService;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据分析API控制器
 * Analytics API Controller
 */
public class AnalyticsController {
    
    private static final Gson gson = new Gson();
    private static AnalyticsService analyticsService;
    
    public static String handleRequest(String action, String body) {
        if (analyticsService == null) {
            analyticsService = new AnalyticsService();
        }
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            switch (action) {
                case "dashboard":
                    response = handleDashboard(body);
                    break;
                case "planStats":
                    response = handlePlanStats(body);
                    break;
                case "weeklyStats":
                    response = handleWeeklyStats(body);
                    break;
                case "nutritionTrend":
                    response = handleNutritionTrend(body);
                    break;
                case "streakStats":
                    response = handleStreakStats(body);
                    break;
                default:
                    response.put("success", false);
                    response.put("message", "未知的操作类型");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "处理请求时发生错误: " + e.getMessage());
        }
        
        return gson.toJson(response);
    }
    
    private static Map<String, Object> handleDashboard(String body) {
        Map<String, Object> data = gson.fromJson(body, Map.class);
        Map<String, Object> response = new HashMap<>();
        
        try {
            Integer userId = ((Number) data.get("userId")).intValue();
            
            Map<String, Object> dashboard = new HashMap<>();
            
            // 获取今日数据
            var todayTask = analyticsService.getTodayTask(userId);
            if (todayTask != null) {
                dashboard.put("todayCompleted", todayTask.isCompleted());
                dashboard.put("todayTasks", todayTask);
            }
            
            // 获取周统计
            var weeklyStats = analyticsService.getWeeklyStatistics(userId);
            dashboard.put("weeklyStats", weeklyStats);
            
            // 获取连续打卡天数
            var streakStats = analyticsService.getStreakStatistics(userId);
            dashboard.put("streakStats", streakStats);
            
            response.put("success", true);
            response.put("data", dashboard);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    private static Map<String, Object> handlePlanStats(String body) {
        Map<String, Object> data = gson.fromJson(body, Map.class);
        Map<String, Object> response = new HashMap<>();
        
        try {
            Integer planId = ((Number) data.get("planId")).intValue();
            var stats = analyticsService.getPlanCompletionStats(planId);
            
            response.put("success", true);
            response.put("data", stats);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    private static Map<String, Object> handleWeeklyStats(String body) {
        Map<String, Object> data = gson.fromJson(body, Map.class);
        Map<String, Object> response = new HashMap<>();
        
        try {
            Integer userId = ((Number) data.get("userId")).intValue();
            var stats = analyticsService.getWeeklyStatistics(userId);
            
            response.put("success", true);
            response.put("data", stats);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    private static Map<String, Object> handleNutritionTrend(String body) {
        Map<String, Object> data = gson.fromJson(body, Map.class);
        Map<String, Object> response = new HashMap<>();
        
        try {
            Integer planId = ((Number) data.get("planId")).intValue();
            var trend = analyticsService.analyzeNutritionTrend(planId);
            
            response.put("success", true);
            response.put("data", trend);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    private static Map<String, Object> handleStreakStats(String body) {
        Map<String, Object> data = gson.fromJson(body, Map.class);
        Map<String, Object> response = new HashMap<>();
        
        try {
            Integer userId = ((Number) data.get("userId")).intValue();
            var stats = analyticsService.getStreakStatistics(userId);
            
            response.put("success", true);
            response.put("data", stats);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
}