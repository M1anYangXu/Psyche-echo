import { ref, computed } from 'vue'
import { echoApiClient } from '@/api/echo'
import type { EchoItem, Category } from '@/types'

const categories = ref<Category[]>([])
const allEchoList = ref<EchoItem[]>([])

const selectedCategory = ref('全部')
const isLoading = ref(false)

export function useEcho() {
  const filteredEchoList = computed(() => {
    if (selectedCategory.value === '全部') {
      return allEchoList.value
    }
    return allEchoList.value.filter(d => d.spec.categoryName === selectedCategory.value)
  })

  const loadCategories = async () => {
    isLoading.value = true
    try {
      const data = await echoApiClient.categories.list()
      categories.value = [
        {
          metadata: { name: '全部' },
          spec: { name: '全部', icon: 'ri:folder-fill', count: allEchoList.value.length }
        },
        ...data.map(cat => ({
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
      allEchoList.value = [...data]
      await loadCategories()
    } catch (error) {
      console.error('Failed to load echoes:', error)
    } finally {
      isLoading.value = false
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
        selectedCategory.value = '全部'
      }
      await new Promise(resolve => setTimeout(resolve, 500))
      await loadEchoes()
    } catch (error: any) {
      console.error('Failed to delete category:', error?.response?.data || error)
      throw error
    }
  }

  return {
    categories,
    filteredEchoList,
    selectedCategory,
    isLoading,
    loadCategories,
    loadEchoes,
    selectCategory,
    addEcho,
    updateEcho,
    removeEcho,
    addCategory,
    updateCategory,
    removeCategory,
  }
}
