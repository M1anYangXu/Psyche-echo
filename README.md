# psyche-echo

> 记录(个人) - Halo 个人笔记记录插件

[![CI](https://github.com/miany/psyche-echo/actions/workflows/ci.yaml/badge.svg)](https://github.com/miany/psyche-echo/actions/workflows/ci.yaml)
[![CD](https://github.com/miany/psyche-echo/actions/workflows/cd.yaml/badge.svg)](https://github.com/miany/psyche-echo/actions/workflows/cd.yaml)
[![License](https://img.shields.io/github/license/miany/psyche-echo)](https://github.com/miany/psyche-echo/blob/main/LICENSE)

---

## 功能特性

- 📝 **笔记管理**：创建、编辑、删除个人笔记
- 📁 **分类管理**：为笔记创建自定义分类
- 🖼️ **媒体支持**：支持在笔记中添加图片和媒体文件
- 📊 **访问统计**：记录笔记的访问次数
- 🎨 **图标选择**：为分类自定义图标
- 😊 **表情支持**：在笔记中添加表情符号

## 技术栈

- **后端**: Java 21 + Spring Boot
- **前端**: Vue 3 + TypeScript + RSBuild
- **框架**: Halo 2.23.0+

## 快速开始

### 安装要求

- Halo >= 2.23.0
- Java >= 21

### 安装方式

1. **从 Halo 应用市场安装**（推荐）
   - 打开 Halo 管理后台
   - 进入「应用」->「应用市场」
   - 搜索「记录(个人)」或「psyche-echo」
   - 点击安装即可

2. **手动安装**
   ```bash
   # 下载最新版本的插件 JAR 文件
   wget https://github.com/miany/psyche-echo/releases/latest/download/psyche-echo.jar
   
   # 将 JAR 文件复制到 Halo 插件目录
   cp psyche-echo.jar /path/to/halo/plugins/
   ```

### 使用说明

1. 安装插件后，在 Halo 管理后台左侧菜单中找到「记录(个人)」
2. 点击进入后可以：
   - 创建新笔记
   - 管理笔记分类
   - 查看和编辑已有笔记

## 开发指南

### 开发环境

- Java 21+
- Node.js 18+
- pnpm

### 开发命令

```bash
# 启动 Halo 开发服务器（包含插件）
./gradlew haloServer

# 开发前端（需要先进入 ui 目录）
cd ui
pnpm install
pnpm dev
```

### 构建命令

```bash
# 构建完整插件
./gradlew build

# 仅构建前端
cd ui
pnpm build
```

构建完成后，可以在 `build/libs` 目录找到插件 JAR 文件。

### 项目结构

```
psyche-echo/
├── src/main/java/com/miany/psycheecho/
│   ├── config/          # 配置类
│   ├── content/         # 内容模型
│   ├── dto/             # 数据传输对象
│   ├── endpoint/        # API 端点
│   ├── service/         # 业务逻辑
│   └── PsycheEchoPlugin.java  # 插件主类
├── src/main/resources/
│   ├── openapi/         # API 文档
│   ├── logo.png         # 插件图标
│   └── plugin.yaml      # 插件配置
├── ui/                  # 前端代码
├── build.gradle         # 构建配置
└── README.md            # 项目说明
```

## API 接口

### 笔记接口

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/apis/api.echo.miany.run/v1alpha1/notes` | 获取笔记列表 |
| GET | `/apis/api.echo.miany.run/v1alpha1/notes/{name}` | 获取单条笔记 |
| POST | `/apis/api.echo.miany.run/v1alpha1/notes` | 创建笔记 |
| PUT | `/apis/api.echo.miany.run/v1alpha1/notes/{name}` | 更新笔记 |
| DELETE | `/apis/api.echo.miany.run/v1alpha1/notes/{name}` | 删除笔记 |

### 分类接口

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/apis/api.echo.miany.run/v1alpha1/categories` | 获取分类列表 |
| GET | `/apis/api.echo.miany.run/v1alpha1/categories/{name}` | 获取分类详情 |
| POST | `/apis/api.echo.miany.run/v1alpha1/categories` | 创建分类 |
| PUT | `/apis/api.echo.miany.run/v1alpha1/categories/{name}` | 更新分类 |
| DELETE | `/apis/api.echo.miany.run/v1alpha1/categories/{name}` | 删除分类 |

## 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建功能分支 (`git checkout -b feature/your-feature`)
3. 提交更改 (`git commit -am 'Add some feature'`)
4. 推送到分支 (`git push origin feature/your-feature`)
5. 创建 Pull Request

## 许可证

[GPL-3.0](https://github.com/miany/psyche-echo/blob/main/LICENSE) © miany

## 作者

- **miany** - [https://gitee.com/miany](https://gitee.com/miany)

---

**如果这个项目对你有帮助，请给它一个 ⭐！**