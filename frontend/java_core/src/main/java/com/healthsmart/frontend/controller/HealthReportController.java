package com.healthsmart.frontend.controller;

import com.google.gson.Gson;
import com.healthsmart.model.HealthReport;
import com.healthsmart.service.HealthReportService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 健康报告API控制器
 * Health Report API Controller
 */
public class HealthReportController {
    
    private static final Gson gson = new Gson();
    private static HealthReportService healthReportService;
    
    public static String handleRequest(String action, String body) {
        if (healthReportService == null) {
            healthReportService = new HealthReportService();
        }
        
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
                case "getById":
                    response = handleGetById(body);
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
            reportData.put("submittedAt", report.getSubmittedAt().toString());
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
            HealthReport report = healthReportService.getLatestReportByUserId(userId);
            
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
    
    private static Map<String, Object> handleGetById(String body) {
        Map<String, Object> data = gson.fromJson(body, Map.class);
        Map<String, Object> response = new HashMap<>();
        
        try {
            Integer reportId = ((Number) data.get("reportId")).intValue();
            HealthReport report = healthReportService.getReportById(reportId);
            
            response.put("success", true);
            response.put("data", healthReportToMap(report));
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
        if (data.containsKey("foodAllergies")) {
            report.setFoodAllergies((String) data.get("foodAllergies"));
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
        if (data.containsKey("additionalNotes")) {
            report.setAdditionalNotes((String) data.get("additionalNotes"));
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
        map.put("systolicPressure", report.getSystolicPressure());
        map.put("diastolicPressure", report.getDiastolicPressure());
        map.put("sleepHoursAvg", report.getSleepHoursAvg());
        map.put("sleepQuality", report.getSleepQuality());
        map.put("smoking", report.isSmoking());
        map.put("drinking", report.isDrinking());
        map.put("exerciseFrequency", report.getExerciseFrequency());
        map.put("dietaryPreferences", report.getDietaryPreferences());
        map.put("foodAllergies", report.getFoodAllergies());
        map.put("healthGoal", report.getHealthGoal());
        map.put("targetWeight", report.getTargetWeight());
        map.put("stressLevel", report.getStressLevel());
        map.put("energyLevel", report.getEnergyLevel());
        map.put("additionalNotes", report.getAdditionalNotes());
        
        if (report.getSubmittedAt() != null) {
            map.put("submittedAt", report.getSubmittedAt().toString());
        }
        
        return map;
    }
}