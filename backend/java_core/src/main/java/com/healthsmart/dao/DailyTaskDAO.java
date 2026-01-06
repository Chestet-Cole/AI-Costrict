package com.healthsmart.dao;

import com.healthsmart.model.DailyTask;
import com.healthsmart.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 每日任务数据访问对象
 * Daily Task Data Access Object
 */
public class DailyTaskDAO {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 插入每日任务
     */
    public int insert(DailyTask task) throws SQLException {
        String sql = "INSERT INTO daily_tasks (plan_id, user_id, task_date, meal_breakfast, " +
                     "meal_lunch, meal_dinner, meal_snacks, daily_calorie_goal, " +
                     "exercise_type, exercise_description, exercise_duration, exercise_intensity, 注意事项, " +
                     "is_completed, completion_rate) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, task.getPlanId());
            pstmt.setInt(2, task.getUserId());
            pstmt.setString(3, task.getTaskDate());
            pstmt.setString(4, task.getMealBreakfast());
            pstmt.setString(5, task.getMealLunch());
            pstmt.setString(6, task.getMealDinner());
            pstmt.setString(7, task.getMealSnacks());
            pstmt.setInt(8, task.getDailyCalorieGoal() != null ? task.getDailyCalorieGoal() : 0);
            pstmt.setString(9, task.getExerciseType());
            pstmt.setString(10, task.getExerciseDescription());
            pstmt.setInt(11, task.getExerciseDuration() != null ? task.getExerciseDuration() : 0);
            pstmt.setString(12, task.getExerciseIntensity());
            pstmt.setString(13, task.get注意事项());
            pstmt.setInt(14, task.getIsCompleted() ? 1 : 0);
            pstmt.setDouble(15, task.getCompletionRate() != null ? task.getCompletionRate() : 0.0);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
            return -1;
        }
    }
    
    /**
     * 批量插入每日任务
     */
    public void batchInsert(List<DailyTask> tasks) throws SQLException {
        String sql = "INSERT INTO daily_tasks (plan_id, user_id, task_date, meal_breakfast, " +
                     "meal_lunch, meal_dinner, meal_snacks, daily_calorie_goal, " +
                     "exercise_type, exercise_description, exercise_duration, exercise_intensity, 注意事项, " +
                     "is_completed, completion_rate) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            for (DailyTask task : tasks) {
                pstmt.setInt(1, task.getPlanId());
                pstmt.setInt(2, task.getUserId());
                pstmt.setString(3, task.getTaskDate());
                pstmt.setString(4, task.getMealBreakfast());
                pstmt.setString(5, task.getMealLunch());
                pstmt.setString(6, task.getMealDinner());
                pstmt.setString(7, task.getMealSnacks());
                pstmt.setInt(8, task.getDailyCalorieGoal() != null ? task.getDailyCalorieGoal() : 0);
                pstmt.setString(9, task.getExerciseType());
                pstmt.setString(10, task.getExerciseDescription());
                pstmt.setInt(11, task.getExerciseDuration() != null ? task.getExerciseDuration() : 0);
                pstmt.setString(12, task.getExerciseIntensity());
                pstmt.setString(13, task.get注意事项());
                pstmt.setInt(14, task.getIsCompleted() ? 1 : 0);
                pstmt.setDouble(15, task.getCompletionRate() != null ? task.getCompletionRate() : 0.0);
                
                pstmt.addBatch();
            }
            
            pstmt.executeBatch();
        }
    }
    
    /**
     * 根据ID查找任务
     */
    public DailyTask findById(Integer taskId) throws SQLException {
        String sql = "SELECT * FROM daily_tasks WHERE task_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, taskId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToTask(rs);
            }
        }
        return null;
    }
    
    /**
     * 根据计划ID查找所有任务
     */
    public List<DailyTask> findByPlanId(Integer planId) throws SQLException {
        String sql = "SELECT * FROM daily_tasks WHERE plan_id = ? ORDER BY task_date";
        List<DailyTask> tasks = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, planId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                tasks.add(mapResultSetToTask(rs));
            }
        }
        return tasks;
    }
    
    /**
     * 根据用户ID和日期查找任务
     */
    public DailyTask findByUserIdAndDate(Integer userId, String date) throws SQLException {
        String sql = "SELECT * FROM daily_tasks WHERE user_id = ? AND task_date = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setString(2, date);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToTask(rs);
            }
        }
        return null;
    }
    
    /**
     * 根据用户ID查找指定期日期范围内的任务
     */
    public List<DailyTask> findByUserIdAndDateRange(Integer userId, String startDate, String endDate) throws SQLException {
        String sql = "SELECT * FROM daily_tasks WHERE user_id = ? AND task_date BETWEEN ? AND ? ORDER BY task_date";
        List<DailyTask> tasks = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setString(2, startDate);
            pstmt.setString(3, endDate);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                tasks.add(mapResultSetToTask(rs));
            }
        }
        return tasks;
    }
    
    /**
     * 更新任务完成状态
     */
    public boolean updateCompletion(Integer taskId, boolean isCompleted, Integer actualCalorie, 
                                    Integer actualExerciseDuration, Double completionRate) throws SQLException {
        String sql = "UPDATE daily_tasks SET is_completed = ?, actual_calorie_intake = ?, " +
                     "actual_exercise_duration = ?, completion_rate = ?, completed_at = ?, updated_at = ? " +
                     "WHERE task_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, isCompleted ? 1 : 0);
            pstmt.setInt(2, actualCalorie != null ? actualCalorie : 0);
            pstmt.setInt(3, actualExerciseDuration != null ? actualExerciseDuration : 0);
            pstmt.setDouble(4, completionRate != null ? completionRate : 0.0);
            pstmt.setString(5, isCompleted ? LocalDateTime.now().format(DATE_FORMATTER) : null);
            pstmt.setString(6, LocalDateTime.now().format(DATE_FORMATTER));
            pstmt.setInt(7, taskId);
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * 删除任务
     */
    public boolean delete(Integer taskId) throws SQLException {
        String sql = "DELETE FROM daily_tasks WHERE task_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, taskId);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * 根据计划ID删除所有任务
     */
    public boolean deleteByPlanId(Integer planId) throws SQLException {
        String sql = "DELETE FROM daily_tasks WHERE plan_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, planId);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * 统计计划已完成的任务数
     */
    public int countCompletedTasks(Integer planId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM daily_tasks WHERE plan_id = ? AND is_completed = 1";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, planId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    /**
     * 映射ResultSet到DailyTask对象
     */
    private DailyTask mapResultSetToTask(ResultSet rs) throws SQLException {
        DailyTask task = new DailyTask();
        task.setTaskId(rs.getInt("task_id"));
        task.setPlanId(rs.getInt("plan_id"));
        task.setUserId(rs.getInt("user_id"));
        task.setTaskDate(rs.getString("task_date"));
        task.setMealBreakfast(rs.getString("meal_breakfast"));
        task.setMealLunch(rs.getString("meal_lunch"));
        task.setMealDinner(rs.getString("meal_dinner"));
        task.setMealSnacks(rs.getString("meal_snacks"));
        
        int calorieGoal = rs.getInt("daily_calorie_goal");
        if (calorieGoal > 0) task.setDailyCalorieGoal(calorieGoal);
        
        task.setExerciseType(rs.getString("exercise_type"));
        task.setExerciseDescription(rs.getString("exercise_description"));
        
        int exerciseDuration = rs.getInt("exercise_duration");
        if (exerciseDuration > 0) task.setExerciseDuration(exerciseDuration);
        
        task.setExerciseIntensity(rs.getString("exercise_intensity"));
        task.set注意事项(rs.getString("注意事项"));
        
        task.setIsCompleted(rs.getInt("is_completed") == 1);
        task.setCompletionRate(rs.getDouble("completion_rate"));
        
        int actualCalorie = rs.getInt("actual_calorie_intake");
        if (actualCalorie > 0) task.setActualCalorieIntake(actualCalorie);
        
        int actualExerciseDuration = rs.getInt("actual_exercise_duration");
        if (actualExerciseDuration > 0) task.setActualExerciseDuration(actualExerciseDuration);
        
        String completedAt = rs.getString("completed_at");
        if (completedAt != null) {
            task.setCompletedAt(LocalDateTime.parse(completedAt, DATE_FORMATTER));
        }
        
        String createdAt = rs.getString("created_at");
        if (createdAt != null) {
            task.setCreatedAt(LocalDateTime.parse(createdAt, DATE_FORMATTER));
        }
        
        String updatedAt = rs.getString("updated_at");
        if (updatedAt != null) {
            task.setUpdatedAt(LocalDateTime.parse(updatedAt, DATE_FORMATTER));
        }
        
        return task;
    }
}