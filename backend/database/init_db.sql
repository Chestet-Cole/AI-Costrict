-- ================================================
-- 健康智护系统 - 数据库初始化脚本
-- SQLite Database Schema
-- ================================================

-- 1. 用户信息表
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
);

-- 2. 健康报告表
CREATE TABLE IF NOT EXISTS health_reports (
    report_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    report_month TEXT NOT NULL,
    -- 身体指标
    height REAL,
    weight REAL,
    bmi REAL,
    body_fat_rate REAL,
    muscle_mass REAL,
    -- 血压
    systolic_pressure INTEGER,
    diastolic_pressure INTEGER,
    -- 睡眠信息
    sleep_hours_avg REAL,
    sleep_quality INTEGER CHECK(sleep_quality BETWEEN 1 AND 10),
    -- 生活习惯
    smoking INTEGER DEFAULT 0,
    drinking INTEGER DEFAULT 0,
    exercise_frequency INTEGER,
    -- 饮食偏好
    dietary_preferences TEXT,
    food_allergies TEXT,
    -- 健康目标
    health_goal TEXT,
    target_weight REAL,
    -- 病史信息
    medical_history TEXT,
    medications TEXT,
    -- 其他信息
    stress_level INTEGER CHECK(stress_level BETWEEN 1 AND 10),
    energy_level INTEGER CHECK(energy_level BETWEEN 1 AND 10),
    additional_notes TEXT,
    submitted_at TEXT DEFAULT (datetime('now', 'localtime')),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 3. 个性化计划表
CREATE TABLE IF NOT EXISTS personalized_plans (
    plan_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    report_id INTEGER NOT NULL,
    plan_month TEXT NOT NULL,
    -- 计划总览
    total_days INTEGER DEFAULT 30,
    plan_type TEXT DEFAULT('monthly'),
    -- 目标设定
    calorie_target INTEGER,
    protein_target REAL,
    carbs_target REAL,
    fat_target REAL,
    exercise_sessions_per_week INTEGER,
    -- 计划生成方式
    generation_method TEXT,
    -- 计划状态
    plan_status TEXT CHECK(plan_status IN ('active', 'completed', 'paused')) DEFAULT 'active',
    created_at TEXT DEFAULT (datetime('now', 'localtime')),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (report_id) REFERENCES health_reports(report_id) ON DELETE CASCADE
);

-- 4. 每日任务表
CREATE TABLE IF NOT EXISTS daily_tasks (
    task_id INTEGER PRIMARY KEY AUTOINCREMENT,
    plan_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    task_date TEXT NOT NULL,
    -- 任务详情
    meal_breakfast TEXT,
    meal_lunch TEXT,
    meal_dinner TEXT,
    meal_snacks TEXT,
    daily_calorie_goal INTEGER,
    -- 运动任务
    exercise_type TEXT,
    exercise_description TEXT,
    exercise_duration INTEGER,
    exercise_intensity TEXT CHECK(exercise_intensity IN ('low', 'medium', 'high')),
    -- 提醒事项
   注意事项 TEXT,
    -- 执行状态
    is_completed INTEGER DEFAULT 0,
    completion_rate REAL DEFAULT 0.0,
    actual_calorie_intake INTEGER,
    actual_exercise_duration INTEGER,
    completed_at TEXT,
    created_at TEXT DEFAULT (datetime('now', 'localtime')),
    updated_at TEXT DEFAULT (datetime('now', 'localtime')),
    FOREIGN KEY (plan_id) REFERENCES personalized_plans(plan_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 5. 饮食记录表
CREATE TABLE IF NOT EXISTS diet_records (
    diet_id INTEGER PRIMARY KEY AUTOINCREMENT,
    task_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    record_date TEXT NOT NULL,
    meal_type TEXT CHECK(meal_type IN ('breakfast', 'lunch', 'dinner', 'snack')),
    food_name TEXT,
    food_description TEXT,
    calories INTEGER,
    protein REAL,
    carbs REAL,
    fat REAL,
    recorded_at TEXT DEFAULT (datetime('now', 'localtime')),
    FOREIGN KEY (task_id) REFERENCES daily_tasks(task_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 6. 运动记录表
CREATE TABLE IF NOT EXISTS exercise_records (
    exercise_id INTEGER PRIMARY KEY AUTOINCREMENT,
    task_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    record_date TEXT NOT NULL,
    exercise_type TEXT,
    exercise_name TEXT,
    duration INTEGER,
    intensity TEXT CHECK(intensity IN ('low', 'medium', 'high')),
    calories_burned INTEGER,
    notes TEXT,
    recorded_at TEXT DEFAULT (datetime('now', 'localtime')),
    FOREIGN KEY (task_id) REFERENCES daily_tasks(task_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 7. 提醒通知表
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
);

-- 8. 完成度统计表
CREATE TABLE IF NOT EXISTS completion_statistics (
    stat_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    plan_id INTEGER NOT NULL,
    stat_month TEXT NOT NULL,
    -- 饮食完成度
    total_meals_planned INTEGER DEFAULT 0,
    meals_completed INTEGER DEFAULT 0,
    -- 运动完成度
    total_exercises_planned INTEGER DEFAULT 0,
    exercises_completed INTEGER DEFAULT 0,
    -- 综合完成度
    overall_completion_rate REAL DEFAULT 0.0,
    -- 体重变化
    start_weight REAL,
    current_weight REAL,
    weight_change REAL,
    -- 连续打卡天数
    consecutive_days INTEGER DEFAULT 0,
    longest_streak INTEGER DEFAULT 0,
    -- 记录时间
    stat_date TEXT DEFAULT (datetime('now', 'localtime')),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (plan_id) REFERENCES personalized_plans(plan_id) ON DELETE CASCADE
);

-- 9. 系统日志表
CREATE TABLE IF NOT EXISTS system_logs (
    log_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER,
    log_type TEXT CHECK(log_type IN ('login', 'report_submit', 'plan_generate', 'task_complete', 'reminder', 'error')),
    action TEXT,
    description TEXT,
    created_at TEXT DEFAULT (datetime('now', 'localtime')),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
);

-- 创建索引以提高查询性能
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_health_reports_user_id ON health_reports(user_id);
CREATE INDEX IF NOT EXISTS idx_health_reports_month ON health_reports(report_month);
CREATE INDEX IF NOT EXISTS idx_personalized_plans_user_id ON personalized_plans(user_id);
CREATE INDEX IF NOT EXISTS idx_daily_tasks_user_id_date ON daily_tasks(user_id, task_date);
CREATE INDEX IF NOT EXISTS idx_daily_tasks_plan_id ON daily_tasks(plan_id);
CREATE INDEX IF NOT EXISTS idx_reminders_user_date ON reminders(user_id, reminder_date);
CREATE INDEX IF NOT EXISTS idx_completion_statistics_user_plan ON completion_statistics(user_id, plan_id);
CREATE INDEX IF NOT EXISTS idx_system_logs_user_id ON system_logs(user_id);

-- 插入初始测试数据
INSERT INTO users (username, password, nickname, email, gender) VALUES 
('demo_user', 'demo123', '演示用户', 'demo@healthsmart.com', 'male'),
('admin', 'admin123', '管理员', 'admin@healthsmart.com', 'male');