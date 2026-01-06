"""
健康智护系统 - 智能引擎核心模块
Intelligent Health Planning Engine

该模块负责：
- 基础规则库 + AI动态调参的混合模型
- 基于月度健康报告生成长达30天计划
- 每日饮食建议生成
- 每日运动计划生成
- 提醒内容生成
"""

import json
from datetime import datetime, timedelta
from typing import List, Dict, Optional
from .models import (
    HealthReport, NutritionTarget, MealSuggestion, 
    ExercisePlan, DailyPlan, MonthlyPlan, ExerciseIntensity
)
from .rules_engine import (
    NutritionRuleBase, ExerciseRuleBase, HealthSafetyRules,
    get_nutrients_for_meal
)


class MealDatabase:
    """餐食数据库"""
    
    # 早餐选项（按卡路里分类）
    BREAKFAST_OPTIONS = {
        'low': [
            {'foods': ['燕麦粥', '鸡蛋', '圣女果'], 'calories': 300, 'protein': 15, 'carbs': 40, 'fat': 10},
            {'foods': ['全麦面包', '低脂牛奶', '香蕉'], 'calories': 320, 'protein': 12, 'carbs': 55, 'fat': 8},
            {'foods': ['蔬菜沙拉', '水煮蛋', '无糖豆浆'], 'calories': 280, 'protein': 14, 'carbs': 30, 'fat': 12}
        ],
        'medium': [
            {'foods': ['燕麦粥', '鸡蛋', '坚果', '水果沙拉'], 'calories': 420, 'protein': 18, 'carbs': 50, 'fat': 15},
            {'foods': ['全麦面包', '煎蛋', '希腊酸奶', '蓝莓'], 'calories': 450, 'protein': 20, 'carbs': 55, 'fat': 16},
            {'foods': ['蔬菜煎蛋', '全麦吐司', '牛奶', '牛油果'], 'calories': 440, 'protein': 18, 'carbs': 48, 'fat': 18}
        ],
        'high': [
            {'foods': ['燕麦粥', '鸡蛋(2个)', '坚果', '黄油面包', '水果'], 'calories': 550, 'protein': 22, 'carbs': 60, 'fat': 20},
            {'foods': ['培根鸡蛋三明治', '酸奶', '水果沙拉', '拿铁'], 'calories': 580, 'protein': 25, 'carbs': 65, 'fat': 22},
            {'foods': ['蔬菜煎蛋', '培根', '全麦吐司', '牛奶', '坚果'], 'calories': 600, 'protein': 24, 'carbs': 62, 'fat': 24}
        ]
    }
    
    # 午餐选项
    LUNCH_OPTIONS = {
        'low': [
            {'foods': ['鸡胸肉沙拉', '全麦面包', '蔬菜汤'], 'calories': 450, 'protein': 35, 'carbs': 35, 'fat': 15},
            {'foods': ['糙米饭', '清蒸鱼', '时令蔬菜'], 'calories': 480, 'protein': 32, 'carbs': 55, 'fat': 10},
            {'foods': ['藜麦沙拉', '水煮虾', '西兰花'], 'calories': 460, 'protein': 34, 'carbs': 40, 'fat': 12}
        ],
        'medium': [
            {'foods': ['糙米饭', '煎鸡胸肉', '混合蔬菜', '番茄汤'], 'calories': 600, 'protein': 40, 'carbs': 65, 'fat': 18},
            {'foods': ['意面', '虾仁', '芦笋', '罗勒酱'], 'calories': 620, 'protein': 38, 'carbs': 70, 'fat': 16},
            {'foods': ['红薯', '烤鸡腿', '彩色蔬菜', '蘑菇汤'], 'calories': 640, 'protein': 42, 'carbs': 68, 'fat': 20}
        ],
        'high': [
            {'foods': ['糙米饭(2份)', '煎牛排', '时令蔬菜', '奶油汤'], 'calories': 780, 'protein': 50, 'carbs': 85, 'fat': 25},
            {'foods': ['意面', '虾仁配牛排', '芦笋', '芝士'], 'calories': 820, 'protein': 48, 'carbs': 90, 'fat': 28},
            {'foods': ['杂粮饭', '烤羊排', '混合蔬菜', '酱汁'], 'calories': 850, 'protein': 52, 'carbs': 95, 'fat': 30}
        ]
    }
    
    # 晚餐选项
    DINNER_OPTIONS = {
        'low': [
            {'foods': ['蔬菜汤', '清蒸鸡胸肉', '小份水果'], 'calories': 350, 'protein': 28, 'carbs': 25, 'fat': 10},
            {'foods': ['蔬菜沙拉', '水煮虾', '无糖酸奶'], 'calories': 330, 'protein': 26, 'carbs': 20, 'fat': 12},
            {'foods': ['蒸蛋羹', '蔬菜', '少量水果'], 'calories': 300, 'protein': 22, 'carbs': 18, 'fat': 11}
        ],
        'medium': [
            {'foods': ['蔬菜汤', '煎鸡腿肉', '糙米饭小份', '蔬菜'], 'calories': 480, 'protein': 35, 'carbs': 40, 'fat': 14},
            {'foods': ['沙拉', '烤鱼', '蒸薯类', '蔬菜'], 'calories': 500, 'protein': 38, 'carbs': 45, 'fat': 12},
            {'foods': ['清蒸鱼', '炒蔬菜', '杂粮粥', '水果'], 'calories': 460, 'protein': 34, 'carbs': 48, 'fat': 10}
        ],
        'high': [
            {'foods': ['蔬菜汤', '烤牛排', '糙米饭', '混合蔬菜'], 'calories': 650, 'protein': 45, 'carbs': 60, 'fat': 22},
            {'foods': ['沙拉', '三文鱼', '意面', '蔬菜'], 'calories': 680, 'protein': 42, 'carbs': 65, 'fat': 20},
            {'foods': ['清蒸鱼', '炒猪排', '玉米', '蔬菜'], 'calories': 700, 'protein': 48, 'carbs': 70, 'fat': 18}
        ]
    }
    
    # 加餐选项
    SNACK_OPTIONS = {
        'low': [
            {'foods': ['苹果'], 'calories': 70, 'protein': 0, 'carbs': 19, 'fat': 0},
            {'foods': ['酸奶小杯'], 'calories': 80, 'protein': 8, 'carbs': 10, 'fat': 2},
            {'foods': ['坚果少量'], 'calories': 60, 'protein': 2, 'carbs': 3, 'fat': 5}
        ],
        'medium': [
            {'foods': ['香蕉'], 'calories': 100, 'protein': 1, 'carbs': 27, 'fat': 0},
            {'foods': ['酸奶中杯', '草莓'], 'calories': 140, 'protein': 10, 'carbs': 20, 'fat': 3},
            {'foods': ['坚果', '黑巧克力'], 'calories': 130, 'protein': 4, 'carbs': 12, 'fat': 8}
        ],
        'high': [
            {'foods': ['水果沙拉'], 'calories': 150, 'protein': 1, 'carbs': 35, 'fat': 1},
            {'foods': ['酸奶大杯', '坚果', '蜂蜜'], 'calories': 180, 'protein': 12, 'carbs': 25, 'fat': 6},
            {'foods': ['能量棒', '水果'], 'calories': 200, 'protein': 6, 'carbs': 30, 'fat': 7}
        ]
    }
    
    @classmethod
    def get_meal_option(cls, meal_type: str, calorie_level: str) -> dict:
        """获取餐食选项"""
        options_map = {
            'breakfast': cls.BREAKFAST_OPTIONS,
            'lunch': cls.LUNCH_OPTIONS,
            'dinner': cls.DINNER_OPTIONS,
            'snack': cls.SNACK_OPTIONS
        }
        
        options = options_map.get(meal_type, {}).get(calorie_level, [])
        if not options:
            return {}
        
        # 简单轮询选择（实际应用中可以使用更智能的推荐算法）
        import random
        return random.choice(options)
    
    @classmethod
    def adjust_for_preferences(cls, meal: dict, preferences: str, allergies: str = None) -> dict:
        """根据饮食偏好和过敏史调整餐食"""
        foods = meal.get('foods', [])
        
        # 简单的偏好过滤逻辑
        if preferences:
            if '素食' in preferences:
                # 移除肉类
                meat_keywords = ['肉', '鸡', '蛋', '虾', '鱼']
                foods = [f for f in foods if not any(k in f for k in meat_keywords)]
        
        if allergies:
            allergy_keywords = [item.strip() for item in allergies.split(',')]
            foods = [f for f in foods if not any(k in f for k in allergy_keywords)]
        
        adjusted_meal = meal.copy()
        adjusted_meal['foods'] = foods
        
        return adjusted_meal


class ExerciseDatabase:
    """运动数据库"""
    
    EXERCISE_TEMPLATES = {
        '有氧运动': {
            '低强度': [
                {'name': '慢跑', 'description': '在平地上进行慢跑，保持均匀呼吸', 'instructions': ['热身5分钟', '慢跑20分钟', '拉伸5分钟']},
                {'name': '快走', 'description': '保持快速的步伐行走，摆臂自然', 'instructions': ['快速行走25分钟', '放松5分钟']},
                {'name': '骑自行车', 'description': '以适中的速度骑行', 'instructions': ['骑行20-30分钟', '注意调整呼吸']}
            ],
            '中强度': [
                {'name': '慢跑', 'description': '提高速度，适当加入坡度', 'instructions': ['热身5分钟', '慢跑30分钟', '拉伸5分钟']},
                {'name': '游泳', 'description': '连续游泳，保持节奏', 'instructions': ['热身泳5分钟', '自由泳/蛙泳25分钟', '放松5分钟']},
                {'name': '骑自行车', 'description': '加快速度或选择起伏路线', 'instructions': ['热身5分钟', '骑行35分钟', '拉伸5分钟']}
            ],
            '高强度': [
                {'name': '间歇跑', 'description': '快慢交替跑步，提高心肺功能', 'instructions': ['热身5分钟', '全力冲刺1分钟', '慢跑2分钟，重复8次', '拉伸5分钟']},
                {'name': '游泳冲刺', 'description': '高强度间歇游泳', 'instructions': ['热身5分钟', '冲刺游泳，休息2分钟，重复6次', '放松5分钟']},
                {'name': 'HIIT自行车', 'description': '高强度间歇骑行', 'instructions': ['热身5分钟', '全力骑行30秒，1.5分钟恢复，重复10次', '拉伸5分钟']}
            ]
        },
        '力量训练': {
            '低强度': [
                {'name': '自重训练', 'description': '使用自身重量进行基础训练', 'instructions': ['热身5分钟', '俯卧撑10次×3组', '深蹲15次×3组', '平板支撑30秒×3组', '拉伸5分钟']},
                {'name': '弹力带训练', 'description': '使用弹力带进行阻力训练', 'instructions': ['热身5分钟', '弹力带划船15次×3组', '侧平举15次×3组', '拉伸5分钟']}
            ],
            '中强度': [
                {'name': '哑铃训练', 'description': '使用哑铃进行全身训练', 'instructions': ['热身5分钟', '哑铃深蹲15次×4组', '哑铃举重12次×4组', '俯身划船12次×4组', '拉伸5分钟']},
                {'name': '复合动作训练', 'description': '结合多个肌群的复合动作', 'instructions': ['热身5分钟', '硬拉10次×4组', '卧推12次×4组', '引体向上至力竭×3组', '拉伸5分钟']}
            ],
            '高强度': [
                {'name': '高强度力量训练', 'description': '大重量低次数的力量训练', 'instructions': ['热身10分钟', '深蹲6次×6组', '卧举8次×5组', '硬拉5次×5组', '拉伸10分钟']},
                {'name': '超级组训练', 'description': '无休息连续完成两个动作', 'instructions': ['热身10分钟', '俯卧撑-深蹲超级组每组12次×4组', '引体向上-划船超级组每组10次×4组', '拉伸10分钟']}
            ]
        },
        '柔韧性训练': {
            '低强度': [
                {'name': '基础拉伸', 'description': '全身的基础拉伸动作', 'instructions': ['颈部拉伸30秒×2', '肩部拉伸30秒×2', '背部拉伸30秒×2', '腿部拉伸30秒×2']}
            ],
            '中强度': [
                {'name': '瑜伽基础', 'description': '基础瑜伽体式组合', 'instructions': ['山式站立', '下犬式保持10次呼吸', '战士一式保持10次呼吸', '树式保持15秒每侧', '大休息式5分钟']}
            ],
            'high': [
                {'name': '高级瑜伽', 'description': '更具挑战性的瑜伽序列', 'instructions': ['拜日式A重复5次', '倒立练习', '后弯体式', '深度拉伸保持']}
            ]
        }
    }
    
    @classmethod
    def get_exercise_template(cls, exercise_type: str, intensity: str) -> dict:
        """获取运动模板"""
        templates = cls.EXERCISE_TEMPLATES.get(exercise_type, {}).get(intensity, [])
        if not templates:
            return {}
        
        # 简单轮询选择
        import random
        return random.choice(templates)


class HealthPlanningEngine:
    """健康规划引擎核心类"""
    
    def __init__(self):
        self.meal_db = MealDatabase()
        self.exercise_db = ExerciseDatabase()
        self.nutrition_rules = NutritionRuleBase()
        self.exercise_rules = ExerciseRuleBase()
        self.safety_rules = HealthSafetyRules()
    
    def analyze_health_report(self, report: HealthReport) -> Dict:
        """
        分析健康报告，生成基础数据
        
        返回:
            包含BMI分类、营养目标、运动建议等信息的字典
        """
        # 计算BMI
        bmi = report.calculate_bmi()
        bmi_category = report.get_bmi_category()
        
        # 确定活动水平
        activity_level = self._determine_activity_level(report)
        
        # 估算年龄（简化版，实际应从用户信息中获取）
        assumed_age = 28  # 默认28岁
        
        # 计算基础代谢率
        bmr = self.nutrition_rules.calculate_bmr(
            weight=report.weight,
            height=report.height,
            age=assumed_age,
            gender='male' if 'male' in str(report.dietary_preferences).lower() else 'female'
        )
        
        # 计算每日热量目标
        base_calories = self.nutrition_rules.calculate_daily_calories(bmr, activity_level)
        target_calories = self.nutrition_rules.adjust_calories_for_goal(
            base_calories, 
            report.health_goal or 'maintain_weight'
        )
        
        # 计算营养目标
        nutrient_targets = self.nutrition_rules.calculate_nutrient_targets(target_calories)
        
        # 生成健康警告
        warnings = self.safety_rules.get_health_warning(report)
        
        return {
            'bmi': bmi,
            'bmi_category': bmi_category.value,
            'bmr': round(bmr, 2),
            'activity_level': activity_level,
            'target_calories': target_calories,
            'nutrient_targets': nutrient_targets,
            'warnings': warnings,
            'health_goal': report.health_goal
        }
    
    def _determine_activity_level(self, report: HealthReport) -> str:
        """根据报告数据确定活动水平"""
        freq = report.exercise_frequency or 0
        
        if freq == 0:
            return 'sedentary'
        elif 1 <= freq <= 2:
            return 'low_active'
        elif 3 <= freq <= 4:
            return 'active'
        else:
            return 'very_active'
    
    def generate_nutrition_target(self, analysis: Dict) -> NutritionTarget:
        """生成营养目标"""
        nutrient_data = analysis['nutrient_targets']
        
        return NutritionTarget(
            daily_calories=analysis['target_calories'],
            protein=nutrient_data['protein'],
            carbs=nutrient_data['carbs'],
            fat=nutrient_data['fat'],
            fiber=nutrient_data['fiber']
        )
    
    def generate_meal_suggestions(self, report: HealthReport, nutrition_target: NutritionTarget, day_number: int) -> Dict[str, MealSuggestion]:
        """
        生成餐食建议
        
        参数:
            report: 健康报告
            nutrition_target: 营养目标
            day_number: 天数编号
        
        返回:
            包含早、午、晚、加餐建议的字典
        """
        # 计算各餐次热量分配
        meal_calories = self.nutrition_rules.get_meal_calorie_distribution(nutrition_target.daily_calories)
        
        # 确定卡路里等级
        calorie_level = self._determine_calorie_level(nutrition_target.daily_calories)
        
        meals = {}
        
        # 生成早餐
        breakfast_data = self.meal_db.get_meal_option('breakfast', calorie_level)
        if breakfast_data:
            breakfast_data = self.meal_db.adjust_for_preferences(
                breakfast_data, 
                report.dietary_preferences or '', 
                report.food_allergies
            )
            meals['breakfast'] = MealSuggestion(
                meal_type='breakfast',
                food_items=breakfast_data['foods'],
                calories=breakfast_data['calories'],
                protein=breakfast_data['protein'],
                carbs=breakfast_data['carbs'],
                fat=breakfast_data['fat'],
                description=f"{', '.join(breakfast_data['foods'])}",
                preparation_tips=self._get_preparation_tips('breakfast', breakfast_data)
            )
        
        # 生成午餐
        lunch_data = self.meal_db.get_meal_option('lunch', calorie_level)
        if lunch_data:
            lunch_data = self.meal_db.adjust_for_preferences(
                lunch_data,
                report.dietary_preferences or '',
                report.food_allergies
            )
            meals['lunch'] = MealSuggestion(
                meal_type='lunch',
                food_items=lunch_data['foods'],
                calories=lunch_data['calories'],
                protein=lunch_data['protein'],
                carbs=lunch_data['carbs'],
                fat=lunch_data['fat'],
                description=f"{', '.join(lunch_data['foods'])}",
                preparation_tips=self._get_preparation_tips('lunch', lunch_data)
            )
        
        # 生成晚餐
        dinner_data = self.meal_db.get_meal_option('dinner', 'low')  # 晚餐通常较轻
        if dinner_data:
            dinner_data = self.meal_db.adjust_for_preferences(
                dinner_data,
                report.dietary_preferences or '',
                report.food_allergies
            )
            meals['dinner'] = MealSuggestion(
                meal_type='dinner',
                food_items=dinner_data['foods'],
                calories=dinner_data['calories'],
                protein=dinner_data['protein'],
                carbs=dinner_data['carbs'],
                fat=dinner_data['fat'],
                description=f"{', '.join(dinner_data['foods'])}",
                preparation_tips=self._get_preparation_tips('dinner', dinner_data)
            )
        
        # 生成加餐（可选）
        if day_number % 3 == 0:  # 每3天提供一次加餐建议
            snack_data = self.meal_db.get_meal_option('snack', calorie_level)
            if snack_data:
                snacks = MealSuggestion(
                    meal_type='snack',
                    food_items=snack_data['foods'],
                    calories=snack_data['calories'],
                    protein=snack_data['protein'],
                    carbs=snack_data['carbs'],
                    fat=snack_data['fat'],
                    description=f"{', '.join(snack_data['foods'])}",
                    preparation_tips="选择下午或上午补充能量"
                )
                meals['snack'] = snacks
        
        return meals
    
    def _determine_calorie_level(self, calories: int) -> str:
        """根据卡路里目标确定等级"""
        if calories < 1500:
            return 'low'
        elif calories < 2200:
            return 'medium'
        else:
            return 'high'
    
    def _get_preparation_tips(self, meal_type: str, meal_data: dict) -> str:
        """获取烹饪小贴士"""
        tips_map = {
            'breakfast': '建议在早餐后30分钟内食用，保证营养吸收',
            'lunch': '午饭后适当休息，避免立即剧烈运动',
            'dinner': '晚餐宜清淡，睡前2小时完成用餐'
        }
        return tips_map.get(meal_type, '')
    
    def generate_exercise_plan(self, report: HealthReport, day_number: int, health_goal: str) -> Optional[ExercisePlan]:
        """
        生成运动计划
        
        参数:
            report: 健康报告
            day_number: 天数编号
            health_goal: 健康目标
        
        返回:
            运动计划对象
        """
        # 确定运动频率规则
        day_in_week = day_number % 7
        
        # 休息日设计
        rest_days = [6]  # 每7天休息1天
        
        if day_in_week in rest_days:
            return None
        
        # 确定运动类型和强度
        activity_level = self._determine_activity_level(report)
        exercise_recommendation = self.exercise_rules.EXERCISE_RECOMMENDATIONS.get(
            activity_level, 
            self.exercise_rules.EXERCISE_RECOMMENDATIONS['sedentary']
        )
        
        # 根据健康目标调整
        goal_adjustment = self.exercise_rules.GOAL_EXERCISE_ADJUSTMENTS.get(
            health_goal, 
            {'frequency_bonus': 0, 'duration_bonus': 0, 'type_preference': []}
        )
        
        # 循环安排不同运动类型
        exercise_types = ['有氧运动', '力量训练', '有氧运动', '力量训练', '有氧运动', '柔韧性训练']
        exercise_type = exercise_types[day_in_week % len(exercise_types)]
        
        # 确定强度
        intensity_map = {'low': '低强度', 'medium': '中强度', 'high': '高强度'}
        intensity_level = intensity_map.get(exercise_recommendation['intensity'], '中强度')
        
        # 获取运动模板
        template = self.exercise_db.get_exercise_template(exercise_type, intensity_level)
        
        if not template:
            return None
        
        # 计算持续时间
        base_duration = exercise_recommendation['duration_per_session']
        duration = base_duration + goal_adjustment.get('duration_bonus', 0)
        
        # 强度枚举
        intensity_enum = ExerciseIntensity.LOW if intensity_level == '低强度' else (
            ExerciseIntensity.MEDIUM if intensity_level == '中强度' else ExerciseIntensity.HIGH
        )
        
        # 估算消耗卡路里
        calories_burned = self.exercise_rules.estimate_calories_burned(
            exercise_type=exercise_type,
            duration=duration,
            weight=report.weight,
            intensity=intensity_enum.value
        )
        
        return ExercisePlan(
            exercise_type=exercise_type,
            exercise_name=template['name'],
            description=template['description'],
            duration=duration,
            intensity=intensity_enum,
            calories_burned=calories_burned,
            instructions=template['instructions']
        )
    
    def generate_reminders_and_notes(self, report: HealthReport, day_number: int, exercise_plan: Optional[ExercisePlan]) -> Dict[str, List[str]]:
        """生成提醒和注意事项"""
        reminders = []
        notes = []
        
        # 通用提醒
        if day_number == 1:
            reminders.append("今天是计划开始的第一天，加油！")
            reminders.append("记得记录今日的饮食和运动情况")
        elif day_number % 7 == 0:
            reminders.append("恭喜您坚持了一周！继续保持！")
        elif exercise_plan:
            reminders.append(f"今日运动：{exercise_plan.exercise_name}，时长{exercise_plan.duration}分钟")
        
        # 根据健康目标生成提醒
        if report.health_goal == 'lose_weight':
            if day_number % 3 == 0:
                reminders.append("记得记录体重，追踪减重进度")
                notes.append("控制碳水化合物摄入，增加蛋白质比例")
        elif report.health_goal == 'build_muscle':
            if day_number % 2 == 0:
                reminders.append("今天记得摄入足够的蛋白质")
                notes.append("运动后30分钟内补充营养，帮助肌肉恢复")
        
        # 睡眠提醒
        if report.sleep_hours_avg and report.sleep_hours_avg < 7:
            notes.append("您的睡眠时间偏少，建议今晚提前30分钟入睡")
        
        # 饮水提醒
        if day_number % 2 == 0:
            reminders.append("记得保持充足的水分摄入，每天至少8杯水")
        
        # 情绪和压力管理
        if report.stress_level and report.stress_level > 7:
            notes.append("检测到近期压力较大，建议适当进行放松活动")
            if exercise_plan and exercise_plan.exercise_type == '柔韧性训练':
                reminders.append("今天的柔韧性训练有助于缓解压力")
        
        return {
            'reminders': reminders,
            'notes': notes
        }
    
    def generate_monthly_plan(self, report: HealthReport, plan_month: str, total_days: int = 30) -> MonthlyPlan:
        """
        生成长达30天的月度计划
        
        参数:
            report: 健康报告
            plan_month: 计划月份 (格式: '2024-01')
            total_days: 总天数
        
        返回:
            月度计划对象
        """
        # 分析健康报告
        analysis = self.analyze_health_report(report)
        
        # 生成营养目标
        nutrition_target = self.generate_nutrition_target(analysis)
        
        # 确定运动频率
        activity_level = self._determine_activity_level(report)
        base_frequency = self.exercise_rules.EXERCISE_RECOMMENDATIONS.get(
            activity_level, 
            self.exercise_rules.EXERCISE_RECOMMENDATIONS['sedentary']
        )['frequency']
        
        # 生成每日计划
        daily_plans = []
        start_date = datetime.strptime(plan_month + '-01', '%Y-%m-%d')
        
        for day_num in range(1, total_days + 1):
            current_date = start_date + timedelta(days=day_num - 1)
            date_str = current_date.strftime('%Y-%m-%d')
            
            # 生成每日计划
            daily_plan = self.generate_daily_plan(
                report=report,
                nutrition_target=nutrition_target,
                day_number=day_num,
                date_str=date_str,
                health_goal=report.health_goal or 'maintain_weight'
            )
            
            daily_plans.append(daily_plan)
        
        return MonthlyPlan(
            plan_id=0,  # 会在Java端生成实际ID
            user_id=report.user_id,
            plan_month=plan_month,
            nutrition_target=nutrition_target,
            exercise_sessions_per_week=base_frequency,
            daily_plans=daily_plans
        )
    
    def generate_daily_plan(
        self, 
        report: HealthReport, 
        nutrition_target: NutritionTarget,
        day_number: int, 
        date_str: str,
        health_goal: str
    ) -> DailyPlan:
        """生成单日计划"""
        # 生成餐食建议
        meals = self.generate_meal_suggestions(report, nutrition_target, day_number)
        
        # 生成运动计划
        exercise = self.generate_exercise_plan(report, day_number, health_goal)
        
        # 生成提醒和注意事项
        reminder_data = self.generate_reminders_and_notes(report, day_number, exercise)
        
        # 创建每日计划对象
        return DailyPlan(
            day_number=day_number,
            date=date_str,
            breakfast=meals.get('breakfast'),
            lunch=meals.get('lunch'),
            dinner=meals.get('dinner'),
            snacks=meals.get('snack'),
            exercise=exercise,
            reminders=reminder_data['reminders'],
            notes=reminder_data['notes']
        )


class HealthEngineService:
    """健康引擎服务类 - 对外提供统一的API接口"""
    
    def __init__(self):
        self.engine = HealthPlanningEngine()
    
    def generate_plan_from_report(self, report_data: Dict) -> Dict:
        """
        从报告数据生成计划（主要供Java调用）
        
        参数:
            report_data: 健康报告字典数据
        
        返回:
            生成的月度计划JSON数据
        """
        # 构建健康报告对象
        report = HealthReport(
            user_id=report_data.get('user_id', 0),
            report_month=report_data.get('report_month', ''),
            height=report_data.get('height', 170),
            weight=report_data.get('weight', 70),
            body_fat_rate=report_data.get('body_fat_rate'),
            muscle_mass=report_data.get('muscle_mass'),
            systolic_pressure=report_data.get('systolic_pressure'),
            diastolic_pressure=report_data.get('diastolic_pressure'),
            sleep_hours_avg=report_data.get('sleep_hours_avg'),
            sleep_quality=report_data.get('sleep_quality'),
            smoking=report_data.get('smoking', False),
            drinking=report_data.get('drinking', False),
            exercise_frequency=report_data.get('exercise_frequency'),
            dietary_preferences=report_data.get('dietary_preferences'),
            food_allergies=report_data.get('food_allergies'),
            health_goal=report_data.get('health_goal', 'general_health'),
            target_weight=report_data.get('target_weight'),
            medical_history=report_data.get('medical_history'),
            medications=report_data.get('medications'),
            stress_level=report_data.get('stress_level'),
            energy_level=report_data.get('energy_level'),
            additional_notes=report_data.get('additional_notes')
        )
        
        # 生成月度计划
        month_plan = self.engine.generate_monthly_plan(
            report=report,
            plan_month=report_data.get('report_month', ''),
            total_days=30
        )
        
        # 转换为字典（包含分析和计划数据）
        analysis = self.engine.analyze_health_report(report)
        
        return {
            'analysis': analysis,
            'plan': month_plan.to_dict()
        }
    
    def generate_daily_recommendation(self, user_health_data: Dict, day_number: int, date_str: str) -> Dict:
        """
        生成单日推荐（用于每日更新）
        
        参数:
            user_health_data: 用户健康数据
            day_number: 天数编号
            date_str: 日期字符串
        
        返回:
            单日推荐数据
        """
        report = HealthReport(
            user_id=user_health_data.get('user_id', 0),
            report_month=user_health_data.get('report_month', ''),
            height=user_health_data.get('height', 170),
            weight=user_health_data.get('weight', 70),
            exercise_frequency=user_health_data.get('exercise_frequency'),
            dietary_preferences=user_health_data.get('dietary_preferences'),
            food_allergies=user_health_data.get('food_allergies'),
            health_goal=user_health_data.get('health_goal', 'general_health'),
            sleep_hours_avg=user_health_data.get('sleep_hours_avg'),
            stress_level=user_health_data.get('stress_level')
        )
        
        # 快速生成单日计划
        daily_plan = self.engine.generate_daily_plan(
            report=report,
            nutrition_target=self.engine.generate_nutrition_target(
                {'nutrient_targets': {
                    'protein': 80, 'carbs': 200, 'fat': 60, 'fiber': 25
                }}
            ),
            day_number=day_number,
            date_str=date_str,
            health_goal=report.health_goal or 'maintain_weight'
        )
        
        return daily_plan.to_dict()


def get_nutrients_for_meal(meal_calories: int, meal_type: str) -> dict:
    """获取特定餐次的营养素分配"""
    ratios = {
        'breakfast': {'protein': 0.15, 'carbs': 0.70, 'fat': 0.15},
        'lunch': {'protein': 0.20, 'carbs': 0.55, 'fat': 0.25},
        'dinner': {'protein': 0.25, 'carbs': 0.45, 'fat': 0.30},
        'snack': {'protein': 0.10, 'carbs': 0.80, 'fat': 0.10}
    }
    
    ratio = ratios.get(meal_type, ratios['lunch'])
    
    return {
        'protein': round((meal_calories * ratio['protein']) / 4, 2),
        'carbs': round((meal_calories * ratio['carbs']) / 4, 2),
        'fat': round((meal_calories * ratio['fat']) / 9, 2)
    }