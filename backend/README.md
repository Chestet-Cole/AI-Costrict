# 健康智护 - 后端开发文档

## 项目概述

健康智护（HealthSmart）是一个基于"月度规划-日常执行-实时反馈"闭环的个人健康动态管理系统。后端采用Python + Java混合架构，Python负责智能数据分析与算法生成，Java负责数据持久化、业务逻辑和API服务。

## 技术栈

### Python模块
- Python 3.x
- 数据处理：pandas, numpy
- 规则引擎：自定义健康规则库

### Java模块
- Java 17
- 构建工具：Maven
- 数据库：SQLite (使用 sqlite-jdbc 驱动)
- JSON处理：Gson
- 日志：SLF4J

## 项目结构

```
backend/
├── data/                          # 数据文件目录
│   └── healthsmart.db             # SQLite数据库文件（运行后生成）
├── database/                      # 数据库脚本
│   └── init_db.sql               # 数据库初始化脚本
├── java_core/                     # Java核心模块
│   ├── pom.xml                   # Maven配置文件
│   ├── ApiServer.java            # REST API服务器
│   └── src/main/java/com/healthsmart/
│       ├── Main.java              # 主入口类
│       ├── dao/                   # 数据访问层
│       │   ├── UserDAO.java
│       │   ├── HealthReportDAO.java
│       │   ├── PersonalizedPlanDAO.java
│       │   └── DailyTaskDAO.java
│       ├── model/                 # 数据模型
│       │   ├── User.java
│       │   ├── HealthReport.java
│       │   ├── PersonalizedPlan.java
│       │   └── DailyTask.java
│       ├── service/               # 业务逻辑层
│       │   ├── UserService.java
│       │   ├── HealthReportService.java
│       │   ├── PlanGenerationService.java
│       │   ├── PythonEngineExecutor.java
│       │   ├── ReminderService.java
│       │   └── AnalyticsService.java
│       └── util/                  # 工具类
│           └── DatabaseConnection.java
└── python_engine/                 # Python智能引擎
    ├── __init__.py
    ├── models.py                 # 数据模型定义
    ├── rules_engine.py           # 规则引擎（基于健康指南）
    ├── health_engine.py          # 核心算法引擎
    ├── api_service.py           # API接口服务
    └── requirements.txt         # Python依赖包
```

## 核心功能模块

### 1. 用户管理模块 (UserService)
- 用户注册
- 用户登录验证
- 用户信息查询与更新
- 用户状态管理

### 2. 健康报告处理模块 (HealthReportService)
- 健康报告提交与存储
- BMI自动计算
- 健康数据历史查询
- 报告数据验证

### 3. 个性化计划生成模块 (PlanGenerationService)
- 基于健康报告生成30天月度计划
- 调用Python引擎进行智能分析
- 生成每日饮食建议
- 生成每日运动计划
- 批量保存每日任务

### 4. Python智能引擎模块

#### 规则引擎 (rules_engine.py)
- **营养规则库**：基于《中国居民膳食指南2022》
  - BMR基础代谢率计算（Mifflin-St Jeor公式）
  - 活动水平热量调整
  - 宏量营养素分配（蛋白质15%、碳水50%、脂肪35%）
  - 餐食热量分配（早餐30%、午餐40%、晚餐25%、加餐5%）

- **运动规则库**：
  - 根据活动水平推荐运动频率与强度
  - 健康目标导向的运动类型调整
  - 卡路里消耗估算

- **健康安全规则**：
  - BMI分类与安全范围检查
  - 血压安全范围验证
  - 睡眠时长建议
  - 健康警告生成

#### 核心算法 (health_engine.py)
- 基础规则库 + AI动态调参的混合模型
- 健康报告分析与评估
- 个性化营养目标计算
- 智能餐食推荐（考虑饮食偏好与过敏史）
- 个性化运动计划生成
- 每日提醒内容生成

### 5. 每日提醒与通知模块 (ReminderService)
- 基于每日任务自动生成提醒
- 餐食提醒（早/午/晚）
- 运动提醒
- 注意事项提醒
- 每日鼓励信息
- 提醒状态管理

### 6. 数据分析与可视化接口 (AnalyticsService)
- **计划完成度统计**
  - 总体完成率
  - 饮食完成度
  - 运动完成度
  - 偏差分析

- **每日完成趋势**
  - 卡路里摄入趋势
  - 运动时长趋势
  - 每日完成状态

- **周度统计**
  - 每周完成率
  - 累计热量消耗
  - 累计运动时长

- **连续打卡统计**
  - 当前连续天数
  - 最长连续天数
  - 总累计天数

- **营养趋势分析**
  - 平均摄入量
  - 目标达标率
  - 偏差统计

### 7. 数据持久化层
- **SQLite数据库**：轻量级、零配置
- **数据表结构**：
  - `users` - 用户信息
  - `health_reports` - 健康报告
  - `personalized_plans` - 个性化计划
  - `daily_tasks` - 每日任务
  - `reminders` - 提醒通知
  - `completion_statistics` - 完成度统计
  - `system_logs` - 系统日志

## API接口文档

### 概述
- 基础路径: `http://localhost:8080/api`
- 响应格式: JSON
- 编码: UTF-8

### 响应结构
```json
{
  "success": true,
  "message": "操作成功",
  "data": { ... }
}
```

### 用户服务接口

#### 用户注册
- **URL**: `/api/user?action=register`
- **方法**: POST
- **请求体**:
```json
{
  "username": "用户名",
  "password": "密码",
  "email": "邮箱",
  "nickname": "昵称"
}
```
- **响应成功**:
```json
{
  "success": true,
  "data": {
    "userId": 1,
    "username": "用户名",
    "nickname": "昵称",
    "email": "邮箱"
  }
}
```

#### 用户登录
- **URL**: `/api/user?action=login`
- **方法**: POST
- **请求体**:
```json
{
  "username": "用户名",
  "password": "密码"
}
```

#### 获取用户资料
- **URL**: `/api/user?action=profile`
- **方法**: POST
- **请求体**:
```json
{
  "userId": 1
}
```

#### 更新用户信息
- **URL**: `/api/user?action=update`
- **方法**: POST
- **请求体**:
```json
{
  "userId": 1,
  "nickname": "新昵称",
  "email": "新邮箱"
}
```

### 健康报告接口

#### 提交健康报告
- **URL**: `/api/health-report?action=submit`
- **方法**: POST
- **请求体**:
```json
{
  "userId": 1,
  "height": 175,
  "weight": 70,
  "age": 25,
  "gender": "male",
  "bloodPressureSystolic": 120,
  "bloodPressureDiastolic": 80,
  "sleepHours": 7,
  "exerciseFrequency": "moderate",
  "dietPreference": "balanced",
  "healthGoal": "lose_weight"
}
```

#### 获取最新报告
- **URL**: `/api/health-report?action=getLatest`
- **方法**: POST
- **请求体**:
```json
{
  "userId": 1
}
```

#### 获取报告列表
- **URL**: `/api/health-report?action=list`
- **方法**: POST
- **请求体**:
```json
{
  "userId": 1
}
```

### 计划接口

#### 获取今日计划
- **URL**: `/api/plan?action=getToday`
- **方法**: POST
- **请求体**:
```json
{
  "userId": 1
}
```

#### 获取某日计划
- **URL**: `/api/plan?action=getByDate`
- **方法**: POST
- **请求体**:
```json
{
  "userId": 1,
  "date": "2024-01-15"
}
```

#### 获取月度计划
- **URL**: `/api/plan?action=getMonthly`
- **方法**: POST
- **请求体**:
```json
{
  "userId": 1,
  "year": 2024,
  "month": 1
}
```

#### 标记任务完成
- **URL**: `/api/plan?action=completeTask`
- **方法**: POST
- **请求体**:
```json
{
  "taskId": 1,
  "completed": true
}
```

#### 生成新计划
- **URL**: `/api/plan?action=generate`
- **方法**: POST
- **请求体**:
```json
{
  "userId": 1,
  "reportId": 1
}
```

### 数据分析接口

#### 获取周度统计
- **URL**: `/api/analytics?action=weekly`
- **方法**: POST
- **请求体**:
```json
{
  "userId": 1
}
```

#### 获取完成度统计
- **URL**: `/api/analytics?action=completion`
- **方法**: POST
- **请求体**:
```json
{
  "userId": 1,
  "planId": 1
}
```

#### 获取连续打卡统计
- **URL**: `/api/analytics?action=streak`
- **方法**: POST
- **请求体**:
```json
{
  "userId": 1
}
```

#### 获取营养趋势
- **URL**: `/api/analytics?action=nutrition`
- **方法**: POST
- **请求体**:
```json
{
  "userId": 1,
  "days": 7
}
```

### 快速开始

#### 环境要求
- Java 17+
- Python 3.8+
- Maven 3.6+

#### 安装步骤

1. **安装Python依赖**
```bash
cd backend/python_engine
pip install -r requirements.txt
```

2. **编译Java项目**
```bash
cd backend/java_core
mvn compile
```

#### 运行项目

**启动Java主程序（含API服务器）:**
```bash
cd backend/java_core
mvn exec:java -Dexec.mainClass="com.healthsmart.Main"
```

**运行演示模式:**
```bash
cd backend/java_core
mvn exec:java -Dexec.mainClass="com.healthsmart.Main" -Dexec.args="--demo"
```

演示模式执行以下流程：
1. 用户注册
2. 用户登录
3. 提交健康报告
4. 生成月度个性化计划
5. 查看每日计划内容
6. 数据统计分析

#### 单独测试Python引擎
```bash
cd backend/python_engine

# 健康检查
python api_service.py health

# 生成计划示例
python api_service.py generate_plan '{"user_id":1,"report_month":"2024-01","height":175,"weight":70,"health_goal":"maintain_weight"}'
```

## 相关文档

- [项目总览](../README.md) - 项目主文档
- [前端开发文档](../frontend/README.md) - 前端技术文档
- [开发总结](开发总结.md) - 开发过程总结
- [需求规格](../深信服比赛项目简介.txt) - 原始需求文档

## 许可证

本项目仅供学习和研究使用。