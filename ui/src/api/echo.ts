import type { Category, EchoItem, Statistics } from '@/types'
import axios from 'axios'

const echoNoteApiClient = axios.create({
  withCredentials: true,
})

echoNoteApiClient.interceptors.request.use((config) => {
  const csrfToken = document.querySelector('meta[name="csrf-token"]')?.getAttribute('content')
  if (csrfToken) {
    config.headers['X-CSRF-Token'] = csrfToken
  }
  return config
})

const API_VERSION = 'echo.miany.run/v1alpha1'
const CUSTOM_ENDPOINT_PREFIX = 'api.echo.miany.run/v1alpha1'

export const echoApiClient = {
  categories: {
    list: async (): Promise<Category[]> => {
      const response = await echoNoteApiClient.get(`/apis/${API_VERSION}/echocategories`)
      return response.data.items || []
    },

    get: async (name: string): Promise<Category> => {
      const response = await echoNoteApiClient.get(`/apis/${API_VERSION}/echocategories/${name}`)
      return response.data
    },

    create: async (category: Omit<Category, 'metadata'>): Promise<Category> => {
      const payload = {
        apiVersion: API_VERSION,
        kind: 'EchoCategory',
        metadata: {
          name: category.spec.name
        },
        spec: category.spec
      }
      const response = await echoNoteApiClient.post(`/apis/${API_VERSION}/echocategories`, payload)
      return response.data
    },

    update: async (name: string, category: Partial<Category>): Promise<Category> => {
      // 先获取最新数据，确保有正确的 version
      const latestCategory = await echoApiClient.categories.get(name)
      const payload = {
        apiVersion: API_VERSION,
        kind: 'EchoCategory',
        metadata: {
          name: name,
          version: latestCategory.metadata?.version
        },
        spec: {
          ...latestCategory.spec,
          ...category.spec
        }
      }
      const response = await echoNoteApiClient.put(`/apis/${API_VERSION}/echocategories/${name}`, payload)
      return response.data
    },

    delete: async (name: string): Promise<void> => {
      await echoNoteApiClient.delete(`/apis/${API_VERSION}/echocategories/${name}`)
    }
  },

  notes: {
    list: async (categoryId?: string): Promise<EchoItem[]> => {
      const params: any = categoryId ? { labelSelector: `spec.categoryName=${categoryId}`, limit: 100 } : { limit: 100 }
      const response = await echoNoteApiClient.get(`/apis/${API_VERSION}/echonotes`, { params })
      return response.data.items || []
    },

    get: async (name: string): Promise<EchoItem> => {
      const response = await echoNoteApiClient.get(`/apis/${API_VERSION}/echonotes/${name}`)
      return response.data
    },

    create: async (note: Omit<EchoItem, 'metadata' | 'status'>): Promise<EchoItem> => {
      const payload = {
        apiVersion: API_VERSION,
        kind: 'EchoNote',
        metadata: {
          generateName: 'echo-note-'
        },
        spec: note.spec
      }
      const response = await echoNoteApiClient.post(`/apis/${API_VERSION}/echonotes`, payload)
      return response.data
    },

    update: async (name: string, echo: EchoItem): Promise<EchoItem> => {
      // 先获取最新数据，确保有正确的 version
      const latestecho = await echoApiClient.notes.get(name)
      const payload = {
        apiVersion: API_VERSION,
        kind: 'EchoNote',
        metadata: {
          name: name,
          version: latestecho.metadata?.version
        },
        spec: echo.spec
      }
      const response = await echoNoteApiClient.put(`/apis/${API_VERSION}/echonotes/${name}`, payload)
      return response.data
    },

    delete: async (name: string): Promise<void> => {
      await echoNoteApiClient.delete(`/apis/${API_VERSION}/echonotes/${name}`)
    }
  },

  statistics: {
    get: async (): Promise<Statistics> => {
      const response = await echoNoteApiClient.get(`/apis/${CUSTOM_ENDPOINT_PREFIX}/echo/statistics`)
      return response.data.data
    }
  }
}
