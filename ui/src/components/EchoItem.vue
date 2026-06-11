<script setup lang="ts">
import { ref, watch, computed, onMounted, onUnmounted } from 'vue'
import { Icon } from '@iconify/vue'
import EchoEditor from './EchoEditor.vue'
import type { EchoItem, Category } from '@/types'

const props = defineProps<{
  echo: EchoItem
  categories: Category[]
}>()

const emit = defineEmits<{
  (e: 'edit'): void
  (e: 'delete'): void
  (e: 'save', echo: EchoItem): void
  (e: 'cancel'): void
  (e: 'preview', url: string): void
  (e: 'open-attachment'): void
  (e: 'update-medias', medias: Array<{ url: string; type: string; cover?: string; displayName?: string }>): void
  (e: 'move-category', categoryName: string): void
}>()

const editContent = ref(props.echo.spec.content)
const editMedias = ref([...(props.echo.spec.medias || [])])
const editWeatherDay = ref(props.echo.spec.weatherDay || '')
const editWeatherNight = ref(props.echo.spec.weatherNight || '')
const editMood = ref(props.echo.spec.mood || '')
const editLocation = ref(props.echo.spec.location || '')
const editEnvironment = ref(props.echo.spec.environment || '')
const showDropdown = ref(false)
const showMoveCategory = ref(false)
const dropdownRef = ref<HTMLElement | null>(null)

watch(() => props.echo, (newEcho) => {
  editContent.value = newEcho.spec.content
  editMedias.value = [...(newEcho.spec.medias || [])]
  editWeatherDay.value = newEcho.spec.weatherDay || ''
  editWeatherNight.value = newEcho.spec.weatherNight || ''
  editMood.value = newEcho.spec.mood || ''
  editLocation.value = newEcho.spec.location || ''
  editEnvironment.value = newEcho.spec.environment || ''
}, { deep: true })

// 可移动的分类（排除当前分类）
const availableCategories = computed(() => {
  return props.categories.filter(cat => cat.spec.name !== props.echo.spec.categoryName)
})

const weatherIcons: Record<string, string> = {
  '晴': '☀️',
  '多云': '☁️',
  '阴': '🌥️',
  '小雨': '🌧️',
  '中雨': '🌧️',
  '大雨': '⛈️',
  '暴雨': '⛈️',
  '雷阵雨': '⛈️',
  '小雪': '❄️',
  '中雪': '❄️',
  '大雪': '🌨️',
  '暴雪': '🌨️',
  '雨夹雪': '🌨️',
  '雾': '🌫️',
  '霾': '🌫️',
  '风': '🌬️',
  '大风': '🌬️',
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
  calm: '😌',
  hopeful: '😊',
  grateful: '🙏',
  love: '❤️',
  surprised: '😲',
}

const environmentIcons: Record<string, string> = {
  indoor: '🏠',
  outdoor: '🌳',
}

const displayWeather = computed(() => {
  const dayWeather = props.echo.spec.weatherDay
  const nightWeather = props.echo.spec.weatherNight
  if (!dayWeather) return ''
  const dayIcon = weatherIcons[dayWeather] || '🌤️'
  if (!nightWeather) return dayIcon
  const nightIcon = weatherIcons[nightWeather] || '🌙'
  return `${dayIcon}→${nightIcon}`
})

const displayMood = computed(() => {
  const mood = props.echo.spec.mood
  if (!mood) return ''
  return moodIcons[mood.toLowerCase()] || '😐'
})

const displayEnvironment = computed(() => {
  const env = props.echo.spec.environment
  if (!env) return ''
  return environmentIcons[env] || ''
})

watch(() => props.echo.spec.content, (newVal) => {
  editContent.value = newVal
})

watch(() => props.echo.spec.medias, (newMedias) => {
  editMedias.value = [...(newMedias || [])]
}, { immediate: true, deep: true })

watch(() => props.echo.spec.weatherDay, (newWeather) => {
  editWeatherDay.value = newWeather || ''
})

watch(() => props.echo.spec.weatherNight, (newWeather) => {
  editWeatherNight.value = newWeather || ''
})

watch(() => props.echo.spec.mood, (newMood) => {
  editMood.value = newMood || ''
})

watch(() => props.echo.spec.location, (newLocation) => {
  editLocation.value = newLocation || ''
})

watch(() => props.echo.spec.environment, (newEnv) => {
  editEnvironment.value = newEnv || ''
})

const startEdit = () => {
  showDropdown.value = false
  emit('edit')
}
const deleteEcho = () => {
  showDropdown.value = false
  emit('delete')
}
const moveCategory = (categoryName: string) => {
  showDropdown.value = false
  showMoveCategory.value = false
  emit('move-category', categoryName)
}
const toggleMoveCategory = (e: Event) => {
  e.stopPropagation()
  showMoveCategory.value = !showMoveCategory.value
}
const handleUpdateMedias = (newMedias: Array<{ url: string; type: string; cover?: string; displayName?: string }>) => {
  editMedias.value = [...newMedias]
  emit('update-medias', [...newMedias])
}

const handleUpdateWeatherDay = (weather: string) => {
  editWeatherDay.value = weather
}

const handleUpdateWeatherNight = (weather: string) => {
  editWeatherNight.value = weather
}

const handleUpdateMood = (mood: string) => {
  editMood.value = mood
}

const handleUpdateLocation = (location: string) => {
  editLocation.value = location
}

const handleUpdateEnvironment = (env: string) => {
  editEnvironment.value = env
}

const saveEdit = () => {
  const updatedEcho = {
    ...props.echo,
    spec: {
      ...props.echo.spec,
      content: editContent.value,
      medias: [...editMedias.value],
      weatherDay: editWeatherDay.value,
      weatherNight: editWeatherNight.value,
      mood: editMood.value,
      location: editLocation.value,
      environment: editEnvironment.value,
    }
  }
  emit('save', updatedEcho)
}
const cancelEdit = () => emit('cancel')
const previewImage = (url: string) => emit('preview', url)
const toggleDropdown = (e: Event) => {
  e.stopPropagation()
  showDropdown.value = !showDropdown.value
}

const handleClickOutside = (e: Event) => {
  const target = e.target as Node
  if (dropdownRef.value && !dropdownRef.value.contains(target)) {
    showDropdown.value = false
    showMoveCategory.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})

const getWeekDay = () => {
  let date: Date | null = null

  if (props.echo.status?.creationTimestamp) {
    date = new Date(props.echo.status.creationTimestamp)
  } else if (props.echo.metadata?.creationTimestamp) {
    date = new Date(props.echo.metadata.creationTimestamp)
  }

  if (date) {
    const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    return weekDays[date.getDay()]
  }
  return ''
}

const getDateInfo = () => {
  let date: Date | null = null

  if (props.echo.status?.creationTimestamp) {
    date = new Date(props.echo.status.creationTimestamp)
  } else if (props.echo.metadata?.creationTimestamp) {
    date = new Date(props.echo.metadata.creationTimestamp)
  }

  if (date) {
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
      <div v-if="displayWeather || displayMood || echo.spec.location || displayEnvironment" class="card-meta">
        <span v-if="displayWeather" class="meta-icon weather">{{ displayWeather }}</span>
        <span v-if="displayMood" class="meta-icon mood">{{ displayMood }}</span>
        <span v-if="displayEnvironment" class="meta-icon environment">{{ displayEnvironment }}</span>
        <span v-if="echo.spec.location" class="meta-icon location">📍 {{ echo.spec.location }}</span>
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
        <div v-if="showDropdown" class="dropdown-wrapper" ref="dropdownRef">
          <div v-if="showMoveCategory" class="category-menu">
            <button
              v-for="cat in availableCategories"
              :key="cat.metadata.name"
              class="category-item"
              @click.stop="moveCategory(cat.spec.name)"
            >
              {{ cat.spec.name }}
            </button>
            <div v-if="availableCategories.length === 0" class="no-categories">
              暂无其他分类
            </div>
          </div>
          <div class="dropdown-menu">
            <button class="dropdown-item" @click.stop="startEdit">
              <Icon icon="ri:edit-2-line" :size="14" />
              编辑
            </button>
            <button class="dropdown-item" @click.stop="toggleMoveCategory">
              <Icon icon="ri:folder-transfer-line" :size="14" />
              {{ showMoveCategory ? '收起' : '移动' }}
            </button>
            <button class="dropdown-item danger" @click.stop="deleteEcho">
              <Icon icon="ri:delete-bin-line" :size="14" />
              删除
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="echo.editing" class="card-body edit-mode">
      <EchoEditor
        v-model="editContent"
        :medias="editMedias"
        :mood="editMood"
        :environment="editEnvironment"
        :editing="true"
        :weatherDay="echo.spec.weatherDay"
        :weatherNight="echo.spec.weatherNight"
        :location="echo.spec.location"
        @update:medias="handleUpdateMedias"
        @update:weatherDay="handleUpdateWeatherDay"
        @update:weatherNight="handleUpdateWeatherNight"
        @update:mood="handleUpdateMood"
        @update:location="handleUpdateLocation"
        @update:environment="handleUpdateEnvironment"
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

  &.weather {
    background: linear-gradient(135deg, rgba(102, 126, 234, 0.15) 0%, rgba(118, 75, 162, 0.15) 100%);
  }

  &.mood {
    background: linear-gradient(135deg, rgba(102, 126, 234, 0.15) 0%, rgba(118, 75, 162, 0.15) 100%);
  }

  &.location {
    font-size: 13px;
    color: #3b82f6;
    background: rgba(59, 130, 246, 0.1);
  }

  &.environment {
    background: linear-gradient(135deg, rgba(102, 126, 234, 0.15) 0%, rgba(118, 75, 162, 0.15) 100%);
  }
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

.dropdown-wrapper {
  position: absolute;
  right: 0;
  top: calc(100% + 6px);
  display: flex;
  gap: 8px;
  z-index: 9999;
}

.category-menu {
  min-width: 100px;
  background-color: #fff;
  border-radius: 10px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  padding: 6px;
  border: 1px solid #f1f5f9;
  max-height: 150px;
  overflow-y: auto;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: #e2e8f0;
    border-radius: 2px;
  }
}

.dropdown-menu {
  min-width: 110px;
  background-color: #fff;
  border-radius: 10px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  padding: 6px;
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

.category-item {
  display: block;
  width: 100%;
  padding: 8px 12px;
  border: none;
  background: transparent;
  cursor: pointer;
  border-radius: 6px;
  font-size: 13px;
  color: #475569;
  text-align: left;
  transition: all 0.15s;

  &:hover {
    background-color: rgba(102, 126, 234, 0.1);
    color: #667eea;
  }
}

.no-categories {
  padding: 8px 12px;
  font-size: 12px;
  color: #94a3b8;
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
