<script setup lang="ts">
defineProps<{
  visible: boolean
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'select', emoji: string): void
}>()

const emojiList = [
  '😀', '😃', '😄', '😁', '😆', '😅', '🤣', '😂',
  '🙂', '😊', '😇', '🥰', '😍', '🤩', '😘', '😗',
  '😚', '😙', '🥲', '😋', '😛', '😜', '🤪', '😝',
  '🤑', '🤗', '🤭', '🤫', '🤔', '🤐', '🤨', '😐',
  '😑', '😶', '😏', '😒', '🙄', '😬', '🤥', '😌',
  '😔', '😪', '🤤', '😴', '😷', '🤒', '🤕', '🤢',
  '🤮', '🥵', '🥶', '🥴', '😵', '🤯', '🤠', '🥳',
  '🥸', '😎', '🤓', '🧐', '😕', '😟', '🙁', '☹️',
  '😮', '😯', '😲', '😳', '🥺', '😦', '😧', '😨',
  '😰', '😥', '😢', '😭', '😱', '😖', '😣', '😞',
]

const selectEmoji = (emoji: string) => {
  emit('select', emoji)
}

const closeModal = () => {
  emit('update:visible', false)
}
</script>

<template>
  <Teleport to="body">
    <div v-if="visible" class="emoji-selector-overlay" @click="closeModal">
      <div class="emoji-selector-modal" @click.stop>
        <div class="modal-header">
          <h3>选择表情</h3>
          <button class="modal-close" @click="closeModal">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18" />
              <line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <div class="emoji-grid">
            <button
              v-for="emoji in emojiList"
              :key="emoji"
              class="emoji-item"
              @click="selectEmoji(emoji)"
            >
              {{ emoji }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.emoji-selector-overlay {
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

.emoji-selector-modal {
  background-color: #fff;
  border-radius: 12px;
  width: 400px;
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
  padding: 16px;
  overflow-y: auto;
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(10, 1fr);
  gap: 8px;
}

.emoji-item {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 8px;
  background-color: #f9fafb;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 24px;
}

.emoji-item:hover {
  background-color: #eff6ff;
  transform: scale(1.1);
}
</style>