import { ref, computed } from 'vue'
import { echoApiClient } from '@/api/echo'
import type { EchoItem, Category, Statistics, ExportData, ImportResult } from '@/types'

const categories = ref<Category[]>([])
const allEchoList = ref<EchoItem[]>([])
const statistics = ref<Statistics | null>(null)

const selectedCategory = ref('默认')
const isLoading = ref(false)

export function useEcho() {
  const filteredEchoList = computed(() => {
    if (selectedCategory.value === '默认') {
      return allEchoList.value.filter(d => d.spec.categoryName === '默认')
    }
    return allEchoList.value.filter(d => d.spec.categoryName === selectedCategory.value)
  })

  const loadCategories = async () => {
    isLoading.value = true
    try {
      const data = await echoApiClient.categories.list()
      const defaultCategory = data.find((cat: Category) => cat.metadata.name === '默认')
      const otherCategories = data.filter(cat => cat.metadata.name !== '默认')

      categories.value = [
        {
          metadata: { name: '默认' },
          spec: { name: '默认', icon: 'ri:folder-fill', count: allEchoList.value.filter(d => d.spec.categoryName === '默认').length }
        },
        ...otherCategories.sort((a, b) => (a.spec.order ?? 0) - (b.spec.order ?? 0)).map(cat => ({
          ...cat,
          spec: {
            ...cat.spec,
            count: allEchoList.value.filter(d => d.spec.categoryName === cat.metadata.name).length
          }
        }))
      ]
    } catch (error) {
      console.error('Failed to load categories:', error)
    } finally {
      isLoading.value = false
    }
  }

  const loadEchoes = async () => {
    isLoading.value = true
    try {
      const data = await echoApiClient.notes.list()
      const processedData = data.map(note => ({
        ...note,
        status: {
          ...note.status,
          creationTimestamp: note.status?.creationTimestamp || note.metadata?.creationTimestamp
        }
      }))
      allEchoList.value = [...processedData]
      await loadCategories()
      await loadStatistics()
    } catch (error) {
      console.error('Failed to load echoes:', error)
    } finally {
      isLoading.value = false
    }
  }

  const loadStatistics = async () => {
    try {
      const data = await echoApiClient.statistics.get()
      statistics.value = data
    } catch (error) {
      console.error('Failed to load statistics:', error)
    }
  }

  const selectCategory = (name: string) => {
    selectedCategory.value = name
  }

  const addEcho = async (echo: Omit<EchoItem, 'metadata'>) => {
    try {
      const newEcho = await echoApiClient.notes.create(echo)
      await loadEchoes()
      return newEcho
    } catch (error: any) {
      console.error('Failed to create echo:', error?.response?.data || error)
      throw error
    }
  }

  const updateEcho = async (name: string, echo: EchoItem) => {
    try {
      const updatedEcho = await echoApiClient.notes.update(name, echo)
      await loadEchoes()
      return updatedEcho
    } catch (error: any) {
      console.error('Failed to update echo:', error?.response?.data || error)
      throw error
    }
  }

  const removeEcho = async (name: string) => {
    try {
      await echoApiClient.notes.delete(name)
      await new Promise(resolve => setTimeout(resolve, 500))
      await loadEchoes()
    } catch (error: any) {
      console.error('Failed to delete echo:', error?.response?.data || error)
      throw error
    }
  }

  const addCategory = async (category: Omit<Category, 'metadata'>) => {
    try {
      const newCategory = await echoApiClient.categories.create(category)
      await loadCategories()
      return newCategory
    } catch (error: any) {
      console.error('Failed to create category:', error?.response?.data || error)
      throw error
    }
  }

  const updateCategory = async (name: string, category: Partial<Category>) => {
    try {
      const updatedCategory = await echoApiClient.categories.update(name, category)
      await loadCategories()
      return updatedCategory
    } catch (error: any) {
      console.error('Failed to update category:', error?.response?.data || error)
      throw error
    }
  }

  const removeCategory = async (name: string) => {
    try {
      await echoApiClient.categories.delete(name)
      if (selectedCategory.value === name) {
        selectedCategory.value = '默认'
      }
      await new Promise(resolve => setTimeout(resolve, 500))
      await loadCategories()
      await loadEchoes()
    } catch (error: any) {
      console.error('Failed to delete category:', error?.response?.data || error)
      throw error
    }
  }

  const exportData = async (exportMode: 'all' | 'current'): Promise<string> => {
    const [allCategories, allNotes] = await Promise.all([
      echoApiClient.categories.list(),
      echoApiClient.notes.list()
    ])

    let notesToExport = allNotes
    if (exportMode === 'current') {
      notesToExport = allNotes.filter(note => note.spec.categoryName === selectedCategory.value)
    }

    const notesWithTimestamp = notesToExport.map(note => ({
      ...note,
      status: {
        ...note.status,
        creationTimestamp: note.status?.creationTimestamp || note.metadata?.creationTimestamp
      }
    }))

    const exportData: ExportData = {
      version: '1.0',
      exportTime: new Date().toISOString(),
      categories: allCategories,
      notes: notesWithTimestamp
    }

    return JSON.stringify(exportData, null, 2)
  }

  const importData = async (jsonString: string): Promise<ImportResult> => {
    try {
      const data: ExportData = JSON.parse(jsonString)

      if (data.version !== '1.0') {
        return {
          success: false,
          message: '不支持的导出格式版本',
          importedCategories: 0,
          importedNotes: 0,
          skippedNotes: 0
        }
      }

      let importedCategories = 0
      let importedNotes = 0
      let skippedNotes = 0

      const existingCategories = await echoApiClient.categories.list()
      const existingCategoryNames = new Set(existingCategories.map(c => c.metadata.name))

      for (const category of data.categories) {
        if (!existingCategoryNames.has(category.metadata.name)) {
          try {
            await echoApiClient.categories.create({ spec: category.spec })
            importedCategories++
          } catch {
            console.warn(`创建分类失败: ${category.spec.name}`)
          }
        }
      }

      for (const note of data.notes) {
        try {
          const originalTimestamp = note.status?.creationTimestamp || note.metadata?.creationTimestamp

          const existingNote = allEchoList.value.find(n =>
            n.spec.content === note.spec.content &&
            (n.status?.creationTimestamp === originalTimestamp || n.metadata?.creationTimestamp === originalTimestamp)
          )

          if (existingNote) {
            skippedNotes++
            continue
          }

          const payload = {
            metadata: {
              generateName: 'echo-note-'
            },
            spec: {
              content: note.spec.content,
              categoryName: note.spec.categoryName || '默认',
              medias: note.spec.medias,
              weatherDay: note.spec.weatherDay,
              weatherNight: note.spec.weatherNight,
              mood: note.spec.mood,
              location: note.spec.location,
              adcode: note.spec.adcode,
              environment: note.spec.environment
            },
            status: {
              categoryId: note.status?.categoryId || note.spec.categoryName || '默认',
              time: note.status?.time || '刚刚',
              visitCount: note.status?.visitCount || 0,
              creationTimestamp: originalTimestamp
            }
          }
          await echoApiClient.notes.create(payload)
          importedNotes++
        } catch {
          skippedNotes++
        }
      }

      await loadEchoes()

      return {
        success: true,
        message: `导入完成！新增 ${importedCategories} 个分类，${importedNotes} 篇日记，跳过 ${skippedNotes} 篇重复日记`,
        importedCategories,
        importedNotes,
        skippedNotes
      }
    } catch (error) {
      return {
        success: false,
        message: '导入失败：无效的JSON格式',
        importedCategories: 0,
        importedNotes: 0,
        skippedNotes: 0
      }
    }
  }

  return {
    categories,
    allEchoList,
    filteredEchoList,
    selectedCategory,
    isLoading,
    statistics,
    loadCategories,
    loadEchoes,
    loadStatistics,
    selectCategory,
    addEcho,
    updateEcho,
    removeEcho,
    addCategory,
    updateCategory,
    removeCategory,
    exportData,
    importData,
  }
}
