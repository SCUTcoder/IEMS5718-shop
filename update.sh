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

# 4. 确保图片目录权限
sudo mkdir -p /var/www/html/images/products
sudo chown -R www-data:www-data /var/www/html
sudo chmod -R 755 /var/www/html
sudo chmod -R 775 /var/www/html/images/products

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

# 6. 构建后端
echo ""
echo "Building backend..."
mvn clean package -DskipTests

# 7. 启动后端服务
echo ""
echo "Starting backend..."
sudo systemctl start iems5718-shop

# 8. 检查状态
echo ""
echo "Checking status..."
sleep 3
sudo systemctl status iems5718-shop --no-pager

echo ""
echo "=========================================="
echo "Deployment completed!"
echo "=========================================="
echo ""
echo "View logs: sudo journalctl -u iems5718-shop -f"