# Backend

Java Spring Boot + SQLite

## 运行

```bash
./run.sh          # macOS/Linux
# 或
run.bat           # Windows
```

## API

- `GET /api/products` - 获取所有产品
- `GET /api/products/{id}` - 获取单个产品
- `GET /api/products/search?q={keyword}` - 搜索产品

## 配置

编辑 `src/main/resources/application.properties` 修改端口等配置
