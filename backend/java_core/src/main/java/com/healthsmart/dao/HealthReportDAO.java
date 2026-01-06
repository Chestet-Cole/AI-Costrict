package com.healthsmart.dao;

import com.healthsmart.model.HealthReport;
import com.healthsmart.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 健康报告数据访问对象
 * Health Report Data Access Object
 */
public class HealthReportDAO {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 插入健康报告
     */
    public int insert(HealthReport report) throws SQLException {
        report.calculateBMI(); // 计算BMI
        
        String sql = "INSERT INTO health_reports (user_id, report_month, height, weight, bmi, " +
                     "body_fat_rate, muscle_mass, systolic_pressure, diastolic_pressure, " +
                     "sleep_hours_avg, sleep_quality, smoking, drinking, exercise_frequency, " +
                     "dietary_preferences, food_allergies, health_goal, target_weight, " +
                     "medical_history, medications, stress_level, energy_level, additional_notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, report.getUserId());
            pstmt.setString(2, report.getReportMonth());
            pstmt.setDouble(3, report.getHeight() != null ? report.getHeight() : 0);
            pstmt.setDouble(4, report.getWeight() != null ? report.getWeight() : 0);
            pstmt.setDouble(5, report.getBmi() != null ? report.getBmi() : 0);
            pstmt.setDouble(6, report.getBodyFatRate() != null ? report.getBodyFatRate() : 0);
            pstmt.setDouble(7, report.getMuscleMass() != null ? report.getMuscleMass() : 0);
            pstmt.setInt(8, report.getSystolicPressure() != null ? report.getSystolicPressure() : 0);
            pstmt.setInt(9, report.getDiastolicPressure() != null ? report.getDiastolicPressure() : 0);
            pstmt.setDouble(10, report.getSleepHoursAvg() != null ? report.getSleepHoursAvg() : 0);
            pstmt.setInt(11, report.getSleepQuality() != null ? report.getSleepQuality() : 0);
            pstmt.setInt(12, report.getSmoking() ? 1 : 0);
            pstmt.setInt(13, report.getDrinking() ? 1 : 0);
            pstmt.setInt(14, report.getExerciseFrequency() != null ? report.getExerciseFrequency() : 0);
            pstmt.setString(15, report.getDietaryPreferences());
            pstmt.setString(16, report.getFoodAllergies());
            pstmt.setString(17, report.getHealthGoal());
            pstmt.setDouble(18, report.getTargetWeight() != null ? report.getTargetWeight() : 0);
            pstmt.setString(19, report.getMedicalHistory());
            pstmt.setString(20, report.getMedications());
            pstmt.setInt(21, report.getStressLevel() != null ? report.getStressLevel() : 0);
            pstmt.setInt(22, report.getEnergyLevel() != null ? report.getEnergyLevel() : 0);
            pstmt.setString(23, report.getAdditionalNotes());
            
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
     * 根据ID查找健康报告
     */
    public HealthReport findById(Integer reportId) throws SQLException {
        String sql = "SELECT * FROM health_reports WHERE report_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, reportId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToReport(rs);
            }
        }
        return null;
    }
    
    /**
     * 根据用户ID查找所有健康报告
     */
    public List<HealthReport> findByUserId(Integer userId) throws SQLException {
        String sql = "SELECT * FROM health_reports WHERE user_id = ? ORDER BY submitted_at DESC";
        List<HealthReport> reports = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                reports.add(mapResultSetToReport(rs));
            }
        }
        return reports;
    }
    
    /**
     * 根据用户ID和月份查找健康报告
     */
    public HealthReport findByUserIdAndMonth(Integer userId, String month) throws SQLException {
        String sql = "SELECT * FROM health_reports WHERE user_id = ? AND report_month = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setString(2, month);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToReport(rs);
            }
        }
        return null;
    }
    
    /**
     * 获取用户最新的健康报告
     */
    public HealthReport findLatestByUserId(Integer userId) throws SQLException {
        String sql = "SELECT * FROM health_reports WHERE user_id = ? " +
                     "ORDER BY submitted_at DESC LIMIT 1";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToReport(rs);
            }
        }
        return null;
    }
    
    /**
     * 映射ResultSet到HealthReport对象
     */
    private HealthReport mapResultSetToReport(ResultSet rs) throws SQLException {
        HealthReport report = new HealthReport();
        report.setReportId(rs.getInt("report_id"));
        report.setUserId(rs.getInt("user_id"));
        report.setReportMonth(rs.getString("report_month"));
        
        report.setHeight(rs.getDouble("height"));
        report.setWeight(rs.getDouble("weight"));
        report.setBmi(rs.getDouble("bmi"));
        report.setBodyFatRate(rs.getDouble("body_fat_rate"));
        report.setMuscleMass(rs.getDouble("muscle_mass"));
        
        int systolic = rs.getInt("systolic_pressure");
        int diastolic = rs.getInt("diastolic_pressure");
        if (systolic > 0) report.setSystolicPressure(systolic);
        if (diastolic > 0) report.setDiastolicPressure(diastolic);
        
        double sleepHours = rs.getDouble("sleep_hours_avg");
        if (sleepHours > 0) report.setSleepHoursAvg(sleepHours);
        
        int sleepQuality = rs.getInt("sleep_quality");
        if (sleepQuality > 0) report.setSleepQuality(sleepQuality);
        
        int exerciseFreq = rs.getInt("exercise_frequency");
        if (exerciseFreq > 0) report.setExerciseFrequency(exerciseFreq);
        
        report.setSmoking(rs.getInt("smoking") == 1);
        report.setDrinking(rs.getInt("drinking") == 1);
        report.setDietaryPreferences(rs.getString("dietary_preferences"));
        report.setFoodAllergies(rs.getString("food_allergies"));
        report.setHealthGoal(rs.getString("health_goal"));
        
        double targetWeight = rs.getDouble("target_weight");
        if (targetWeight > 0) report.setTargetWeight(targetWeight);
        
        report.setMedicalHistory(rs.getString("medical_history"));
        report.setMedications(rs.getString("medications"));
        
        int stressLevel = rs.getInt("stress_level");
        if (stressLevel > 0) report.setStressLevel(stressLevel);
        
        int energyLevel = rs.getInt("energy_level");
        if (energyLevel > 0) report.setEnergyLevel(energyLevel);
        
        report.setAdditionalNotes(rs.getString("additional_notes"));
        
        String submittedAt = rs.getString("submitted_at");
        if (submittedAt != null) {
            report.setSubmittedAt(LocalDateTime.parse(submittedAt, DATE_FORMATTER));
        }
        
        return report;
    }
}