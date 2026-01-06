package com.healthsmart.model;

import java.time.LocalDateTime;

/**
 * 个性化计划模型类
 * Personalized Plan Model
 */
public class PersonalizedPlan {
    private Integer planId;
    private Integer userId;
    private Integer reportId;
    private String planMonth;

    // 总览
    private Integer totalDays;
    private String planType;

    // 目标设定
    private Integer calorieTarget;
    private Double proteinTarget;
    private Double carbsTarget;
    private Double fatTarget;
    private Integer exerciseSessionsPerWeek;

    // 计划生成方式
    private String generationMethod;

    // 计划状态
    private String planStatus;

    private LocalDateTime createdAt;

    public PersonalizedPlan() {
        this.totalDays = 30;
        this.planType = "monthly";
        this.planStatus = "active";
    }

    // Getters and Setters

    public Integer getPlanId() {
        return planId;
    }

    public void setPlanId(Integer planId) {
        this.planId = planId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getReportId() {
        return reportId;
    }

    public void setReportId(Integer reportId) {
        this.reportId = reportId;
    }

    public String getPlanMonth() {
        return planMonth;
    }

    public void setPlanMonth(String planMonth) {
        this.planMonth = planMonth;
    }

    public Integer getTotalDays() {
        return totalDays;
    }

    public void setTotalDays(Integer totalDays) {
        this.totalDays = totalDays;
    }

    public String getPlanType() {
        return planType;
    }

    public void setPlanType(String planType) {
        this.planType = planType;
    }

    public Integer getCalorieTarget() {
        return calorieTarget;
    }

    public void setCalorieTarget(Integer calorieTarget) {
        this.calorieTarget = calorieTarget;
    }

    public Double getProteinTarget() {
        return proteinTarget;
    }

    public void setProteinTarget(Double proteinTarget) {
        this.proteinTarget = proteinTarget;
    }

    public Double getCarbsTarget() {
        return carbsTarget;
    }

    public void setCarbsTarget(Double carbsTarget) {
        this.carbsTarget = carbsTarget;
    }

    public Double getFatTarget() {
        return fatTarget;
    }

    public void setFatTarget(Double fatTarget) {
        this.fatTarget = fatTarget;
    }

    public Integer getExerciseSessionsPerWeek() {
        return exerciseSessionsPerWeek;
    }

    public void setExerciseSessionsPerWeek(Integer exerciseSessionsPerWeek) {
        this.exerciseSessionsPerWeek = exerciseSessionsPerWeek;
    }

    public String getGenerationMethod() {
        return generationMethod;
    }

    public void setGenerationMethod(String generationMethod) {
        this.generationMethod = generationMethod;
    }

    public String getPlanStatus() {
        return planStatus;
    }

    public void setPlanStatus(String planStatus) {
        this.planStatus = planStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "PersonalizedPlan{" +
                "planId=" + planId +
                ", userId=" + userId +
                ", planMonth='" + planMonth + '\'' +
                ", planStatus='" + planStatus + '\'' +
                '}';
    }
}