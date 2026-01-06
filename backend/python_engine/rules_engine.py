"""
健康智护系统 - 规则引擎模块
Basic Rule Engine for HealthSmart System

该模块负责：
- 健康指标的安全范围规则
- 基础营养摄入推荐规则
- 运动计划基础规则
- 基于《中国居民膳食指南》的科学依据
"""

from .models import BMICategory, HealthGoal, ExerciseIntensity


class NutritionRuleBase:
    """营养规则基础类"""
    
    # 中国居民膳食指南推荐值（2022版）
    ADULT_BASE_CALORIES = {
        'male': {
            'sedentary': 2000,  # 久坐
            'low_active': 2400,  # 低活跃度
            'active': 2800,  # 活跃
            'very_active': 3200  # 高活跃度
        },
        'female': {
            'sedentary': 1600,
            'low_active': 2000,
            'active': 2400,
            'very_active': 2800
        }
    }
    
    # 营养素推荐摄入比例
    MACRONUTRIENT_RATIOS = {
        'protein': 0.15,  # 蛋白质占比
        'carbs': 0.50,    # 碳水化合物占比
        'fat': 0.35       # 脂肪占比
    }
    
    # 餐食热量分配比例
    MEAL_DISTRIBUTION = {
        'breakfast': 0.30,  # 早餐
        'lunch': 0.40,      # 午餐
        'dinner': 0.25,     # 晚餐
        'snack': 0.05       # 加餐
    }
    
    # 宏量营养素能量系数 (kcal/g)
    CALORIES_PER_GRAM = {
        'protein': 4,
        'carbs': 4,
        'fat': 9
    }
    
    # 身材调整系数
    WEIGHT_ADJUSTMENT = {
        'lose_weight': -500,  # 减重：每日减少500kcal
        'maintain_weight': 0,  # 保持
        'gain_weight': 300,    # 增重：每日增加300kcal
        'build_muscle': 500   # 增肌：每日增加500kcal
    }
    
    @staticmethod
    def calculate_bmr(weight: float, height: float, age: int, gender: str) -> float:
        """
        计算基础代谢率 (BMR)
        使用 Mifflin-St Jeor 公式
        
        参数:
            weight: 体重 (kg)
            height: 身高 (cm)
            age: 年龄
            gender: 性别 ('male' 或 'female')
        
        返回:
            基础代谢率 (kcal/day)
        """
        if gender == 'male':
            bmr = (10 * weight) + (6.25 * height) - (5 * age) + 5
        else:
            bmr = (10 * weight) + (6.25 * height) - (5 * age) - 161
        
        return round(bmr, 2)
    
    @staticmethod
    def calculate_daily_calories(bmr: float, activity_level: str) -> int:
        """
        根据活动水平计算每日总能量消耗
        
        参数:
            bmr: 基础代谢率
            activity_level: 活动水平 ('sedentary', 'low_active', 'active', 'very_active')
        
        返回:
            每日总热量需求 (kcal)
        """
        activity_factors = {
            'sedentary': 1.2,      # 久坐，几乎不运动
            'low_active': 1.375,   # 轻度活动，每周1-3天运动
            'active': 1.55,        # 中度活动，每周3-5天运动
            'very_active': 1.725   # 重度活动，每周6-7天运动
        }
        
        factor = activity_factors.get(activity_level, 1.2)
        total_calories = bmr * factor
        return int(round(total_calories))
    
    @staticmethod
    def adjust_calories_for_goal(calories: int, goal: str) -> int:
        """根据健康目标调整热量摄入"""
        adjustment = NutritionRuleBase.WEIGHT_ADJUSTMENT.get(goal, 0)
        adjusted = calories + adjustment
        # 确保不低于安全下限
        return max(adjusted, 1200)
    
    @staticmethod
    def calculate_nutrient_targets(calories: int, ratio_override: dict = None) -> dict:
        """
        计算营养素目标
        
        参数:
            calories: 每日热量目标
            ratio_override: 自定义比例覆盖
        
        返回:
            {'protein': float, 'carbs': float, 'fat': float, 'fiber': float}
        """
        ratios = NutritionRuleBase.MACRONUTRIENT_RATIOS.copy()
        if ratio_override:
            ratios.update(ratio_override)
        
        protein = round((calories * ratios['protein']) / NutritionRuleBase.CALORIES_PER_GRAM['protein'], 2)
        carbs = round((calories * ratios['carbs']) / NutritionRuleBase.CALORIES_PER_GRAM['carbs'], 2)
        fat = round((calories * ratios['fat']) / NutritionRuleBase.CALORIES_PER_GRAM['fat'], 2)
        
        # 纤维推荐摄入量：每1000kcal对应14g
        fiber = round((calories / 1000) * 14, 2)
        
        return {
            'protein': protein,
            'carbs': carbs,
            'fat': fat,
            'fiber': fiber
        }
    
    @staticmethod
    def get_meal_calorie_distribution(daily_calories: int) -> dict:
        """获取各餐次的热量分配"""
        return {
            'breakfast': int(daily_calories * NutritionRuleBase.MEAL_DISTRIBUTION['breakfast']),
            'lunch': int(daily_calories * NutritionRuleBase.MEAL_DISTRIBUTION['lunch']),
            'dinner': int(daily_calories * NutritionRuleBase.MEAL_DISTRIBUTION['dinner']),
            'snack': int(daily_calories * NutritionRuleBase.MEAL_DISTRIBUTION['snack'])
        }


class ExerciseRuleBase:
    """运动规则基础类"""
    
    # 中国居民膳食指南推荐运动量
    EXERCISE_RECOMMENDATIONS = {
        'sedentary': {
            'frequency': 3,  # 每周3次
            'duration_per_session': 30,  # 每次30分钟
            'intensity': 'low'
        },
        'low_active': {
            'frequency': 3,
            'duration_per_session': 40,
            'intensity': 'low'
        },
        'active': {
            'frequency': 4,
            'duration_per_session': 45,
            'intensity': 'medium'
        },
        'very_active': {
            'frequency': 5,
            'duration_per_session': 60,
            'intensity': 'high'
        }
    }
    
    # 不同健康目标的运动方案调整
    GOAL_EXERCISE_ADJUSTMENTS = {
        'lose_weight': {
            'frequency_bonus': 1,
            'duration_bonus': 15,
            'type_preference': ['有氧运动', 'HIIT']
        },
        'build_muscle': {
            'frequency_bonus': 0,
            'duration_bonus': 0,
            'type_preference': ['力量训练', '抗阻力训练']
        },
        'improve_sleep': {
            'frequency_bonus': 0,
            'duration_bonus': 0,
            'type_preference': ['轻度有氧', '瑜伽', '拉伸']
        },
        'general_health': {
            'frequency_bonus': 0,
            'duration_bonus': 0,
            'type_preference': ['综合训练']
        }
    }
    
    # 运动类型定义
    EXERCISE_TYPES = {
        '有氧运动': {
            'examples': ['慢跑', '快走', '游泳', '骑自行车', '跳绳'],
            'calories_per_minute': 8,  # 估算值
            'intensity_preference': ['medium', 'high']
        },
        '力量训练': {
            'examples': ['哑铃训练', '俯卧撑', '深蹲', '平板支撑', '引体向上'],
            'calories_per_minute': 5,
            'intensity_preference': ['medium', 'high']
        },
        '柔韧性训练': {
            'examples': ['瑜伽', '拉伸', '普拉提'],
            'calories_per_minute': 3,
            'intensity_preference': ['low']
        },
        'HIIT': {
            'examples': ['高强度间歇训练', '波比跳', '登山者'],
            'calories_per_minute': 12,
            'intensity_preference': ['high']
        }
    }
    
    @staticmethod
    def estimate_calories_burned(exercise_type: str, duration: int, weight: float, intensity: str) -> int:
        """
        估算运动消耗卡路里
        
        参数:
            exercise_type: 运动类型
            duration: 持续时间（分钟）
            weight: 体重 (kg)
            intensity: 强度 ('low', 'medium', 'high')
        
        返回:
            消耗的卡路里
        """
        base_calories_per_min = {
            '有氧运动': 7,
            '力量训练': 5,
            '柔韧性训练': 3,
            'HIIT': 10
        }.get(exercise_type, 5)
        
        # 强度系数
        intensity_factor = {
            'low': 0.8,
            'medium': 1.0,
            'high': 1.3
        }.get(intensity, 1.0)
        
        # 体重系数（以60kg为基准）
        weight_factor = weight / 60
        
        # 运动前10分钟通常热身，不计入
        effective_duration = duration
        if duration > 10:
            effective_duration = duration * 0.9
        
        calories_burned = int(base_calories_per_min * effective_duration * intensity_factor * weight_factor)
        return max(calories_burned, 50)  # 最少消耗50kcal
    
    @staticmethod
    def get_recommended_exercise_type(weight_goal: str, current_fitness: str) -> str:
        """
        根据目标和当前体能推荐运动类型
        
        参数:
            weight_goal: 减重/增重/保持
            current_fitness: 当前体能水平
        
        返回:
            推荐的运动类型
        """
        if weight_goal == 'lose_weight':
            return '有氧运动'
        elif weight_goal == 'build_muscle':
            return '力量训练'
        else:
            return '综合训练'


class HealthSafetyRules:
    """健康安全规则"""
    
    # BMI安全范围
    BMI_SAFE_RANGES = {
        'min': 18.5,
        'max': 24
    }
    
    # 血压安全范围
    BLOOD_PRESSURE_RANGES = {
        'systolic': {'min': 90, 'max': 140},
        'diastolic': {'min': 60, 'max': 90}
    }
    
    # 睡眠推荐时长（小时）
    SLEEP_RECOMMENDATIONS = {
        'min': 7,
        'max': 9,
        'ideal': 8
    }
    
    @staticmethod
    def get_bmi_category(bmi: float) -> str:
        """获取BMI分类"""
        if bmi < 18.5:
            return 'underweight'
        elif 18.5 <= bmi < 24:
            return 'normal'
        elif 24 <= bmi < 28:
            return 'overweight'
        else:
            return 'obese'
    
    @staticmethod
    def is_bmi_safe(bmi: float) -> bool:
        """检查BMI是否在安全范围内"""
        return HealthSafetyRules.BMI_SAFE_RANGES['min'] <= bmi <= HealthSafetyRules.BMI_SAFE_RANGES['max']
    
    @staticmethod
    def is_blood_pressure_safe(systolic: int, diastolic: int) -> bool:
        """检查血压是否安全"""
        systolic_safe = HealthSafetyRules.BLOOD_PRESSURE_RANGES['systolic']['min'] <= systolic <= HealthSafetyRules.BLOOD_PRESSURE_RANGES['systolic']['max']
        diastolic_safe = HealthSafetyRules.BLOOD_PRESSURE_RANGES['diastolic']['min'] <= diastolic <= HealthSafetyRules.BLOOD_PRESSURE_RANGES['diastolic']['max']
        return systolic_safe and diastolic_safe
    
    @staticmethod
    def is_sleep_adequate(hours: float) -> bool:
        """检查睡眠时长是否充足"""
        return HealthSafetyRules.SLEEP_RECOMMENDATIONS['min'] <= hours <= HealthSafetyRules.SLEEP_RECOMMENDATIONS['max']
    
    @staticmethod
    def get_health_warning(health_report) -> list:
        """
        生成健康警告
        
        返回:
            警告信息列表
        """
        warnings = []
        
        bmi = health_report.calculate_bmi()
        if not HealthSafetyRules.is_bmi_safe(bmi):
            category = HealthSafetyRules.get_bmi_category(bmi)
            warnings.append(f"BMI {bmi} 属于 {category} 范围，建议咨询专业医生")
        
        if health_report.systolic_pressure and health_report.diastolic_pressure:
            if not HealthSafetyRules.is_blood_pressure_safe(health_report.systolic_pressure, health_report.diastolic_pressure):
                warnings.append(f"血压 {health_report.systolic_pressure}/{health_report.diastolic_pressure} mmHg 异常，请关注")
        
        if health_report.sleep_hours_avg:
            if not HealthSafetyRules.is_sleep_adequate(health_report.sleep_hours_avg):
                warnings.append(f"每日睡眠 {health_report.sleep_hours_avg} 小时，建议调整到7-9小时")
        
        if health_report.smoking:
            warnings.append("吸烟会影响身体健康，建议戒烟")
        
        return warnings