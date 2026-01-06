/**
 * 健康智护系统 - 前端API模块
 * HealthSmart Frontend API Module
 * 
 * 提供与后端Java服务的通信接口
 */

// API 基地址 - 连接后端Java REST API服务器
const API_BASE = 'http://localhost:8080/api';
const USE_MOCK_DATA = false; // 设为true使用模拟数据，false使用真实后端API

/**
 * API客户端类
 */
const HealthSmartAPI = {
    /**
     * 用户登录/注册（统一认证）
     * 自动识别是新用户还是老用户，新用户自动注册
     */
    async auth(username, password, nickname, email) {
        if (USE_MOCK_DATA) {
            return this.mockAuth(username, password, nickname, email);
        }
        
        try {
            const response = await fetch(`${API_BASE}/user/auth`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ username, password, nickname, email })
            });
            return await response.json();
        } catch (error) {
            console.error('认证请求失败:', error);
            return { success: false, message: '网络请求失败' };
        }
    },

    /**
     * 用户登录
     */
    async login(username, password) {
        if (USE_MOCK_DATA) {
            return this.mockLogin(username, password);
        }
        
        try {
            const response = await fetch(`${API_BASE}/user/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ username, password })
            });
            return await response.json();
        } catch (error) {
            console.error('登录请求失败:', error);
            return { success: false, message: '网络请求失败' };
        }
    },

    /**
     * 用户注册
     */
    async register(username, password, email, nickname) {
        if (USE_MOCK_DATA) {
            return this.mockRegister(username, password, email, nickname);
        }
        
        try {
            const response = await fetch(`${API_BASE}/user/register`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ username, password, email, nickname })
            });
            return await response.json();
        } catch (error) {
            console.error('注册请求失败:', error);
            return { success: false, message: '网络请求失败' };
        }
    },

    /**
     * 获取用户信息
     */
    async getUserProfile(userId) {
        if (USE_MOCK_DATA) {
            return this.mockGetUserProfile(userId);
        }
        
        try {
            const response = await fetch(`${API_BASE}/user/${userId}`);
            return await response.json();
        } catch (error) {
            console.error('获取用户信息失败:', error);
            return { success: false, message: '网络请求失败' };
        }
    },

    /**
     * 更新用户信息
     */
    async updateProfile(userData) {
        if (USE_MOCK_DATA) {
            return { success: true, message: '更新成功' };
        }
        
        try {
            const response = await fetch(`${API_BASE}/user/${userData.userId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(userData)
            });
            return await response.json();
        } catch (error) {
            console.error('更新用户信息失败:', error);
            return { success: false, message: '网络请求失败' };
        }
    },

    /**
     * 提交健康报告
     */
    async submitHealthReport(reportData) {
        if (USE_MOCK_DATA) {
            return this.mockSubmitReport(reportData);
        }
        
        try {
            const response = await fetch(`${API_BASE}/health-report`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(reportData)
            });
            return await response.json();
        } catch (error) {
            console.error('提交报告失败:', error);
            return { success: false, message: '网络请求失败' };
        }
    },

    /**
     * 获取用户健康报告
     */
    async getHealthReports(userId) {
        if (USE_MOCK_DATA) {
            return this.mockGetReports(userId);
        }
        
        try {
            const response = await fetch(`${API_BASE}/health-report/user/${userId}`);
            return await response.json();
        } catch (error) {
            console.error('获取报告失败:', error);
            return { success: false, message: '网络请求失败' };
        }
    },

    /**
     * 生成个性化计划
     */
    async generatePlan(userId, reportId) {
        if (USE_MOCK_DATA) {
            return { success: true, message: '计划生成成功' };
        }
        
        try {
            const response = await fetch(`${API_BASE}/plan/generate`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ userId, reportId })
            });
            return await response.json();
        } catch (error) {
            console.error('生成计划失败:', error);
            return { success: false, message: '网络请求失败' };
        }
    },

    /**
     * 获取每日计划
     */
    async getDailyPlan(userId, date) {
        if (USE_MOCK_DATA) {
            return this.mockGetDailyPlan(userId, date);
        }
        
        try {
            const response = await fetch(`${API_BASE}/plan/daily?userId=${userId}&date=${date}`);
            return await response.json();
        } catch (error) {
            console.error('获取每日计划失败:', error);
            return { success: false, message: '网络请求失败' };
        }
    },

    /**
     * 获取仪表盘数据
     */
    async getDashboardData(userId) {
        if (USE_MOCK_DATA) {
            return this.mockGetDashboardData(userId);
        }
        
        try {
            const response = await fetch(`${API_BASE}/analytics/dashboard?userId=${userId}`);
            return await response.json();
        } catch (error) {
            console.error('获取仪表盘数据失败:', error);
            return { success: false, message: '网络请求失败' };
        }
    },

    /**
     * 获取统计数据
     */
    async getStatistics(userId, period) {
        if (USE_MOCK_DATA) {
            return this.mockGetStatistics(userId, period);
        }
        
        try {
            const response = await fetch(`${API_BASE}/analytics/statistics?userId=${userId}&period=${period}`);
            return await response.json();
        } catch (error) {
            console.error('获取统计数据失败:', error);
            return { success: false, message: '网络请求失败' };
        }
    },

    /**
     * 获取提醒列表
     */
    async getReminders(userId, filter) {
        if (USE_MOCK_DATA) {
            return this.mockGetReminders(userId, filter);
        }
        
        try {
            const response = await fetch(`${API_BASE}/reminders?userId=${userId}&filter=${filter}`);
            return await response.json();
        } catch (error) {
            console.error('获取提醒失败:', error);
            return { success: false, message: '网络请求失败' };
        }
    },

    /**
     * 完成每日任务
     */
    async completeTask(taskId, data) {
        if (USE_MOCK_DATA) {
            return { success: true, message: '任务已完成' };
        }
        
        try {
            const response = await fetch(`${API_BASE}/task/${taskId}/complete`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data)
            });
            return await response.json();
        } catch (error) {
            console.error('完成任务失败:', error);
            return { success: false, message: '网络请求失败' };
        }
    },

    // ============ Mock Data Methods ============

    /**
     * Mock统一认证（登录或自动注册）
     */
    mockAuth(username, password, nickname, email) {
        return new Promise((resolve) => {
            setTimeout(() => {
                if (!username || username.length < 2) {
                    resolve({ success: false, message: '用户名至少需要2个字符' });
                    return;
                }
                if (!password || password.length < 3) {
                    resolve({ success: false, message: '密码至少需要3个字符' });
                    return;
                }
                
                // 检查是否已存在用户
                const savedUsers = JSON.parse(localStorage.getItem('healthsmart_users') || '[]');
                const existingUser = savedUsers.find(u => u.username === username);
                
                if (existingUser) {
                    // 老用户登录
                    resolve({
                        success: true,
                        data: {
                            userId: existingUser.userId,
                            username: existingUser.username,
                            nickname: existingUser.nickname,
                            email: existingUser.email,
                            isNewUser: false,
                            message: '登录成功，欢迎回来！'
                        }
                    });
                } else {
                    // 新用户自动注册
                    const newUser = {
                        userId: Date.now(),
                        username: username,
                        nickname: nickname || username,
                        email: email || username + '@example.com',
                        isNewUser: true,
                        message: '新用户自动注册成功，欢迎使用健康智护！'
                    };
                    savedUsers.push(newUser);
                    localStorage.setItem('healthsmart_users', JSON.stringify(savedUsers));
                    
                    resolve({
                        success: true,
                        data: newUser
                    });
                }
            }, 500);
        });
    },

    /**
     * Mock登录
     */
    mockLogin(username, password) {
        return new Promise((resolve) => {
            setTimeout(() => {
                if (username && password.length >= 3) {
                    resolve({
                        success: true,
                        data: {
                            userId: 1,
                            username: username,
                            nickname: username,
                            email: username + '@example.com'
                        }
                    });
                } else {
                    resolve({ success: false, message: '用户名或密码错误' });
                }
            }, 500);
        });
    },

    /**
     * Mock注册
     */
    mockRegister(username, password, email, nickname) {
        return new Promise((resolve) => {
            setTimeout(() => {
                resolve({
                    success: true,
                    data: {
                        userId: Math.floor(Math.random() * 1000),
                        username: username,
                        nickname: nickname,
                        email: email
                    }
                });
            }, 500);
        });
    },

    /**
     * Mock获取用户资料
     */
    mockGetUserProfile(userId) {
        return new Promise((resolve) => {
            setTimeout(() => {
                resolve({
                    success: true,
                    data: {
                        userId: userId,
                        username: 'demo_user',
                        nickname: '演示用户',
                        email: 'demo@healthsmart.com',
                        phone: '13800138000',
                        gender: 'male',
                        birthDate: '1990-01-01'
                    }
                });
            }, 300);
        });
    },

    /**
     * Mock提交健康报告
     */
    mockSubmitReport(reportData) {
        return new Promise((resolve) => {
            setTimeout(() => {
                resolve({
                    success: true,
                    data: {
                        reportId: Math.floor(Math.random() * 1000),
                        message: '报告提交成功',
                        bmi: (reportData.weight / ((reportData.height / 100) ** 2)).toFixed(1)
                    }
                });
            }, 1000);
        });
    },

    /**
     * Mock获取报告列表
     */
    mockGetReports(userId) {
        return new Promise((resolve) => {
            setTimeout(() => {
                resolve({
                    success: true,
                    data: [
                        { reportId: 1, month: '2024-01', weight: 70, bmi: 22.9 },
                        { reportId: 2, month: '2024-02', weight: 69, bmi: 22.5 }
                    ]
                });
            }, 300);
        });
    },

    /**
     * Mock获取每日计划
     */
    mockGetDailyPlan(userId, date) {
        return new Promise((resolve) => {
            setTimeout(() => {
                const hour = new Date().getHours();
                let mealBreakfast = '燕麦粥 + 鸡蛋 + 牛奶';
                let mealLunch = '糙米饭 + 鸡胸肉 + 蔬菜沙拉';
                let mealDinner = '清蒸鱼 + 炒时蔬 + 紫薯';
                let mealSnacks = '坚果 + 水果';

                // 根据时间动态调整显示内容
                if (hour < 9) {
                    mealBreakfast = '已吃完：燕麦粥、鸡蛋、牛奶 ✓';
                    mealLunch = '待午餐';
                } else if (hour < 14) {
                    mealLunch = '已吃完：糙米饭、鸡胸肉、蔬菜沙拉 ✓';
                } else if (hour < 18) {
                    mealDinner = '待晚餐';
                }

                resolve({
                    success: true,
                    data: {
                        date: date,
                        dailyCalorieGoal: 2000,
                        mealBreakfast: mealBreakfast,
                        mealLunch: mealLunch,
                        mealDinner: mealDinner,
                        mealSnacks: mealSnacks,
                        exerciseType: '有氧运动',
                        exerciseDuration: 45,
                        exerciseIntensity: 'medium',
                        exerciseDescription: '慢跑或快走45分钟，有助于提升心肺功能和脂肪燃烧。建议在饭后1小时进行。',
                        reminders: [
                            '记得多喝水，每天至少8杯',
                            '保持良好坐姿，避免久坐',
                            '今天精神状态不错，继续保持！'
                        ]
                    }
                });
            }, 500);
        });
    },

    /**
     * Mock获取仪表盘数据
     */
    mockGetDashboardData(userId) {
        return new Promise((resolve) => {
            setTimeout(() => {
                resolve({
                    success: true,
                    data: {
                        calorie: {
                            current: 1650,
                            target: 2000
                        },
                        macros: {
                            protein: 85,
                            carbs: 220,
                            fat: 55
                        },
                        streak: {
                            current: 7,
                            total: 45,
                            best: 15
                        },
                        healthMetrics: {
                            bmi: 22.5,
                            weight: 70,
                            sleep: 7.5,
                            exercise: 45
                        },
                        todayTasks: [
                            { taskId: 1, title: '早餐：燕麦粥+鸡蛋', type: 'meal', completed: true, time: '07:00-08:00' },
                            { taskId: 2, title: '午餐：糙米饭+鸡胸肉', type: 'meal', completed: true, time: '12:00-13:00' },
                            { taskId: 3, title: '运动：慢跑45分钟', type: 'exercise', completed: false, time: '17:00-18:00' },
                            { taskId: 4, title: '晚餐：清蒸鱼+蔬菜', type: 'meal', completed: false, time: '18:30-19:30' }
                        ]
                    }
                });
            }, 500);
        });
    },

    /**
     * Mock获取统计数据
     */
    mockGetStatistics(userId, period) {
        return new Promise((resolve) => {
            setTimeout(() => {
                const days = period === 'week' ? 7 : period === 'month' ? 30 : 90;
                const dailyCompletion = [];
                
                for (let i = 0; i < days; i++) {
                    const completion = Math.floor(Math.random() * 40) + 60;
                    dailyCompletion.push({
                        label: `${i + 1}日`,
                        completion: completion
                    });
                }

                resolve({
                    success: true,
                    data: {
                        completionRate: 78.5,
                        plannedCalories: 14000,
                        actualCalories: 13200,
                        calorieDeviation: -800,
                        plannedExercise: 315,
                        actualExercise: 280,
                        dailyCompletion: dailyCompletion.slice(-7)
                    }
                });
            }, 500);
        });
    },

    /**
     * Mock获取提醒列表
     */
    mockGetReminders(userId, filter) {
        return new Promise((resolve) => {
            setTimeout(() => {
                const reminders = [
                    { id: 1, type: 'meal', title: '早餐提醒', content: '该吃早餐了，均衡营养开启美好一天！', time: '07:00', unread: true },
                    { id: 2, type: 'exercise', title: '运动提醒', content: '今日运动计划：慢跑45分钟', time: '17:00', unread: true },
                    { id: 3, type: 'encouragement', title: '鼓励信息', content: '你已经坚持7天了！太棒了！', time: '08:00', unread: false },
                    { id: 4, type: 'meal', title: '午餐提醒', content: '午餐时间到了，记得荤素搭配', time: '12:00', unread: true },
                    { id: 5, type: 'meal', title: '晚餐提醒', content: '晚餐建议清淡少量', time: '18:30', unread: false }
                ];

                let filtered = reminders;
                if (filter !== 'all') {
                    if (filter === 'unread') {
                        filtered = reminders.filter(r => r.unread);
                    } else {
                        filtered = reminders.filter(r => r.type === filter);
                    }
                }

                resolve({
                    success: true,
                    data: filtered
                });
            }, 300);
        });
    }
};

// 导出API模块
window.HealthSmartAPI = HealthSmartAPI;