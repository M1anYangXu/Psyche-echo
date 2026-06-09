<script setup lang="ts">
import { ref, watch } from 'vue'
import { Icon } from '@iconify/vue'
import type { ImportResult } from '@/types'

const props = defineProps<{
  visible: boolean
  selectedCategory: string
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'export', mode: 'all' | 'current'): void
  (e: 'import', data: string): void
}>()

const activeTab = ref<'export' | 'import'>('export')
const exportMode = ref<'all' | 'current'>('all')
const importFile = ref<File | null>(null)
const importText = ref('')
const isProcessing = ref(false)
const importResult = ref<ImportResult | null>(null)

watch(() => props.visible, (val) => {
  if (val) {
    activeTab.value = 'export'
    exportMode.value = 'all'
    importFile.value = null
    importText.value = ''
    importResult.value = null
    isProcessing.value = false
  }
})

const handleClose = () => {
  emit('update:visible', false)
}

const handleExport = () => {
  emit('export', exportMode.value)
  handleClose()
}

const handleFileSelect = (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (file) {
    importFile.value = file
    const reader = new FileReader()
    reader.onload = (e) => {
      importText.value = e.target?.result as string
    }
    reader.readAsText(file)
  }
}

const handleImport = async () => {
  if (!importText.value.trim()) {
    importResult.value = {
      success: false,
      message: '请选择文件或输入数据',
      importedCategories: 0,
      importedNotes: 0,
      skippedNotes: 0
    }
    return
  }

  isProcessing.value = true
  emit('import', importText.value)
}
</script>

<template>
  <div v-if="visible" class="modal-overlay" @click.self="handleClose">
    <div class="modal-content">
      <div class="modal-header">
        <h3 class="modal-title">
          <Icon icon="ri:database-fill" :size="20" />
          数据导入导出
        </h3>
        <button class="close-btn" @click="handleClose">
          <Icon icon="ri:close-fill" :size="18" />
        </button>
      </div>

      <div class="modal-body">
        <div class="tab-container">
          <button 
            class="tab-btn" 
            :class="{ active: activeTab === 'export' }"
            @click="activeTab = 'export'"
          >
            <Icon icon="ri:download-fill" :size="16" />
            导出数据
          </button>
          <button 
            class="tab-btn"
            :class="{ active: activeTab === 'import' }"
            @click="activeTab = 'import'"
          >
            <Icon icon="ri:upload-fill" :size="16" />
            导入数据
          </button>
        </div>

        <div v-if="activeTab === 'export'" class="export-section">
          <div class="option-group">
            <label class="radio-label">
              <input 
                type="radio" 
                v-model="exportMode" 
                value="all" 
                name="export-mode"
              />
              <span>全部导出（包含所有分类和日记）</span>
            </label>
            <label class="radio-label">
              <input 
                type="radio" 
                v-model="exportMode" 
                value="current" 
                name="export-mode"
              />
              <span>仅导出当前分类：{{ selectedCategory }}</span>
            </label>
          </div>

          <button class="action-btn export-btn" @click="handleExport">
            <Icon icon="ri:download-fill" :size="18" />
            导出数据
          </button>
        </div>

        <div v-else class="import-section">
          <div class="file-upload">
            <input 
              type="file" 
              accept=".json" 
              id="import-file" 
              class="file-input"
              @change="handleFileSelect"
            />
            <label for="import-file" class="file-label">
              <Icon icon="ri:file-json-fill" :size="24" />
              <span>选择JSON文件</span>
            </label>
          </div>

          <textarea
            v-model="importText"
            placeholder="或直接粘贴JSON数据..."
            class="import-textarea"
          />

          <button 
            class="action-btn import-btn" 
            @click="handleImport"
            :disabled="isProcessing"
          >
            <Icon icon="ri:upload-fill" :size="18" />
            {{ isProcessing ? '导入中...' : '导入数据' }}
          </button>

          <div v-if="importResult" class="result-message" :class="{ success: importResult.success }">
            <Icon 
              :icon="importResult.success ? 'ri:check-circle-fill' : 'ri:error-circle-fill'" 
              :size="20" 
            />
            {{ importResult.message }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.modal-content {
  width: 480px;
  max-width: 90%;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.modal-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #fff;
}

.close-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  color: #fff;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: rgba(255, 255, 255, 0.3);
  }
}

.modal-body {
  padding: 24px;
}

.tab-container {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
}

.tab-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  background: #f8fafc;
  font-size: 14px;
  font-weight: 500;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: #f1f5f9;
  }

  &.active {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-color: transparent;
    color: #fff;
  }
}

.export-section {
  .option-group {
    display: flex;
    flex-direction: column;
    gap: 12px;
    margin-bottom: 20px;
  }

  .radio-label {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px;
    background: #f8fafc;
    border-radius: 10px;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      background: #f1f5f9;
    }

    input[type="radio"] {
      width: 18px;
      height: 18px;
      accent-color: #667eea;
    }

    span {
      font-size: 14px;
      color: #475569;
    }
  }
}

.import-section {
  .file-upload {
    margin-bottom: 16px;
  }

  .file-input {
    display: none;
  }

  .file-label {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    width: 100%;
    padding: 20px;
    border: 2px dashed #cbd5e1;
    border-radius: 12px;
    background: #f8fafc;
    font-size: 14px;
    color: #64748b;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      border-color: #667eea;
      background: rgba(102, 126, 234, 0.05);
    }
  }

  .import-textarea {
    width: 100%;
    height: 120px;
    padding: 14px;
    border: 1px solid #e5e7eb;
    border-radius: 10px;
    font-family: monospace;
    font-size: 13px;
    color: #475569;
    resize: none;
    transition: all 0.2s;

    &:focus {
      outline: none;
      border-color: #667eea;
      box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
    }

    &::placeholder {
      color: #94a3b8;
    }
  }

  .result-message {
    margin-top: 16px;
    padding: 12px 16px;
    border-radius: 8px;
    font-size: 14px;
    display: flex;
    align-items: center;
    gap: 8px;

    &.success {
      background: rgba(34, 197, 94, 0.1);
      color: #16a34a;
    }

    &:not(.success) {
      background: rgba(239, 68, 68, 0.1);
      color: #dc2626;
    }
  }
}

.action-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px;
  border: none;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
}

.export-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);

  &:hover:not(:disabled) {
    transform: translateY(-1px);
    box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
  }
}

.import-btn {
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
  color: #fff;
  box-shadow: 0 4px 12px rgba(34, 197, 94, 0.3);

  &:hover:not(:disabled) {
    transform: translateY(-1px);
    box-shadow: 0 6px 16px rgba(34, 197, 94, 0.4);
  }
}
</style>