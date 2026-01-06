package com.healthsmart.service;

import com.healthsmart.dao.HealthReportDAO;
import com.healthsmart.model.HealthReport;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 健康报告服务类
 * Health Report Service
 */
public class HealthReportService {
    private HealthReportDAO healthReportDAO;
    
    public HealthReportService() {
        this.healthReportDAO = new HealthReportDAO();
    }
    
    /**
     * 提交健康报告
     */
    public HealthReport submitReport(HealthReport report) throws SQLException {
        // 计算BMI
        report.calculateBMI();
        
        // 插入报告
        int reportId = healthReportDAO.insert(report);
        if (reportId > 0) {
            report.setReportId(reportId);
            return report;
        } else {
            throw new RuntimeException("健康报告提交失败");
        }
    }
    
    /**
     * 获取健康报告
     */
    public HealthReport getReportById(Integer reportId) throws SQLException {
        return healthReportDAO.findById(reportId);
    }
    
    /**
     * 获取用户的所有健康报告
     */
    public List<HealthReport> getReportsByUserId(Integer userId) throws SQLException {
        return healthReportDAO.findByUserId(userId);
    }
    
    /**
     * 获取用户指定月份的健康报告
     */
    public HealthReport getReportByUserIdAndMonth(Integer userId, String month) throws SQLException {
        return healthReportDAO.findByUserIdAndMonth(userId, month);
    }
    
    /**
     * 获取用户最新的健康报告
     */
    public HealthReport getLatestReportByUserId(Integer userId) throws SQLException {
        return healthReportDAO.findLatestByUserId(userId);
    }
    
    /**
     * 获取当前月份的报告
     */
    public HealthReport getCurrentMonthReport(Integer userId) throws SQLException {
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        return healthReportDAO.findByUserIdAndMonth(userId, currentMonth);
    }
    
    /**
     * 获取可用的月份数据（生成报告时用）
     */
    public String generateNextReportMonth(Integer userId) throws SQLException {
        // 获取用户最新报告的月份
        HealthReport latestReport = healthReportDAO.findLatestByUserId(userId);
        
        if (latestReport == null) {
            // 如果没有历史报告，使用当前月份
            return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        } else {
            // 如果有历史报告，使用下个月
            LocalDate lastMonth = LocalDate.parse(latestReport.getReportMonth() + "-01", 
                    DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDate nextMonth = lastMonth.plusMonths(1);
            return nextMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }
    }
    
    /**
     * 检查用户是否有当前或下个月的报告
     */
    public boolean hasActiveReport(Integer userId) throws SQLException {
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String nextMonth = LocalDate.now().plusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));
        
        return healthReportDAO.findByUserIdAndMonth(userId, currentMonth) != null ||
               healthReportDAO.findByUserIdAndMonth(userId, nextMonth) != null;
    }
    
    /**
     * 验证健康报告数据
     */
    public boolean validateReport(HealthReport report) {
        // 验证必填字段
        if (report.getUserId() == null) {
            return false;
        }
        
        if (report.getHeight() == null || report.getHeight() < 100 || report.getHeight() > 250) {
            return false;
        }
        
        if (report.getWeight() == null || report.getWeight() < 30 || report.getWeight() > 200) {
            return false;
        }
        
        // 验证血压范围
        if (report.getSystolicPressure() != null && report.getSystolicPressure() < 60 || report.getSystolicPressure() > 250) {
            return false;
        }
        
        if (report.getDiastolicPressure() != null && report.getDiastolicPressure() < 40 || report.getDiastolicPressure() > 150) {
            return false;
        }
        
        return true;
    }
}