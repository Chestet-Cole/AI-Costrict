package com.healthsmart.service;

import com.healthsmart.dao.DailyTaskDAO;
import com.healthsmart.dao.PersonalizedPlanDAO;
import com.healthsmart.model.DailyTask;
import com.healthsmart.model.PersonalizedPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据分析与可视化服务
 * Analytics and Visualization Service
 */
public class AnalyticsService {
    
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    private PersonalizedPlanDAO planDAO;
    private DailyTaskDAO dailyTaskDAO;
    
    public AnalyticsService() {
        this.planDAO = new PersonalizedPlanDAO();
        this.dailyTaskDAO = new DailyTaskDAO();
    }
    
    /**
     * 获取计划完成度统计
     */
    public PlanCompletionStatistics getPlanCompletionStats(Integer planId) throws SQLException {
        PersonalizedPlan plan = planDAO.findById(planId);
        if (plan == null) {
            throw new SQLException("计划不存在");
        }
        
        List<DailyTask> tasks = dailyTaskDAO.findByPlanId(planId);
        
        int totalTasks = tasks.size();
        int completedTasks = 0;
        int totalPlannedCalories = 0;
        int totalActualCalories = 0;
        int totalPlannedExercise = 0;
        int totalActualExercise = 0;
        
        for (DailyTask task : tasks) {
            totalPlannedCalories += task.getDailyCalorieGoal() != null ? task.getDailyCalorieGoal() : 0;
            totalPlannedExercise += task.getExerciseDuration() != null ? task.getExerciseDuration() : 0;
            
            if (task.getIsCompleted()) {
                completedTasks++;
                totalActualCalories += task.getActualCalorieIntake() != null ? task.getActualCalorieIntake() : 0;
                totalActualExercise += task.getActualExerciseDuration() != null ? task.getActualExerciseDuration() : 0;
            }
        }
        
        double overallCompletionRate = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0;
        
        PlanCompletionStatistics stats = new PlanCompletionStatistics();
        stats.setPlanId(planId);
        stats.setTotalDays(totalTasks);
        stats.setCompletedDays(completedTasks);
        stats.setOverallCompletionRate(overallCompletionRate);
        stats.setTotalPlannedCalories(totalPlannedCalories);
        stats.setTotalActualCalories(totalActualCalories);
        stats.setCalorieDeviation(totalActualCalories - totalPlannedCalories);
        stats.setTotalPlannedExerciseMinutes(totalPlannedExercise);
        stats.setTotalActualExerciseMinutes(totalActualExercise);
        stats.setExerciseDeviation(totalActualExercise - totalPlannedExercise);
        
        return stats;
    }
    
    /**
     * 获取每日完成度趋势数据
     */
    public List<DailyCompletionTrend> getDailyCompletionTrend(Integer planId) throws SQLException {
        List<DailyTask> tasks = dailyTaskDAO.findByPlanId(planId);
        List<DailyCompletionTrend> trends = new ArrayList<>();
        
        for (DailyTask task : tasks) {
            DailyCompletionTrend trend = new DailyCompletionTrend();
            trend.setDate(task.getTaskDate());
            
            int plannedCalorie = task.getDailyCalorieGoal() != null ? task.getDailyCalorieGoal() : 0;
            int actualCalorie = task.getActualCalorieIntake() != null ? task.getActualCalorieIntake() : 0;
            int plannedExercise = task.getExerciseDuration() != null ? task.getExerciseDuration() : 0;
            int actualExercise = task.getActualExerciseDuration() != null ? task.getActualExerciseDuration() : 0;
            
            trend.setPlannedCalories(plannedCalorie);
            trend.setActualCalories(actualCalorie);
            trend.setCalorieCompletionRate(plannedCalorie > 0 ? 
                (double) actualCalorie / plannedCalorie * 100 : 0);
            
            trend.setPlannedExerciseMinutes(plannedExercise);
            trend.setActualExerciseMinutes(actualExercise);
            trend.setExerciseCompletionRate(plannedExercise > 0 ? 
                (double) actualExercise / plannedExercise * 100 : 0);
            
            trend.setCompleted(task.getIsCompleted());
            
            trends.add(trend);
        }
        
        // 按日期排序
        trends.sort((a, b) -> a.getDate().compareTo(b.getDate()));
        
        return trends;
    }
    
    /**
     * 获取周度统计数据（根据计划ID）
     */
    public Map<Integer, WeeklyStatistics> getWeeklyStatistics(Integer planId) throws SQLException {
        List<DailyTask> tasks = dailyTaskDAO.findByPlanId(planId);
        Map<Integer, WeeklyStatistics> weeklyStatsMap = new HashMap<>();
        
        for (DailyTask task : tasks) {
            int weekNumber = getWeekNumberOfPlan(task.getTaskDate());
            
            WeeklyStatistics weekStats = weeklyStatsMap.computeIfAbsent(weekNumber, k -> new WeeklyStatistics());
            weekStats.setWeekNumber(weekNumber);
            weekStats.setTotalDays(weekStats.getTotalDays() + 1);
            
            if (task.getIsCompleted()) {
                weekStats.setCompletedDays(weekStats.getCompletedDays() + 1);
                
                Integer calorieIntake = task.getActualCalorieIntake();
                if (calorieIntake != null) {
                    weekStats.setTotalCaloriesConsumed(
                        weekStats.getTotalCaloriesConsumed() + calorieIntake);
                }
                
                Integer exerciseDuration = task.getActualExerciseDuration();
                if (exerciseDuration != null) {
                    weekStats.setTotalExerciseMinutes(
                        weekStats.getTotalExerciseMinutes() + exerciseDuration);
                }
            }
            
            Integer plannedCalorie = task.getDailyCalorieGoal();
            if (plannedCalorie != null) {
                weekStats.setTotalPlannedCalories(
                    weekStats.getTotalPlannedCalories() + plannedCalorie);
            }
            
            Integer plannedExercise = task.getExerciseDuration();
            if (plannedExercise != null) {
                weekStats.setTotalPlannedExerciseMinutes(
                    weekStats.getTotalPlannedExerciseMinutes() + plannedExercise);
            }
        }
        
        // 计算完成率
        for (WeeklyStatistics stats : weeklyStatsMap.values()) {
            stats.setCompletionRate(stats.getTotalDays() > 0 ? 
                (double) stats.getCompletedDays() / stats.getTotalDays() * 100 : 0);
        }
        
        return weeklyStatsMap;
    }
    
    /**
     * 获取周度统计数据（根据用户ID）
     */
    public Map<Integer, WeeklyStatistics> getWeeklyStatisticsByUserId(Integer userId) throws SQLException {
        // 获取用户最新的活跃计划
        PersonalizedPlanDAO planDAO = new PersonalizedPlanDAO();
        List<PersonalizedPlan> plans = planDAO.findByUserId(userId);
        PersonalizedPlan activePlan = null;
        for (PersonalizedPlan plan : plans) {
            if ("active".equals(plan.getPlanStatus())) {
                activePlan = plan;
                break;
            }
        }
        
        if (activePlan == null) {
            return new HashMap<>();
        }
        
        return getWeeklyStatistics(activePlan.getPlanId());
    }
    
    /**
     * 获取营养摄入趋势（根据用户ID）
     */
    public NutritionTrendAnalysis getNutritionTrendByUserId(Integer userId) throws SQLException {
        // 获取用户最新的活跃计划
        PersonalizedPlanDAO planDAO = new PersonalizedPlanDAO();
        List<PersonalizedPlan> plans = planDAO.findByUserId(userId);
        PersonalizedPlan activePlan = null;
        for (PersonalizedPlan plan : plans) {
            if ("active".equals(plan.getPlanStatus())) {
                activePlan = plan;
                break;
            }
        }
        
        if (activePlan == null) {
            return new NutritionTrendAnalysis();
        }
        
        return getNutritionTrend(activePlan.getPlanId());
    }
    
    /**
     * 获取累计执行天数和连续打卡天数
     */
    public StreakStatistics getStreakStatistics(Integer userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM daily_tasks WHERE user_id = ? AND is_completed = 1";
        
        int totalCompletedDays = 0;
        try (Connection conn = com.healthsmart.util.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                totalCompletedDays = rs.getInt(1);
            }
        }
        
        // 计算连续打卡天数
        String sqlRecent = "SELECT task_date FROM daily_tasks WHERE user_id = ? " +
                          "AND is_completed = 1 ORDER BY task_date DESC LIMIT 30";
        
        int currentStreak = 0;
        int longestStreak = 0;
        List<String> recentDates = new ArrayList<>();
        
        try (Connection conn = com.healthsmart.util.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlRecent)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                recentDates.add(rs.getString("task_date"));
            }
        }
        
        // 计算连续打卡
        LocalDate today = LocalDate.now();
        for (int i = 0; i < recentDates.size(); i++) {
            LocalDate taskDate = LocalDate.parse(recentDates.get(i), DATE_FORMATTER);
            long daysDiff = ChronoUnit.DAYS.between(taskDate, today);
            
            if (daysDiff == i) {
                currentStreak++;
            } else {
                break;
            }
        }
        
        // 计算最长连续打卡
        int tempStreak = 1;
        if (recentDates.size() > 0) {
            longestStreak = 1;
        }
        
        for (int i = 0; i < recentDates.size() - 1; i++) {
            LocalDate currentDate = LocalDate.parse(recentDates.get(i), DATE_FORMATTER);
            LocalDate previousDate = LocalDate.parse(recentDates.get(i + 1), DATE_FORMATTER);
            long daysDiff = ChronoUnit.DAYS.between(previousDate, currentDate);
            
            if (daysDiff == 1) {
                tempStreak++;
                longestStreak = Math.max(longestStreak, tempStreak);
            } else {
                tempStreak = 1;
            }
        }
        
        StreakStatistics stats = new StreakStatistics();
        stats.setTotalCompletedDays(totalCompletedDays);
        stats.setCurrentStreak(currentStreak);
        stats.setLongestStreak(longestStreak);
        
        return stats;
    }
    
    /**
     * 获取营养摄入趋势
     */
    public NutritionTrendAnalysis getNutritionTrend(Integer planId) throws SQLException {
        List<DailyTask> tasks = dailyTaskDAO.findByPlanId(planId);
        NutritionTrendAnalysis analysis = new NutritionTrendAnalysis();
        
        int totalDays = tasks.size();
        int daysWithCalorieData = 0;
        int totalPlannedCalories = 0;
        int totalActualCalories = 0;
        int daysOnTarget = 0;
        int daysUnderTarget = 0;
        int daysOverTarget = 0;
        
        int averageDeviationFromTarget = 0;
        
        for (DailyTask task : tasks) {
            if (task.getIsCompleted() && task.getActualCalorieIntake() != null && 
                task.getDailyCalorieGoal() != null && task.getDailyCalorieGoal() > 0) {
                
                daysWithCalorieData++;
                totalPlannedCalories += task.getDailyCalorieGoal();
                int actual = task.getActualCalorieIntake();
                totalActualCalories += actual;
                
                // 评分范围 +/- 10%
                double deviation = Math.abs(actual - task.getDailyCalorieGoal());
                double tolerance = task.getDailyCalorieGoal() * 0.1;
                
                if (deviation <= tolerance) {
                    daysOnTarget++;
                } else if (actual < task.getDailyCalorieGoal()) {
                    daysUnderTarget++;
                } else {
                    daysOverTarget++;
                }
                
                averageDeviationFromTarget += deviation;
            }
        }
        
        averageDeviationFromTarget = daysWithCalorieData > 0 ? 
            averageDeviationFromTarget / daysWithCalorieData : 0;
        
        analysis.setTotalDays(totalDays);
        analysis.setDaysWithCalorieData(daysWithCalorieData);
        analysis.setAveragePlannedCalories(daysWithCalorieData > 0 ? 
            totalPlannedCalories / daysWithCalorieData : 0);
        analysis.setAverageActualCalories(daysWithCalorieData > 0 ? 
            totalActualCalories / daysWithCalorieData : 0);
        analysis.setDaysOnTarget(daysOnTarget);
        analysis.setDaysUnderTarget(daysUnderTarget);
        analysis.setDaysOverTarget(daysOverTarget);
        analysis.setAverageDeviationFromTarget(averageDeviationFromTarget);
        
        dailyTaskDAO.countCompletedTasks(planId);
        
        return analysis;
    }
    
    /**
     * 获取用户的今日任务
     */
    public DailyTask getTodayTask(Integer userId) throws SQLException {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return dailyTaskDAO.findByUserIdAndDate(userId, today);
    }
    
    /**
     * 获取计划中的周数（基于任务日期）
     */
    private int getWeekNumberOfPlan(String dateStr) {
        // 简化的周数计算：假设从每月1号开始
        LocalDate date = LocalDate.parse(dateStr, DATE_FORMATTER);
        int dayOfMonth = date.getDayOfMonth();
        return (dayOfMonth - 1) / 7 + 1;
    }
    
    // ========== 统计数据模型类 ==========
    
    public static class PlanCompletionStatistics {
        private Integer planId;
        private Integer totalDays;
        private Integer completedDays;
        private Double overallCompletionRate;
        private Integer totalPlannedCalories;
        private Integer totalActualCalories;
        private Integer calorieDeviation;
        private Integer totalPlannedExerciseMinutes;
        private Integer totalActualExerciseMinutes;
        private Integer exerciseDeviation;
        
        // Getters and Setters
        public Integer getPlanId() { return planId; }
        public void setPlanId(Integer planId) { this.planId = planId; }
        public Integer getTotalDays() { return totalDays; }
        public void setTotalDays(Integer totalDays) { this.totalDays = totalDays; }
        public Integer getCompletedDays() { return completedDays; }
        public void setCompletedDays(Integer completedDays) { this.completedDays = completedDays; }
        public Double getOverallCompletionRate() { return overallCompletionRate; }
        public void setOverallCompletionRate(Double overallCompletionRate) { this.overallCompletionRate = overallCompletionRate; }
        public Integer getTotalPlannedCalories() { return totalPlannedCalories; }
        public void setTotalPlannedCalories(Integer totalPlannedCalories) { this.totalPlannedCalories = totalPlannedCalories; }
        public Integer getTotalActualCalories() { return totalActualCalories; }
        public void setTotalActualCalories(Integer totalActualCalories) { this.totalActualCalories = totalActualCalories; }
        public Integer getCalorieDeviation() { return calorieDeviation; }
        public void setCalorieDeviation(Integer calorieDeviation) { this.calorieDeviation = calorieDeviation; }
        public Integer getTotalPlannedExerciseMinutes() { return totalPlannedExerciseMinutes; }
        public void setTotalPlannedExerciseMinutes(Integer totalPlannedExerciseMinutes) { this.totalPlannedExerciseMinutes = totalPlannedExerciseMinutes; }
        public Integer getTotalActualExerciseMinutes() { return totalActualExerciseMinutes; }
        public void setTotalActualExerciseMinutes(Integer totalActualExerciseMinutes) { this.totalActualExerciseMinutes = totalActualExerciseMinutes; }
        public Integer getExerciseDeviation() { return exerciseDeviation; }
        public void setExerciseDeviation(Integer exerciseDeviation) { this.exerciseDeviation = exerciseDeviation; }
    }
    
    public static class DailyCompletionTrend {
        private String date;
        private Integer plannedCalories;
        private Integer actualCalories;
        private Double calorieCompletionRate;
        private Integer plannedExerciseMinutes;
        private Integer actualExerciseMinutes;
        private Double exerciseCompletionRate;
        private Boolean completed;
        
        // Getters and Setters
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public Integer getPlannedCalories() { return plannedCalories; }
        public void setPlannedCalories(Integer plannedCalories) { this.plannedCalories = plannedCalories; }
        public Integer getActualCalories() { return actualCalories; }
        public void setActualCalories(Integer actualCalories) { this.actualCalories = actualCalories; }
        public Double getCalorieCompletionRate() { return calorieCompletionRate; }
        public void setCalorieCompletionRate(Double calorieCompletionRate) { this.calorieCompletionRate = calorieCompletionRate; }
        public Integer getPlannedExerciseMinutes() { return plannedExerciseMinutes; }
        public void setPlannedExerciseMinutes(Integer plannedExerciseMinutes) { this.plannedExerciseMinutes = plannedExerciseMinutes; }
        public Integer getActualExerciseMinutes() { return actualExerciseMinutes; }
        public void setActualExerciseMinutes(Integer actualExerciseMinutes) { this.actualExerciseMinutes = actualExerciseMinutes; }
        public Double getExerciseCompletionRate() { return exerciseCompletionRate; }
        public void setExerciseCompletionRate(Double exerciseCompletionRate) { this.exerciseCompletionRate = exerciseCompletionRate; }
        public Boolean getCompleted() { return completed; }
        public void setCompleted(Boolean completed) { this.completed = completed; }
    }
    
    public static class WeeklyStatistics {
        private Integer weekNumber;
        private Integer totalDays;
        private Integer completedDays;
        private Double completionRate;
        private Integer totalPlannedCalories;
        private Integer totalCaloriesConsumed;
        private Integer totalPlannedExerciseMinutes;
        private Integer totalExerciseMinutes;
        
        // Getters and Setters
        public Integer getWeekNumber() { return weekNumber; }
        public void setWeekNumber(Integer weekNumber) { this.weekNumber = weekNumber; }
        public Integer getTotalDays() { return totalDays; }
        public void setTotalDays(Integer totalDays) { this.totalDays = totalDays; }
        public Integer getCompletedDays() { return completedDays; }
        public void setCompletedDays(Integer completedDays) { this.completedDays = completedDays; }
        public Double getCompletionRate() { return completionRate; }
        public void setCompletionRate(Double completionRate) { this.completionRate = completionRate; }
        public Integer getTotalPlannedCalories() { return totalPlannedCalories; }
        public void setTotalPlannedCalories(Integer totalPlannedCalories) { this.totalPlannedCalories = totalPlannedCalories; }
        public Integer getTotalCaloriesConsumed() { return totalCaloriesConsumed; }
        public void setTotalCaloriesConsumed(Integer totalCaloriesConsumed) { this.totalCaloriesConsumed = totalCaloriesConsumed; }
        public Integer getTotalPlannedExerciseMinutes() { return totalPlannedExerciseMinutes; }
        public void setTotalPlannedExerciseMinutes(Integer totalPlannedExerciseMinutes) { this.totalPlannedExerciseMinutes = totalPlannedExerciseMinutes; }
        public Integer getTotalExerciseMinutes() { return totalExerciseMinutes; }
        public void setTotalExerciseMinutes(Integer totalExerciseMinutes) { this.totalExerciseMinutes = totalExerciseMinutes; }
    }
    
    public static class StreakStatistics {
        private Integer totalCompletedDays;
        private Integer currentStreak;
        private Integer longestStreak;
        
        // Getters and Setters
        public Integer getTotalCompletedDays() { return totalCompletedDays; }
        public void setTotalCompletedDays(Integer totalCompletedDays) { this.totalCompletedDays = totalCompletedDays; }
        public Integer getCurrentStreak() { return currentStreak; }
        public void setCurrentStreak(Integer currentStreak) { this.currentStreak = currentStreak; }
        public Integer getLongestStreak() { return longestStreak; }
        public void setLongestStreak(Integer longestStreak) { this.longestStreak = longestStreak; }
    }
    
    public static class NutritionTrendAnalysis {
        private Integer totalDays;
        private Integer daysWithCalorieData;
        private Integer averagePlannedCalories;
        private Integer averageActualCalories;
        private Integer daysOnTarget;
        private Integer daysUnderTarget;
        private Integer daysOverTarget;
        private Integer averageDeviationFromTarget;
        
        // Getters and Setters
        public Integer getTotalDays() { return totalDays; }
        public void setTotalDays(Integer totalDays) { this.totalDays = totalDays; }
        public Integer getDaysWithCalorieData() { return daysWithCalorieData; }
        public void setDaysWithCalorieData(Integer daysWithCalorieData) { this.daysWithCalorieData = daysWithCalorieData; }
        public Integer getAveragePlannedCalories() { return averagePlannedCalories; }
        public void setAveragePlannedCalories(Integer averagePlannedCalories) { this.averagePlannedCalories = averagePlannedCalories; }
        public Integer getAverageActualCalories() { return averageActualCalories; }
        public void setAverageActualCalories(Integer averageActualCalories) { this.averageActualCalories = averageActualCalories; }
        public Integer getDaysOnTarget() { return daysOnTarget; }
        public void setDaysOnTarget(Integer daysOnTarget) { this.daysOnTarget = daysOnTarget; }
        public Integer getDaysUnderTarget() { return daysUnderTarget; }
        public void setDaysUnderTarget(Integer daysUnderTarget) { this.daysUnderTarget = daysUnderTarget; }
        public Integer getDaysOverTarget() { return daysOverTarget; }
        public void setDaysOverTarget(Integer daysOverTarget) { this.daysOverTarget = daysOverTarget; }
        public Integer getAverageDeviationFromTarget() { return averageDeviationFromTarget; }
        public void setAverageDeviationFromTarget(Integer averageDeviationFromTarget) { this.averageDeviationFromTarget = averageDeviationFromTarget; }
    }
}