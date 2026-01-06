package com.healthsmart.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.healthsmart.dao.DailyTaskDAO;
import com.healthsmart.dao.PersonalizedPlanDAO;
import com.healthsmart.model.DailyTask;
import com.healthsmart.model.HealthReport;
import com.healthsmart.model.PersonalizedPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 个性化计划生成服务
 * Personalized Plan Generation Service
 */
public class PlanGenerationService {
    
    private static final Logger logger = LoggerFactory.getLogger(PlanGenerationService.class);
    
    private PersonalizedPlanDAO planDAO;
    private DailyTaskDAO dailyTaskDAO;
    private PythonEngineExecutor pythonEngine;
    private Gson gson;
    
    public PlanGenerationService() {
        this.planDAO = new PersonalizedPlanDAO();
        this.dailyTaskDAO = new DailyTaskDAO();
        this.pythonEngine = PythonEngineExecutor.getInstance();
        this.gson = new Gson();
    }
    
    /**
     * 生成长达30天的月度个性化计划
     */
    public PersonalizedPlan generateMonthlyPlan(Integer userId, HealthReport report) throws Exception {
        logger.info("开始生成用户 {} 的月度计划", userId);
        
        // 检查该月份是否已有计划
        if (report.getReportMonth() == null) {
            throw new IllegalArgumentException("健康报告月份不能为空");
        }
        
        PersonalizedPlan existingPlan = planDAO.findByUserIdAndMonth(userId, report.getReportMonth());
        if (existingPlan != null && existingPlan.getPlanStatus().equals("active")) {
            logger.warn("用户 {} 在 {} 已存在活跃计划，将重新生成", userId, report.getReportMonth());
        }
        
        // 调用Python引擎生成计划
        String pythonResult = pythonEngine.generateMonthlyPlan(report);
        logger.debug("Python引擎返回结果: {}", pythonResult);
        
        // 解析Python返回结果
        JsonObject result = gson.fromJson(pythonResult, JsonObject.class);
        
        if (!result.get("success").getAsBoolean()) {
            String errorMsg = result.get("message").getAsString();
            throw new RuntimeException("计划生成失败: " + errorMsg);
        }
        
        JsonObject planData = result.get("plan").getAsJsonObject();
        JsonObject analysisData = result.get("analysis").getAsJsonObject();
        
        // 创建计划对象
        PersonalizedPlan plan = new PersonalizedPlan();
        plan.setUserId(userId);
        plan.setReportId(report.getReportId());
        plan.setPlanMonth(report.getReportMonth());
        plan.setTotalDays(30);
        
        // 设置营养目标
        plan.setCalorieTarget(analysisData.get("target_calories").getAsInt());
        
        JsonObject nutrients = analysisData.get("nutrient_targets").getAsJsonObject();
        plan.setProteinTarget(nutrients.get("protein").getAsDouble());
        plan.setCarbsTarget(nutrients.get("carbs").getAsDouble());
        plan.setFatTarget(nutrients.get("fat").getAsDouble());
        
        // 设置运动频率
        String activityLevel = analysisData.get("activity_level").getAsString();
        int exerciseFreq = getExerciseFrequencyFromActivity(activityLevel);
        plan.setExerciseSessionsPerWeek(exerciseFreq);
        
        // 设置生成方式
        plan.setGenerationMethod("python_engine");
        plan.setPlanStatus("active");
        
        // 保存计划到数据库
        int planId = planDAO.insert(plan);
        if (planId <= 0) {
            throw new SQLException("计划保存失败");
        }
        plan.setPlanId(planId);
        
        // 解每日任务
        JsonArray dailyPlans = planData.get("daily_plans").getAsJsonArray();
        List<DailyTask> tasks = new ArrayList<>();
        
        LocalDate startDate = LocalDate.parse(report.getReportMonth() + "-01", 
                DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        for (int i = 0; i < dailyPlans.size(); i++) {
            JsonObject dayPlan = dailyPlans.get(i).getAsJsonObject();
            
            DailyTask task = new DailyTask();
            task.setPlanId(planId);
            task.setUserId(userId);
            
            // 设置日期（第1天对应报告月份的第1天）
            LocalDate taskDate = startDate.plusDays(i);
            task.setTaskDate(taskDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            
            // 餐食计划
            if (dayPlan.has("meal_breakfast") && !dayPlan.get("meal_breakfast").isJsonNull()) {
                task.setMealBreakfast(dayPlan.get("meal_breakfast").getAsString());
            }
            
            if (dayPlan.has("meal_lunch") && !dayPlan.get("meal_lunch").isJsonNull()) {
                task.setMealLunch(dayPlan.get("meal_lunch").getAsString());
            }
            
            if (dayPlan.has("meal_dinner") && !dayPlan.get("meal_dinner").isJsonNull()) {
                task.setMealDinner(dayPlan.get("meal_dinner").getAsString());
            }
            
            if (dayPlan.has("meal_snacks") && !dayPlan.get("meal_snacks").isJsonNull()) {
                task.setMealSnacks(dayPlan.get("meal_snacks").getAsString());
            }
            
            // 每日卡路里目标
            if (dayPlan.has("daily_calorie_goal")) {
                task.setDailyCalorieGoal(dayPlan.get("daily_calorie_goal").getAsInt());
            }
            
            // 运动计划
            if (dayPlan.has("exercise_type") && !dayPlan.get("exercise_type").isJsonNull()) {
                task.setExerciseType(dayPlan.get("exercise_type").getAsString());
            }
            
            if (dayPlan.has("exercise_description") && !dayPlan.get("exercise_description").isJsonNull()) {
                task.setExerciseDescription(dayPlan.get("exercise_description").getAsString());
            }
            
            if (dayPlan.has("exercise_duration")) {
                task.setExerciseDuration(dayPlan.get("exercise_duration").getAsInt());
            }
            
            if (dayPlan.has("exercise_intensity") && !dayPlan.get("exercise_intensity").isJsonNull()) {
                task.setExerciseIntensity(dayPlan.get("exercise_intensity").getAsString());
            }
            
            // 注意事项和提醒
            if (dayPlan.has("reminders") && !dayPlan.get("reminders").isJsonNull()) {
                task.set注意事项(dayPlan.get("reminders").getAsString());
            }
            
            tasks.add(task);
        }
        
        // 批量保存每日任务
        if (!tasks.isEmpty()) {
            dailyTaskDAO.batchInsert(tasks);
            logger.info("成功保存 {} 个每日任务", tasks.size());
        }
        
        logger.info("成功生成用户 {} 的月度计划 (计划ID: {})", userId, planId);
        return plan;
    }
    
    /**
     * 根据活动水平获取运动频率
     */
    private int getExerciseFrequencyFromActivity(String activityLevel) {
        switch (activityLevel) {
            case "sedentary":
                return 3;
            case "low_active":
                return 3;
            case "active":
                return 4;
            case "very_active":
                return 5;
            default:
                return 3;
        }
    }
    
    /**
     * 根据健康报告生成计划
     */
    public PersonalizedPlan generatePlanFromReport(HealthReport report) throws Exception {
        return generateMonthlyPlan(report.getUserId(), report);
    }
    
    /**
     * 获取用户的活跃计划
     */
    public PersonalizedPlan getActivePlan(Integer userId, String month) throws SQLException {
        PersonalizedPlan plan = planDAO.findByUserIdAndMonth(userId, month);
        if (plan == null || !"active".equals(plan.getPlanStatus())) {
            return null;
        }
        return plan;
    }
    
    /**
     * 获取计划的所有每日任务
     */
    public List<DailyTask> getPlanTasks(Integer planId) throws SQLException {
        return dailyTaskDAO.findByPlanId(planId);
    }
    
    /**
     * 暂停计划
     */
    public boolean pausePlan(Integer planId) throws SQLException {
        return planDAO.updateStatus(planId, "paused");
    }
    
    /**
     * 完成计划
     */
    public boolean completePlan(Integer planId) throws SQLException {
        return planDAO.updateStatus(planId, "completed");
    }
    
    /**
     * 激活计划
     */
    public boolean activatePlan(Integer planId) throws SQLException {
        return planDAO.updateStatus(planId, "active");
    }
    
    /**
     * 根据日期获取任务
     */
    public DailyTask getTaskByDate(Integer planId, String taskDate) throws SQLException {
        List<DailyTask> tasks = dailyTaskDAO.findByPlanId(planId);
        for (DailyTask task : tasks) {
            if (task.getTaskDate().equals(taskDate)) {
                return task;
            }
        }
        return null;
    }
    
    /**
     * 标记任务完成
     */
    public boolean markTaskCompleted(Integer taskId, boolean completed) throws SQLException {
        DailyTask task = dailyTaskDAO.findById(taskId);
        if (task == null) {
            return false;
        }
        // 如果完成，设置完成时间为当前时间
        return dailyTaskDAO.updateCompletion(taskId, completed,
            completed ? task.getDailyCalorieGoal() : null,
            completed ? task.getExerciseDuration() : null,
            completed ? 100.0 : 0.0);
    }
    
    /**
     * 获取用户的今日任务
     */
    public DailyTask getTodayTask(Integer userId) throws SQLException {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return dailyTaskDAO.findByUserIdAndDate(userId, today);
    }
}