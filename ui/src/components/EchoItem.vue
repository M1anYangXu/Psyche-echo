<script setup lang="ts">
import { ref, watch } from 'vue'
import { Icon } from '@iconify/vue'
import EchoEditor from './EchoEditor.vue'
import type { EchoItem } from '@/types'

const props = defineProps<{
  echo: EchoItem
}>()

const emit = defineEmits<{
  (e: 'edit', echo: EchoItem): void
  (e: 'delete', echo: EchoItem): void
  (e: 'save', echo: EchoItem): void
  (e: 'cancel', echo: EchoItem): void
  (e: 'preview', url: string): void
  (e: 'open-attachment'): void
  (e: 'update-medias', medias: Array<{ url: string; type: string; cover?: string; displayName?: string }>): void
}>()

const editContent = ref(props.echo.spec.content)
const editMedias = ref([...(props.echo.spec.medias || [])])
const showDropdown = ref(false)

watch(() => props.echo.spec.content, (newVal) => {
  editContent.value = newVal
})

watch(() => props.echo.spec.medias, (newMedias) => {
  editMedias.value = [...(newMedias || [])]
}, { immediate: true, deep: true })

const startEdit = () => {
  showDropdown.value = false
  emit('edit', props.echo)
}
const deleteEcho = () => {
  showDropdown.value = false
  emit('delete', props.echo)
}
const handleUpdateMedias = (newMedias: Array<{ url: string; type: string; cover?: string; displayName?: string }>) => {
  editMedias.value = [...newMedias]
  emit('update-medias', [...newMedias])
}

const saveEdit = () => {
  const updatedEcho = {
    ...props.echo,
    spec: {
      ...props.echo.spec,
      content: editContent.value,
      medias: [...editMedias.value]
    }
  }
  emit('save', updatedEcho)
}
const cancelEdit = () => emit('cancel', props.echo)
const previewImage = (url: string) => emit('preview', url)
const toggleDropdown = (e: Event) => {
  e.stopPropagation()
  showDropdown.value = !showDropdown.value
}

const formatDate = () => {
  if (props.echo.status?.time) {
    return props.echo.status.time
  }
  if (props.echo.metadata?.creationTimestamp) {
    return new Date(props.echo.metadata.creationTimestamp).toLocaleString()
  }
  return ''
}
</script>

<template>
  <div class="echo-item">
    <div class="echo-avatar">
      <div class="avatar-placeholder">
        <Icon icon="ri:user-line" :size="24" />
      </div>
    </div>
    <div class="echo-body">
      <div v-if="echo.editing" class="edit-mode">
        <EchoEditor
          v-model="editContent"
          :medias="editMedias"
          @update:medias="handleUpdateMedias"
          @open-attachment="$emit('open-attachment')"
        >
          <template #footer-right>
            <div class="edit-actions">
              <button class="cancel-btn" @click="cancelEdit">取消</button>
              <button class="save-btn" @click="saveEdit">保存</button>
            </div>
          </template>
        </EchoEditor>
      </div>
      <template v-else>
        <div class="echo-header">
          <span class="echo-author">{{ echo.spec.author }}</span>
          <span class="echo-time">{{ formatDate() }}</span>
          <div class="echo-more-wrapper">
            <button
              class="echo-more"
              @click.stop="toggleDropdown"
              :class="{ active: showDropdown }"
            >
              <Icon icon="ri:more-fill" :size="20" />
            </button>
            <div v-if="showDropdown" class="echo-dropdown-menu">
              <button class="echo-dropdown-item" @click.stop="startEdit">
                <Icon icon="ri:edit-2-fill" :size="16" />
                编辑
              </button>
              <button class="echo-dropdown-item danger" @click.stop="deleteEcho">
                <Icon icon="ri:delete-bin-2-fill" :size="16" />
                删除
              </button>
            </div>
          </div>
        </div>
        <div class="echo-text" v-html="echo.spec.content"></div>
        <div v-if="echo.spec.medias?.length" class="echo-medias">
          <ul class="medias-grid">
            <li
              v-for="(media, index) in echo.spec.medias"
              :key="index"
              @click="previewImage(media.url)"
              :class="{ 'media-file': !media.type?.startsWith('image/') }"
            >
              <img
                v-if="media.type?.startsWith('image/')"
                :src="media.cover || media.url"
                :alt="media.displayName || 'image'"
                class="echo-image"
              />
              <div v-else class="file-placeholder">
                <Icon icon="ri:image-line" :size="24" />
              </div>
            </li>
          </ul>
        </div>
      </template>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.echo-item {
  display: flex;
  gap: 16px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f1f5f9;

  &:last-child {
    border-bottom: none;
    padding-bottom: 0;
  }
}

.echo-avatar {
  flex-shrink: 0;
}

.avatar-placeholder {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background-color: #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;

  svg {
    width: 24px;
    height: 24px;
  }
}

.echo-body {
  flex: 1;
  min-width: 0;
}

.echo-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.echo-author {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
}

.echo-time {
  font-size: 13px;
  color: #94a3b8;
  flex: 1;
}

.echo-more-wrapper {
  position: relative;
}

.echo-more {
  background: none;
  border: none;
  color: #94a3b8;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;

  &:hover {
    background-color: #f1f5f9;
    color: #64748b;
  }

  &.active {
    background-color: #f1f5f9;
  }
}

.echo-dropdown-menu {
  position: absolute;
  right: 0;
  top: calc(100% + 4px);
  min-width: 120px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  padding: 4px;
  z-index: 9999;
}

.echo-dropdown-item {
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

  &.danger {
    color: #ef4444;

    &:hover {
      background-color: #fef2f2;
    }
  }
}

.echo-text {
  font-size: 15px;
  line-height: 1.6;
  color: #334155;
  margin: 0 0 12px 0;
  white-space: pre-wrap;
  word-break: break-word;

  :deep(p) {
    margin: 0;
  }
}

.echo-medias {
  margin-top: 12px;
}

.medias-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 8px;
  list-style: none;
  padding: 0;
  margin: 0;

  li {
    aspect-ratio: 1 / 1;
    border-radius: 8px;
    overflow: hidden;
    cursor: pointer;
    background-color: #f8fafc;
    border: 1px solid #f1f5f9;

    &:hover {
      opacity: 0.9;
    }
  }
}

.echo-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.edit-actions {
  display: flex;
  gap: 8px;
}

.cancel-btn, .save-btn {
  padding: 6px 16px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
}

.cancel-btn {
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  color: #64748b;

  &:hover {
    background: #e2e8f0;
  }
}

.save-btn {
  background: #3b82f6;
  border: none;
  color: #fff;

  &:hover {
    background: #2563eb;
  }
}

@media (max-width: 640px) {
  .medias-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>
