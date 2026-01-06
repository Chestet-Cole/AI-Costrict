package com.healthsmart.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.healthsmart.model.HealthReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Python引擎执行器
 * 负责与Python智能引擎进行进程间通信，调用计划生成等核心算法
 */
public class PythonEngineExecutor {
    
    private static final Logger logger = LoggerFactory.getLogger(PythonEngineExecutor.class);
    
    private static PythonEngineExecutor instance;
    private final Gson gson = new Gson();
    
    private static String PYTHON_SCRIPT_PATH;
    private static final String PYTHON_EXECUTABLE = "python";
    
    private PythonEngineExecutor() {
        // 初始化Python脚本路径
        String projectRoot = System.getProperty("user.dir");
        PYTHON_SCRIPT_PATH = projectRoot + File.separator + "backend" + 
                            File.separator + "python_engine" + File.separator + "api_service.py";
        logger.info("Python引擎脚本路径: {}", PYTHON_SCRIPT_PATH);
    }
    
    /**
     * 获取单例实例
     */
    public static synchronized PythonEngineExecutor getInstance() {
        if (instance == null) {
            instance = new PythonEngineExecutor();
        }
        return instance;
    }
    
    /**
     * 执行Python脚本并获取结果
     */
    private String executePython(String[] command) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        
        logger.debug("执行命令: {}", String.join(" ", command));
        
        Process process = processBuilder.start();
        
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            logger.error("Python执行失败，退出码: {}, 输出: {}", exitCode, output.toString());
            throw new RuntimeException("Python引擎执行失败: " + output.toString());
        }
        
        return output.toString().trim();
    }
    
    /**
     * 根据健康报告生成长达30天的月度计划
     */
    public String generateMonthlyPlan(HealthReport report) throws Exception {
        // 构建请求JSON
        JsonObject requestJson = new JsonObject();
        requestJson.addProperty("user_id", report.getUserId());
        requestJson.addProperty("report_month", report.getReportMonth());
        requestJson.addProperty("height", report.getHeight());
        requestJson.addProperty("weight", report.getWeight());
        
        if (report.getBodyFatRate() != null) {
            requestJson.addProperty("body_fat_rate", report.getBodyFatRate());
        }
        
        if (report.getSystolicPressure() != null) {
            requestJson.addProperty("systolic_pressure", report.getSystolicPressure());
        }
        
        if (report.getDiastolicPressure() != null) {
            requestJson.addProperty("diastolic_pressure", report.getDiastolicPressure());
        }
        
        if (report.getSleepHoursAvg() != null) {
            requestJson.addProperty("sleep_hours_avg", report.getSleepHoursAvg());
        }
        
        if (report.getSleepQuality() != null) {
            requestJson.addProperty("sleep_quality", report.getSleepQuality());
        }
        
        requestJson.addProperty("smoking", report.getSmoking());
        requestJson.addProperty("drinking", report.getDrinking());
        
        if (report.getExerciseFrequency() != null) {
            requestJson.addProperty("exercise_frequency", report.getExerciseFrequency());
        }
        
        if (report.getDietaryPreferences() != null) {
            requestJson.addProperty("dietary_preferences", report.getDietaryPreferences());
        }
        
        if (report.getFoodAllergies() != null) {
            requestJson.addProperty("food_allergies", report.getFoodAllergies());
        }
        
        if (report.getHealthGoal() != null) {
            requestJson.addProperty("health_goal", report.getHealthGoal());
        }
        
        if (report.getTargetWeight() != null) {
            requestJson.addProperty("target_weight", report.getTargetWeight());
        }
        
        if (report.getMedicalHistory() != null) {
            requestJson.addProperty("medical_history", report.getMedicalHistory());
        }
        
        if (report.getMedications() != null) {
            requestJson.addProperty("medications", report.getMedications());
        }
        
        if (report.getStressLevel() != null) {
            requestJson.addProperty("stress_level", report.getStressLevel());
        }
        
        if (report.getEnergyLevel() != null) {
            requestJson.addProperty("energy_level", report.getEnergyLevel());
        }
        
        // 执行Python命令
        String jsonInput = gson.toJson(requestJson);
        String[] command = {PYTHON_EXECUTABLE, PYTHON_SCRIPT_PATH, "generate_plan", jsonInput};
        
        String result = executePython(command);
        
        return result;
    }
    
    /**
     * 生成每日推荐
     */
    public String generateDailyRecommendation(Integer userId, String healthGoal, int dayNumber, 
                                              String date) throws Exception {
        JsonObject requestJson = new JsonObject();
        requestJson.addProperty("user_id", userId);
        requestJson.addProperty("health_goal", healthGoal);
        requestJson.addProperty("day_number", dayNumber);
        requestJson.addProperty("date", date);
        
        String jsonInput = gson.toJson(requestJson);
        String[] command = {PYTHON_EXECUTABLE, PYTHON_SCRIPT_PATH, 
                           "daily_recommendation", jsonInput};
        
        String result = executePython(command);
        
        return result;
    }
    
    /**
     * 健康数据分析
     */
    public String analyzeHealth(HealthReport report) throws Exception {
        String jsonInput = gson.toJson(report);
        String[] command = {PYTHON_EXECUTABLE, PYTHON_SCRIPT_PATH, "analyze_health", jsonInput};
        
        String result = executePython(command);
        
        return result;
    }
    
    /**
     * 检查Python引擎是否可用
     */
    public boolean checkEngineHealth() {
        try {
            String[] command = {PYTHON_EXECUTABLE, PYTHON_SCRIPT_PATH, "health"};
            String result = executePython(command);
            
            JsonObject response = gson.fromJson(result, JsonObject.class);
            return response.get("status") != null && response.get("status").getAsString().equals("ok");
        } catch (Exception e) {
            logger.error("Python引擎健康检查失败", e);
            return false;
        }
    }
}