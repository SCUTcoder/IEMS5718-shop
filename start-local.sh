#!/bin/bash

echo "=========================================="
echo "  IEMS5718 Shop - Local Development"
echo "=========================================="
echo ""

# 检查是否已有后端在运行
if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null 2>&1; then
    echo "⚠️  Warning: Port 8080 is already in use"
    echo "Backend may already be running, or another service is using port 8080"
    echo ""
    read -p "Kill existing process on port 8080? (y/n): " kill_choice
    if [ "$kill_choice" = "y" ]; then
        echo "Killing process on port 8080..."
        lsof -ti:8080 | xargs kill -9 2>/dev/null || true
        sleep 2
    fi
fi

# 启动后端
echo "Starting backend..."
cd backend

# 检查是否需要构建
if [ ! -f "target/shop-backend-1.0.0.jar" ]; then
    echo "Building backend (first time)..."
    mvn clean package -DskipTests
fi

# 后台启动后端
nohup mvn spring-boot:run > ../backend.log 2>&1 &
BACKEND_PID=$!

echo "Backend starting (PID: $BACKEND_PID)..."
echo "Waiting for backend to start..."

# 等待后端启动（最多30秒）
for i in {1..30}; do
    if curl -s http://localhost:8080/api/products > /dev/null 2>&1; then
        echo "✅ Backend is ready!"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "❌ Backend failed to start in 30 seconds"
        echo "Check backend.log for errors"
        exit 1
    fi
    sleep 1
    echo -n "."
done

echo ""
cd ..

# 启动前端
echo ""
echo "Starting frontend..."

# 检查前端服务器是否已在运行
if lsof -Pi :3000 -sTCP:LISTEN -t >/dev/null 2>&1; then
    echo "⚠️  Warning: Port 3000 is already in use"
    lsof -ti:3000 | xargs kill -9 2>/dev/null || true
    sleep 1
fi

# 启动前端（使用 Python 3）
nohup python3 -m http.server 3000 > frontend.log 2>&1 &
FRONTEND_PID=$!

sleep 2

echo ""
echo "=========================================="
echo "✅ All services started!"
echo "=========================================="
echo ""
echo "📍 Access URLs:"
echo "   Frontend:  http://localhost:3000/index.html"
echo "   Admin:     http://localhost:3000/admin.html"
echo "   Backend:   http://localhost:8080/api/products"
echo ""
echo "📋 Process IDs:"
echo "   Backend PID:  $BACKEND_PID"
echo "   Frontend PID: $FRONTEND_PID"
echo ""
echo "📝 Logs:"
echo "   Backend:  tail -f backend.log"
echo "   Frontend: tail -f frontend.log"
echo ""
echo "🛑 To stop services:"
echo "   ./stop-local.sh"
echo "   or: kill $BACKEND_PID $FRONTEND_PID"
echo ""
echo "=========================================="

# 自动打开浏览器（macOS）
sleep 2
open http://localhost:3000/index.html 2>/dev/null || true

echo ""
echo "Press Ctrl+C to view this message again"
echo "(Services will keep running in background)"
