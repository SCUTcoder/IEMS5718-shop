#!/bin/bash
set -e

echo "=========================================="
echo "  Updating IEMS5718 Shop"
echo "=========================================="
echo ""

# 1. 停止后端服务
echo "Stopping backend..."
sudo systemctl stop iems5718-shop || true

# 2. 更新代码
echo "Pulling latest code..."
cd /opt/app/IEMS5718-shop
git pull

# 3. 更新前端（保护上传的图片）
echo "Updating frontend..."
sudo cp -r *.html /var/www/html/
sudo cp -r css /var/www/html/
sudo cp -r js /var/www/html/
sudo rsync -av --ignore-existing images/ /var/www/html/images/

# 4. 确保图片目录权限（后端 www-data 用户需要写入权限）
sudo mkdir -p /var/www/html/images/products
sudo chown -R www-data:www-data /var/www/html
sudo chmod -R 755 /var/www/html
sudo chmod -R 775 /var/www/html/images/products
sudo chown www-data:www-data /var/www/html/images/products

# 5. 检查数据库
echo ""
echo "Checking database..."
cd backend

if [ ! -f "shop.db" ]; then
    echo "⚠️  Database not found, creating..."
    sqlite3 shop.db < src/main/resources/database-init.sql
    sudo chown www-data:www-data shop.db
    sudo chmod 664 shop.db
    echo "✓ Database created"
else
    # Check if categories table exists
    if ! sqlite3 shop.db "SELECT name FROM sqlite_master WHERE type='table' AND name='categories';" | grep -q "categories"; then
        echo "⚠️  Categories table missing!"
        echo "Please run: ./quick-fix-db.sh"
        echo "Or manually fix database"
        exit 1
    fi
    echo "✓ Database OK"
fi

# 确保数据库文件及目录对 www-data 可写
sudo chown www-data:www-data shop.db
sudo chmod 664 shop.db
sudo chown www-data:www-data /opt/app/IEMS5718-shop/backend
sudo chmod 775 /opt/app/IEMS5718-shop/backend

# 6. 更新 systemd service 文件
echo ""
echo "Updating systemd service..."
sudo cp /opt/app/IEMS5718-shop/backend/iems5718-shop.service /etc/systemd/system/
sudo systemctl daemon-reload

# 7. 构建后端
echo ""
echo "Building backend..."
mvn clean package -DskipTests

# 8. 构建完成后修复权限（mvn 以 ubuntu 用户运行会重置目录 owner）
echo "Fixing permissions after build..."
sudo chown www-data:www-data /opt/app/IEMS5718-shop/backend/shop.db
sudo chmod 664 /opt/app/IEMS5718-shop/backend/shop.db
sudo chown www-data:www-data /opt/app/IEMS5718-shop/backend
sudo chmod 775 /opt/app/IEMS5718-shop/backend

# 9. 更新 nginx 配置并重载
echo ""
echo "Updating nginx config..."
sudo cp /opt/app/IEMS5718-shop/nginx.conf /etc/nginx/sites-enabled/iems5718-shop
sudo nginx -t && sudo systemctl reload nginx

# 10. 启动后端服务
echo ""
echo "Starting backend..."
sudo systemctl start iems5718-shop

# 11. 检查状态
echo ""
echo "Checking status..."
sleep 5
sudo systemctl status iems5718-shop --no-pager

echo ""
echo "=========================================="
echo "Deployment completed!"
echo "=========================================="
echo ""
echo "View logs: sudo journalctl -u iems5718-shop -f"