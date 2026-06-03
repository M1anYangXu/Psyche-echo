# Soul Echoes 项目规范

> **项目:** Soul Echoes - Halo日记笔记插件
> **工作目录:** `m:\Halo\plugin-soul-echoes`

---

## 🎯 项目概述

Soul Echoes（灵魂回响）是一个Halo博客系统的插件，用于记录日记、笔记、梦境、未来打算和内心独白。

### 技术栈

**后端：**
- Java 17
- Spring Reactor (响应式编程)
- Halo Extension API
- 包结构：`com.miany.soulechoes`

**前端：**
- Vue 3 + TypeScript
- @halo-dev/components
- @halo-dev/richtext-editor
- Rspack (rsbuild)

---

## 📁 项目结构

```
plugin-soul-echoes/
├── src/main/java/com/miany/soulechoes/
│   ├── DiaryNotePlugin.java          # 插件入口
│   ├── config/WebConfig.java         # Web配置
│   ├── content/                      # Halo Extension内容定义
│   │   ├── DiaryCategory.java       # 分类实体
│   │   └── DiaryNote.java           # 日记实体
│   ├── dto/                          # 数据传输对象
│   │   ├── request/                 # 请求DTO
│   │   └── response/                # 响应DTO
│   ├── endpoint/DiaryEndpoint.java   # API端点
│   └── service/                      # 业务逻辑
│       ├── DiaryService.java
│       └── impl/DiaryServiceImpl.java
├── ui/src/
│   ├── api/diary.ts                 # API客户端
│   ├── components/                   # Vue组件
│   │   ├── DiaryEditor.vue         # 日记编辑器
│   │   ├── DiaryItem.vue           # 日记项
│   │   ├── DiaryList.vue           # 日记列表
│   │   └── DiarySidebar.vue        # 分类侧边栏
│   ├── composables/
│   │   └── useDiary.ts             # 日记数据管理
│   └── views/
│       └── HomeView.vue             # 主页面
└── .trae/
    ├── tasks/                       # 开发任务记录
    └── rules/                       # 项目规范
```

---

## 🔧 开发规范

### 1. Halo Extension API 使用

**API路径格式：**
```typescript
/apiVersion: "diary.miany.run/v1alpha1"
/kind: "DiaryNote" | "DiaryCategory"
/endpoint: `/apis/${API_VERSION}/diarynotes` | `/apis/${API_VERSION}/diarycategories`
```

**重要提醒：**
- ✅ 使用 Halo Extension 自动注册的 API
- ❌ 不要使用自定义 Controller 端点（如 `/diary-note/notes`）

### 2. 数据操作规范

**创建数据：**
```typescript
// 需要完整的对象结构
const payload = {
  apiVersion: API_VERSION,
  kind: 'DiaryNote',
  metadata: {
    generateName: 'diary-note-'
  },
  spec: { /* ... */ }
}
```

**更新数据：**
```typescript
// 重要：先GET最新数据获取version
const latest = await diaryApiClient.diaries.get(name)
const payload = {
  apiVersion: API_VERSION,
  kind: 'DiaryNote',
  metadata: {
    name: name,
    version: latest.metadata.version  // 必须使用最新version
  },
  spec: { /* ... */ }
}
```

**删除数据：**
```typescript
// 后端使用 Mono.fromRunnable() 包装
Mono.fromRunnable(() -> extensionClient.delete(DiaryNote.class, name))
    .then()
```

### 3. 前端响应式规范

**数组更新：**
```typescript
// ✅ 使用展开运算符创建新数组
diaryList.value = [...diaryList.value, newDiary]

// ❌ 直接push可能不会触发更新
diaryList.value.push(newDiary)
```

**删除后刷新：**
```typescript
// 添加延迟确保 Halo Extension 完成操作
await new Promise(resolve => setTimeout(resolve, 500))
await loadDiaries()
```

### 4. Vue 组件规范

**下拉框实现：**
```vue
<!-- 模板中 -->
<button @click.stop="toggleDropdown(name, $event)">

<!-- Script中 -->
const toggleDropdown = (name: string, e: Event) => {
  e.stopPropagation()
  activeDropdown.value = activeDropdown.value === name ? null : name
}
```

**避免 overflow 裁剪：**
```css
/* ❌ 会裁剪下拉菜单 */
.category-item-wrapper {
  overflow: hidden;
}

/* ✅ 移除或使用 visible */
.category-item-wrapper {
  overflow: visible;
}
```

### 5. Halo 原生组件使用

**全局组件（无需import）：**
- `AttachmentSelectorModal` - 附件选择器
- `PostSelectorModal` - 文章选择器
- `CategorySelectorModal` - 分类选择器
- `TagSelectorModal` - 标签选择器
- `IconSelectorModal` - 图标选择器
- `v-permission` - 权限指令
- `v-tooltip` - 悬浮提示
- `v-ripple` - 水波纹效果
- `$t` - 国际化
- `$utils` - 工具类

**图标导入：**
```typescript
// ✅ 使用 ~icons/ 前缀
import IconImageAddLine from '~icons/ant-design/image-add-filled'
import TablerPhoto from '~icons/tabler/photo'

// ❌ 不要使用未安装的图标
```

### 6. API 客户端规范

**路径：**
```typescript
// ✅ Halo Extension API
/apiVersion = 'diary.miany.run/v1alpha1'

// ✅ 正确的数据提取
const data = response.data.data  // 自定义端点
const items = response.data.items  // Halo Extension API
```

---

## 🐛 常见问题解决方案

### Q1: 删除后页面不刷新
**原因：** Halo Extension 异步操作未完成
**解决：** 添加500ms延迟后重新加载

### Q2: 更新时出现409冲突
**原因：** version过期
**解决：** 更新前先GET最新数据获取最新version

### Q3: 下拉框不显示
**原因：** `overflow: hidden` 裁剪
**解决：** 移除或设置为 `visible`

### Q4: 下拉框点击无反应
**原因：** 事件处理方式错误
**解决：** 使用 `@click.stop` + 函数内部 `event.stopPropagation()`

### Q5: 附件选择器无法调用
**原因：** AttachmentSelectorModal 是全局组件
**解决：** 直接使用，无需import

---

## 📝 提交规范

**提交信息格式：**
```
<type>: <subject>

<body>

<footer>
```

**Type类型：**
- `feat`: 新功能
- `fix`: Bug修复
- `docs`: 文档更新
- `style`: 代码格式（不影响功能）
- `refactor`: 重构
- `test`: 测试
- `chore`: 构建/工具

**示例：**
```
feat: 实现日记编辑功能

1. 添加DiaryEditor组件
2. 集成富文本编辑器
3. 支持图片上传

Closes #123
```

---

## 🚀 常用命令

**前端构建：**
```bash
cd ui
pnpm install
pnpm build
```

**后端构建：**
```bash
./gradlew build
./gradlew jar
```

**开发模式：**
```bash
./gradlew bootRun
```

---

## 📚 参考文档

- [Halo插件开发文档](https://docs.halo.run/)
- [Halo API参考](https://docs.halo.run/developer-guide/plugin/api-reference/)
- [Vue 3文档](https://vuejs.org/)
- [TypeScript文档](https://www.typescriptlang.org/)

---

**最后更新：** 2026-06-01
**版本：** 1.0.0
