# Halo 插件开发指南

> 本指南总结了在 Soul Echoes 项目开发过程中积累的 Halo 插件开发经验

---

## 🎯 Halo 插件核心概念

### 1. Extension 机制

Halo 使用 Kubernetes 的 Extension 机制来扩展内容类型。

**定义 Extension：**
```java
@Singular
private final ExtensionClient extensionClient;
```

**常见操作：**
```java
// 创建
extensionClient.create(diaryNote);

// 读取
extensionClient.fetch(DiaryNote.class, name);

// 更新
extensionClient.update(diaryNote);

// 删除
extensionClient.delete(DiaryNote.class, name);
```

### 2. Reactive 编程

Halo 后端使用 Spring Reactor 进行响应式编程。

**关键点：**
- ✅ 使用 `Mono` 表示单个值
- ✅ 使用 `Flux` 表示多个值
- ✅ 使用 `Mono.fromRunnable()` 包装副作用操作
- ✅ 使用 `.then()` 链接操作

**错误示例：**
```java
// ❌ 错误：直接调用delete返回Mono.empty
public Mono<Void> deleteDiary(String name) {
    return extensionClient.delete(DiaryNote.class, name);  // 不会真正执行
}
```

**正确示例：**
```java
// ✅ 正确：使用 Mono.fromRunnable 包装
public Mono<Void> deleteDiary(String name) {
    return Mono.fromRunnable(() -> extensionClient.delete(DiaryNote.class, name))
        .then();
}
```

### 3. GVK (GroupVersionKind) 配置

每个 Extension 都需要定义 GVK：

```java
@GVK(
    group = "diary.miany.run",
    version = "v1alpha1",
    kind = "DiaryNote",
    pluralKind = "diarynotes",
    plural = "diarynotes"
)
public class DiaryNote extends Extension implements ClusterGetter<DiaryNote.DiaryNoteSpec> {
    
    @JSONField
    private DiaryNoteSpec spec;
    
    public DiaryNoteSpec getSpec() {
        return spec;
    }
}
```

---

## 🖥️ 前端开发指南

### 1. Halo API 客户端

**导入：**
```typescript
import { diaryApiClient } from '@/api/diary'
```

**常用方法：**
```typescript
// 列表查询
const response = await diaryApiClient.diaries.list({
  page: 1,
  size: 10
})
const diaries = response.data.items

// 获取单个
const diary = await diaryApiClient.diaries.get(name)

// 创建
const newDiary = await diaryApiClient.diaries.create(payload)

// 更新
const updatedDiary = await diaryApiClient.diaries.update(name, payload)

// 删除
await diaryApiClient.diaries.delete(name)
```

### 2. 响应式数据管理

**使用 Vue Composables：**
```typescript
// ui/src/composables/useDiary.ts
import { ref, computed } from 'vue'

export function useDiary() {
  const diaryList = ref<DiaryItem[]>([])
  
  const loadDiaries = async () => {
    const response = await diaryApiClient.diaries.list({ page: 0, size: 100 })
    diaryList.value = [...response.data.items]  // 创建新数组触发响应式
  }
  
  const addDiary = async (diary: DiaryItem) => {
    const newDiary = await diaryApiClient.diaries.create(diary)
    diaryList.value = [newDiary, ...diaryList.value]  // 触发响应式
  }
  
  return {
    diaryList,
    loadDiaries,
    addDiary
  }
}
```

### 3. Halo 原生组件

**全局组件（无需import）：**

| 组件 | 用途 |
|------|------|
| `AttachmentSelectorModal` | 附件选择器 |
| `PostSelectorModal` | 文章选择器 |
| `CategorySelectorModal` | 分类选择器 |
| `TagSelectorModal` | 标签选择器 |
| `UserSelectorModal` | 用户选择器 |
| `IconSelectorModal` | 图标选择器 |
| `MenuSelectorModal` | 菜单选择器 |
| `ThemeSelectorModal` | 主题选择器 |

**全局指令：**

| 指令 | 用途 |
|------|------|
| `v-permission` | 权限控制 |
| `v-tooltip` | 悬浮提示 |
| `v-ripple` | 水波纹效果 |

**全局工具：**

| 工具 | 用途 |
|------|------|
| `$t` | 多语言国际化 |
| `$utils` | 通用工具类 |
| `$p` | 权限检查 |

### 4. 图标使用

**安装的图标库：**
- `@icon-park/vue` - 图标公园
- `@halo-dev/components` - Halo组件库图标
- `~icons/` - 动态图标导入

**使用方式：**
```vue
<template>
  <IconHome />
  <IconImageAddLine />
  <IconUserLine />
</template>

<script setup>
import { IconHome, IconImageAddLine, IconUserLine } from '@icon-park/vue'
</script>
```

**动态导入（推荐）：**
```vue
<template>
  <component :is="iconComponent" />
</template>

<script setup>
import { h } from 'vue'

// 使用 ~icons/ 前缀
const IconPhoto = () => import('~icons/tabler/photo')
</script>
```

### 5. 富文本编辑器

**安装：**
```bash
pnpm add @halo-dev/richtext-editor
```

**使用：**
```vue
<template>
  <RichTextEditor 
    v-if="editor" 
    :editor="editor" 
    locale="zh-CN" 
  />
</template>

<script setup>
import { ExtensionsKit, RichTextEditor } from '@halo-dev/richtext-editor'

const editor = useEditor({
  extensions: [
    ExtensionsKit.configure({
      placeholder: {
        placeholder: '有什么想说的吗...'
      }
    })
  ]
})
</script>
```

---

## 🐛 常见问题与解决方案

### 1. 后端删除不生效

**症状：** 调用删除API后，数据仍然存在

**原因：** Reactive 编程中直接调用 `extensionClient.delete()` 不会执行

**解决：**
```java
public Mono<Void> deleteDiary(String name) {
    return Mono.fromRunnable(() -> extensionClient.delete(DiaryNote.class, name))
        .then();
}
```

### 2. 前端更新时409冲突

**症状：** 连续更新同一资源时收到409 Conflict错误

**原因：** version 字段过期

**解决：**
```typescript
const updateDiary = async (name: string, diary: DiaryItem) => {
  // 1. 先获取最新数据
  const latest = await diaryApiClient.diaries.get(name)
  
  // 2. 使用最新的 version
  const payload = {
    ...diary,
    metadata: {
      ...diary.metadata,
      version: latest.metadata.version  // 关键！
    }
  }
  
  // 3. 提交更新
  return await diaryApiClient.diaries.update(name, payload)
}
```

### 3. 下拉框不显示

**症状：** 点击按钮后下拉菜单不出现

**原因：** CSS `overflow: hidden` 裁剪了下拉菜单

**解决：**
```css
/* ❌ 错误 */
.category-item-wrapper {
  overflow: hidden;  /* 会裁剪下拉菜单 */
}

/* ✅ 正确 */
.category-item-wrapper {
  overflow: visible;  /* 或移除 */
}
```

### 4. 下拉框点击无反应

**症状：** 点击下拉按钮没有响应

**原因：** 事件处理方式不正确

**解决：**
```vue
<!-- 模板 -->
<button @click.stop="toggleDropdown(name, $event)">

<!-- Script -->
const toggleDropdown = (name: string, e: Event) => {
  e.stopPropagation()  // 阻止事件冒泡
  activeDropdown.value = activeDropdown.value === name ? null : name
}
```

### 5. 删除后列表不刷新

**症状：** 删除数据后列表仍然显示已删除项

**原因：** Halo Extension 异步操作未完成

**解决：**
```typescript
const removeDiary = async (name: string) => {
  await diaryApiClient.diaries.delete(name)
  
  // 添加延迟确保 Halo 完成操作
  await new Promise(resolve => setTimeout(resolve, 500))
  
  // 重新加载数据
  await loadDiaries()
}
```

### 6. 附件选择器无法调用

**症状：** AttachmentSelectorModal 组件报错或不存在

**原因：** 这是 Halo 全局挂载的组件，不需要 import

**解决：**
```vue
<template>
  <!-- ✅ 直接使用，无需 import -->
  <AttachmentSelectorModal 
    v-model:visible="attachmentSelectorModal"
    :min="1"
    :max="9"
    :accepts="['image/*']"
    @select="onAttachmentsSelect"
  />
</template>

<script setup>
const attachmentSelectorModal = ref(false)

const onAttachmentsSelect = (attachments: AttachmentLike[]) => {
  console.log('Selected:', attachments)
}
</script>
```

---

## 📦 常用依赖

### 后端 (build.gradle)
```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-webflux'
    implementation 'run.halo.app:plugin'
    implementation 'org.springframework.security:spring-security-core'
}
```

### 前端 (package.json)
```json
{
  "dependencies": {
    "@halo-dev/components": "^2.23.0",
    "@halo-dev/richtext-editor": "^2.23.0",
    "@halo-dev/api-client": "^2.23.0",
    "@icon-park/vue": "^2.3.2",
    "vue": "^3.5.0"
  }
}
```

---

## 🔧 调试技巧

### 1. 浏览器控制台

**添加日志：**
```typescript
console.log('Loading diaries:', diaryList.value)
console.log('API response:', response.data)
console.error('Error:', error)
```

### 2. Halo 后端日志

**查看日志位置：**
- Halo 运行日志
- 插件启动日志

**关键日志点：**
```java
log.info("Creating diary: {}", diary.getMetadata().getName());
log.debug("Extension client operation: {}", operation);
```

### 3. API 测试

**Swagger 文档：**
- Halo 自带 OpenAPI 文档
- 测试 API 端点
- 检查响应格式

---

## 📚 参考资源

### 官方文档
- [Halo 开发者文档](https://docs.halo.run/)
- [Halo Plugin 开发指南](https://docs.halo.run/developer-guide/plugin/)
- [Halo API 参考](https://docs.halo.run/developer-guide/plugin/api-reference/)

### Vue 生态
- [Vue 3 文档](https://vuejs.org/)
- [TypeScript 文档](https://www.typescriptlang.org/)
- [Vite 文档](https://vitejs.dev/)

### 图标资源
- [Icon Park](https://iconpark.oceanengine.com/)
- [Halo Icons](https://github.com/halo-dev/icons)

---

## 🔄 Git 工作流规范

### 1. 远程仓库配置

**Gitee（国内）：**
- 默认分支：`master`
- 用途：主分支，生产环境部署

**GitHub（国际）：**
- 默认分支：`main`
- 用途：主分支，生产环境部署

**分支映射：**
- Gitee `master` ↔ GitHub `main`（同步镜像）

### 2. 分支命名规范

| 分支类型 | 命名格式 | 示例 |
|---------|---------|------|
| 功能分支 | `feature/xxx` | `feature/diary-statistics` |
| Bug修复 | `fix/xxx` | `fix/api-404-error` |
| 文档更新 | `docs/xxx` | `docs/update-readme` |
| 重构分支 | `refactor/xxx` | `refactor/database-schema` |
| 测试分支 | `test/xxx` | `test/api-test` |

### 3. 提交规范

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
feat: 实现日记统计功能

1. 添加 StatisticsDTO 数据传输对象
2. 实现统计API端点
3. 添加前端统计卡片组件

Closes #123
```

### 4. 同步流程

**从 Gitee 同步到 GitHub：**
```bash
# 添加远程仓库
git remote add gitee https://gitee.com/username/repo.git
git remote add github https://github.com/username/repo.git

# 从 Gitee master 拉取最新
git checkout master
git pull gitee master

# 推送到 GitHub main
git push github master:main
```

**从 GitHub 同步到 Gitee：**
```bash
git checkout main
git pull github main
git push gitee main:master
```

---

**最后更新：** 2026-06-05
**维护者：** Soul Echoes 开发团队
