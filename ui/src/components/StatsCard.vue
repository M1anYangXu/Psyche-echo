<script setup lang="ts">
import { computed } from 'vue'
import { Icon } from '@iconify/vue'
import type { Statistics } from '@/types'

const props = defineProps<{
  statistics: Statistics
}>()

const statCards = computed(() => [
  {
    title: '总记录数',
    value: props.statistics.totalNotes,
    icon: 'ri:file-text-fill',
    color: 'from-blue-500 to-blue-600',
    bgColor: 'bg-blue-50'
  },
  {
    title: '今日记录',
    value: props.statistics.todayNotes,
    icon: 'ri:sun-fill',
    color: 'from-orange-500 to-orange-600',
    bgColor: 'bg-orange-50'
  },
  {
    title: '本周记录',
    value: props.statistics.thisWeekNotes,
    icon: 'ri:calendar-week-fill',
    color: 'from-green-500 to-green-600',
    bgColor: 'bg-green-50'
  },
  {
    title: '本月记录',
    value: props.statistics.thisMonthNotes,
    icon: 'ri:calendar-fill',
    color: 'from-purple-500 to-purple-600',
    bgColor: 'bg-purple-50'
  }
])

const formatDateRange = computed(() => {
  if (props.statistics.earliestDate && props.statistics.latestDate) {
    return `${props.statistics.earliestDate} ~ ${props.statistics.latestDate}`
  }
  return '-'
})

const maxDailyCount = computed(() => {
  return Math.max(...props.statistics.recentDaysStats.map(d => d.count), 1)
})

const formatMonth = (month: string) => {
  const [year, m] = month.split('-')
  return `${year}年${parseInt(m)}月`
}

const formatDay = (date: string) => {
  const [, month, day] = date.split('-')
  return `${parseInt(month)}/${parseInt(day)}`
}
</script>

<template>
  <div class="stats-section">
    <div class="stats-header">
      <h2 class="stats-title">
        <Icon icon="ri:bar-chart-fill" :size="20" />
        数据统计
      </h2>
      <span class="date-range">{{ formatDateRange }}</span>
    </div>

    <div class="stats-cards">
      <div
        v-for="stat in statCards"
        :key="stat.title"
        class="stat-card"
        :class="stat.bgColor"
      >
        <div class="stat-icon" :class="stat.color">
          <Icon :icon="stat.icon" :size="24" />
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stat.value }}</span>
          <span class="stat-label">{{ stat.title }}</span>
        </div>
      </div>
    </div>

    <div class="stats-charts">
      <div class="chart-card">
        <h3 class="chart-title">
          <Icon icon="ri:folder-fill" :size="16" />
          分类分布
        </h3>
        <div class="category-bars">
          <div
            v-for="(count, category) in statistics.categoryStats"
            :key="category"
            class="category-bar-item"
          >
            <span class="category-name">{{ category }}</span>
            <div class="category-bar-wrapper">
              <div
                class="category-bar"
                :style="{ width: `${(count / Math.max(...Object.values(statistics.categoryStats), 1)) * 100}%` }"
              />
            </div>
            <span class="category-count">{{ count }}</span>
          </div>
        </div>
      </div>

      <div class="chart-card">
        <h3 class="chart-title">
          <Icon icon="ri:trending-up" :size="16" />
          近14天记录趋势
        </h3>
        <div class="daily-bars">
          <div
            v-for="day in statistics.recentDaysStats"
            :key="day.date"
            class="daily-bar-item"
          >
            <div
              class="daily-bar"
              :style="{ height: `${(day.count / maxDailyCount) * 100}%` }"
            />
            <span class="daily-label">{{ formatDay(day.date) }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="chart-card">
      <h3 class="chart-title">
        <Icon icon="ri:calendar-fill" :size="16" />
        月度记录统计
      </h3>
      <div class="monthly-list">
        <div
          v-for="month in statistics.monthlyStats.slice(-6)"
          :key="month.month"
          class="monthly-item"
        >
          <span class="monthly-label">{{ formatMonth(month.month) }}</span>
          <span class="monthly-count">{{ month.count }} 条</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.stats-section {
  margin-bottom: 32px;
}

.stats-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.stats-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
}

.date-range {
  font-size: 14px;
  color: #94a3b8;
  background-color: #f1f5f9;
  padding: 4px 12px;
  border-radius: 16px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border-radius: 12px;
  background-color: #fff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--tw-gradient-stops));

  svg {
    color: #fff;
  }
}

.stat-content {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #64748b;
  margin-top: 4px;
}

.stats-charts {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 20px;
}

.chart-card {
  background-color: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.chart-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  font-weight: 600;
  color: #334155;
  margin-bottom: 16px;
}

.category-bars {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.category-bar-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.category-name {
  width: 60px;
  font-size: 13px;
  color: #475569;
  text-align: right;
}

.category-bar-wrapper {
  flex: 1;
  height: 12px;
  background-color: #f1f5f9;
  border-radius: 6px;
  overflow: hidden;
}

.category-bar {
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 6px;
  transition: width 0.3s ease;
}

.category-count {
  width: 32px;
  font-size: 13px;
  font-weight: 600;
  color: #334155;
  text-align: right;
}

.daily-bars {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  height: 120px;
  padding-top: 20px;
}

.daily-bar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.daily-bar {
  width: 24px;
  background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
  border-radius: 4px 4px 0 0;
  transition: height 0.3s ease;
  min-height: 4px;
}

.daily-label {
  font-size: 11px;
  color: #94a3b8;
}

.monthly-list {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
}

.monthly-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px;
  background-color: #f8fafc;
  border-radius: 8px;
}

.monthly-label {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 4px;
}

.monthly-count {
  font-size: 18px;
  font-weight: 600;
  color: #334155;
}

@media (max-width: 1024px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .stats-charts {
    grid-template-columns: 1fr;
  }
  
  .monthly-list {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>