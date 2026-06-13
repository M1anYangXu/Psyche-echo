<script setup lang="ts">
import { shallowRef, watch, onMounted, onUnmounted, ref, computed } from 'vue'
import {
  ExtensionsKit,
  RichTextEditor,
  VueEditor,
} from '@halo-dev/richtext-editor'
import { IconImageAddLine } from '@halo-dev/components'
import MediaCard from './MediaCard.vue'
import EmojiSelectorModal from './EmojiSelectorModal.vue'

const props = defineProps<{
  modelValue: string
  medias?: Array<{ url: string; type: string; cover?: string; displayName?: string }>
  mood?: string
  environment?: string
  editing?: boolean
  weatherDay?: string
  weatherNight?: string
  location?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
  (e: 'open-attachment'): void
  (e: 'remove-media', index: number): void
  (e: 'update:medias', medias: Array<{ url: string; type: string; cover?: string; displayName?: string }>): void
  (e: 'update:weatherDay', value: string): void
  (e: 'update:weatherNight', value: string): void
  (e: 'update:mood', value: string): void
  (e: 'update:location', value: string): void
  (e: 'update:environment', value: string): void
}>()

const currentCity = ref('')
const weatherDay = ref('')
const weatherNight = ref('')

const fetchCityFromCoords = async (lat: number, lng: number) => {
  try {
    const response = await fetch(`https://restapi.amap.com/v3/geocode/regeo?key=1560db1cdb6f71f169d02454758d2e40&location=${lng},${lat}`)
    const data = await response.json()
    if (data.status === '1' && data.regeocode) {
      const addressComponent = data.regeocode.addressComponent
      if (addressComponent.province && addressComponent.city && addressComponent.district) {
        currentCity.value = `${addressComponent.province}：${addressComponent.city}：${addressComponent.district}`
      } else if (addressComponent.province && addressComponent.city) {
        currentCity.value = `${addressComponent.province}：${addressComponent.city}`
      } else if (addressComponent.city) {
        currentCity.value = addressComponent.city
      } else if (addressComponent.province) {
        currentCity.value = addressComponent.province
      }
      emit('update:location', currentCity.value)
      return true
    }
  } catch (e) {
    console.error('逆地理编码失败:', e)
  }
  return false
}

const fetchCityFromIP = async () => {
  try {
    const response = await fetch('https://restapi.amap.com/v3/ip?key=1560db1cdb6f71f169d02454758d2e40')
    const data = await response.json()
    if (data.status === '1') {
      if (data.city && data.province) {
        currentCity.value = `${data.province}：${data.city}`
      } else if (data.city) {
        currentCity.value = data.city
      } else if (data.province) {
        currentCity.value = data.province
      } else {
        currentCity.value = '未知城市'
      }
      emit('update:location', currentCity.value)
    } else {
      currentCity.value = '未知城市'
    }
  } catch (error) {
    console.error('获取城市失败:', error)
    currentCity.value = '未知城市'
  }
}

const getLocationByGPS = () => {
  if (!navigator.geolocation) {
    fetchCityFromIP()
    return
  }

  navigator.geolocation.getCurrentPosition(
    async (position) => {
      const { latitude, longitude } = position.coords
      const success = await fetchCityFromCoords(latitude, longitude)
      if (!success) {
        fetchCityFromIP()
      }
    },
    () => {
      fetchCityFromIP()
    },
    {
      enableHighAccuracy: true,
      timeout: 5000,
      maximumAge: 300000
    }
  )
}

const editor = shallowRef<VueEditor>()
const localMedias = ref<Array<{ url: string; type: string; cover?: string; displayName?: string }>>([])
const emojiSelectorModal = ref(false)
const showMoodDropdown = ref(false)
const showEnvironmentDropdown = ref(false)
const showWeatherDropdown = ref(false)
const showWeatherNightDropdown = ref(false)

const weatherOptions = [
  { value: '晴', label: '☀️ 晴' },
  { value: '多云', label: '☁️ 多云' },
  { value: '阴', label: '🌥️ 阴' },
  { value: '小雨', label: '🌧️ 小雨' },
  { value: '中雨', label: '🌧️ 中雨' },
  { value: '大雨', label: '⛈️ 大雨' },
  { value: '暴雨', label: '⛈️ 暴雨' },
  { value: '雷阵雨', label: '⛈️ 雷阵雨' },
  { value: '小雪', label: '❄️ 小雪' },
  { value: '中雪', label: '❄️ 中雪' },
  { value: '大雪', label: '🌨️ 大雪' },
  { value: '暴雪', label: '🌨️ 暴雪' },
  { value: '雨夹雪', label: '🌨️ 雨夹雪' },
  { value: '雾', label: '🌫️ 雾' },
  { value: '霾', label: '🌫️ 霾' },
  { value: '风', label: '🌬️ 风' },
  { value: '大风', label: '🌬️ 大风' },
]

const moodOptions = [
  { value: 'happy', label: '😊 开心' },
  { value: 'sad', label: '😢 难过' },
  { value: 'angry', label: '😠 生气' },
  { value: 'tired', label: '😴 疲惫' },
  { value: 'excited', label: '🎉 兴奋' },
  { value: 'anxious', label: '😰 焦虑' },
  { value: 'peaceful', label: '😌 平静' },
  { value: 'confused', label: '😕 困惑' },
]

const environmentOptions = [
  { value: 'indoor', label: '🏠 室内' },
  { value: 'outdoor', label: '🌳 户外' },
]

const selectedWeatherDayLabel = computed(() => {
  const option = weatherOptions.find(w => w.value === weatherDay.value)
  return option ? option.label : '🌤️ 天气'
})

const selectedWeatherNightLabel = computed(() => {
  const option = weatherOptions.find(w => w.value === weatherNight.value)
  return option ? option.label : '🌙 夜间'
})

const selectedMoodLabel = computed(() => {
  const option = moodOptions.find(m => m.value === props.mood)
  return option ? option.label : '💭 心情'
})

const selectedEnvironmentLabel = computed(() => {
  const option = environmentOptions.find(e => e.value === props.environment)
  return option ? option.label : '🏠 场景'
})

const selectWeatherDay = (value: string) => {
  if (weatherDay.value === value) {
    weatherDay.value = ''
    emit('update:weatherDay', '')
  } else {
    weatherDay.value = value
    emit('update:weatherDay', value)
  }
  showWeatherDropdown.value = false
}

const selectWeatherNight = (value: string) => {
  if (weatherNight.value === value) {
    weatherNight.value = ''
    emit('update:weatherNight', '')
  } else {
    weatherNight.value = value
    emit('update:weatherNight', value)
  }
  showWeatherNightDropdown.value = false
}

const selectMood = (value: string) => {
  emit('update:mood', value === props.mood ? '' : value)
  showMoodDropdown.value = false
}

const selectEnvironment = (value: string) => {
  emit('update:environment', value === props.environment ? '' : value)
  showEnvironmentDropdown.value = false
}

const onEmojiSelect = (emoji: string) => {
  editor.value?.commands?.insertContent(emoji)
  emojiSelectorModal.value = false
}

const removeMedium = (index: number) => {
  localMedias.value.splice(index, 1)
  emit('remove-media', index)
  emit('update:medias', [...localMedias.value])
}

onMounted(() => {
  editor.value = new VueEditor({
    content: props.modelValue,
    extensions: [
      ExtensionsKit.configure({
        placeholder: {
          placeholder: '有什么想说的吗...',
        },
      }),
    ],
    parseOptions: {
      preserveWhitespace: true,
    },
    onUpdate: () => {
      emit('update:modelValue', editor.value?.getHTML() || '')
    },
  })

  weatherDay.value = props.weatherDay || ''
  weatherNight.value = props.weatherNight || ''

  if (props.editing) {
    currentCity.value = props.location || ''
  } else {
    getLocationByGPS()
  }

  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})

watch(
  () => props.modelValue,
  (newValue) => {
    if (editor.value && newValue !== editor.value.getHTML()) {
      editor.value.commands?.setContent(newValue)
    }
  }
)

watch(
  () => props.medias,
  (newMedias) => {
    if (newMedias) {
      localMedias.value = [...newMedias]
    }
  },
  { immediate: true, deep: true }
)

watch(
  () => props.weatherDay,
  (newWeather) => {
    weatherDay.value = newWeather || ''
  }
)

watch(
  () => props.weatherNight,
  (newWeather) => {
    weatherNight.value = newWeather || ''
  }
)

watch(
  () => props.location,
  (newLocation) => {
    currentCity.value = newLocation || ''
  }
)

const closeDropdowns = () => {
  showMoodDropdown.value = false
  showEnvironmentDropdown.value = false
  showWeatherDropdown.value = false
  showWeatherNightDropdown.value = false
}

const handleClickOutside = (event: MouseEvent) => {
  const wrapper = document.querySelector('.halo-echo-editor-wrapper')
  if (wrapper && !wrapper.contains(event.target as Node)) {
    closeDropdowns()
  }
}
</script>

<template>
  <div class="halo-echo-editor-wrapper" @click="closeDropdowns">
    <div class="halo-echo-editor relative">
      <RichTextEditor v-if="editor" :editor="editor" locale="zh-CN" />
    </div>

    <div v-if="localMedias.length" class="img-box">
      <ul class="img-grid" role="list">
        <li
          v-for="(media, index) in localMedias"
          :key="index"
          class="img-item-wrapper"
        >
          <MediaCard :media="media" @remove="removeMedium(index)"></MediaCard>
        </li>
      </ul>
    </div>

    <div class="editor-footer">
      <div class="footer-left">
        <div class="group" @click="emit('open-attachment')">
          <IconImageAddLine class="attachment-icon" />
        </div>
        <div class="group" @click="emojiSelectorModal = true">
          <span class="emoji-icon">😊</span>
        </div>
        <div class="divider"></div>
        <div
          class="selector-group"
          @click.stop="showWeatherDropdown = !showWeatherDropdown; closeDropdowns(); showWeatherDropdown = true"
        >
          <span>{{ selectedWeatherDayLabel }}</span>
          <span class="dropdown-arrow">▼</span>
          <div v-if="showWeatherDropdown" class="dropdown-menu">
            <button
              v-for="weather in weatherOptions"
              :key="weather.value"
              class="dropdown-item"
              :class="{ active: weather.value === weatherDay }"
              @click.stop="selectWeatherDay(weather.value)"
            >
              {{ weather.label }}
            </button>
          </div>
        </div>
        <div
          v-if="weatherDay"
          class="selector-group night-selector"
          @click.stop="showWeatherNightDropdown = !showWeatherNightDropdown; closeDropdowns(); showWeatherNightDropdown = true"
        >
          <span>{{ selectedWeatherNightLabel }}</span>
          <span class="dropdown-arrow">▼</span>
          <div v-if="showWeatherNightDropdown" class="dropdown-menu">
            <button
              v-for="weather in weatherOptions"
              :key="weather.value"
              class="dropdown-item"
              :class="{ active: weather.value === weatherNight }"
              @click.stop="selectWeatherNight(weather.value)"
            >
              {{ weather.label }}
            </button>
          </div>
        </div>
        <div
          class="selector-group"
          @click.stop="showMoodDropdown = !showMoodDropdown; closeDropdowns(); showMoodDropdown = true"
        >
          <span>{{ selectedMoodLabel }}</span>
          <span class="dropdown-arrow">▼</span>
          <div v-if="showMoodDropdown" class="dropdown-menu">
            <button
              v-for="mood in moodOptions"
              :key="mood.value"
              class="dropdown-item"
              :class="{ active: mood.value === props.mood }"
              @click.stop="selectMood(mood.value)"
            >
              {{ mood.label }}
            </button>
          </div>
        </div>
        <div
          class="selector-group"
          @click.stop="showEnvironmentDropdown = !showEnvironmentDropdown; closeDropdowns(); showEnvironmentDropdown = true"
        >
          <span>{{ selectedEnvironmentLabel }}</span>
          <span class="dropdown-arrow">▼</span>
          <div v-if="showEnvironmentDropdown" class="dropdown-menu">
            <button
              v-for="env in environmentOptions"
              :key="env.value"
              class="dropdown-item"
              :class="{ active: env.value === props.environment }"
              @click.stop="selectEnvironment(env.value)"
            >
              {{ env.label }}
            </button>
          </div>
        </div>
      </div>
      <div class="footer-center">
        <span v-if="currentCity" class="location-badge">
          📍 {{ currentCity }}
          <button class="refresh-location-btn" @click.stop="getLocationByGPS" title="重新定位">🔄</button>
        </span>
      </div>
      <div class="footer-right">
        <slot name="footer-right"></slot>
      </div>
    </div>

    <EmojiSelectorModal
      v-model:visible="emojiSelectorModal"
      @select="onEmojiSelect"
    />
  </div>
</template>

<style scoped>
.halo-echo-editor-wrapper {
  border: 1px solid #e5e7eb;
  border-radius: 0.5rem;
  background-color: #fff;
}

.halo-echo-editor {
  box-sizing: border-box;

  :deep(.editor-header) {
    display: none;
  }

  :deep(.editor-content) {
    height: 100%;
    width: 100%;

    .ProseMirror {
      min-height: 10rem;
      overflow: auto;
      outline: none !important;
      padding: 0.875rem !important;

      p {
        &:first-child {
          margin-top: 0 !important;
        }
        &.is-editor-empty {
          &:first-child {
            &::before {
              content: attr(data-placeholder);
              float: left;
              color: #adb5bd;
              pointer-events: none;
              height: 0;
            }
          }
        }
      }

      ul {
        &[data-type="taskList"] {
          list-style: none;
          padding: 0;

          li {
            display: flex;
            align-items: center;

            > label {
              flex: 0 0 auto;
              margin-right: 0.5rem;
            }
          }
        }
      }

      table {
        .selectedCell {
          &:after {
            z-index: 2;
            position: absolute;
            content: "";
            left: 0;
            right: 0;
            top: 0;
            bottom: 0;
            background: rgba(200, 200, 255, 0.4);
            pointer-events: none;
          }
        }

        .column-resize-handle {
          position: absolute;
          right: -2px;
          top: 0;
          bottom: -2px;
          width: 4px;
          background-color: #adf;
          pointer-events: none;
          cursor: col-resize !important;
        }
      }
    }
  }
}

.img-box {
  padding: 12px;
  border-top: 1px solid #e5e7eb;
}

.img-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 8px;
  list-style: none;
  padding: 0;
  margin: 0;
}

.img-item-wrapper {
  overflow: hidden;
  border-radius: 6px;
  aspect-ratio: 1 / 1;
  width: 100%;
  max-width: 120px;
}

.editor-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.875rem;
  background-color: #fff;
  border-top: 1px solid #e5e7eb;
}

.footer-center {
  flex: 1;
  display: flex;
  justify-content: center;
}

.location-badge {
  padding: 6px 14px;
  border-radius: 20px;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.15) 0%, rgba(37, 99, 235, 0.15) 100%);
  border: 1px solid rgba(59, 130, 246, 0.2);
  color: #3b82f6;
  font-size: 14px;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.refresh-location-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 14px;
  padding: 0;
  transition: transform 0.2s;
  display: inline-flex;
  align-items: center;
  justify-content: center;

  &:hover {
    transform: rotate(180deg);
  }
}

.footer-left {
  display: flex;
  gap: 0.5rem;
}

.group {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 2.25rem;
  height: 2.25rem;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.2s;
  background-color: rgba(102, 126, 234, 0.1);

  &:hover {
    background-color: rgba(102, 126, 234, 0.2);
  }
}

.attachment-icon {
  width: 1.25rem;
  height: 1.25rem;
  color: #667eea;
  transition: color 0.2s;

  .group:hover & {
    color: #5a6fd6;
  }
}

.emoji-icon {
  font-size: 1.25rem;
}

.divider {
  width: 1px;
  height: 1.5rem;
  background-color: #e5e7eb;
  margin: 0 0.25rem;
}

.selector-group {
  position: relative;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.2s;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.15) 0%, rgba(118, 75, 162, 0.15) 100%);
  border: 1px solid rgba(102, 126, 234, 0.2);

  &:hover {
    background: linear-gradient(135deg, rgba(102, 126, 234, 0.25) 0%, rgba(118, 75, 162, 0.25) 100%);
  }

  span {
    font-size: 14px;
    color: #667eea;
    font-weight: 500;
  }
}

.night-selector {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.15) 0%, rgba(118, 75, 162, 0.15) 100%);
  border: 1px solid rgba(102, 126, 234, 0.2);

  &:hover {
    background: linear-gradient(135deg, rgba(102, 126, 234, 0.25) 0%, rgba(118, 75, 162, 0.25) 100%);
  }

  span {
    color: #667eea;
  }
}

.dropdown-arrow {
  font-size: 10px;
  color: #9ca3af;
  transition: transform 0.2s;
}

.selector-group:hover .dropdown-arrow {
  transform: rotate(180deg);
}

.dropdown-menu {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  min-width: 140px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
  z-index: 100;
  overflow: hidden;
}

.dropdown-item {
  width: 100%;
  padding: 8px 12px;
  text-align: left;
  border: none;
  background: none;
  cursor: pointer;
  font-size: 14px;
  color: #374151;
  transition: background-color 0.2s;
  display: flex;
  align-items: center;
  gap: 8px;

  &:hover {
    background-color: #f3f4f6;
  }

  &.active {
    background-color: #dbeafe;
    color: #1d4ed8;
  }
}
</style>
