<script setup lang="ts">
import { ref, watch, computed } from 'vue'
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
const editWeather = ref(props.echo.spec.weather || '')
const editMood = ref(props.echo.spec.mood || '')
const showDropdown = ref(false)

const weatherIcons: Record<string, string> = {
  sunny: '☀️',
  cloudy: '☁️',
  lightRain: '🌧️',
  heavyRain: '⛈️',
  lightSnow: '❄️',
  heavySnow: '🌨️',
  windy: '🌬️',
  foggy: '🌫️',
}

const moodIcons: Record<string, string> = {
  happy: '😊',
  sad: '😢',
  angry: '😠',
  tired: '😴',
  excited: '🎉',
  anxious: '😰',
  peaceful: '😌',
  confused: '😕',
}

const displayWeather = computed(() => {
  return props.echo.spec.weather ? weatherIcons[props.echo.spec.weather] : ''
})

const displayMood = computed(() => {
  return props.echo.spec.mood ? moodIcons[props.echo.spec.mood] : ''
})

watch(() => props.echo.spec.content, (newVal) => {
  editContent.value = newVal
})

watch(() => props.echo.spec.medias, (newMedias) => {
  editMedias.value = [...(newMedias || [])]
}, { immediate: true, deep: true })

watch(() => props.echo.spec.weather, (newWeather) => {
  editWeather.value = newWeather || ''
})

watch(() => props.echo.spec.mood, (newMood) => {
  editMood.value = newMood || ''
})

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

const handleUpdateWeather = (weather: string) => {
  editWeather.value = weather
}

const handleUpdateMood = (mood: string) => {
  editMood.value = mood
}

const saveEdit = () => {
  const updatedEcho = {
    ...props.echo,
    spec: {
      ...props.echo.spec,
      content: editContent.value,
      medias: [...editMedias.value],
      weather: editWeather.value,
      mood: editMood.value,
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

const getWeekDay = () => {
  if (props.echo.metadata?.creationTimestamp) {
    const date = new Date(props.echo.metadata.creationTimestamp)
    const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    return weekDays[date.getDay()]
  }
  return ''
}

const getDateInfo = () => {
  if (props.echo.metadata?.creationTimestamp) {
    const date = new Date(props.echo.metadata.creationTimestamp)
    const month = date.getMonth() + 1
    const day = date.getDate()
    const hours = date.getHours().toString().padStart(2, '0')
    const minutes = date.getMinutes().toString().padStart(2, '0')
    return {
      month,
      day,
      time: `${hours}:${minutes}`
    }
  }
  return { month: '', day: '', time: '' }
}
</script>

<template>
  <div class="echo-card">
    <div class="card-header">
      <div class="date-badge">
        <span class="badge-date">{{ getDateInfo().month }}月{{ getDateInfo().day }}日 {{ getDateInfo().time }}</span>
        <span class="badge-weekday">{{ getWeekDay() }}</span>
      </div>
      <div v-if="displayWeather || displayMood" class="card-meta">
        <span v-if="displayWeather" class="meta-icon">{{ displayWeather }}</span>
        <span v-if="displayMood" class="meta-icon">{{ displayMood }}</span>
      </div>
      <div class="card-actions">
        <button
          class="action-btn"
          @click.stop="toggleDropdown"
          :class="{ active: showDropdown }"
        >
          <Icon icon="ri:edit-2-line" :size="16" />
          <span class="btn-text">编辑</span>
        </button>
        <div v-if="showDropdown" class="dropdown-menu">
          <button class="dropdown-item" @click.stop="startEdit">
            <Icon icon="ri:edit-2-line" :size="14" />
            编辑
          </button>
          <button class="dropdown-item danger" @click.stop="deleteEcho">
            <Icon icon="ri:delete-bin-line" :size="14" />
            删除
          </button>
        </div>
      </div>
    </div>
    
    <div v-if="echo.editing" class="card-body edit-mode">
      <EchoEditor
        v-model="editContent"
        :medias="editMedias"
        :weather="editWeather"
        :mood="editMood"
        @update:medias="handleUpdateMedias"
        @update:weather="handleUpdateWeather"
        @update:mood="handleUpdateMood"
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
    
    <div v-else class="card-body">
      <div class="card-content" v-html="echo.spec.content"></div>
      
      <div v-if="echo.spec.medias?.length" class="card-medias">
        <div class="medias-container">
          <div
            v-for="(media, index) in echo.spec.medias"
            :key="index"
            @click="previewImage(media.url)"
            class="media-item"
            :class="{ 'media-file': !media.type?.startsWith('image/') }"
          >
            <img
              v-if="media.type?.startsWith('image/')"
              :src="media.cover || media.url"
              :alt="media.displayName || 'image'"
              class="media-image"
            />
            <div v-else class="file-placeholder">
              <Icon icon="ri:file-text-line" :size="20" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.echo-card {
  background: linear-gradient(145deg, #ffffff 0%, #fafafa 100%);
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 
    0 1px 3px rgba(0, 0, 0, 0.05),
    0 4px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid #f0f0f0;
  transition: all 0.2s ease;

  &:hover {
    box-shadow: 
      0 2px 8px rgba(0, 0, 0, 0.08),
      0 8px 24px rgba(0, 0, 0, 0.06);
    transform: translateY(-1px);
  }
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.date-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  color: white;
  flex-shrink: 0;
}

.badge-date {
  font-size: 14px;
  font-weight: 600;
}

.badge-weekday {
  font-size: 13px;
  font-weight: 600;
  opacity: 0.9;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.meta-icon {
  font-size: 16px;
  padding: 4px 8px;
  background-color: #f1f5f9;
  border-radius: 16px;
}

.card-actions {
  position: relative;
  margin-left: auto;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
  cursor: pointer;
  padding: 8px 14px;
  border-radius: 8px;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
  font-size: 14px;
  font-weight: 500;

  &:hover {
    background: linear-gradient(135deg, #5a6fd6 0%, #6b429e 100%);
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
    transform: translateY(-1px);
  }

  &.active {
    background: linear-gradient(135deg, #5a6fd6 0%, #6b429e 100%);
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
  }
}

.btn-text {
  display: inline-block;
}

.dropdown-menu {
  position: absolute;
  right: 0;
  top: calc(100% + 6px);
  min-width: 110px;
  background-color: #fff;
  border-radius: 10px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  padding: 6px;
  z-index: 9999;
  border: 1px solid #f1f5f9;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 8px 12px;
  border: none;
  background: transparent;
  cursor: pointer;
  border-radius: 6px;
  font-size: 13px;
  color: #475569;
  transition: all 0.15s;

  &:hover {
    background-color: #f8fafc;
    color: #334155;
  }

  &.danger {
    color: #ef4444;

    &:hover {
      background-color: #fef2f2;
    }
  }
}

.card-body {
  position: relative;
}

.card-content {
  font-size: 15px;
  line-height: 1.7;
  color: #334155;
  white-space: pre-wrap;
  word-break: break-word;
  margin-bottom: 16px;

  :deep(p) {
    margin: 0 0 12px 0;

    &:last-child {
      margin-bottom: 0;
    }
  }
}

.card-medias {
  margin-bottom: 12px;
}

.medias-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 8px;
}

.media-item {
  aspect-ratio: 1 / 1;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  background-color: #f8fafc;
  border: 1px solid #e2e8f0;
  transition: all 0.2s;

  &:hover {
    transform: scale(1.02);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  }
}

.media-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.file-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
}

.edit-mode {
  background: #fff;
  border-radius: 12px;
  padding: 12px;
}

.edit-actions {
  display: flex;
  gap: 8px;
}

.cancel-btn, .save-btn {
  padding: 6px 14px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: #fff;

  &:hover {
    opacity: 0.9;
    transform: translateY(-1px);
  }
}

@media (max-width: 640px) {
  .echo-card {
    padding: 16px;
  }

  .date-badge {
    padding: 6px 12px;
    gap: 6px;
  }

  .badge-date {
    font-size: 13px;
  }

  .badge-weekday {
    font-size: 12px;
  }

  .meta-icon {
    font-size: 14px;
    padding: 2px 6px;
  }

  .medias-container {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>