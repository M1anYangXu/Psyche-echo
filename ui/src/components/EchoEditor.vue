<script setup lang="ts">
import { shallowRef, watch, onMounted, ref, computed } from 'vue'
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
  (e: 'update:adcode', value: string): void
  (e: 'update:environment', value: string): void
}>()

const currentCity = ref('')
const currentAdcode = ref('')
const weatherDay = ref('')
const weatherNight = ref('')

const weatherEmojis: Record<string, string> = {
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

const getWeatherEmoji = (weather: string) => {
  return weatherEmojis[weather] || '🌤️'
}

const fetchWeather = async (adcode: string) => {
  try {
    const response = await fetch(`https://restapi.amap.com/v3/weather/weatherInfo?key=1560db1cdb6f71f169d02454758d2e40&city=${adcode}&extensions=all`)
    const data = await response.json()
    console.log('=== 完整天气返回 ===')
    console.log('完整数据:', JSON.stringify(data, null, 2))
    console.log('status:', data.status)
    console.log('info:', data.info)
    console.log('infocode:', data.infocode)
    console.log('count:', data.count)
    if (data.lives) {
      console.log('lives数据:', data.lives)
    }
    if (data.forecasts) {
      console.log('forecasts数据:', data.forecasts)
    }
    console.log('====================')
    
    if (data.status === '1' && data.forecasts && data.forecasts.length > 0 && data.forecasts[0].casts && data.forecasts[0].casts.length > 0) {
      const forecast = data.forecasts[0].casts[0]
      console.log('使用预报天气 - 白天:', forecast.dayweather, ', 晚上:', forecast.nightweather)
      weatherDay.value = forecast.dayweather
      weatherNight.value = forecast.nightweather
      emit('update:weatherDay', forecast.dayweather)
      emit('update:weatherNight', forecast.nightweather)
    } else if (data.status === '1' && data.lives && data.lives.length > 0) {
      const weatherData = data.lives[0]
      console.log('使用实时天气（无预报数据） - 天气:', weatherData.weather)
      weatherDay.value = weatherData.weather
      weatherNight.value = weatherData.weather
      emit('update:weatherDay', weatherData.weather)
      emit('update:weatherNight', weatherData.weather)
    }
  } catch (e) {
    console.error('获取天气失败:', e)
  }
}

const fetchCityFromCoords = async (lat: number, lng: number) => {
  try {
    const response = await fetch(`https://restapi.amap.com/v3/geocode/regeo?key=1560db1cdb6f71f169d02454758d2e40&location=${lng},${lat}`)
    const data = await response.json()
    if (data.status === '1' && data.regeocode) {
      const addressComponent = data.regeocode.addressComponent
      currentAdcode.value = addressComponent.adcode || ''
      emit('update:adcode', currentAdcode.value)
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
      if (currentAdcode.value) {
        fetchWeather(currentAdcode.value)
      }
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
    console.log('高德定位返回:', data)
    if (data.status === '1') {
      currentAdcode.value = data.adcode || ''
      emit('update:adcode', currentAdcode.value)
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
      if (currentAdcode.value) {
        fetchWeather(currentAdcode.value)
      }
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
    console.warn('浏览器不支持定位，将使用IP定位')
    fetchCityFromIP()
    return
  }

  navigator.geolocation.getCurrentPosition(
    async (position) => {
      const { latitude, longitude } = position.coords
      const success = await fetchCityFromCoords(latitude, longitude)
      if (!success) {
        console.warn('GPS定位成功，但获取城市信息失败')
        fetchCityFromIP()
      }
    },
    (error) => {
      console.warn('GPS定位失败，将使用IP定位:', error)
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

const selectedMoodLabel = computed(() => {
  const option = moodOptions.find(m => m.value === props.mood)
  return option ? option.label : '💭 心情'
})

const selectedEnvironmentLabel = computed(() => {
  const option = environmentOptions.find(e => e.value === props.environment)
  return option ? option.label : '📍 场景'
})

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

  if (props.editing) {
    weatherDay.value = props.weatherDay || ''
    weatherNight.value = props.weatherNight || ''
    currentCity.value = props.location || ''
  } else {
    getLocationByGPS()
  }
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

const closeDropdowns = () => {
  showMoodDropdown.value = false
  showEnvironmentDropdown.value = false
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
        <div v-if="weatherDay && weatherNight" class="weather-display">
          <span class="weather-emoji">{{ getWeatherEmoji(weatherDay) }}</span>
          <span class="weather-text">{{ weatherDay }}</span>
          <span class="weather-arrow">→</span>
          <span class="weather-emoji">{{ getWeatherEmoji(weatherNight) }}</span>
          <span class="weather-text">{{ weatherNight }}</span>
        </div>
        <div v-else class="weather-loading">
          <span>🌤️ 加载天气...</span>
        </div>
        <div
          class="selector-group"
          @click.stop="showMoodDropdown = !showMoodDropdown; showEnvironmentDropdown = false"
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
          @click.stop="showEnvironmentDropdown = !showEnvironmentDropdown; showMoodDropdown = false"
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
  padding: 6px 12px;
  border-radius: 16px;
  background-color: rgba(59, 130, 246, 0.1);
  color: #3b82f6;
  font-size: 13px;
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

.weather-display {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  border-radius: 20px;
  background: linear-gradient(135deg, rgba(6, 182, 212, 0.15) 0%, rgba(8, 145, 178, 0.15) 100%);
  border: 1px solid rgba(6, 182, 212, 0.2);
}

.weather-emoji {
  font-size: 16px;
}

.weather-text {
  font-size: 13px;
  color: #0891b2;
  font-weight: 500;
}

.weather-arrow {
  font-size: 12px;
  color: #64748b;
  margin: 0 2px;
}

.weather-loading {
  padding: 6px 14px;
  border-radius: 20px;
  background: rgba(148, 163, 184, 0.1);
  font-size: 13px;
  color: #94a3b8;
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
