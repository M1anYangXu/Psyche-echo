<script setup lang="ts">
import { ref } from 'vue'
import { Icon } from '@iconify/vue'

defineProps<{
  visible: boolean
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'select', iconName: string): void
}>()

const customIcon = ref('')

const iconList = [
  'ri-folder-fill',
  'ri-mail-fill',
  'ri-file-text-fill',
  'ri-showers-fill',
  'ri-zzz-line',
  'ri-edit-2-fill',
  'ri-time-fill',
  'ri-star-fill',
  'ri-cloud-fill',
  'ri-moon-fill',
  'ri-heart-3-fill',
  'ri-sun-fill',
  'ri-calendar-fill',
  'ri-film-fill',
  'ri-map-pin-fill',
]

const selectIcon = (iconName: string) => {
  emit('select', iconName.replace('-', ':'))
}

const applyCustomIcon = () => {
  if (customIcon.value.trim()) {
    emit('select', customIcon.value.trim())
    customIcon.value = ''
  }
}

const closeModal = () => {
  emit('update:visible', false)
}
</script>

<template>
  <Teleport to="body">
    <div v-if="visible" class="icon-selector-overlay" @click="closeModal">
      <div class="icon-selector-modal" @click.stop>
        <div class="modal-header">
          <h3>选择图标</h3>
          <button class="modal-close" @click="closeModal">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18" />
              <line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <div class="custom-icon-section">
            <p class="icon-hint">
              在 <a href="https://icones.js.org" target="_blank" rel="noopener">icones.js.org</a> 搜索图标，复制图标名称粘贴到下方输入框
            </p>
            <div class="custom-icon-input-wrapper">
              <input
                v-model="customIcon"
                type="text"
                placeholder="例如: ri:heart-fill"
                class="custom-icon-input"
                @keyup.enter="applyCustomIcon"
              />
              <button class="apply-icon-btn" @click="applyCustomIcon">应用</button>
            </div>
          </div>
          <div class="icon-grid">
            <button
              v-for="iconName in iconList"
              :key="iconName"
              class="icon-item"
              @click="selectIcon(iconName)"
            >
              <Icon :icon="iconName" />
            </button>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.icon-selector-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.icon-selector-modal {
  background-color: #fff;
  border-radius: 12px;
  width: 500px;
  max-width: 90vw;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #e2e8f0;
}

.modal-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.modal-close {
  padding: 4px;
  border: none;
  background: transparent;
  color: #9ca3af;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.2s;
}

.modal-close:hover {
  background-color: #f3f4f6;
  color: #1f2937;
}

.modal-close svg {
  width: 16px;
  height: 16px;
}

.modal-body {
  padding: 20px;
  overflow-y: auto;
}

.custom-icon-section {
  margin-bottom: 16px;
  padding: 12px;
  background-color: #f8fafc;
  border-radius: 8px;
}

.icon-hint {
  margin: 0 0 10px 0;
  font-size: 13px;
  color: #64748b;
  line-height: 1.5;
}

.icon-hint a {
  color: #3b82f6;
  text-decoration: none;
}

.icon-hint a:hover {
  text-decoration: underline;
}

.custom-icon-input-wrapper {
  display: flex;
  gap: 8px;
}

.custom-icon-input {
  flex: 1;
  padding: 10px 14px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 13px;
  box-sizing: border-box;
  transition: border-color 0.2s;
}

.custom-icon-input:focus {
  outline: none;
  border-color: #3b82f6;
}

.apply-icon-btn {
  padding: 10px 18px;
  border: none;
  border-radius: 6px;
  background-color: #3b82f6;
  color: #fff;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.apply-icon-btn:hover {
  background-color: #2563eb;
}

.apply-icon-btn:active {
  background-color: #1d4ed8;
}

.icon-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 8px;
}

.icon-item {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background-color: #f9fafb;
  cursor: pointer;
  transition: all 0.2s;
  color: #4b5563;
}

.icon-item:hover {
  border-color: #3b82f6;
  background-color: #eff6ff;
  color: #3b82f6;
}

.icon-item svg {
  width: 24px;
  height: 24px;
}
</style>
