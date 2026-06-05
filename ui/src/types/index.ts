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
  }
}

export interface EchoItem {
  metadata: {
    name: string
    creationTimestamp?: string
    version?: string
  }
  spec: {
    author: string
    avatar: string
    content: string
    categoryName?: string
    medias?: Array<{
      url: string
      type: string
      cover?: string
      displayName?: string
    }>
    weather?: string
    mood?: string
  }
  status: {
    categoryId: string
    time: string
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
