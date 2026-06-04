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
  weather?: string
  mood?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
  (e: 'open-attachment'): void
  (e: 'remove-media', index: number): void
  (e: 'update:medias', medias: Array<{ url: string; type: string; cover?: string; displayName?: string }>): void
  (e: 'update:weather', value: string): void
  (e: 'update:mood', value: string): void
}>()

const editor = shallowRef<VueEditor>()
const localMedias = ref<Array<{ url: string; type: string; cover?: string; displayName?: string }>>([])
const emojiSelectorModal = ref(false)
const showWeatherDropdown = ref(false)
const showMoodDropdown = ref(false)

const weatherOptions = [
  { value: 'sunny', label: '☀️ 晴天' },
  { value: 'cloudy', label: '☁️ 多云' },
  { value: 'lightRain', label: '🌧️ 小雨' },
  { value: 'heavyRain', label: '⛈️ 大雨' },
  { value: 'lightSnow', label: '❄️ 小雪' },
  { value: 'heavySnow', label: '🌨️ 大雪' },
  { value: 'windy', label: '🌬️ 大风' },
  { value: 'foggy', label: '🌫️ 雾天' },
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

const selectedWeatherLabel = computed(() => {
  const option = weatherOptions.find(w => w.value === props.weather)
  return option ? option.label : '🌤️ 天气'
})

const selectedMoodLabel = computed(() => {
  const option = moodOptions.find(m => m.value === props.mood)
  return option ? option.label : '💭 心情'
})

const selectWeather = (value: string) => {
  emit('update:weather', value === props.weather ? '' : value)
  showWeatherDropdown.value = false
}

const selectMood = (value: string) => {
  emit('update:mood', value === props.mood ? '' : value)
  showMoodDropdown.value = false
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
  showWeatherDropdown.value = false
  showMoodDropdown.value = false
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
          @click.stop="showWeatherDropdown = !showWeatherDropdown; showMoodDropdown = false"
        >
          <span>{{ selectedWeatherLabel }}</span>
          <span class="dropdown-arrow">▼</span>
          <div v-if="showWeatherDropdown" class="dropdown-menu">
            <button
              v-for="weather in weatherOptions"
              :key="weather.value"
              class="dropdown-item"
              :class="{ active: weather.value === props.weather }"
              @click.stop="selectWeather(weather.value)"
            >
              {{ weather.label }}
            </button>
          </div>
        </div>
        <div
          class="selector-group"
          @click.stop="showMoodDropdown = !showMoodDropdown; showWeatherDropdown = false"
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

  &:hover {
    background-color: rgba(59, 130, 246, 0.1);
  }
}

.attachment-icon {
  width: 1.25rem;
  height: 1.25rem;
  color: #6b7280;
  transition: color 0.2s;

  .group:hover & {
    color: #3b82f6;
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
  padding: 6px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  background-color: #f3f4f6;

  &:hover {
    background-color: #e5e7eb;
  }

  span {
    font-size: 14px;
    color: #374151;
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
