#!/bin/bash

echo "Stopping local services..."

# 停止端口 8080 的进程（后端）
if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null 2>&1; then
    echo "Stopping backend (port 8080)..."
    lsof -ti:8080 | xargs kill -9 2>/dev/null
    echo "✅ Backend stopped"
else
    echo "ℹ️  Backend not running"
fi

# 停止端口 3000 的进程（前端）
if lsof -Pi :3000 -sTCP:LISTEN -t >/dev/null 2>&1; then
    echo "Stopping frontend (port 3000)..."
    lsof -ti:3000 | xargs kill -9 2>/dev/null
    echo "✅ Frontend stopped"
else
    echo "ℹ️  Frontend not running"
fi

# 清理日志文件（可选）
if [ "$1" = "clean" ]; then
    echo "Cleaning log files..."
    rm -f backend.log frontend.log
    echo "✅ Logs cleaned"
fi

echo ""
echo "All services stopped!"
