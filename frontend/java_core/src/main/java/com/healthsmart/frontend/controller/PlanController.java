package com.healthsmart.frontend.controller;

import com.google.gson.Gson;
import com.healthsmart.model.DailyTask;
import com.healthsmart.model.HealthReport;
import com.healthsmart.model.PersonalizedPlan;
import com.healthsmart.service.HealthReportService;
import com.healthsmart.service.PlanGenerationService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 计划API控制器
 * Plan API Controller
 */
public class PlanController {
    
    private static final Gson gson = new Gson();
    private static PlanGenerationService planService;
    private static HealthReportService reportService;
    
    public static String handleRequest(String action, String body) {
        if (planService == null) {
            planService = new PlanGenerationService();
        }
        if (reportService == null) {
            reportService = new HealthReportService();
        }
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            switch (action) {
                case "generate":
                    response = handleGenerate(body);
                    break;
                case "getPlan":
                    response = handleGetPlan(body);
                    break;
                case "getDailyTasks":
                    response = handleGetDailyTasks(body);
                    break;
                case "getTaskByDate":
                    response = handleGetTaskByDate(body);
                    break;
                case "completeTask":
                    response = handleCompleteTask(body);
                    break;
                case "getTodayPlan":
                    response = handleGetTodayPlan(body);
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
    
    private static Map<String, Object> handleGenerate(String body) {
        Map<String, Object> data = gson.fromJson(body, Map.class);
        Map<String, Object> response = new HashMap<>();
        
        try {
            Integer userId = ((Number) data.get("userId")).intValue();
            String reportMonth = (String) data.get("reportMonth");
            
            // 获取最新的健康报告
            HealthReport report = reportService.getLatestReportByUserId(userId);
            if (report == null) {
                response.put("success", false);
                response.put("message", "请先提交健康报告");
                return response;
            }
            
            // 生成个性化计划
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
    
    private static Map<String, Object> handleGetPlan(String body) {
        Map<String, Object> data = gson.fromJson(body, Map.class);
        Map<String, Object> response = new HashMap<>();
        
        try {
            Integer planId = ((Number) data.get("planId")).intValue();
            PersonalizedPlan plan = planService.getPlanById(planId);
            
            response.put("success", true);
            response.put("data", planToMap(plan));
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    private static Map<String, Object> handleGetDailyTasks(String body) {
        Map<String, Object> data = gson.fromJson(body, Map.class);
        Map<String, Object> response = new HashMap<>();
        
        try {
            Integer planId = ((Number) data.get("planId")).intValue();
            List<DailyTask> tasks = planService.getPlanTasks(planId);
            
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
    
    private static Map<String, Object> handleGetTaskByDate(String body) {
        Map<String, Object> data = gson.fromJson(body, Map.class);
        Map<String, Object> response = new HashMap<>();
        
        try {
            Integer planId = ((Number) data.get("planId")).intValue();
            String taskDate = (String) data.get("taskDate");
            
            DailyTask task = planService.getTaskByDate(planId, taskDate);
            
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
    
    private static Map<String, Object> handleCompleteTask(String body) {
        Map<String, Object> data = gson.fromJson(body, Map.class);
        Map<String, Object> response = new HashMap<>();
        
        try {
            Integer taskId = ((Number) data.get("taskId")).intValue();
            boolean completed = data.containsKey("completed") ? (Boolean) data.get("completed") : true;
            
            planService.markTaskCompleted(taskId, completed);
            
            response.put("success", true);
            response.put("message", completed ? "任务已完成" : "任务已取消");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    private static Map<String, Object> handleGetTodayPlan(String body) {
        Map<String, Object> data = gson.fromJson(body, Map.class);
        Map<String, Object> response = new HashMap<>();
        
        try {
            Integer userId = ((Number) data.get("userId")).intValue();
            
            DailyTask todayTask = planService.getTodayTask(userId);
            
            response.put("success", true);
            if (todayTask != null) {
                response.put("data", dailyTaskToMap(todayTask));
            } else {
                response.put("data", null);
                response.put("message", "今日暂无计划");
            }
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
        map.put("carbsTarget", plan.getCarbsTarget());
        map.put("fatTarget", plan.getFatTarget());
        map.put("exerciseSessionsPerWeek", plan.getExerciseSessionsPerWeek());
        map.put("exerciseDurationPerSession", plan.getExerciseDurationPerSession());
        map.put("waterIntakeTarget", plan.getWaterIntakeTarget());
        map.put("sleepTarget", plan.getSleepTarget());
        
        if (plan.getCreatedAt() != null) {
            map.put("createdAt", plan.getCreatedAt().toString());
        }
        
        return map;
    }
    
    private static Map<String, Object> dailyTaskToMap(DailyTask task) {
        Map<String, Object> map = new HashMap<>();
        if (task == null) return map;
        
        map.put("taskId", task.getTaskId());
        map.put("planId", task.getPlanId());
        map.put("userId", task.getUserId());
        map.put("taskDate", task.getTaskDate());
        map.put("dayNumber", 0); // DailyTask中没有此字段
        
        // 饮食计划
        map.put("mealBreakfast", task.getMealBreakfast());
        map.put("mealLunch", task.getMealLunch());
        map.put("mealDinner", task.getMealDinner());
        map.put("mealSnacks", task.getMealSnacks());
        map.put("calorieGoal", task.getDailyCalorieGoal());
        
        // 运动计划
        map.put("exerciseType", task.getExerciseType());
        map.put("exerciseDescription", task.getExerciseDescription());
        map.put("exerciseDuration", task.getExerciseDuration());
        map.put("exerciseIntensity", task.getExerciseIntensity());
        
        // 注意事项
        map.put("注意事项", task.get注意事项());
        
        // 执行状态
        map.put("completed", task.getIsCompleted());
        map.put("completionRate", task.getCompletionRate());
        
        return map;
    }
}