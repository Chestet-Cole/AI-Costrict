@echo off
chcp 65001 >nul
echo ========================================
echo    健康智护系统 - 启动脚本
echo ========================================
echo.

cd /d "%~dp0java_core"

echo [1] 检查环境...
java -version >nul 2>&1
if errorlevel 1 (
    echo 错误: 未找到Java运行环境
    echo 请确保已安装JDK 11+
    pause
    exit /b 1
)

echo [2] 初始化数据库...
java -cp target/classes;target/lib/* com.healthsmart.util.DatabaseConnection
if errorlevel 1 (
    echo 警告: 数据库初始化失败
)

echo [3] 启动API服务器...
echo.
echo 系统将在 http://localhost:8080 提供服务
echo 按 Ctrl+C 停止服务器
echo.

java -cp target/classes;target/lib/* com.healthsmart.Main

pause