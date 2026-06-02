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
import NewCategoryModal from '@/components/NewCategoryModal.vue'
import ImagePreviewModal from '@/components/ImagePreviewModal.vue'
import IconSelectorModal from '@/components/IconSelectorModal.vue'
import { useEcho } from '@/composables/useEcho'
import type { EchoItem, Category } from '@/types'

const {
  categories,
  filteredEchoList,
  selectedCategory,
  isLoading,
  loadEchoes,
  selectCategory,
  addEcho,
  updateEcho,
  removeEcho,
  addCategory,
  updateCategory,
  removeCategory,
} = useEcho()

// Category Management
const newCategoryModal = ref(false)
const editCategoryModal = ref(false)
const newCategoryIcon = ref('')
const editCategoryIcon = ref('')
const iconSelectorModal = ref(false)
const editingCategory = ref<Category | null>(null)

const openNewCategoryModal = () => {
  newCategoryIcon.value = ''
  newCategoryModal.value = true
}

const openEditCategoryModal = (category: Category) => {
  editingCategory.value = category
  editCategoryIcon.value = category.spec.icon
  editCategoryModal.value = true
}

const closeEditCategoryModal = () => {
  editingCategory.value = null
  editCategoryIcon.value = ''
  editCategoryModal.value = false
}

const onIconSelect = (iconName: string) => {
  if (editingCategory.value) {
    editCategoryIcon.value = iconName
  } else {
    newCategoryIcon.value = iconName
  }
  iconSelectorModal.value = false
}

const createCategory = async (category: { name: string; icon: string }) => {
  const categoryNames = categories.value.filter(cat => cat.metadata.name !== '全部').map(cat => cat.metadata.name)
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

const saveCategory = async (category: { name: string; icon: string }) => {
  if (!editingCategory.value) return

  const categoryNames = categories.value.filter(cat => cat.metadata.name !== '全部' && cat.metadata.name !== editingCategory.value!.metadata.name).map(cat => cat.metadata.name)
  if (categoryNames.includes(category.name)) {
    Toast.error('分类名称已存在')
    return
  }

  try {
    await updateCategory(editingCategory.value!.metadata.name, {
      spec: {
        name: category.name,
        icon: category.icon,
        count: editingCategory.value!.spec.count,
      },
    })
    closeEditCategoryModal()
    Toast.success('分类更新成功')
  } catch {
    Toast.error('更新失败')
  }
}

const deleteCategory = (name: string) => {
  Dialog.warning({
    title: '确定要删除该分类吗？',
    description: '该操作不可逆',
    confirmType: 'danger',
    onConfirm: async () => {
      try {
        await removeCategory(name)
        Toast.success('删除成功')
      } catch {
        Toast.error('删除失败')
      }
    },
  })
}

// Echo Publishing
const newEcho = reactive({
  content: '',
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
        author: 'Administrator',
        avatar: '',
        content: newEcho.content,
        categoryName: selectedCategory.value,
        medias: selectedMedias.value.map(m => ({
          url: m.url,
          type: m.type,
        })),
      },
      status: {
        categoryId: selectedCategory.value,
        time: '刚刚',
        visitCount: 0,
      },
    })
    newEcho.content = ''
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

// Image Preview
const previewImageModal = ref(false)
const previewImageUrl = ref('')

const openPreviewImage = (url: string) => {
  previewImageUrl.value = url
  previewImageModal.value = true
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
      @select-category="selectCategory"
      @open-new-category="openNewCategoryModal"
      @edit-category="openEditCategoryModal"
      @delete-category="deleteCategory"
    />

    <main class="echo-content">
      <div class="write-section">
        <div class="write-card">
          <EchoEditor
            v-model="newEcho.content"
            :medias="selectedMedias"
            @update:medias="handleUpdateMedias"
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
        @edit="startEdit"
        @delete="deleteEcho"
        @save="saveEdit"
        @cancel="cancelEdit"
        @preview="openPreviewImage"
        @open-attachment="handleOpenAttachment"
      />
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

    <NewCategoryModal
      v-model:visible="editCategoryModal"
      :icon="editCategoryIcon"
      :title="'编辑分类'"
      :default-name="editingCategory?.spec.name || ''"
      @confirm="saveCategory"
      @open-icon-selector="iconSelectorModal = true"
      @cancel="closeEditCategoryModal"
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
  padding: 8px 20px;
  border-radius: 8px;
  background-color: #3b82f6;
  color: #fff;
  border: none;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background-color: #2563eb;
  }

  &:disabled {
    background-color: #94a3b8;
    cursor: not-allowed;
  }

  svg {
    width: 18px;
    height: 18px;
  }
}
</style>
