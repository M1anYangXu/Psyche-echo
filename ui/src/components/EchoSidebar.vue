<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { Icon } from '@iconify/vue'
import type { Category as EchoCategory } from '@/types'

const props = defineProps<{
  categories: EchoCategory[]
  selectedCategory: string
  showStats: boolean
}>()

const emit = defineEmits<{
  (e: 'select-category', name: string): void
  (e: 'toggle-stats'): void
  (e: 'open-new-category'): void
  (e: 'edit-category', category: EchoCategory): void
  (e: 'delete-category', name: string): void
  (e: 'reorder-categories', categories: EchoCategory[]): void
}>()

const getCategoryIcon = (icon: string): string => {
  return icon || 'ri:folder-fill'
}

const activeDropdown = ref<string | null>(null)
const draggingIndex = ref<number | null>(null)
const dragOverIndex = ref<number | null>(null)

const toggleDropdown = (name: string, event: Event) => {
  event.stopPropagation()
  activeDropdown.value = activeDropdown.value === name ? null : name
}

const handleEdit = (category: EchoCategory) => {
  emit('edit-category', category)
  activeDropdown.value = null
}

const handleDelete = (name: string) => {
  emit('delete-category', name)
  activeDropdown.value = null
}

const handleClickOutside = (event: MouseEvent) => {
  const target = event.target as HTMLElement
  if (!target.closest('.category-actions')) {
    activeDropdown.value = null
  }
}

const handleDragStart = (index: number, event: DragEvent) => {
  draggingIndex.value = index
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = 'move'
    event.dataTransfer.setData('text/plain', index.toString())
  }
}

const handleDragOver = (index: number, event: DragEvent) => {
  event.preventDefault()
  if (event.dataTransfer) {
    event.dataTransfer.dropEffect = 'move'
  }
  if (draggingIndex.value !== index) {
    dragOverIndex.value = index
  }
}

const handleDragLeave = () => {
  dragOverIndex.value = null
}

const handleDrop = (index: number, event: DragEvent) => {
  event.preventDefault()
  if (draggingIndex.value === null || draggingIndex.value === index) {
    draggingIndex.value = null
    dragOverIndex.value = null
    return
  }

  const newCategories = [...props.categories]
  const [draggedItem] = newCategories.splice(draggingIndex.value, 1)
  newCategories.splice(index, 0, draggedItem)

  emit('reorder-categories', newCategories)

  draggingIndex.value = null
  dragOverIndex.value = null
}

const handleDragEnd = () => {
  draggingIndex.value = null
  dragOverIndex.value = null
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<template>
  <aside class="echo-sidebar">
    <div class="sidebar-header">
      <h2 class="sidebar-title">
        <Icon icon="ri:book-read-fill" :size="20" />
        记录
      </h2>
      <button
        class="stats-btn"
        :class="{ active: showStats }"
        @click="emit('toggle-stats')"
        title="统计"
      >
        <Icon icon="ri:bar-chart-fill" :size="18" />
      </button>
    </div>

    <nav class="category-list">
      <div
        v-for="(category, index) in categories"
        :key="category.metadata.name"
        :class="[
          'category-item-wrapper',
          {
            active: selectedCategory === category.metadata.name,
            dragging: draggingIndex === index,
            'drag-over': dragOverIndex === index,
            'drag-over-top': dragOverIndex === index && draggingIndex !== null && draggingIndex < index,
            'drag-over-bottom': dragOverIndex === index && draggingIndex !== null && draggingIndex > index
          }
        ]"
        :draggable="category.metadata.name !== '默认'"
        @dragstart="handleDragStart(index, $event)"
        @dragover="handleDragOver(index, $event)"
        @dragleave="handleDragLeave"
        @drop="handleDrop(index, $event)"
        @dragend="handleDragEnd"
      >
        <div class="drag-handle" v-if="category.metadata.name !== '默认'">
          <Icon icon="ri:drag-move-line" :size="20" />
        </div>
        <button
          class="category-item"
          @click="emit('select-category', category.metadata.name)"
        >
          <Icon :icon="getCategoryIcon(category.spec.icon)" :size="20" class="category-icon" />
          <span class="category-name">{{ category.spec.name }}</span>
          <span class="category-count">{{ category.spec.count }}</span>
        </button>

        <div v-if="category.metadata.name !== '默认'" class="category-actions">
          <button
            class="action-btn"
            @click="toggleDropdown(category.metadata.name, $event)"
            :class="{ active: activeDropdown === category.metadata.name }"
          >
            <Icon icon="ri:more-fill" :size="22" />
          </button>

          <div
            v-if="activeDropdown === category.metadata.name"
            class="dropdown-menu"
          >
            <button class="dropdown-item" @click.stop="handleEdit(category)">
              <Icon icon="ri:edit-2-fill" :size="16" />
              编辑
            </button>
            <button class="dropdown-item delete" @click.stop="handleDelete(category.metadata.name)">
              <Icon icon="ri:delete-bin-2-fill" :size="16" />
              删除
            </button>
          </div>
        </div>

      </div>
    </nav>

    <button class="new-category-btn" @click="emit('open-new-category')">
      <Icon icon="ri:add-circle-fill" :size="20" />
      新建分类
    </button>
  </aside>
</template>

<style lang="scss" scoped>
.echo-sidebar {
  width: 280px;
  background: linear-gradient(145deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  padding: 20px;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.1);
  flex-shrink: 0;
  max-height: calc(100vh - 48px);
  overflow-y: hidden;
  overflow-x: hidden;
  border: 1px solid rgba(102, 126, 234, 0.1);

  &::-webkit-scrollbar {
    display: none;
  }
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  margin-bottom: 16px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.stats-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.2);
  border: none;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.9);
  transition: all 0.2s;

  &:hover {
    background: rgba(255, 255, 255, 0.3);
    transform: scale(1.05);
  }

  &.active {
    background: rgba(255, 255, 255, 0.4);
    color: #fff;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  }
}

.sidebar-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 700;
  color: #fff;

  svg {
    color: rgba(255, 255, 255, 0.9);
  }
}

.category-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
  overflow-y: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;

  &::-webkit-scrollbar {
    display: none;
  }
}

.category-item-wrapper {
  display: flex;
  align-items: stretch;
  background-color: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid transparent;
  border-left-width: 3px;
  cursor: grab;

  &:hover {
    background-color: #fafafa;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.08);
    border-color: rgba(102, 126, 234, 0.1);
  }

  &.active {
    background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
    border-color: rgba(102, 126, 234, 0.3);
    border-left-color: #667eea;
  }

  &.dragging {
    opacity: 0.5;
    cursor: grabbing;
    transform: scale(1.02);
    box-shadow: 0 8px 24px rgba(102, 126, 234, 0.2);
  }

  &.drag-over {
    border-color: #667eea;
    background: linear-gradient(135deg, rgba(102, 126, 234, 0.08) 0%, rgba(118, 75, 162, 0.08) 100%);
  }

  &.drag-over-top {
    border-top: 2px solid #667eea;
  }

  &.drag-over-bottom {
    border-bottom: 2px solid #667eea;
  }

  &:active {
    cursor: grabbing;
  }
}

.drag-handle {
  display: flex !important;
  align-items: center;
  justify-content: center;
  padding: 0 4px 0 6px;
  color: #94a3b8 !important;
  opacity: 1 !important;
  transition: color 0.2s;
  cursor: grab;
  pointer-events: auto;
  width: 32px;

  &:hover {
    color: #64748b;
  }

  svg {
    display: block !important;
    opacity: 1 !important;
    color: inherit;
    width: 18px !important;
    height: 18px !important;
  }
}

.category-item {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  background: transparent;
  border: none;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background-color: transparent;
  }

  .category-icon {
    color: #94a3b8;
  }

  .category-count {
    padding: 2px 8px;
    border-radius: 12px;
    background-color: #f1f5f9;
    color: #64748b;
    font-size: 12px;
    font-weight: 600;
  }
}

.category-actions {
  position: relative;
  display: flex;
  align-items: center;
  padding-right: 4px;

  .action-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 36px;
    height: 36px;
    border-radius: 8px;
    background: transparent;
    border: none;
    cursor: pointer;
    color: #94a3b8;
    transition: all 0.2s;
    font-size: 18px;

    &:hover {
      background-color: rgba(102, 126, 234, 0.1);
      color: #667eea;
    }

    &.active {
      background-color: rgba(102, 126, 234, 0.15);
      color: #667eea;
    }
  }

  .dropdown-menu {
    position: absolute;
    right: 0;
    top: 100%;
    margin-top: 4px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
    padding: 4px;
    z-index: 100;
    min-width: 120px;

    .dropdown-item {
      display: flex;
      align-items: center;
      gap: 8px;
      width: 100%;
      padding: 8px 12px;
      border-radius: 6px;
      background: transparent;
      border: none;
      cursor: pointer;
      color: #64748b;
      font-size: 14px;
      transition: all 0.2s;

      &:hover {
        background-color: rgba(102, 126, 234, 0.1);
        color: #667eea;
      }

      &.delete {
        &:hover {
          background-color: rgba(239, 68, 68, 0.1);
          color: #ef4444;
        }
      }
    }
  }
}

.category-item-wrapper.active .category-item {
  .category-icon {
    color: #667eea;
  }

  .category-count {
    background-color: rgba(102, 126, 234, 0.15);
    color: #667eea;
  }
}

.category-icon {
  color: #94a3b8;
}

.category-name {
  flex: 1;
  text-align: left;
  font-size: 14px;
  font-weight: 500;
  color: #475569;
}

.category-count {
  padding: 2px 8px;
  border-radius: 10px;
  background-color: #f1f5f9;
  color: #64748b;
  font-size: 12px;
  font-weight: 600;
}

.category-actions {
  position: relative;
  display: flex;
  align-items: center;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px;
  border: none;
  background: transparent;
  cursor: pointer;
  border-radius: 4px;
  color: #94a3b8;
  transition: all 0.2s;

  &:hover {
    background-color: #f1f5f9;
    color: #64748b;
  }

  &.active {
    background-color: #f1f5f9;
  }
}

.dropdown-menu {
  position: absolute;
  right: 0;
  top: calc(100% + 4px);
  min-width: 120px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  padding: 4px;
  z-index: 99999;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 10px 14px;
  border: none;
  background: transparent;
  cursor: pointer;
  border-radius: 4px;
  font-size: 14px;
  color: #475569;
  transition: all 0.15s;

  &:hover {
    background-color: #f1f5f9;
    color: #334155;
  }

  &.delete {
    color: #ef4444;

    &:hover {
      background-color: #fef2f2;
    }
  }
}

.new-category-btn {
  margin-top: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  padding: 12px 24px;
  border-radius: 10px;
  background: linear-gradient(135deg, #8b5cf6 0%, #a78bfa 100%);
  color: #fff;
  border: none;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 4px 12px rgba(139, 92, 246, 0.3);

  &:hover {
    background: linear-gradient(135deg, #7c3aed 0%, #9333ea 100%);
    box-shadow: 0 6px 16px rgba(139, 92, 246, 0.4);
    transform: translateY(-1px);
  }
}
</style>
