<script setup lang="ts">
import { ref, watch } from 'vue'
import { Icon } from '@iconify/vue'

const props = withDefaults(defineProps<{
  visible: boolean
  icon: string
  title?: string
  defaultName?: string
}>(), {
  title: '新建分类',
  defaultName: ''
})

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'confirm', category: { name: string; icon: string }): void
  (e: 'open-icon-selector'): void
  (e: 'cancel'): void
}>()

const name = ref(props.defaultName)

watch(() => props.visible, (newVal) => {
  if (newVal) {
    name.value = props.defaultName || ''
  }
})

watch(() => props.defaultName, (newVal) => {
  if (props.visible) {
    name.value = newVal || ''
  }
})

const close = () => {
  emit('update:visible', false)
  emit('cancel')
}

const confirm = () => {
  if (!name.value.trim() || !props.icon) return
  emit('confirm', { name: name.value.trim(), icon: props.icon })
  close()
}
</script>

<template>
  <div v-if="visible" class="modal-overlay" @click="close">
    <div class="modal-content" @click.stop>
      <div class="modal-header">
        <h3>{{ title }}</h3>
        <button class="modal-close" @click="close">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18" />
            <line x1="6" y1="6" x2="18" y2="18" />
          </svg>
        </button>
      </div>
      <div class="modal-body">
        <div class="form-group">
          <label>分类名称</label>
          <input
            v-model="name"
            type="text"
            placeholder="请输入分类名称"
            class="category-input"
            @keyup.enter="confirm"
          />
        </div>
        <div class="form-group">
          <label>选择图标</label>
          <button type="button" class="icon-select-button" @click.stop="emit('open-icon-selector')">
            <Icon v-if="icon" :icon="icon" class="icon-preview" />
            <span v-else>点击选择图标</span>
          </button>
        </div>
      </div>
      <div class="modal-footer">
        <button class="modal-cancel" @click="close">取消</button>
        <button
          class="modal-confirm"
          :disabled="!name.trim() || !icon"
          @click="confirm"
        >
          确定
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.modal-overlay {
  position: fixed;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background-color: #fff;
  border-radius: 12px;
  width: 100%;
  max-width: 400px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

.modal-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: space-between;

  h3 {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
    color: #1e293b;
  }
}

.modal-close {
  background: none;
  border: none;
  color: #94a3b8;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;

  &:hover {
    background-color: #f1f5f9;
    color: #64748b;
  }

  svg {
    width: 20px;
    height: 20px;
  }
}

.modal-body {
  padding: 20px;
}

.form-group {
  margin-bottom: 16px;

  label {
    display: block;
    font-size: 14px;
    font-weight: 500;
    color: #475569;
    margin-bottom: 6px;
  }
}

.category-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 14px;
  transition: all 0.2s;

  &:focus {
    outline: none;
    border-color: #3b82f6;
    box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
  }
}

.icon-select-button {
  width: 100%;
  padding: 10px 12px;
  border: 1px dashed #e2e8f0;
  border-radius: 6px;
  background-color: #f8fafc;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #64748b;
  font-size: 14px;
  transition: all 0.2s;

  &:hover {
    border-color: #3b82f6;
    background-color: #f1f5f9;
    color: #3b82f6;
  }
}

.icon-preview {
  width: 24px;
  height: 24px;
}

.modal-footer {
  padding: 16px 20px;
  border-top: 1px solid #f1f5f9;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.modal-cancel {
  padding: 8px 16px;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
  background-color: #fff;
  color: #64748b;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;

  &:hover {
    background-color: #f8fafc;
    border-color: #cbd5e1;
  }
}

.modal-confirm {
  padding: 8px 16px;
  border-radius: 6px;
  border: none;
  background-color: #3b82f6;
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;

  &:hover {
    background-color: #2563eb;
  }

  &:disabled {
    background-color: #94a3b8;
    cursor: not-allowed;
  }
}
</style>
