#!/bin/bash

# IEMS5718 Shop 一键部署脚本
# 用法: sudo ./deploy.sh

set -e  # 遇到错误立即退出

echo "======================================"
echo "  IEMS5718 Shop 部署脚本"
echo "======================================"
echo ""

# 检查是否以 root 运行
if [ "$EUID" -ne 0 ]; then 
    echo "❌ 请使用 sudo 运行此脚本"
    exit 1
fi

# 配置变量
APP_DIR="/opt/app/IEMS5718-shop"
WEB_DIR="/var/www/html"
SERVICE_NAME="iems5718-shop"

echo "📍 应用目录: $APP_DIR"
echo "📍 Web目录: $WEB_DIR"
echo ""

# 1. 拉取最新代码
echo "📥 拉取最新代码..."
cd $APP_DIR
git pull
echo "✅ 代码更新完成"
echo ""

# 2. 构建后端
echo "🔨 构建后端..."
cd $APP_DIR/backend
mvn clean package -DskipTests
if [ ! -f "target/shop-backend-1.0.0.jar" ]; then
    echo "❌ 后端构建失败！"
    exit 1
fi
echo "✅ 后端构建完成"
echo ""

# 3. 部署前端
echo "📦 部署前端..."
cp -r $APP_DIR/*.html $WEB_DIR/
cp -r $APP_DIR/css $WEB_DIR/
cp -r $APP_DIR/js $WEB_DIR/
cp -r $APP_DIR/images $WEB_DIR/
chown -R www-data:www-data $WEB_DIR
chmod -R 755 $WEB_DIR
echo "✅ 前端部署完成"
echo ""

# 4. 重启后端服务
echo "🔄 重启后端服务..."
systemctl restart $SERVICE_NAME
sleep 2
if systemctl is-active --quiet $SERVICE_NAME; then
    echo "✅ 后端服务启动成功"
else
    echo "❌ 后端服务启动失败，查看日志:"
    journalctl -u $SERVICE_NAME -n 20 --no-pager
    exit 1
fi
echo ""

# 5. 重启 Nginx
echo "🔄 重启 Nginx..."
systemctl reload nginx
if systemctl is-active --quiet nginx; then
    echo "✅ Nginx 启动成功"
else
    echo "❌ Nginx 启动失败"
    exit 1
fi
echo ""

# 6. 显示状态
echo "======================================"
echo "  ✅ 部署完成！"
echo "======================================"
echo ""
echo "📊 服务状态:"
systemctl status $SERVICE_NAME --no-pager -l | head -n 5
echo ""
systemctl status nginx --no-pager -l | head -n 5
echo ""

# 获取服务器 IP
SERVER_IP=$(hostname -I | awk '{print $1}')
echo "🌐 访问地址:"
echo "   http://$SERVER_IP/"
echo "   http://$SERVER_IP/index.html"
echo "   http://$SERVER_IP/index-dynamic.html"
echo ""
echo "📝 查看日志:"
echo "   sudo journalctl -u $SERVICE_NAME -f"
echo ""
