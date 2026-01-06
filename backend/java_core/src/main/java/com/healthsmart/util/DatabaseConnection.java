package com.healthsmart.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 数据库连接管理类
 * Database Connection Manager
 */
public class DatabaseConnection {
    private static final String DB_URL = "jdbc:sqlite:backend/data/healthsmart.db";
    private static Connection connection = null;

    private DatabaseConnection() {
        // 私有构造函数，防止实例化
    }

    /**
     * 获取数据库连接
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
        }
        return connection;
    }

    /**
     * 关闭数据库连接
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化数据库表结构
     */
    public static void initializeDatabase() {
        try {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();

            // 创建用户表
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    email TEXT,
                    phone TEXT,
                    nickname TEXT,
                    gender TEXT CHECK(gender IN ('male', 'female', 'other')),
                    birth_date TEXT,
                    created_at TEXT DEFAULT (datetime('now', 'localtime')),
                    updated_at TEXT DEFAULT (datetime('now', 'localtime')),
                    last_login TEXT,
                    is_active INTEGER DEFAULT 1
                )
            """);

            // 创建健康报告表
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS health_reports (
                    report_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    report_month TEXT NOT NULL,
                    height REAL,
                    weight REAL,
                    bmi REAL,
                    body_fat_rate REAL,
                    muscle_mass REAL,
                    systolic_pressure INTEGER,
                    diastolic_pressure INTEGER,
                    sleep_hours_avg REAL,
                    sleep_quality INTEGER CHECK(sleep_quality BETWEEN 1 AND 10),
                    smoking INTEGER DEFAULT 0,
                    drinking INTEGER DEFAULT 0,
                    exercise_frequency INTEGER,
                    dietary_preferences TEXT,
                    food_allergies TEXT,
                    health_goal TEXT,
                    target_weight REAL,
                    medical_history TEXT,
                    medications TEXT,
                    stress_level INTEGER CHECK(stress_level BETWEEN 1 AND 10),
                    energy_level INTEGER CHECK(energy_level BETWEEN 1 AND 10),
                    additional_notes TEXT,
                    submitted_at TEXT DEFAULT (datetime('now', 'localtime')),
                    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
                )
            """);

            // 创建个性化计划表
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS personalized_plans (
                    plan_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    report_id INTEGER NOT NULL,
                    plan_month TEXT NOT NULL,
                    total_days INTEGER DEFAULT 30,
                    plan_type TEXT DEFAULT('monthly'),
                    calorie_target INTEGER,
                    protein_target REAL,
                    carbs_target REAL,
                    fat_target REAL,
                    exercise_sessions_per_week INTEGER,
                    generation_method TEXT,
                    plan_status TEXT CHECK(plan_status IN ('active', 'completed', 'paused')) DEFAULT 'active',
                    created_at TEXT DEFAULT (datetime('now', 'localtime')),
                    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                    FOREIGN KEY (report_id) REFERENCES health_reports(report_id) ON DELETE CASCADE
                )
            """);

            // 创建每日任务表
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS daily_tasks (
                    task_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    plan_id INTEGER NOT NULL,
                    user_id INTEGER NOT NULL,
                    task_date TEXT NOT NULL,
                    meal_breakfast TEXT,
                    meal_lunch TEXT,
                    meal_dinner TEXT,
                    meal_snacks TEXT,
                    daily_calorie_goal INTEGER,
                    exercise_type TEXT,
                    exercise_description TEXT,
                    exercise_duration INTEGER,
                    exercise_intensity TEXT CHECK(exercise_intensity IN ('low', 'medium', 'high')),
                    注意事项 TEXT,
                    is_completed INTEGER DEFAULT 0,
                    completion_rate REAL DEFAULT 0.0,
                    actual_calorie_intake INTEGER,
                    actual_exercise_duration INTEGER,
                    completed_at TEXT,
                    created_at TEXT DEFAULT (datetime('now', 'localtime')),
                    updated_at TEXT DEFAULT (datetime('now', 'localtime')),
                    FOREIGN KEY (plan_id) REFERENCES personalized_plans(plan_id) ON DELETE CASCADE,
                    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
                )
            """);

            // 创建提醒通知表
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS reminders (
                    reminder_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    task_id INTEGER NOT NULL,
                    reminder_date TEXT NOT NULL,
                    reminder_time TEXT,
                    reminder_type TEXT CHECK(reminder_type IN ('meal', 'exercise', 'notice', 'encouragement')),
                    title TEXT,
                    content TEXT,
                    is_read INTEGER DEFAULT 0,
                    sent_at TEXT,
                    created_at TEXT DEFAULT (datetime('now', 'localtime')),
                    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                    FOREIGN KEY (task_id) REFERENCES daily_tasks(task_id) ON DELETE CASCADE
                )
            """);

            // 创建完成度统计表
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS completion_statistics (
                    stat_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    plan_id INTEGER NOT NULL,
                    stat_month TEXT NOT NULL,
                    total_meals_planned INTEGER DEFAULT 0,
                    meals_completed INTEGER DEFAULT 0,
                    total_exercises_planned INTEGER DEFAULT 0,
                    exercises_completed INTEGER DEFAULT 0,
                    overall_completion_rate REAL DEFAULT 0.0,
                    start_weight REAL,
                    current_weight REAL,
                    weight_change REAL,
                    consecutive_days INTEGER DEFAULT 0,
                    longest_streak INTEGER DEFAULT 0,
                    stat_date TEXT DEFAULT (datetime('now', 'localtime')),
                    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                    FOREIGN KEY (plan_id) REFERENCES personalized_plans(plan_id) ON DELETE CASCADE
                )
            """);

            // 创建系统日志表
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS system_logs (
                    log_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER,
                    log_type TEXT CHECK(log_type IN ('login', 'report_submit', 'plan_generate', 'task_complete', 'reminder', 'error')),
                    action TEXT,
                    description TEXT,
                    created_at TEXT DEFAULT (datetime('now', 'localtime')),
                    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
                )
            """);

            // 创建索引
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_users_username ON users(username)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_health_reports_user_id ON health_reports(user_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_personalized_plans_user_id ON personalized_plans(user_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_daily_tasks_user_date ON daily_tasks(user_id, task_date)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_reminders_user_date ON reminders(user_id, reminder_date)");

            stmt.close();
            System.out.println("数据库初始化完成");

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("数据库初始化失败: " + e.getMessage());
        }
    }
}