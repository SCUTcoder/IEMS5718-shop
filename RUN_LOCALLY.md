# 本地运行指南

## ✅ 数据库已修复

数据库已重建，新结构包含：
- ✅ `categories` 表（2个分类）
- ✅ `products` 表（6个产品，含 catid 外键）

## 🚀 本地启动步骤

### 第 1 步：启动后端

**在 Cursor 或 IntelliJ IDEA 中运行（最简单）：**
1. 打开 `backend/src/main/java/com/iems5718/shop/ShopApplication.java`
2. 点击绿色运行按钮 ▶️
3. 或右键选择 "Run ShopApplication"

**或者使用终端（需要配置 Java）：**
```bash
cd backend
mvn spring-boot:run
```

**成功标志**：看到
```
Started ShopApplication in X.XXX seconds (process running on YYY)
```

**后端运行在**：`http://localhost:8080`

### 第 2 步：在浏览器中打开前端

**方法 A：直接双击（已修复 file:// 问题）**
- 双击 `index.html` 文件
- ✅ 现在会正确连接到 `http://localhost:8080/api`

**方法 B：使用本地服务器（推荐）**
```bash
# 新开一个终端窗口
cd /Users/Zhuanz1/Documents/IEMS\ 5718\ 网络安全/project/iems5718-shop
python3 -m http.server 3000
```

然后访问：`http://localhost:3000/index.html`

---

## 📍 访问地址

| 页面 | 地址 |
|------|------|
| 主页 | http://localhost:3000/index.html |
| 管理后台 | http://localhost:3000/admin.html |
| 产品详情 | http://localhost:3000/product.html?id=1 |
| 后端 API | http://localhost:8080/api/products |

---

## 🧪 测试功能

### 1. 测试后端 API
打开浏览器访问或使用 curl：
```bash
curl http://localhost:8080/api/categories
curl http://localhost:8080/api/products
```

应该看到 JSON 格式的数据

### 2. 测试主页
- 打开 `http://localhost:3000/index.html`
- 按 F12 打开控制台
- 应该看到：`✅ Connected to backend API`
- 产品列表自动加载

### 3. 测试购物车
- 点击 "Add to Cart"
- 购物车图标数字增加
- 点击购物车图标查看商品
- 刷新页面，购物车数据保留

### 4. 测试管理后台
- 打开 `http://localhost:3000/admin.html`
- 可以添加/编辑/删除分类和产品

---

## 🛑 停止服务

**停止后端**：
- 如果在 IDE 中运行：点击停止按钮 ⏹️
- 如果在终端运行：按 `Ctrl + C`

**停止前端**：
- 按 `Ctrl + C`

---

## ⚠️ 常见问题

### Q: 找不到 Java
**解决**：在 IDE 中运行更可靠，或安装 Java JDK 17+

### Q: 端口被占用
```bash
# 查看端口占用
lsof -i:8080
lsof -i:3000

# 结束进程
kill -9 <PID>
```

### Q: 数据库错误
```bash
cd backend
rm shop.db
# 重新启动后端，会自动创建
```

---

## 📊 项目状态

✅ **数据库**：已修复，使用新结构
✅ **后端代码**：已更新支持 categories
✅ **前端代码**：已修复 API URL 和 pid 问题
✅ **购物车功能**：完整实现

现在所有功能都已就绪！
