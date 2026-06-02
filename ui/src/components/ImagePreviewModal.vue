<script setup lang="ts">
defineProps<{
  visible: boolean
  url: string
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
}>()

const close = () => {
  emit('update:visible', false)
}
</script>

<template>
  <div v-if="visible" class="image-preview-modal" @click="close">
    <div class="preview-content" @click.stop>
      <button class="preview-close-btn" @click="close">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="18" y1="6" x2="6" y2="18" />
          <line x1="6" y1="6" x2="18" y2="18" />
        </svg>
      </button>
      <img :src="url" alt="preview" class="preview-image" />
    </div>
  </div>
</template>

<style scoped lang="scss">
.image-preview-modal {
  position: fixed;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.75);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 24px;
}

.preview-content {
  position: relative;
  max-width: 90vw;
  max-height: 90vh;
}

.preview-image {
  display: block;
  max-width: 100%;
  max-height: 90vh;
  object-fit: contain;
  border-radius: 8px;
}

.preview-close-btn {
  position: absolute;
  top: -40px;
  right: -40px;
  background: none;
  border: none;
  color: #fff;
  cursor: pointer;
  padding: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s;

  &:hover {
    transform: scale(1.1);
  }

  svg {
    width: 24px;
    height: 24px;
  }
}

@media (max-width: 640px) {
  .preview-close-btn {
    top: -48px;
    right: 0;
  }
}
</style>
