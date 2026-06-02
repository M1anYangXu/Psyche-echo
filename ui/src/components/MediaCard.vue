<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  media: {
    url: string
    type?: string
    cover?: string
    displayName?: string
  }
}>()

const emit = defineEmits<{
  (e: 'remove'): void
}>()

const isImage = computed(() => {
  return props.media.type?.startsWith('image/') || props.media.url.match(/\.(jpg|jpeg|png|gif|webp)$/i)
})

const displayUrl = computed(() => {
  return props.media.cover || props.media.url
})
</script>

<template>
  <div class="media-card">
    <div class="media-content">
      <img
        v-if="isImage"
        :src="displayUrl"
        :alt="media.displayName || 'media'"
        class="media-image"
        loading="lazy"
      />
      <div v-else class="media-placeholder">
        <span>{{ media.displayName || '文件' }}</span>
      </div>
      <button class="remove-btn" @click="emit('remove')">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="18" y1="6" x2="6" y2="18"/>
          <line x1="6" y1="6" x2="18" y2="18"/>
        </svg>
      </button>
    </div>
  </div>
</template>

<style scoped>
.media-card {
  position: relative;
  width: 100%;
  height: 100%;
  border-radius: 0.375rem;
  overflow: hidden;
  background-color: #f3f4f6;
}

.media-content {
  position: relative;
  width: 100%;
  height: 100%;
}

.media-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.media-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #e5e7eb;
  color: #6b7280;
  font-size: 12px;
}

.remove-btn {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(0, 0, 0, 0.5);
  border: none;
  border-radius: 50%;
  color: #fff;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.2s;
}

.media-card:hover .remove-btn {
  opacity: 1;
}

.remove-btn:hover {
  background-color: rgba(239, 68, 68, 0.8);
}

.remove-btn svg {
  width: 14px;
  height: 14px;
}
</style>