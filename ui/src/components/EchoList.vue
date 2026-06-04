<script setup lang="ts">
import { computed } from 'vue'
import EchoItem from './EchoItem.vue'
import type { EchoItem as EchoItemType } from '@/types'

const props = defineProps<{
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

const MAX_DISPLAY = 10

const displayedEchoes = computed(() => {
  return props.echoes.slice(0, MAX_DISPLAY)
})

const hasMore = computed(() => {
  return props.echoes.length > MAX_DISPLAY
})
</script>

<template>
  <div class="echo-list-container">
    <div v-if="!isLoading" class="echo-list">
      <EchoItem
        v-for="echo in displayedEchoes"
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
      <div v-if="hasMore" class="more-hint">
        <p>仅显示前{{ MAX_DISPLAY }}条，共{{ echoes.length }}条</p>
      </div>
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

.more-hint {
  text-align: center;
  padding: 20px 0;
  color: #94a3b8;
  font-size: 14px;
  border-top: 1px solid #f1f5f9;
  margin-top: 16px;
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
