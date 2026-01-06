# 健康智护 - 后端开发文档

## 项目概述

健康智护（HealthSmart）是一个基于"月度规划-日常执行-实时反馈"闭环的个人健康动态管理系统。后端采用Python + Java混合架构，Python负责智能数据分析与算法生成，Java负责数据持久化、业务逻辑和进程管理。

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

### 1. 用户管理模块 (`UserService`)
- 用户注册
- 用户登录验证
- 用户信息查询与更新
- 用户状态管理

### 2. 健康报告处理模块 (`HealthReportService`)
- 健康报告提交与存储
- BMI自动计算
- 健康数据历史查询
- 报告数据验证

### 3. 个性化计划生成模块 (`PlanGenerationService`)
- 基于健康报告生成30天月度计划
- 调用Python引擎进行智能分析
- 生成每日饮食建议
- 生成每日运动计划
- 批量保存每日任务

### 4. Python智能引擎模块
#### 规则引擎 (`rules_engine.py`)
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

#### 核心算法 (`health_engine.py`)
- 基础规则库 + AI动态调参的混合模型
- 健康报告分析与评估
- 个性化营养目标计算
- 智能餐食推荐（考虑饮食偏好与过敏史）
- 个性化运动计划生成
- 每日提醒内容生成

### 5. 每日提醒与通知模块 (`ReminderService`)
- 基于每日任务自动生成提醒
- 餐食提醒（早/午/晚）
- 运动提醒
- 注意事项提醒
- 每日鼓励信息
- 提醒状态管理

### 6. 数据分析与可视化接口 (`AnalyticsService`)
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

## 快速开始

### 环境要求
- Java 17+
- Python 3.8+
- Maven 3.6+

### 安装步骤

1. **克隆项目**
```bash
cd d:/深信服创意比赛/backend
```

2. **安装Python依赖**
```bash
cd python_engine
pip install -r requirements.txt
```

3. **初始化数据库**
数据库会在首次运行时自动初始化，包含测试数据。

### 运行项目

#### 1. 启动Java主程序
```bash
cd java_core
mvn compile exec:java -Dexec.mainClass="com.healthsmart.Main"
```

#### 2. 运行演示模式
```bash
mvn compile exec:java -Dexec.mainClass="com.healthsmart.Main" -Dexec.args="--demo"
```

演示模式会执行以下流程：
1. 用户注册
2. 用户登录
3. 提交健康报告
4. 生成月度个性化计划
5. 查看每日计划内容
6. 数据统计分析

#### 3. 单独测试Python引擎
```bash
cd python_engine
# 健康检查
python api_service.py health

# 生成计划示例
python api_service.py generate_plan '{"user_id":1,"report_month":"2024-01","height":175,"weight":70,"health_goal":"maintain_weight"}'
```

## API接口

### Java服务接口

#### 用户服务
```java
UserService userService = new UserService();
User user = userService.register(username, password, email, nickname);
user = userService.login(username, password);
```

#### 健康报告服务
```java
HealthReportService reportService = new HealthReportService();
HealthReport report = reportService.submitReport(healthReport);
List<HealthReport> reports = reportService.getReportsByUserId(userId);
```

#### 计划生成服务
```java
PlanGenerationService planService = new PlanGenerationService();
PersonalizedPlan plan = planService.generatePlanFromReport(healthReport);
List<DailyTask> tasks = planService.getPlanTasks(planId);
```

#### 提醒服务
```java
ReminderService reminderService = new ReminderService();
reminderService.generateRemindersForTask(userId, dailyTask);
List<Reminder> reminders = reminderService.getUnreadReminders(userId);
```

#### 分析服务
```java
AnalyticsService analyticsService = new AnalyticsService();
PlanCompletionStatistics stats = analyticsService.getPlanCompletionStats(planId);
```

### Python引擎API

**命令行接口**：
```bash
python api_service.py <action> <json_data>
```

**支持的操作**：
- `health` - 检查引擎健康状态
- `generate_plan` - 生成月度计划
- `daily_recommendation` - 生成每日推荐
- `analyze_health` - 健康数据分析

**示例**：
```bash
python api_service.py generate_plan '{"user_id":1,"height":175,"weight":70,"health_goal":"lose_weight"}'
```

**返回格式**（JSON）：
```json
{
  "success": true,
  "data": {
    "analysis": { /* 健康分析结果 */ },
    "plan": { /* 月度计划数据 */ }
  },
  "message": "计划生成成功"
}
```

## 数据库设计

### 核心表关系
```
users (1) ---- (N) health_reports (1) ---- (N) personalized_plans (1) ---- (N) daily_tasks
```

### 关键字段说明
- **HealthReport**: 存储30天周期所需的完整健康数据
- **PersonalizedPlan**: 存储计划的总体目标和参数
- **DailyTask**: 每日具体的饮食、运动和提醒内容

## 设计特点

1. **模块化架构**：清晰的分层设计（Model-DAO-Service）
2. **混合语言协作**：Python负责算法，Java负责业务与持久化
3. **进程间通信**：通过命令行JSON接口进行通信
4. **安全性**：密码加密存储（应用层面），数据本地化管理
5. **科学依据**：算法基于《中国居民膳食指南》和运动科学
6. **可扩展性**：预留AI接口，可接入大语言模型增强智能化

## 扩展建议

### 未来优化方向
1. **AI增强**：接入大语言模型实现更自然的交互和食谱生成
2. **多端同步**：扩展到移动App和Web端
3. **智能硬件对接**：集成手环、体脂秤等设备数据
4. **营养数据库集成**：接入专业营养成分API
5. **数据加密**：使用SQLCipher对SQLite数据库进行整体加密

## 注意事项

1. **Python环境**：确保Python在系统PATH中，或使用绝对路径
2. **数据库路径**：数据库默认生成在 `backend/data/healthsmart.db`
3. **编码问题**：所有文件使用UTF-8编码
4. **异常处理**：所有服务调用需处理SQLException

## 作者

HealthSmart Team - 深信服创意比赛项目组

## 版本历史

- **v1.0.0** (2024) - 初始版本
  - 实现核心健康管理功能
  - Python智能引擎集成
  - SQLite数据持久化
  - 数据分析与可视化接口

## 许可证

本项目为比赛参赛作品，仅供学习交流使用。