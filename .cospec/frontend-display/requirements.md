# 前端桌面显示模块需求规格说明书

## 1. 项目概述

### 1.1 背景
健康智护（HealthSmart）是一款基于Windows平台的个人健康动态管理系统，需要开发前端桌面显示模块来呈现用户交互界面。

### 1.2 目标
- 提供直观的用户界面
- 展示健康报告、个性化计划、数据分析等内容
- 实现与后端API的通信

### 1.3 范围
**包含：**
- 用户注册/登录界面
- 健康报告提交界面
- 个性化计划展示界面
- 数据统计分析展示

**不包含：**
- 后端业务逻辑实现
- 数据库操作

---

## 2. 功能需求

### 2.1 用户角色

| 角色名称 | 描述 | 权限 |
|----------|------|------|
| 访客 | 未注册用户 | 仅能查看登录/注册界面 |
| 注册用户 | 已注册并登录的用户 | 访问所有功能模块 |

### 2.2 功能清单

#### 2.2.1 用户认证模块

| 需求ID | 功能描述 | 优先级 | 依赖 |
|--------|----------|--------|------|
| FR-001 | 用户登录界面 | P0 | - |
| FR-002 | 用户注册界面（自动注册新用户） | P0 | - |
| FR-003 | 登录/注册数据验证 | P0 | - |
| FR-004 | 用户会话管理 | P0 | - |

#### 2.2.2 健康报告模块

| 需求ID | 功能描述 | 优先级 | 依赖 |
|--------|----------|--------|------|
| FR-005 | 健康报告提交表单 | P0 | 用户认证 |
| FR-006 | 身体指标输入（身高、体重、BMI等） | P0 | - |
| FR-007 | 生活习惯记录（睡眠、运动、饮食） | P0 | - |
| FR-008 | 健康报告提交与存储 | P0 | 后端API |
| FR-009 | 历史健康报告查看 | P1 | FR-005 |

#### 2.2.3 计划展示模块

| 需求ID | 功能描述 | 优先级 | 依赖 |
|--------|----------|--------|------|
| FR-010 | 月度计划概览 | P0 | 健康报告 |
| FR-011 | 每日任务详情展示 | P0 | 月度计划 |
| FR-012 | 饮食建议卡片 | P0 | 每日任务 |
| FR-013 | 运动计划卡片 | P0 | 每日任务 |
| FR-014 | 任务完成状态切换 | P1 | 每日任务 |
| FR-015 | 每日计划日历视图 | P1 | FR-010 |

#### 2.2.4 数据统计模块

| 需求ID | 功能描述 | 优先级 | 依赖 |
|--------|----------|--------|------|
| FR-016 | 计划完成率统计 | P1 | 每日任务 |
| FR-017 | 连续打卡天数展示 | P1 | 每日任务 |
| FR-018 | 营养摄入趋势图表 | P2 | 饮食记录 |
| FR-019 | 周/月度完成度趋势 | P2 | 统计数据 |

---

## 3. 用户故事

### 3.1 用户登录

**作为** 访客用户  
**我想要** 通过用户名和密码登录系统  
**以便于** 访问我的健康数据

**验收条件：**
- [ ] 显示登录表单（用户名、密码）
- [ ] 输入验证（必填项检查）
- [ ] 调用后端登录API
- [ ] 登录成功后跳转到主界面
- [ ] 登录失败显示错误提示

### 3.2 健康报告提交

**作为** 注册用户  
**我想要** 提交包含身体指标和生活习惯的健康报告  
**以便于** 系统生成个性化健康计划

**验收条件：**
- [ ] 显示健康报告表单
- [ ] 身体指标：身高(cm)、体重(kg)、年龄、性别
- [ ] 生活习惯：睡眠时长、运动频率、饮食偏好
- [ ] 表单验证
- [ ] 提交成功后显示确认信息
- [ ] 跳转到计划生成页面

### 3.3 查看每日计划

**作为** 注册用户  
**我想要** 查看今天的健康任务和计划  
**以便于** 按照计划执行健康活动

**验收条件：**
- [ ] 显示今日饮食建议（早/午/晚餐）
- [ ] 显示今日运动计划
- [ ] 显示注意事项
- [ ] 任务可标记完成
- [ ] 显示完成进度

### 3.4 数据统计

**作为** 注册用户  
**我想要** 查看我的健康数据统计  
**以便于** 了解我的健康状况变化

**验收条件：**
- [ ] 显示计划完成率
- [ ] 显示连续打卡天数
- [ ] 显示营养趋势图表
- [ ] 图表数据实时更新

---

## 4. 数据需求

### 4.1 数据实体

| 实体 | 描述 | 主要字段 |
|------|------|----------|
| User | 用户信息 | userId, username, nickname, email |
| HealthReport | 健康报告 | reportId, userId, height, weight, sleep, diet, exercise |
| DailyTask | 每日任务 | taskId, planId, date, mealPlan, exercisePlan, completed |
| PersonalizedPlan | 个性化计划 | planId, userId, startDate, dailyCalories |

### 4.2 数据流

```
用户操作 → 前端控制器 → REST API → 后端服务 → 数据库
                              ↓
                         响应数据 → 前端展示
```

---

## 5. 接口规范

### 5.1 API端点

| 端点 | 方法 | 功能 |
|------|------|------|
| /api/user?action=login | POST | 用户登录 |
| /api/user?action=register | POST | 用户注册 |
| /api/user?action=profile | POST | 获取用户资料 |
| /api/health-report?action=submit | POST | 提交健康报告 |
| /api/health-report?action=getLatest | POST | 获取最新报告 |
| /api/plan?action=getToday | POST | 获取今日计划 |
| /api/analytics?action=weekly | POST | 获取周统计 |

### 5.2 响应格式

```json
{
  "success": true,
  "message": "操作成功",
  "data": {
    // 具体数据
  }
}
```

---

## 6. 假设和依赖

### 6.1 假设

- 用户设备已安装Java 17运行环境
- 后端API服务器在localhost:8080运行
- 数据库已初始化完成

### 6.2 依赖

- 后端REST API服务（backend/java_core/src/main/java/com/healthsmart/ApiServer.java）
- 数据库连接（backend/java_core/src/main/java/com/healthsmart/util/DatabaseConnection.java）
- 用户服务（backend/java_core/src/main/java/com/healthsmart/service/UserService.java）
- 健康报告服务（backend/java_core/src/main/java/com/healthsmart/service/HealthReportService.java）
- 计划生成服务（backend/java_core/src/main/java/com/healthsmart/service/PlanGenerationService.java）
- 数据分析服务（backend/java_core/src/main/java/com/healthsmart/service/AnalyticsService.java）

---

## 7. 当前实现状态

### 7.1 已完成文件

| 文件 | 状态 | 说明 |
|------|------|------|
| `frontend/java_core/src/main/java/com/healthsmart/frontend/HealthSmartApp.java` | ✅ 完成 | 主应用程序入口 |
| `frontend/java_core/src/main/java/com/healthsmart/frontend/controller/UserController.java` | ✅ 完成 | 用户API控制器 |
| `frontend/java_core/src/main/java/com/healthsmart/frontend/controller/PlanController.java` | ✅ 完成 | 计划API控制器 |
| `frontend/java_core/src/main/java/com/healthsmart/frontend/controller/AnalyticsController.java` | ✅ 完成 | 分析API控制器 |
| `frontend/java_core/src/main/java/com/healthsmart/frontend/controller/HealthReportController.java` | ✅ 完成 | 健康报告控制器 |
| `frontend/java_core/src/main/java/com/healthsmart/frontend/server/ApiServer.java` | ✅ 完成 | REST API服务器 |
| `frontend/java_core/pom.xml` | ✅ 完成 | Maven配置 |
| `web/index.html` | ✅ 完成 | 主HTML页面 |
| `web/css/app.css` | ✅ 完成 | 样式文件 |
| `web/js/api.js` | ✅ 完成 | API通信模块 |

### 7.2 待验证

- [ ] Java编译通过
- [ ] API服务器正常启动
- [ ] 前端页面正常显示
- [ ] 前后端通信正常

---

## 8. 版本信息

| 版本 | 日期 | 描述 |
|------|------|------|
| 1.0.0 | 2024-01-06 | 初始需求文档 |

---

## 9. 附录

### 9.1 相关文档

- [深信服比赛项目简介.txt](../深信服比赛项目简介.txt) - 项目原始需求
- [README.md](../README.md) - 项目主文档
- [backend/README.md](../backend/README.md) - 后端技术文档