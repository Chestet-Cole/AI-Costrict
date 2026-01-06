package com.healthsmart.model;

import java.time.LocalDateTime;

/**
 * 每日任务模型类
 * Daily Task Model
 */
public class DailyTask {
    private Integer taskId;
    private Integer planId;
    private Integer userId;
    private String taskDate;

    // 餐食计划
    private String mealBreakfast;
    private String mealLunch;
    private String mealDinner;
    private String mealSnacks;
    private Integer dailyCalorieGoal;

    // 运动任务
    private String exerciseType;
    private String exerciseDescription;
    private Integer exerciseDuration;
    private String exerciseIntensity;

    // 注意事项
    private String 注意事项;

    // 执行状态
    private Boolean isCompleted;
    private Double completionRate;
    private Integer actualCalorieIntake;
    private Integer actualExerciseDuration;
    private LocalDateTime completedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DailyTask() {
        this.isCompleted = false;
        this.completionRate = 0.0;
    }

    // Getters and Setters

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

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

    public String getTaskDate() {
        return taskDate;
    }

    public void setTaskDate(String taskDate) {
        this.taskDate = taskDate;
    }

    public String getMealBreakfast() {
        return mealBreakfast;
    }

    public void setMealBreakfast(String mealBreakfast) {
        this.mealBreakfast = mealBreakfast;
    }

    public String getMealLunch() {
        return mealLunch;
    }

    public void setMealLunch(String mealLunch) {
        this.mealLunch = mealLunch;
    }

    public String getMealDinner() {
        return mealDinner;
    }

    public void setMealDinner(String mealDinner) {
        this.mealDinner = mealDinner;
    }

    public String getMealSnacks() {
        return mealSnacks;
    }

    public void setMealSnacks(String mealSnacks) {
        this.mealSnacks = mealSnacks;
    }

    public Integer getDailyCalorieGoal() {
        return dailyCalorieGoal;
    }

    public void setDailyCalorieGoal(Integer dailyCalorieGoal) {
        this.dailyCalorieGoal = dailyCalorieGoal;
    }

    public String getExerciseType() {
        return exerciseType;
    }

    public void setExerciseType(String exerciseType) {
        this.exerciseType = exerciseType;
    }

    public String getExerciseDescription() {
        return exerciseDescription;
    }

    public void setExerciseDescription(String exerciseDescription) {
        this.exerciseDescription = exerciseDescription;
    }

    public Integer getExerciseDuration() {
        return exerciseDuration;
    }

    public void setExerciseDuration(Integer exerciseDuration) {
        this.exerciseDuration = exerciseDuration;
    }

    public String getExerciseIntensity() {
        return exerciseIntensity;
    }

    public void setExerciseIntensity(String exerciseIntensity) {
        this.exerciseIntensity = exerciseIntensity;
    }

    public String get注意事项() {
        return 注意事项;
    }

    public void set注意事项(String 注意事项) {
        this.注意事项 = 注意事项;
    }

    public Boolean getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(Boolean isCompleted) {
        this.isCompleted = isCompleted;
    }

    public Double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(Double completionRate) {
        this.completionRate = completionRate;
    }

    public Integer getActualCalorieIntake() {
        return actualCalorieIntake;
    }

    public void setActualCalorieIntake(Integer actualCalorieIntake) {
        this.actualCalorieIntake = actualCalorieIntake;
    }

    public Integer getActualExerciseDuration() {
        return actualExerciseDuration;
    }

    public void setActualExerciseDuration(Integer actualExerciseDuration) {
        this.actualExerciseDuration = actualExerciseDuration;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "DailyTask{" +
                "taskId=" + taskId +
                ", userId=" + userId +
                ", taskDate='" + taskDate + '\'' +
                ", isCompleted=" + isCompleted +
                '}';
    }
}