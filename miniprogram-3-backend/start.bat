@echo off
echo ==================================
echo   启动宋家纺织B2B小程序后端服务
echo ==================================
echo.

REM 检查Java环境
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ❌ 错误：未找到Java 17，请先安装Java 17
    pause
    exit /b 1
)

echo ✅ Java环境检查通过

REM 检查Maven环境
mvn -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ❌ 错误：未找到Maven，请先安装Maven
    pause
    exit /b 1
)

echo ✅ Maven环境检查通过

REM 检查当前目录
if not exist "pom.xml" (
    echo ❌ 错误：请在项目根目录运行此脚本
    pause
    exit /b 1
)

echo ✅ 项目目录检查通过

echo.
echo 正在编译项目...
mvn clean compile

if %ERRORLEVEL% NEQ 0 (
    echo ❌ 编译失败，请检查代码错误
    pause
    exit /b 1
)

echo ✅ 编译成功

echo.
echo 正在启动后端服务...
echo 服务启动后可访问：
echo - 首页API: http://localhost:8080/api/home/data
echo - 测试登录: http://localhost:8080/api/user/login
echo.
echo 按Ctrl+C停止服务
echo ==================================
mvn spring-boot:run