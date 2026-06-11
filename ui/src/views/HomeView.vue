<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import {
  Dialog,
  Toast,
  IconSendPlaneFill,
} from '@halo-dev/components'
import EchoEditor from '@/components/EchoEditor.vue'
import EchoSidebar from '@/components/EchoSidebar.vue'
import EchoList from '@/components/EchoList.vue'
import StatsCard from '@/components/StatsCard.vue'
import NewCategoryModal from '@/components/NewCategoryModal.vue'
import ImagePreviewModal from '@/components/ImagePreviewModal.vue'
import IconSelectorModal from '@/components/IconSelectorModal.vue'
import ImportExportModal from '@/components/ImportExportModal.vue'
import { useEcho } from '@/composables/useEcho'
import type { EchoItem, Category as EchoCategory } from '@/types'

const {
  categories,
  allEchoList,
  filteredEchoList,
  selectedCategory,
  isLoading,
  statistics,
  loadCategories,
  loadEchoes,
  selectCategory,
  addEcho,
  updateEcho,
  removeEcho,
  addCategory,
  updateCategory,
  removeCategory,
  exportData,
  importData,
} = useEcho()

// Import/Export
const importExportModal = ref(false)

// Stats Page
const showStats = ref(true)

const toggleStats = () => {
  showStats.value = !showStats.value
}

const handleSelectCategory = (name: string) => {
  showStats.value = false
  selectCategory(name)
}

// Category Management
const newCategoryModal = ref(false)
const newCategoryIcon = ref('')
const iconSelectorModal = ref(false)

const openNewCategoryModal = () => {
  newCategoryIcon.value = ''
  newCategoryModal.value = true
}

const onIconSelect = (iconName: string) => {
  newCategoryIcon.value = iconName
  iconSelectorModal.value = false
}

const createCategory = async (category: { name: string; icon: string }) => {
  if (category.name === '默认') {
    Toast.error('不能创建名为"默认"的分类')
    return
  }

  const categoryNames = categories.value.filter(cat => cat.metadata.name !== '默认').map(cat => cat.metadata.name)
  if (categoryNames.includes(category.name)) {
    Toast.error('分类名称已存在')
    return
  }

  try {
    await addCategory({
      spec: {
        name: category.name,
        icon: category.icon,
        count: 0,
      },
    })
    Toast.success('分类创建成功')
  } catch {
    Toast.error('创建失败')
  }
}



// Echo Publishing
const newEcho = reactive({
  content: '',
  weatherDay: '',
  weatherNight: '',
  mood: '',
  location: '',
  adcode: '',
  environment: '',
})
const selectedMedias = ref<Array<{ url: string; type: string; cover?: string; displayName?: string }>>([])
const currentlyEditingEcho = ref<EchoItem | null>(null)

// Attachment Selector
const attachmentSelectorModal = ref(false)

const handleOpenAttachment = () => {
  attachmentSelectorModal.value = true
}

const onAttachmentsSelect = (attachments: Array<{ url?: string; status?: { permalink?: string }; spec?: { displayName?: string; mediaType?: string } }>) => {
  const medias = attachments.map((attachment) => {
    if (typeof attachment === 'string') {
      return {
        url: attachment,
        cover: attachment,
        type: 'image/jpeg',
      }
    }
    if ('url' in attachment && attachment.url) {
      return {
        url: attachment.url,
        cover: attachment.url,
        type: 'image/jpeg',
      }
    }
    if ('status' in attachment && attachment.status?.permalink) {
      return {
        url: attachment.status.permalink,
        cover: attachment.status.permalink,
        displayName: attachment.spec?.displayName,
        type: attachment.spec?.mediaType || 'image/jpeg',
      }
    }
    return null
  }).filter(Boolean) as Array<{ url: string; type: string; cover?: string; displayName?: string }>

  const existingUrls = new Set(selectedMedias.value.map(m => m.url))
  const newMedias = medias.filter(m => !existingUrls.has(m.url))

  selectedMedias.value = [...selectedMedias.value, ...newMedias]
  attachmentSelectorModal.value = false
}

const handleUpdateMedias = (medias: Array<{ url: string; type: string; cover?: string; displayName?: string }>) => {
  selectedMedias.value = [...medias]
}

const publishEcho = async () => {
  if (!newEcho.content.trim() && selectedMedias.value.length === 0) return

  try {
    await addEcho({
      spec: {
        content: newEcho.content,
        categoryName: selectedCategory.value,
        medias: selectedMedias.value.map(m => ({
          url: m.url,
          type: m.type,
        })),
        weatherDay: newEcho.weatherDay,
        weatherNight: newEcho.weatherNight,
        mood: newEcho.mood,
        location: newEcho.location,
        adcode: newEcho.adcode,
        environment: newEcho.environment,
      },
      status: {
        categoryId: selectedCategory.value,
        time: '刚刚',
        visitCount: 0,
      },
    })
    newEcho.content = ''
    newEcho.weatherDay = ''
    newEcho.weatherNight = ''
    newEcho.mood = ''
    newEcho.location = ''
    newEcho.adcode = ''
    newEcho.environment = ''
    selectedMedias.value = []
    Toast.success('发布成功')
  } catch {
    Toast.error('发布失败')
  }
}

// Echo Actions
const deleteEcho = (echo: EchoItem) => {
  Dialog.warning({
    title: '确定要删除该echo吗？',
    description: '该操作不可逆',
    confirmType: 'danger',
    onConfirm: async () => {
      try {
        await removeEcho(echo.metadata.name)
        await new Promise(resolve => setTimeout(resolve, 500))
        await loadEchoes()
        Toast.success('删除成功')
      } catch {
        Toast.error('删除失败')
      }
    },
  })
}

const startEdit = (echo: EchoItem) => {
  currentlyEditingEcho.value = echo
  echo.editing = true
}

const cancelEdit = (echo: EchoItem) => {
  currentlyEditingEcho.value = null
  echo.editing = false
}

const saveEdit = async (echo: EchoItem) => {
  try {
    await updateEcho(echo.metadata.name, echo)
    currentlyEditingEcho.value = null
    Toast.success('更新成功')
  } catch {
    Toast.error('更新失败')
  }
}

const handleMoveCategory = async (echo: EchoItem, categoryName: string) => {
  try {
    const updatedEcho = {
      ...echo,
      spec: {
        ...echo.spec,
        categoryName: categoryName,
      },
      status: {
        ...echo.status,
        categoryId: categoryName,
      }
    }
    await updateEcho(echo.metadata.name, updatedEcho)
    Toast.success(`已移动到「${categoryName}」分类`)
  } catch {
    Toast.error('移动失败')
  }
}

// Image Preview
const previewImageModal = ref(false)
const previewImageUrl = ref('')

const openPreviewImage = (url: string) => {
  previewImageUrl.value = url
  previewImageModal.value = true
}

// Category Reorder Handler
const handleReorderCategories = async (newCategories: EchoCategory[]) => {
  try {
    const API_VERSION = 'echo.miany.run/v1alpha1'
    let orderIndex = 0
    for (const category of newCategories) {
      if (category.metadata.name === '默认') {
        continue
      }
      const encodedName = encodeURIComponent(category.metadata.name)

      const getResponse = await fetch(`/apis/${API_VERSION}/echocategories/${encodedName}`, {
        credentials: 'include',
        headers: {
          'X-CSRF-Token': document.querySelector('meta[name="csrf-token"]')?.getAttribute('content') || ''
        }
      })
      if (!getResponse.ok) {
        console.error('获取分类失败:', getResponse.status)
        continue
      }
      const latestCategory = await getResponse.json()

      const payload = {
        apiVersion: API_VERSION,
        kind: 'EchoCategory',
        metadata: {
          name: category.metadata.name,
          version: latestCategory.metadata?.version
        },
        spec: {
          ...latestCategory.spec,
          order: orderIndex
        }
      }

      const putResponse = await fetch(`/apis/${API_VERSION}/echocategories/${encodedName}`, {
        method: 'PUT',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
          'X-CSRF-Token': document.querySelector('meta[name="csrf-token"]')?.getAttribute('content') || ''
        },
        body: JSON.stringify(payload)
      })
      if (!putResponse.ok) {
        const errorText = await putResponse.text()
        console.error('更新分类失败:', putResponse.status, errorText)
        continue
      }
      orderIndex++
    }
    await loadCategories()
    Toast.success('排序已更新')
  } catch (error) {
    console.error('排序更新失败:', error)
    Toast.error('排序更新失败')
  }
}

// Import/Export Handlers
const handleExport = async (mode: 'all' | 'current') => {
  try {
    const data = await exportData(mode)
    const blob = new Blob([data], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `echo-backup-${new Date().toISOString().split('T')[0]}.json`
    a.click()
    URL.revokeObjectURL(url)
    Toast.success('导出成功')
    return { success: true, message: '导出成功' }
  } catch {
    Toast.error('导出失败')
    return { success: false, message: '导出失败' }
  }
}

const handleImport = async (data: string) => {
  const result = await importData(data)
  if (result.success) {
    Toast.success(result.message)
    await loadEchoes()
  } else {
    Toast.error(result.message)
  }
  return result
}

onMounted(() => {
  loadEchoes()
})
</script>

<template>
  <div class="echo-layout">
    <EchoSidebar
      :categories="categories"
      :selected-category="selectedCategory"
      :show-stats="showStats"
      @select-category="handleSelectCategory"
      @toggle-stats="toggleStats"
      @open-new-category="openNewCategoryModal"
      @reorder-categories="handleReorderCategories"
    />

    <button class="import-export-btn" @click="importExportModal = true">
      <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
        <polyline points="17 8 12 3 7 8"></polyline>
        <line x1="12" x2="12" y1="3" y2="15"></line>
      </svg>
    </button>

    <main class="echo-content">
      <template v-if="showStats">
        <StatsCard v-if="statistics" :statistics="statistics" />
      </template>

      <template v-else>
        <div class="write-section">
        <div class="write-card">
          <EchoEditor
            v-model="newEcho.content"
            :medias="selectedMedias"
            :mood="newEcho.mood"
            :environment="newEcho.environment"
            :weatherDay="newEcho.weatherDay"
            :weatherNight="newEcho.weatherNight"
            :location="newEcho.location"
            @update:medias="handleUpdateMedias"
            @update:weatherDay="newEcho.weatherDay = $event"
            @update:weatherNight="newEcho.weatherNight = $event"
            @update:mood="newEcho.mood = $event"
            @update:location="newEcho.location = $event"
            @update:adcode="newEcho.adcode = $event"
            @update:environment="newEcho.environment = $event"
            @open-attachment="handleOpenAttachment"
          >
            <template #footer-right>
              <button
                class="publish-btn"
                :disabled="!newEcho.content.trim() && selectedMedias.length === 0"
                @click="publishEcho"
              >
                <IconSendPlaneFill />
                发布
              </button>
            </template>
          </EchoEditor>
        </div>
      </div>

      <EchoList
        :echoes="filteredEchoList"
        :is-loading="isLoading"
        :categories="categories"
        @edit="startEdit"
        @delete="deleteEcho"
        @save="saveEdit"
        @cancel="cancelEdit"
        @preview="openPreviewImage"
        @open-attachment="handleOpenAttachment"
        @move-category="handleMoveCategory"
      />
      </template>
    </main>

    <ImagePreviewModal
      v-model:visible="previewImageModal"
      :url="previewImageUrl"
    />

    <NewCategoryModal
      v-model:visible="newCategoryModal"
      :icon="newCategoryIcon"
      @confirm="createCategory"
      @open-icon-selector="iconSelectorModal = true"
    />



    <IconSelectorModal
      v-model:visible="iconSelectorModal"
      @select="onIconSelect"
    />

    <AttachmentSelectorModal
      v-model:visible="attachmentSelectorModal"
      :min="1"
      :max="9"
      :accepts="['image/*']"
      @select="onAttachmentsSelect"
    />

    <ImportExportModal
      v-model:visible="importExportModal"
      :selected-category="selectedCategory"
      :on-export="handleExport"
      :on-import="handleImport"
    />
  </div>
</template>

<style lang="scss" scoped>
.echo-layout {
  display: flex;
  gap: 24px;
  padding: 24px;
  background-color: #f3f4f6;
  height: 100vh;
  box-sizing: border-box;
  position: relative;
}

.import-export-btn {
  position: fixed;
  bottom: 32px;
  right: 32px;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5);
  }

  &:active {
    transform: translateY(0);
  }
}

.echo-content {
  flex: 1;
  background-color: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  min-width: 0;
  overflow-y: auto;
  display: flex;
  flex-direction: column;

  &::-webkit-scrollbar {
    width: 6px;
  }
  &::-webkit-scrollbar-thumb {
    background: #e2e8f0;
    border-radius: 3px;
  }
}

.write-section {
  margin-bottom: 32px;
}

.publish-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 24px;
  border-radius: 10px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border: none;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);

  &:hover {
    background: linear-gradient(135deg, #5a6fd6 0%, #6b429e 100%);
    box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
    transform: translateY(-1px);
  }

  &:disabled {
    background: #94a3b8;
    cursor: not-allowed;
    box-shadow: none;
    transform: none;
  }

  svg {
    width: 18px;
    height: 18px;
  }
}
</style>
