"""
健康智护系统 - API服务模块
API Service for HealthSmart System

该模块提供Python引擎的对外API接口，支持：
- 命令行交互模式
- JSON数据输入/输出
- 与Java进程通信
"""

import sys
import json
import io
from typing import Dict, Any
from .health_engine import HealthEngineService


class EngineAPI:
    """引擎API服务类"""
    
    def __init__(self):
        self.service = HealthEngineService()
    
    def generate_plan(self, report_data: Dict) -> Dict:
        """
        生成计划
        
        参数:
            report_data: 健康报告数据
        
        返回:
            生成结果字典
        """
        try:
            result = self.service.generate_plan_from_report(report_data)
            return {
                'success': True,
                'data': result,
                'message': '计划生成成功'
            }
        except Exception as e:
            return {
                'success': False,
                'data': None,
                'message': f'生成计划失败: {str(e)}'
            }
    
    def generate_daily_recommendation(self, user_data: Dict, day_number: int, date_str: str) -> Dict:
        """
        生成每日推荐
        
        参数:
            user_data: 用户健康数据
            day_number: 天数编号
            date_str: 日期字符串
        
        返回:
            生成结果字典
        """
        try:
            result = self.service.generate_daily_recommendation(user_data, day_number, date_str)
            return {
                'success': True,
                'data': result,
                'message': '每日推荐生成成功'
            }
        except Exception as e:
            return {
                'success': False,
                'data': None,
                'message': f'生成每日推荐失败: {str(e)}'
            }
    
    def analyze_health(self, report_data: Dict) -> Dict:
        """
        分析健康数据
        
        参数:
            report_data: 健康报告数据
        
        返回:
            分析结果字典
        """
        try:
            from .models import HealthReport
            
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
                energy_level=report_data.get('energy_level')
            )
            
            from .health_engine import HealthPlanningEngine
            engine = HealthPlanningEngine()
            analysis = engine.analyze_health_report(report)
            
            return {
                'success': True,
                'data': analysis,
                'message': '健康分析成功'
            }
        except Exception as e:
            return {
                'success': False,
                'data': None,
                'message': f'健康分析失败: {str(e)}'
            }


def output_json(data: Dict):
    """输出JSON到stdout"""
    print(json.dumps(data, ensure_ascii=False, indent=2))


def main():
    """命令行入口"""
    if len(sys.argv) < 2:
        # 输出帮助信息
        help_info = {
            'name': 'HealthSmart Python Engine',
            'version': '1.0.0',
            'usage': 'python api_service.py <action> <json_data>',
            'actions': {
                'generate_plan': '根据健康报告生成月度计划',
                'daily_recommendation': '生成每日推荐',
                'analyze_health': '分析健康数据',
                'health': '检查引擎健康状态'
            },
            'example': 'python api_service.py generate_plan \'{"user_id":1,"height":175,"weight":70}\''
        }
        output_json(help_info)
        sys.exit(0)
    
    action = sys.argv[1]
    api = EngineAPI()
    
    if action == 'health':
        output_json({
            'status': 'ok',
            'message': 'Engine is running'
        })
        sys.exit(0)
    
    # 从stdin或命令行读取JSON数据
    if len(sys.argv) >= 3:
        # 从命令行参数读取
        json_str = sys.argv[2]
    else:
        # 从stdin读取
        json_str = sys.stdin.read()
    
    try:
        data = json.loads(json_str)
    except json.JSONDecodeError:
        output_json({
            'success': False,
            'message': 'Invalid JSON data'
        })
        sys.exit(1)
    
    # 根据动作调用对应方法
    if action == 'generate_plan':
        result = api.generate_plan(data)
    elif action == 'daily_recommendation':
        day_number = data.get('day_number', 1)
        date_str = data.get('date', '')
        user_data = data.get('user_data', {})
        result = api.generate_daily_recommendation(user_data, day_number, date_str)
    elif action == 'analyze_health':
        result = api.analyze_health(data)
    else:
        output_json({
            'success': False,
            'message': f'Unknown action: {action}'
        })
        sys.exit(1)
    
    output_json(result)
    
    # 返回退出码
    sys.exit(0 if result.get('success') else 1)


if __name__ == '__main__':
    main()