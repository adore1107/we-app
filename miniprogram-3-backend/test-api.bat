@echo off
echo ==================================
echo   测试宋家纺织B2B小程序后端API
echo ==================================
echo.

REM 设置服务地址
set BASE_URL=http://localhost:8080/api

echo 1. 测试首页数据
echo 访问: %BASE_URL%/home/data
curl -X GET "%BASE_URL%/home/data" --connect-timeout 5
echo.
echo.

echo 2. 测试用户登录
curl -X POST "%BASE_URL%/user/login" ^
  -H "Content-Type: application/x-www-form-urlencoded" ^
  -d "openid=test_openid_123&nickname=张三&avatarUrl=http://example.com/avatar.jpg" ^
  --connect-timeout 5
echo.
echo.

echo 3. 测试商品列表
curl -X GET "%BASE_URL%/product/list?page=0&size=3" --connect-timeout 5
echo.
echo.

echo 4. 测试分类列表
curl -X GET "%BASE_URL%/category/list" --connect-timeout 5
echo.
echo.

echo 5. 测试轮播图
curl -X GET "%BASE_URL%/banner/list" --connect-timeout 5
echo.
echo.

echo ==================================
echo   测试完成，请查看上述响应结果
echo ==================================
pause