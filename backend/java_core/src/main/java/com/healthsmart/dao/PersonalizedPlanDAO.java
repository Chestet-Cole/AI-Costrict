package com.healthsmart.dao;

import com.healthsmart.model.PersonalizedPlan;
import com.healthsmart.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 个性化计划数据访问对象
 * Personalized Plan Data Access Object
 */
public class PersonalizedPlanDAO {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 插入个性化计划
     */
    public int insert(PersonalizedPlan plan) throws SQLException {
        String sql = "INSERT INTO personalized_plans (user_id, report_id, plan_month, total_days, " +
                     "plan_type, calorie_target, protein_target, carbs_target, fat_target, " +
                     "exercise_sessions_per_week, generation_method, plan_status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, plan.getUserId());
            pstmt.setInt(2, plan.getReportId());
            pstmt.setString(3, plan.getPlanMonth());
            pstmt.setInt(4, plan.getTotalDays());
            pstmt.setString(5, plan.getPlanType());
            pstmt.setInt(6, plan.getCalorieTarget() != null ? plan.getCalorieTarget() : 0);
            pstmt.setDouble(7, plan.getProteinTarget() != null ? plan.getProteinTarget() : 0);
            pstmt.setDouble(8, plan.getCarbsTarget() != null ? plan.getCarbsTarget() : 0);
            pstmt.setDouble(9, plan.getFatTarget() != null ? plan.getFatTarget() : 0);
            pstmt.setInt(10, plan.getExerciseSessionsPerWeek() != null ? plan.getExerciseSessionsPerWeek() : 0);
            pstmt.setString(11, plan.getGenerationMethod());
            pstmt.setString(12, plan.getPlanStatus());
            
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
     * 根据ID查找计划
     */
    public PersonalizedPlan findById(Integer planId) throws SQLException {
        String sql = "SELECT * FROM personalized_plans WHERE plan_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, planId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToPlan(rs);
            }
        }
        return null;
    }
    
    /**
     * 根据用户ID查找所有计划
     */
    public List<PersonalizedPlan> findByUserId(Integer userId) throws SQLException {
        String sql = "SELECT * FROM personalized_plans WHERE user_id = ? ORDER BY created_at DESC";
        List<PersonalizedPlan> plans = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                plans.add(mapResultSetToPlan(rs));
            }
        }
        return plans;
    }
    
    /**
     * 根据用户ID和月份查找计划
     */
    public PersonalizedPlan findByUserIdAndMonth(Integer userId, String month) throws SQLException {
        String sql = "SELECT * FROM personalized_plans WHERE user_id = ? AND plan_month = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setString(2, month);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToPlan(rs);
            }
        }
        return null;
    }
    
    /**
     * 根据报告ID查找计划
     */
    public PersonalizedPlan findByReportId(Integer reportId) throws SQLException {
        String sql = "SELECT * FROM personalized_plans WHERE report_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, reportId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToPlan(rs);
            }
        }
        return null;
    }
    
    /**
     * 更新计划状态
     */
    public boolean updateStatus(Integer planId, String status) throws SQLException {
        String sql = "UPDATE personalized_plans SET plan_status = ? WHERE plan_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            pstmt.setInt(2, planId);
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * 删除计划
     */
    public boolean delete(Integer planId) throws SQLException {
        String sql = "DELETE FROM personalized_plans WHERE plan_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, planId);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * 映射ResultSet到PersonalizedPlan对象
     */
    private PersonalizedPlan mapResultSetToPlan(ResultSet rs) throws SQLException {
        PersonalizedPlan plan = new PersonalizedPlan();
        plan.setPlanId(rs.getInt("plan_id"));
        plan.setUserId(rs.getInt("user_id"));
        plan.setReportId(rs.getInt("report_id"));
        plan.setPlanMonth(rs.getString("plan_month"));
        plan.setTotalDays(rs.getInt("total_days"));
        plan.setPlanType(rs.getString("plan_type"));
        
        int calorieTarget = rs.getInt("calorie_target");
        if (calorieTarget > 0) plan.setCalorieTarget(calorieTarget);
        
        double proteinTarget = rs.getDouble("protein_target");
        if (proteinTarget > 0) plan.setProteinTarget(proteinTarget);
        
        double carbsTarget = rs.getDouble("carbs_target");
        if (carbsTarget > 0) plan.setCarbsTarget(carbsTarget);
        
        double fatTarget = rs.getDouble("fat_target");
        if (fatTarget > 0) plan.setFatTarget(fatTarget);
        
        int exerciseSessions = rs.getInt("exercise_sessions_per_week");
        if (exerciseSessions > 0) plan.setExerciseSessionsPerWeek(exerciseSessions);
        
        plan.setGenerationMethod(rs.getString("generation_method"));
        plan.setPlanStatus(rs.getString("plan_status"));
        
        String createdAt = rs.getString("created_at");
        if (createdAt != null) {
            plan.setCreatedAt(LocalDateTime.parse(createdAt, DATE_FORMATTER));
        }
        
        return plan;
    }
}