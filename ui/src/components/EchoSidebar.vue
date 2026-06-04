<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { Icon } from '@iconify/vue'
import type { Category as EchoCategory } from '@/types'

defineProps<{
  categories: EchoCategory[]
  selectedCategory: string
}>()

const emit = defineEmits<{
  (e: 'select-category', name: string): void
  (e: 'open-new-category'): void
  (e: 'edit-category', category: EchoCategory): void
  (e: 'delete-category', name: string): void
}>()

const getCategoryIcon = (icon: string): string => {
  return icon || 'ri:folder-fill'
}

const activeDropdown = ref<string | null>(null)

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
    </div>

    <nav class="category-list">
      <div
        v-for="category in categories"
        :key="category.metadata.name"
        :class="['category-item-wrapper', { active: selectedCategory === category.metadata.name }]"
      >
        <button
          class="category-item"
          @click="emit('select-category', category.metadata.name)"
        >
          <Icon :icon="getCategoryIcon(category.spec.icon)" :size="20" class="category-icon" />
          <span class="category-name">{{ category.spec.name }}</span>
          <span class="category-count">{{ category.spec.count }}</span>
        </button>

        <div class="category-actions">
          <button
            class="action-btn"
            @click="toggleDropdown(category.metadata.name, $event)"
            :class="{ active: activeDropdown === category.metadata.name }"
          >
            <Icon icon="ri:more-fill" :size="20" />
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
  background-color: #fff;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  flex-shrink: 0;
  max-height: calc(100vh - 48px);
  overflow-y: auto;
}

.sidebar-header {
  padding: 16px;
  background-color: #fff;
  border-radius: 4px;
  margin-bottom: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.sidebar-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
}

.category-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
  overflow-y: auto;
}

.category-item-wrapper {
  display: flex;
  align-items: stretch;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
  transition: all 0.2s;

  &:hover {
    background-color: #f8fafc;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  }

  &.active {
    background-color: #f1f5f9;
    border: 1px solid #dbeafe;
    border-left: 3px solid #3b82f6;
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

.category-item-wrapper.active .category-item {
  .category-icon {
    color: #3b82f6;
  }

  .category-count {
    background-color: #e0f2fe;
    color: #0284c7;
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
  gap: 6px;
  width: 100%;
  padding: 10px 20px;
  border-radius: 8px;
  background-color: #3b82f6;
  color: #fff;
  border: none;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background-color: #2563eb;
  }
}
</style>
