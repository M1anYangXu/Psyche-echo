# Soul Echoes 开发任务索引

> **项目:** Soul Echoes (Halo日记笔记插件)
> **工作目录:** `m:\Halo\plugin-soul-echoes`
> **创建时间:** 2026年6月1日

---

## 📋 任务列表

### 已完成任务

| 编号 | 任务名称 | 状态 | 关键文件 | 描述 |
|------|----------|------|----------|------|
| 01 | 项目重命名 | ✅ 完成 | `settings.gradle`, `plugin.yaml` | 从 diary-note 重命名为 soul-echoes |
| 02 | 富文本编辑器集成 | ✅ 完成 | `DiaryEditor.vue` | 使用Halo原生RichTextEditor |
| 03 | 分类功能修复 | ✅ 完成 | `DiarySidebar.vue` | 修复下拉框点击无响应问题 |
| 04 | 下拉框交互修复 | ✅ 完成 | `DiaryItem.vue` | 统一事件处理方式 |
| 05 | 构建问题修复 | ✅ 完成 | `ui/build.gradle` | 修复Gradle和pnpm构建问题 |
| 06 | 图标组件问题 | ✅ 完成 | `ui/src/components/*` | 修复VIcon未生效问题 |
| 07 | 前后端数据关联 | ✅ 完成 | `useDiary.ts`, `api/diary.ts` | 实现分类与日记关联 |
| 08 | 样式优化 | ✅ 完成 | `*.vue` | 去除点赞和评论样式 |
| 09 | 插件热重载 | ✅ 完成 | `DiaryNotePlugin.java` | 实现开发时热重载 |
| 10 | 日记编辑功能 | ✅ 完成 | `DiaryItem.vue`, `HomeView.vue` | 完善增删改查功能 |

### 待完成任务

| 编号 | 任务名称 | 优先级 | 描述 |
|------|----------|--------|------|
| 11 | 前台展示页面 | 🔜 下一个 | 实现前台日记展示页面 |

---

## 🎯 核心功能清单

### ✅ 已实现
- [x] 日记创建（富文本编辑器）
- [x] 日记编辑（图片上传、删除）
- [x] 日记删除（自动刷新）
- [x] 日记列表展示（分类过滤）
- [x] 分类管理（创建、编辑、删除、重名检查）
- [x] 分类下拉框（点击外部关闭）
- [x] 图片附件选择器（使用原生AttachmentSelectorModal）

### 🔄 进行中
- [ ] 前台日记展示页面开发

### 📝 计划中
- [ ] 用户个人页面集成
- [ ] 多媒体支持优化
- [ ] 数据导入导出功能

---

## 🛠 技术栈

### 后端
- **框架:** Halo Plugin (Spring Reactor)
- **语言:** Java 17
- **API风格:** Halo Extension API
- **包结构:** `com.miany.soulechoes`

### 前端
- **框架:** Vue 3 + TypeScript
- **UI库:** @halo-dev/components
- **富文本:** @halo-dev/richtext-editor
- **构建工具:** Rspack (rsbuild)

---

## 📁 关键文件路径

### 后端核心
- 插件入口: `src/main/java/com/miany/soulechoes/DiaryNotePlugin.java`
- 业务逻辑: `src/main/java/com/miany/soulechoes/service/impl/DiaryServiceImpl.java`
- API端点: `src/main/java/com/miany/soulechoes/endpoint/DiaryEndpoint.java`

### 前端核心
- 主页面: `ui/src/views/HomeView.vue`
- 日记编辑器: `ui/src/components/DiaryEditor.vue`
- 分类侧边栏: `ui/src/components/DiarySidebar.vue`
- 日记项组件: `ui/src/components/DiaryItem.vue`
- 数据管理: `ui/src/composables/useDiary.ts`
- API客户端: `ui/src/api/diary.ts`

---

## ⚠️ 重要经验总结

### 1. Halo Extension API 路径
- ✅ 使用 `/apis/${API_VERSION}/diarynotes` 而非自定义端点
- ✅ 创建/更新/删除都需要完整的对象结构（apiVersion, kind, metadata）
- ✅ 更新前需要先GET获取最新version避免409冲突

### 2. 前端响应式问题
- ✅ 删除后添加延迟（500ms）确保Halo Extension完成操作
- ✅ 使用展开运算符创建新数组触发Vue响应式更新
- ✅ `allDiaryList` 保存所有日记，计算分类count

### 3. Vue组件事件处理
- ✅ 下拉框使用 `@click.stop` + 函数内部 `event.stopPropagation()`
- ✅ 移除 `overflow: hidden` 避免下拉菜单被裁剪
- ✅ 点击外部关闭使用 `handleClickOutside`

### 4. Halo原生组件使用
- ✅ `AttachmentSelectorModal` 直接使用，无需import（全局挂载）
- ✅ `RichTextEditor` 来自 `@halo-dev/richtext-editor`
- ✅ 图标使用 `~icons/` 前缀导入

---

## 🔗 相关文档

- [项目重命名记录](./01-项目重命名.md)
- [富文本编辑器集成](./02-富文本编辑器集成.md)
- [分类功能修复](./03-分类功能修复.md)
- [下拉框交互修复](./04-下拉框交互修复.md)
- [构建问题修复](./05-构建问题修复.md)
- [图标组件问题](./06-图标组件问题.md)
- [前后端数据关联](./07-前后端数据关联.md)
- [样式优化](./08-样式优化.md)
- [插件热重载](./09-插件热重载.md)

---

**最后更新:** 2026-06-01
**维护者:** Soul Echoes 开发团队
