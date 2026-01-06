package com.healthsmart.service;

import com.healthsmart.dao.DailyTaskDAO;
import com.healthsmart.model.DailyTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 提醒服务类
 * Reminder Service
 */
public class ReminderService {
    
    private static final Logger logger = LoggerFactory.getLogger(ReminderService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public ReminderService() {
    }
    
    /**
     * 创建提醒
     */
    public int createReminder(Integer userId, Integer taskId, String date, String time, 
                             String type, String title, String content) throws SQLException {
        String sql = "INSERT INTO reminders (user_id, task_id, reminder_date, reminder_time, " +
                     "reminder_type, title, content) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = com.healthsmart.util.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, userId);
            pstmt.setInt(2, taskId);
            pstmt.setString(3, date);
            pstmt.setString(4, time);
            pstmt.setString(5, type);
            pstmt.setString(6, title);
            pstmt.setString(7, content);
            
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
     * 根据用户ID获取所有提醒
     */
    public List<Reminder> getRemindersByUserId(Integer userId) throws SQLException {
        String sql = "SELECT * FROM reminders WHERE user_id = ? ORDER BY reminder_date DESC, reminder_time DESC";
        List<Reminder> reminders = new ArrayList<>();
        
        try (Connection conn = com.healthsmart.util.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                reminders.add(mapResultSetToReminder(rs));
            }
        }
        return reminders;
    }
    
    /**
     * 获取指定日期的提醒
     */
    public List<Reminder> getRemindersByDate(Integer userId, String date) throws SQLException {
        String sql = "SELECT * FROM reminders WHERE user_id = ? AND reminder_date = ? " +
                     "ORDER BY reminder_time";
        List<Reminder> reminders = new ArrayList<>();
        
        try (Connection conn = com.healthsmart.util.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setString(2, date);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                reminders.add(mapResultSetToReminder(rs));
            }
        }
        return reminders;
    }
    
    /**
     * 获取未读提醒
     */
    public List<Reminder> getUnreadReminders(Integer userId) throws SQLException {
        String sql = "SELECT * FROM reminders WHERE user_id = ? AND is_read = 0 " +
                     "ORDER BY reminder_date DESC, reminder_time DESC";
        List<Reminder> reminders = new ArrayList<>();
        
        try (Connection conn = com.healthsmart.util.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                reminders.add(mapResultSetToReminder(rs));
            }
        }
        return reminders;
    }
    
    /**
     * 标记提醒为已读
     */
    public boolean markAsRead(Integer reminderId) throws SQLException {
        String sql = "UPDATE reminders SET is_read = 1 WHERE reminder_id = ?";
        
        try (Connection conn = com.healthsmart.util.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, reminderId);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * 删除提醒
     */
    public boolean deleteReminder(Integer reminderId) throws SQLException {
        String sql = "DELETE FROM reminders WHERE reminder_id = ?";
        
        try (Connection conn = com.healthsmart.util.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, reminderId);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * 基于每日任务生成提醒
     */
    public void generateRemindersForTask(Integer userId, DailyTask task) throws SQLException {
        String date = task.getTaskDate();
        
        // 早餐提醒
        if (task.getMealBreakfast() != null && !task.getMealBreakfast().isEmpty()) {
            createReminder(userId, task.getTaskId(), date, "07:30", "meal", "早餐提醒", 
                         "今日早餐建议：" + task.getMealBreakfast());
        }
        
        // 午餐提醒
        if (task.getMealLunch() != null && !task.getMealLunch().isEmpty()) {
            createReminder(userId, task.getTaskId(), date, "12:00", "meal", "午餐提醒", 
                         "今日午餐建议：" + task.getMealLunch());
        }
        
        // 晚餐提醒
        if (task.getMealDinner() != null && !task.getMealDinner().isEmpty()) {
            createReminder(userId, task.getTaskId(), date, "18:30", "meal", "晚餐提醒", 
                         "今日晚餐建议：" + task.getMealDinner());
        }
        
        // 运动提醒
        if (task.getExerciseDescription() != null && !task.getExerciseDescription().isEmpty()) {
            createReminder(userId, task.getTaskId(), date, "19:00", "exercise", "运动提醒", 
                         "今日运动任务：" + task.getExerciseDescription() + 
                         "，时长" + task.getExerciseDuration() + "分钟");
        }
        
        // 注意事项提醒
        if (task.get注意事项() != null && !task.get注意事项().isEmpty()) {
            createReminder(userId, task.getTaskId(), date, "09:00", "notice", "注意事项", 
                         task.get注意事项());
        }
    }
    
    /**
     * 为一周的任务批量生成提醒
     */
    public void generateWeeklyReminders(Integer userId, List<DailyTask> tasks) throws SQLException {
        for (DailyTask task : tasks) {
            if (!task.getIsCompleted()) {
                generateRemindersForTask(userId, task);
            }
        }
    }
    
    /**
     * 生成鼓励信息
     */
    public void generateEncouragement(Integer userId, Integer completedDays, Integer totalDays) throws SQLException {
        if (totalDays == 0) {
            return;
        }
        
        double completionRate = (double) completedDays / totalDays * 100;
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        String title = "每日鼓励";
        String content = "";
        
        if (completedDays == 1) {
            content = "完成了第一天的任务，继续保持！";
        } else if (completedDays == totalDays / 2) {
            content = "恭喜你完成了任务的一半！坚持下去，胜利在望！你已经完成了" + completedDays + "天，" +
                      "完成度" + String.format("%.1f%%", completionRate);
        } else if (completedDays == totalDays) {
            content = "太棒了！你已经完成了整个30天的计划！你的坚持值得表扬！";
        } else {
            String[] messages = {
                "每天都在进步，为你的坚持点赞！",
                "健康生活，从每天的行动开始！",
                "你的坚持正在改变自己！", 
                "保持节奏，你能做得更好！",
                "休息是为了走更远的路，加油！"
            };
            int index = (completedDays - 1) % messages.length;
            content = messages[index] + " 已完成" + completedDays + "天，完成度" + 
                      String.format("%.1f%%", completionRate);
        }
        
        createReminder(userId, null, today, "08:00", "encouragement", title, content);
    }
    
    /**
     * 映射ResultSet到Reminder对象
     */
    private Reminder mapResultSetToReminder(ResultSet rs) throws SQLException {
        Reminder reminder = new Reminder();
        reminder.setReminderId(rs.getInt("reminder_id"));
        reminder.setUserId(rs.getInt("user_id"));
        reminder.setTaskId(rs.getInt("task_id"));
        reminder.setReminderDate(rs.getString("reminder_date"));
        reminder.setReminderTime(rs.getString("reminder_time"));
        reminder.setReminderType(rs.getString("reminder_type"));
        reminder.setTitle(rs.getString("title"));
        reminder.setContent(rs.getString("content"));
        reminder.setIsRead(rs.getInt("is_read") == 1);
        
        String sentAt = rs.getString("sent_at");
        if (sentAt != null) {
            reminder.setSentAt(LocalDateTime.parse(sentAt, DATE_FORMATTER));
        }
        
        String createdAt = rs.getString("created_at");
        if (createdAt != null) {
            reminder.setCreatedAt(LocalDateTime.parse(createdAt, DATE_FORMATTER));
        }
        
        return reminder;
    }
    
    /**
     * 提醒内部类
     */
    public static class Reminder {
        private Integer reminderId;
        private Integer userId;
        private Integer taskId;
        private String reminderDate;
        private String reminderTime;
        private String reminderType;
        private String title;
        private String content;
        private Boolean isRead;
        private LocalDateTime sentAt;
        private LocalDateTime createdAt;
        
        // Getters and Setters
        public Integer getReminderId() {
            return reminderId;
        }
        
        public void setReminderId(Integer reminderId) {
            this.reminderId = reminderId;
        }
        
        public Integer getUserId() {
            return userId;
        }
        
        public void setUserId(Integer userId) {
            this.userId = userId;
        }
        
        public Integer getTaskId() {
            return taskId;
        }
        
        public void setTaskId(Integer taskId) {
            this.taskId = taskId;
        }
        
        public String getReminderDate() {
            return reminderDate;
        }
        
        public void setReminderDate(String reminderDate) {
            this.reminderDate = reminderDate;
        }
        
        public String getReminderTime() {
            return reminderTime;
        }
        
        public void setReminderTime(String reminderTime) {
            this.reminderTime = reminderTime;
        }
        
        public String getReminderType() {
            return reminderType;
        }
        
        public void setReminderType(String reminderType) {
            this.reminderType = reminderType;
        }
        
        public String getTitle() {
            return title;
        }
        
        public void setTitle(String title) {
            this.title = title;
        }
        
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
        
        public Boolean getIsRead() {
            return isRead;
        }
        
        public void setIsRead(Boolean isRead) {
            this.isRead = isRead;
        }
        
        public LocalDateTime getSentAt() {
            return sentAt;
        }
        
        public void setSentAt(LocalDateTime sentAt) {
            this.sentAt = sentAt;
        }
        
        public LocalDateTime getCreatedAt() {
            return createdAt;
        }
        
        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }
}