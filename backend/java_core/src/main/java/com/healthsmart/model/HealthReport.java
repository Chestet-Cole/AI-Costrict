package com.healthsmart.model;

import java.time.LocalDateTime;

/**
 * 健康报告模型类
 * Health Report Model
 */
public class HealthReport {
    private Integer reportId;
    private Integer userId;
    private String reportMonth;

    // 身体指标
    private Double height;
    private Double weight;
    private Double bmi;
    private Double bodyFatRate;
    private Double muscleMass;

    // 血压
    private Integer systolicPressure;
    private Integer diastolicPressure;

    // 睡眠信息
    private Double sleepHoursAvg;
    private Integer sleepQuality;

    // 生活习惯
    private Boolean smoking;
    private Boolean drinking;
    private Integer exerciseFrequency;

    // 饮食偏好
    private String dietaryPreferences;
    private String foodAllergies;

    // 健康目标
    private String healthGoal;
    private Double targetWeight;

    // 病史信息
    private String medicalHistory;
    private String medications;

    // 其他指标
    private Integer stressLevel;
    private Integer energyLevel;
    private String additionalNotes;

    private LocalDateTime submittedAt;

    public HealthReport() {
        this.smoking = false;
        this.drinking = false;
    }

    // Getters and Setters

    public Integer getReportId() {
        return reportId;
    }

    public void setReportId(Integer reportId) {
        this.reportId = reportId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getReportMonth() {
        return reportMonth;
    }

    public void setReportMonth(String reportMonth) {
        this.reportMonth = reportMonth;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Double getBmi() {
        return bmi;
    }

    public void setBmi(Double bmi) {
        this.bmi = bmi;
    }

    public Double getBodyFatRate() {
        return bodyFatRate;
    }

    public void setBodyFatRate(Double bodyFatRate) {
        this.bodyFatRate = bodyFatRate;
    }

    public Double getMuscleMass() {
        return muscleMass;
    }

    public void setMuscleMass(Double muscleMass) {
        this.muscleMass = muscleMass;
    }

    public Integer getSystolicPressure() {
        return systolicPressure;
    }

    public void setSystolicPressure(Integer systolicPressure) {
        this.systolicPressure = systolicPressure;
    }

    public Integer getDiastolicPressure() {
        return diastolicPressure;
    }

    public void setDiastolicPressure(Integer diastolicPressure) {
        this.diastolicPressure = diastolicPressure;
    }

    public Double getSleepHoursAvg() {
        return sleepHoursAvg;
    }

    public void setSleepHoursAvg(Double sleepHoursAvg) {
        this.sleepHoursAvg = sleepHoursAvg;
    }

    public Integer getSleepQuality() {
        return sleepQuality;
    }

    public void setSleepQuality(Integer sleepQuality) {
        this.sleepQuality = sleepQuality;
    }

    public Boolean getSmoking() {
        return smoking;
    }

    public void setSmoking(Boolean smoking) {
        this.smoking = smoking;
    }

    public Boolean getDrinking() {
        return drinking;
    }

    public void setDrinking(Boolean drinking) {
        this.drinking = drinking;
    }

    public Integer getExerciseFrequency() {
        return exerciseFrequency;
    }

    public void setExerciseFrequency(Integer exerciseFrequency) {
        this.exerciseFrequency = exerciseFrequency;
    }

    public String getDietaryPreferences() {
        return dietaryPreferences;
    }

    public void setDietaryPreferences(String dietaryPreferences) {
        this.dietaryPreferences = dietaryPreferences;
    }

    public String getFoodAllergies() {
        return foodAllergies;
    }

    public void setFoodAllergies(String foodAllergies) {
        this.foodAllergies = foodAllergies;
    }

    public String getHealthGoal() {
        return healthGoal;
    }

    public void setHealthGoal(String healthGoal) {
        this.healthGoal = healthGoal;
    }

    public Double getTargetWeight() {
        return targetWeight;
    }

    public void setTargetWeight(Double targetWeight) {
        this.targetWeight = targetWeight;
    }

    public String getMedicalHistory() {
        return medicalHistory;
    }

    public void setMedicalHistory(String medicalHistory) {
        this.medicalHistory = medicalHistory;
    }

    public String getMedications() {
        return medications;
    }

    public void setMedications(String medications) {
        this.medications = medications;
    }

    public Integer getStressLevel() {
        return stressLevel;
    }

    public void setStressLevel(Integer stressLevel) {
        this.stressLevel = stressLevel;
    }

    public Integer getEnergyLevel() {
        return energyLevel;
    }

    public void setEnergyLevel(Integer energyLevel) {
        this.energyLevel = energyLevel;
    }

    public String getAdditionalNotes() {
        return additionalNotes;
    }

    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    /**
     * 计算BMI
     */
    public void calculateBMI() {
        if (height != null && weight != null && height > 0) {
            double heightInMeters = height / 100.0;
            this.bmi = Math.round((weight / (heightInMeters * heightInMeters)) * 100.0) / 100.0;
        }
    }

    @Override
    public String toString() {
        return "HealthReport{" +
                "reportId=" + reportId +
                ", userId=" + userId +
                ", reportMonth='" + reportMonth + '\'' +
                ", height=" + height +
                ", weight=" + weight +
                ", bmi=" + bmi +
                '}';
    }
}