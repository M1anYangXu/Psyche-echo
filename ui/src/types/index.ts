export interface Category {
  metadata: {
    name: string
    creationTimestamp?: string
    version?: string
  }
  spec: {
    name: string
    icon: string
    count: number
    order?: number
  }
}

export interface EchoItem {
  metadata: {
    name: string
    creationTimestamp?: string
    version?: string
  }
  spec: {
    content: string
    categoryName?: string
    medias?: Array<{
      url: string
      type: string
      cover?: string
      displayName?: string
    }>
    weatherDay?: string
    weatherNight?: string
    mood?: string
    location?: string
    adcode?: string
    environment?: string
  }
  status: {
    categoryId: string
    time: string
    creationTimestamp?: string
    visitCount?: number
  }
  editing?: boolean
}

export interface PageResult<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface Statistics {
  totalNotes: number
  todayNotes: number
  thisWeekNotes: number
  thisMonthNotes: number
  categoryStats: Record<string, number>
  moodStats: Record<string, number>
  weatherStats: Record<string, number>
  environmentStats: Record<string, number>
  locationStats: Record<string, number>
  monthlyStats: MonthlyStat[]
  recentDaysStats: DailyStat[]
  earliestDate: string | null
  latestDate: string | null
}

export interface MonthlyStat {
  month: string
  count: number
}

export interface DailyStat {
  date: string
  count: number
}

export interface ExportData {
  version: string
  exportTime: string
  categories: Category[]
  notes: EchoItem[]
}

export interface ImportResult {
  success: boolean
  message: string
  importedCategories: number
  importedNotes: number
  skippedNotes: number
}
