<script setup lang="ts">
import EchoItem from './EchoItem.vue'
import type { EchoItem as EchoItemType } from '@/types'

defineProps<{
  echoes: EchoItemType[]
  isLoading: boolean
}>()

const emit = defineEmits<{
  (e: 'edit', echo: EchoItemType): void
  (e: 'delete', echo: EchoItemType): void
  (e: 'save', echo: EchoItemType): void
  (e: 'cancel', echo: EchoItemType): void
  (e: 'preview', url: string): void
  (e: 'open-attachment'): void
  (e: 'update-medias', echo: EchoItemType, medias: Array<{ url: string; type: string; cover?: string; displayName?: string }>): void
}>()
</script>

<template>
  <div class="echo-list-container">
    <div v-if="!isLoading" class="echo-list">
      <EchoItem
        v-for="echo in echoes"
        :key="echo.metadata.name"
        :echo="echo"
        @edit="emit('edit', $event)"
        @delete="emit('delete', $event)"
        @save="emit('save', $event)"
        @cancel="emit('cancel', $event)"
        @preview="emit('preview', $event)"
        @open-attachment="emit('open-attachment')"
        @update-medias="emit('update-medias', echo, $event)"
      />
      <div v-if="echoes.length === 0" class="empty-state">
        <p>暂无echo，快去写一篇吧！</p>
      </div>
    </div>

    <div v-if="isLoading" class="loading">
      <div class="spinner"></div>
      <span>加载中...</span>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.echo-list {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
  color: #94a3b8;
  font-size: 15px;
}

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  color: #64748b;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f4f6;
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 12px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
