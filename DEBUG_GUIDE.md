# 🐞 Debug 调试指南

## 快速开始

### 1. 启动 Debug 模式

```bash
./start-debug.sh
```

脚本会自动：
- ✅ 清理端口占用
- ✅ 以 Debug 模式启动后端（端口 8080，Debug 端口 5005）
- ✅ 启动前端服务器（端口 3000）
- ✅ 打开浏览器

### 2. 连接调试器

#### 🎯 IntelliJ IDEA / Cursor

**方式 A：使用预配置**（推荐）
1. 打开 IDE 后，在右上角的运行配置下拉框中选择 **"IEMS5718 Debug Attach"**
2. 点击 🐞 **Debug** 按钮
3. 看到 "Connected to the target VM" 表示连接成功

**方式 B：手动创建**
1. 菜单：`Run` → `Edit Configurations`
2. 点击 `+` → 选择 `Remote JVM Debug`
3. 配置：
   - Name: `IEMS5718 Debug`
   - Host: `localhost`
   - Port: `5005`
   - Use module classpath: `iems5718-shop-backend`
4. 点击 `OK` 保存
5. 点击 🐞 Debug 按钮连接

#### 🎯 VS Code

1. 创建或编辑 `.vscode/launch.json`：

```json
{
    "version": "0.2.0",
    "configurations": [
        {
            "type": "java",
            "name": "Attach to IEMS5718 Shop",
            "request": "attach",
            "hostName": "localhost",
            "port": 5005
        }
    ]
}
```

2. 按 `F5` 或点击 Debug 面板的绿色播放按钮
3. 选择 "Attach to IEMS5718 Shop"

### 3. 设置断点

在代码行号左侧点击，设置红色断点。推荐位置：

**ProductController.java**
```java
@PostMapping("/upload")
public ResponseEntity<?> createProductWithImage(...) {
    // 👈 在这里打断点，查看请求参数
    ...
}
```

**ProductService.java**
```java
public Product createProductWithImage(...) {
    // 👈 在这里打断点，查看业务逻辑
    ...
}
```

**CategoryService.java**
```java
public List<Category> getAllCategories() {
    // 👈 在这里打断点，查看数据库查询
    return categoryRepository.findAll();
}
```

### 4. 触发断点

1. 打开浏览器：`http://localhost:3000/admin.html`
2. 填写表单，点击 "Save Product"
3. IDE 会自动暂停在断点处
4. 你可以：
   - 查看所有变量的值
   - 单步执行代码（F8 - Step Over, F7 - Step Into）
   - 在 Debug 控制台执行表达式
   - 修改变量值

---

## 常用快捷键

### IntelliJ IDEA / Cursor
- `F8` - Step Over（单步跳过）
- `F7` - Step Into（单步进入）
- `Shift + F8` - Step Out（跳出）
- `F9` - Resume（继续执行）
- `Ctrl/Cmd + F8` - 切换断点
- `Alt + F9` - Run to Cursor（运行到光标处）

### VS Code
- `F10` - Step Over
- `F11` - Step Into
- `Shift + F11` - Step Out
- `F5` - Continue
- `F9` - Toggle Breakpoint

---

## 日志文件

- **后端日志**：`backend-debug.log`
  ```bash
  tail -f backend-debug.log
  ```

- **前端日志**：`frontend-debug.log`
  ```bash
  tail -f frontend-debug.log
  ```

---

## 停止服务

```bash
./stop-local.sh
```

或手动：
```bash
lsof -ti:8080 | xargs kill -9  # 停止后端
lsof -ti:5005 | xargs kill -9  # 停止 Debug 端口
lsof -ti:3000 | xargs kill -9  # 停止前端
```

---

## 故障排查

### 问题 1：无法连接到 5005 端口

**检查后端是否启动**：
```bash
lsof -i:5005
```

**查看后端日志**：
```bash
tail -n 50 backend-debug.log
```

应该看到：
```
Listening for transport dt_socket at address: 5005
```

### 问题 2：断点不生效

1. **确认调试器已连接**：IDE 底部应显示 "Connected to the target VM"
2. **确认断点设置正确**：断点应该是实心红点，不是空心
3. **重新编译代码**：如果修改了代码但没重新编译，断点位置可能不对
   ```bash
   cd backend && mvn clean compile
   ```
4. **重启 Debug 服务**：
   ```bash
   ./stop-local.sh
   ./start-debug.sh
   ```

### 问题 3：Debug 端口被占用

```bash
# 查找占用进程
lsof -i:5005

# 强制停止
lsof -ti:5005 | xargs kill -9
```

### 问题 4：IDE 连接后立即断开

检查防火墙是否阻止了连接，或尝试修改 `start-debug.sh` 中的 `suspend` 参数：

```bash
# 改为 suspend=y，等待调试器连接后才启动
-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=*:5005
```

---

## 高级技巧

### 条件断点

右键点击断点 → 输入条件表达式，例如：
```java
productId == 1
name.equals("Test Product")
```

断点只在条件满足时才会暂停。

### 表达式求值

在 Debug 模式下：
1. 选中代码中的表达式
2. 右键 → "Evaluate Expression" (Alt + F8)
3. 可以动态执行代码并查看结果

### 监视变量

在 Debug 窗口的 "Watches" 面板添加变量或表达式，实时监控其值的变化。

---

## 访问地址

- 🏠 **首页**: http://localhost:3000/index.html
- 🛡️ **管理后台**: http://localhost:3000/admin.html
- 🔌 **API 文档**: http://localhost:8080/api/products
- 🐞 **Debug 端口**: localhost:5005

---

**Happy Debugging! 🎉**
