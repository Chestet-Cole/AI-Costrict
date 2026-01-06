"""
健康智护系统 - 数据模型定义
Data Models for HealthSmart System
"""

from dataclasses import dataclass, field
from typing import List, Dict, Optional
from datetime import datetime
from enum import Enum


class HealthGoal(Enum):
    """健康目标类型"""
    LOSE_WEIGHT = "lose_weight"
    MAINTAIN_WEIGHT = "maintain_weight"
    GAIN_WEIGHT = "gain_weight"
    BUILD_MUSCLE = "build_muscle"
    IMPROVE_SLEEP = "improve_sleep"
    GENERAL_HEALTH = "general_health"


class BMICategory(Enum):
    """BMI分类"""
    UNDERWEIGHT = "underweight"
    NORMAL = "normal"
    OVERWEIGHT = "overweight"
    OBESE = "obese"


class ExerciseIntensity(Enum):
    """运动强度"""
    LOW = "low"
    MEDIUM = "medium"
    HIGH = "high"


@dataclass
class HealthReport:
    """健康报告数据模型"""
    # 基础信息
    user_id: int
    report_month: str
    
    # 身体指标
    height: float  # cm
    weight: float  # kg
    body_fat_rate: Optional[float] = None
    muscle_mass: Optional[float] = None
    
    # 血压
    systolic_pressure: Optional[int] = None  # 收缩压
    diastolic_pressure: Optional[int] = None  # 舒张压
    
    # 睡眠信息
    sleep_hours_avg: Optional[float] = None
    sleep_quality: Optional[int] = None  # 1-10
    
    # 生活习惯
    smoking: bool = False
    drinking: bool = False
    exercise_frequency: Optional[int] = None  # 每周运动次数
    
    # 饮食偏好
    dietary_preferences: Optional[str] = None
    food_allergies: Optional[str] = None
    
    # 健康目标
    health_goal: Optional[str] = None
    target_weight: Optional[float] = None
    
    # 病史信息
    medical_history: Optional[str] = None
    medications: Optional[str] = None
    
    # 其他指标
    stress_level: Optional[int] = None  # 1-10
    energy_level: Optional[int] = None  # 1-10
    additional_notes: Optional[str] = None
    
    def calculate_bmi(self) -> float:
        """计算BMI"""
        height_m = self.height / 100
        return round(self.weight / (height_m ** 2), 2)
    
    def get_bmi_category(self) -> BMICategory:
        """获取BMI分类"""
        bmi = self.calculate_bmi()
        if bmi < 18.5:
            return BMICategory.UNDERWEIGHT
        elif 18.5 <= bmi < 24:
            return BMICategory.NORMAL
        elif 24 <= bmi < 28:
            return BMICategory.OVERWEIGHT
        else:
            return BMICategory.OBESE
    
    def to_dict(self) -> Dict:
        """转换为字典"""
        return {
            'user_id': self.user_id,
            'report_month': self.report_month,
            'height': self.height,
            'weight': self.weight,
            'bmi': self.calculate_bmi(),
            'body_fat_rate': self.body_fat_rate,
            'muscle_mass': self.muscle_mass,
            'systolic_pressure': self.systolic_pressure,
            'diastolic_pressure': self.diastolic_pressure,
            'sleep_hours_avg': self.sleep_hours_avg,
            'sleep_quality': self.sleep_quality,
            'smoking': self.smoking,
            'drinking': self.drinking,
            'exercise_frequency': self.exercise_frequency,
            'dietary_preferences': self.dietary_preferences,
            'food_allergies': self.food_allergies,
            'health_goal': self.health_goal,
            'target_weight': self.target_weight,
            'medical_history': self.medical_history,
            'medications': self.medications,
            'stress_level': self.stress_level,
            'energy_level': self.energy_level
        }


@dataclass
class NutritionTarget:
    """营养目标"""
    daily_calories: int  # 每日卡路里目标
    protein: float  # 蛋白质 (g)
    carbs: float  # 碳水化合物 (g)
    fat: float  # 脂肪 (g)
    fiber: float  # 纤维 (g)
    
    def to_dict(self) -> Dict:
        return {
            'calorie_target': self.daily_calories,
            'protein_target': self.protein,
            'carbs_target': self.carbs,
            'fat_target': self.fat,
            'fiber_target': self.fiber
        }


@dataclass
class MealSuggestion:
    """餐食建议"""
    meal_type: str  # breakfast, lunch, dinner, snack
    food_items: List[str]
    calories: int
    protein: float
    carbs: float
    fat: float
    description: str
    preparation_tips: Optional[str] = None
    
    def to_dict(self) -> Dict:
        return {
            'meal_type': self.meal_type,
            'food_items': ', '.join(self.food_items),
            'calories': self.calories,
            'protein': self.protein,
            'carbs': self.carbs,
            'fat': self.fat,
            'description': self.description,
            'preparation_tips': self.preparation_tips
        }


@dataclass
class ExercisePlan:
    """运动计划"""
    exercise_type: str  # 有氧运动, 力量训练, 柔韧性训练等
    exercise_name: str
    description: str
    duration: int  # 分钟
    intensity: ExerciseIntensity
    calories_burned: int
    instructions: List[str]
    
    def to_dict(self) -> Dict:
        return {
            'exercise_type': self.exercise_type,
            'exercise_name': self.exercise_name,
            'description': self.description,
            'duration': self.duration,
            'intensity': self.intensity.value,
            'calories_burned': self.calories_burned,
            'instructions': '; '.join(self.instructions)
        }


@dataclass
class DailyPlan:
    """每日计划"""
    day_number: int
    date: str
    
    # 餐食计划
    breakfast: Optional[MealSuggestion] = None
    lunch: Optional[MealSuggestion] = None
    dinner: Optional[MealSuggestion] = None
    snacks: Optional[MealSuggestion] = None
    
    # 运动计划
    exercise: Optional[ExercisePlan] = None
    
    # 每日提醒
    reminders: List[str] = field(default_factory=list)
    
    # 注意事项
    notes: List[str] = field(default_factory=list)
    
    def get_total_calories(self) -> int:
        """获取每日总卡路里"""
        total = 0
        if self.breakfast:
            total += self.breakfast.calories
        if self.lunch:
            total += self.lunch.calories
        if self.dinner:
            total += self.dinner.calories
        if self.snacks:
            total += self.snacks.calories
        return total
    
    def to_dict(self) -> Dict:
        return {
            'day_number': self.day_number,
            'date': self.date,
            'meal_breakfast': self.breakfast.description if self.breakfast else None,
            'meal_lunch': self.lunch.description if self.lunch else None,
            'meal_dinner': self.dinner.description if self.dinner else None,
            'meal_snacks': self.snacks.description if self.snacks else None,
            'daily_calorie_goal': self.get_total_calories(),
            'exercise_type': self.exercise.exercise_type if self.exercise else None,
            'exercise_description': self.exercise.description if self.exercise else None,
            'exercise_duration': self.exercise.duration if self.exercise else 0,
            'exercise_intensity': self.exercise.intensity.value if self.exercise else None,
            'reminders': '; '.join(self.reminders),
            'notes': '; '.join(self.notes)
        }


@dataclass
class MonthlyPlan:
    """月度计划"""
    plan_id: int
    user_id: int
    plan_month: str
    
    # 营养目标
    nutrition_target: NutritionTarget
    
    # 运动计划
    exercise_sessions_per_week: int
    
    # 每日计划列表
    daily_plans: List[DailyPlan] = field(default_factory=list)
    
    def get_completed_days_count(self) -> int:
        """获取已完成天数"""
        return sum(1 for day in self.daily_plans if day)
    
    def to_dict(self) -> Dict:
        return {
            'user_id': self.user_id,
            'plan_month': self.plan_month,
            'total_days': len(self.daily_plans),
            **self.nutrition_target.to_dict(),
            'exercise_sessions_per_week': self.exercise_sessions_per_week,
            'daily_plans': [day.to_dict() for day in self.daily_plans]
        }