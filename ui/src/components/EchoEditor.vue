<script setup lang="ts">
import { shallowRef, watch, onMounted, ref } from 'vue'
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
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
  (e: 'open-attachment'): void
  (e: 'remove-media', index: number): void
  (e: 'update:medias', medias: Array<{ url: string; type: string; cover?: string; displayName?: string }>): void
}>()

const editor = shallowRef<VueEditor>()
const localMedias = ref<Array<{ url: string; type: string; cover?: string; displayName?: string }>>([])
const emojiSelectorModal = ref(false)

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
</script>

<template>
  <div class="halo-moment-editor-wrapper">
    <div class="halo-moment-editor relative">
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
.halo-moment-editor-wrapper {
  border: 1px solid #e5e7eb;
  border-radius: 0.5rem;
  overflow: hidden;
  background-color: #fff;
}

.halo-moment-editor {
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
</style>
