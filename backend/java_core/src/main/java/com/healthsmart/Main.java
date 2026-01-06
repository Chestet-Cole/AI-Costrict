package com.healthsmart;

import com.healthsmart.model.HealthReport;
import com.healthsmart.model.User;
import com.healthsmart.service.*;
import com.healthsmart.util.DatabaseConnection;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 健康智护系统 - 主入口类
 * HealthSmart - Main Entry Point
 */
public class Main {
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   健康智护 - 个人健康动态管理系统");
        System.out.println("   HealthSmart - Health Management System");
        System.out.println("========================================\n");
        
        try {
            // 初始化数据库
            DatabaseConnection.initializeDatabase();
            System.out.println("[系统初始化] 数据库初始化完成\n");
            
            // 启动API服务器
            startApiServer();
            
            // 测试Python引擎连接
            PythonEngineExecutor pythonEngine = PythonEngineExecutor.getInstance();
            boolean isEngineHealthy = pythonEngine.checkEngineHealth();
            if (isEngineHealthy) {
                System.out.println("[系统初始化] Python智能引擎连接成功\n");
            } else {
                System.out.println("[警告] Python智能引擎无法连接，部分功能将不可用\n");
            }
            
            // 运行模式判断
            if (args.length > 0) {
                if (args[0].equals("--demo")) {
                    runDemoMode();
                } else if (args[0].equals("--api-only")) {
                    System.out.println("API服务器已启动，按Ctrl+C停止...\n");
                    // 保持运行
                    Thread.currentThread().join();
                }
            } else {
                System.out.println("系统准备就绪！");
                System.out.println("API服务器运行中: http://localhost:8080");
                System.out.println("提示：使用 --demo 参数运行演示模式\n");
            }
            
        } catch (Exception e) {
            System.err.println("系统启动失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 启动API服务器
     */
    private static void startApiServer() {
        try {
            // 检测端口是否被占用
            boolean portInUse = checkPortInUse(8080);
            if (portInUse) {
                System.out.println("[系统初始化] 端口8080已被占用，尝试使用端口8081...");
                ApiServer.PORT = 8081;
            }
            
            ApiServer.start();
            System.out.println("[系统初始化] REST API服务器启动成功\n");
        } catch (Exception e) {
            System.out.println("[警告] API服务器启动失败: " + e.getMessage());
            System.out.println("前端将使用模拟数据模式运行\n");
        }
    }
    
    /**
     * 检查端口是否被占用
     */
    private static boolean checkPortInUse(int port) {
        try (java.net.ServerSocket socket = new java.net.ServerSocket(port)) {
            return false;
        } catch (java.io.IOException e) {
            return true;
        }
    }
    
    /**
     * 演示模式
     */
    private static void runDemoMode() {
        try {
            System.out.println("========================================");
            System.out.println("          演示模式启动");
            System.out.println("========================================\n");
            
            UserService userService = new UserService();
            HealthReportService reportService = new HealthReportService();
            PlanGenerationService planService = new PlanGenerationService();
            AnalyticsService analyticsService = new AnalyticsService();
            
            // 1. 用户注册
            System.out.println("【步骤1】用户注册演示");
            String username = "demo_user_" + System.currentTimeMillis();
            User user = userService.register(username, "demo123", "demo@healthsmart.com", "演示用户");
            System.out.println("✓ 用户创建成功: " + user.getUsername() + " (ID: " + user.getUserId() + ")\n");
            
            // 2. 用户登录
            System.out.println("【步骤2】用户登录演示");
            user = userService.login(username, "demo123");
            System.out.println("✓ 登录成功: " + user.getNickname() + "\n");
            
            // 3. 提交健康报告
            System.out.println("【步骤3】提交健康报告演示");
            String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            HealthReport report = new HealthReport();
            report.setUserId(user.getUserId());
            report.setReportMonth(currentMonth);
            report.setHeight(175.0);  // cm
            report.setWeight(75.0);   // kg
            report.setBodyFatRate(20.0);
            report.setMuscleMass(50.0);
            report.setSystolicPressure(120);
            report.setDiastolicPressure(80);
            report.setSleepHoursAvg(7.5);
            report.setSleepQuality(8);
            report.setSmoking(false);
            report.setDrinking(false);
            report.setExerciseFrequency(3);
            report.setDietaryPreferences("均衡饮食");
            report.setHealthGoal("maintain_weight");
            report.setTargetWeight(72.0);
            report.setStressLevel(5);
            report.setEnergyLevel(7);
            
            report = reportService.submitReport(report);
            System.out.println("✓ 健康报告提交成功 (ID: " + report.getReportId() + ")");
            System.out.println("  BMI: " + report.getBmi() + "\n");
            
            // 4. 生成个性化计划
            System.out.println("【步骤4】生成月度个性化计划");
            System.out.println("正在调用Python智能引擎生成计划...");
            var monthlyPlan = planService.generatePlanFromReport(report);
            System.out.println("✓ 月度计划生成成功!");
            System.out.println("  计划ID: " + monthlyPlan.getPlanId());
            System.out.println("  计划月份: " + monthlyPlan.getPlanMonth());
            System.out.println("  每日热量目标: " + monthlyPlan.getCalorieTarget() + " kcal");
            System.out.println("  蛋白质目标: " + monthlyPlan.getProteinTarget() + " g");
            System.out.println("  每周运动次数: " + monthlyPlan.getExerciseSessionsPerWeek() + " 次\n");
            
            // 5. 查看每日计划内容
            System.out.println("【步骤5】查看每日计划内容");
            var dailyTasks = planService.getPlanTasks(monthlyPlan.getPlanId());
            System.out.println("✓ 共生成 " + dailyTasks.size() + " 天的计划");
            System.out.println("\n前3天计划示例:");
            for (int i = 0; i < Math.min(3, dailyTasks.size()); i++) {
                var task = dailyTasks.get(i);
                System.out.println("\n--- 第 " + task.getTaskDate() + " ---");
                System.out.println("  早餐: " + task.getMealBreakfast());
                System.out.println("  午餐: " + task.getMealLunch());
                System.out.println("  晚餐: " + task.getMealDinner());
                System.out.println("  运动: " + task.getExerciseDescription() + 
                                   "(" + task.getExerciseDuration() + "分钟)");
            }
            System.out.println();
            
            // 6. 生成统计数据分析
            System.out.println("【步骤6】数据统计分析演示");
            var stats = analyticsService.getPlanCompletionStats(monthlyPlan.getPlanId());
            System.out.println("✓ 计划统计:");
            System.out.println("  总天数: " + stats.getTotalDays());
            System.out.println("  已完成: " + stats.getCompletedDays());
            System.out.println("  完成率: " + String.format("%.1f%%", stats.getOverallCompletionRate()));
            System.out.println("\n========================================");
            System.out.println("          演示完成");
            System.out.println("========================================");
            
        } catch (Exception e) {
            System.err.println("演示模式执行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}