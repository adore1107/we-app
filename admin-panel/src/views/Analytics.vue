<template>
  <div class="analytics-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">数据分析</h2>
      <p class="page-subtitle">实时查看平台运营数据</p>
    </div>

    <!-- 概览卡片 -->
    <div class="overview-grid">
      <div class="stat-card" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
        <div class="stat-icon">
          <el-icon size="40"><Goods /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-label">商品总数</div>
          <div class="stat-value">{{ overview.productCount }}</div>
          <div class="stat-desc">上架: {{ overview.onlineProductCount }}</div>
        </div>
      </div>

      <div class="stat-card" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
        <div class="stat-icon">
          <el-icon size="40"><User /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-label">用户总数</div>
          <div class="stat-value">{{ overview.userCount }}</div>
          <div class="stat-desc">注册用户</div>
        </div>
      </div>

      <div class="stat-card" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
        <div class="stat-icon">
          <el-icon size="40"><Star /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-label">收藏总数</div>
          <div class="stat-value">{{ overview.favoriteCount }}</div>
          <div class="stat-desc">用户收藏</div>
        </div>
      </div>

      <div class="stat-card" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);">
        <div class="stat-icon">
          <el-icon size="40"><ChatDotRound /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-label">评论总数</div>
          <div class="stat-value">{{ overview.commentCount }}</div>
          <div class="stat-desc">用户评论</div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <el-row :gutter="24" class="chart-row">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">商品状态分布</span>
            </div>
          </template>
          <div ref="productStatusChart" class="chart"></div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">评论评分分布</span>
            </div>
          </template>
          <div ref="ratingChart" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="24" class="chart-row">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">热门商品 TOP 10（按收藏数）</span>
            </div>
          </template>
          <div ref="topFavoriteChart" class="chart-large"></div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">热门商品 TOP 10（按浏览数）</span>
            </div>
          </template>
          <div ref="topViewChart" class="chart-large"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Goods, User, Star, ChatDotRound } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import {
  getOverview,
  getProductStatus,
  getCommentRating,
  getTopProductsByFavorite,
  getTopProductsByView
} from '@/api/analytics'

// 概览数据
const overview = reactive({
  productCount: 0,
  onlineProductCount: 0,
  userCount: 0,
  favoriteCount: 0,
  commentCount: 0,
  bannerCount: 0
})

// ECharts 实例
const productStatusChart = ref(null)
const ratingChart = ref(null)
const topFavoriteChart = ref(null)
const topViewChart = ref(null)

let productStatusChartInstance = null
let ratingChartInstance = null
let topFavoriteChartInstance = null
let topViewChartInstance = null

// 获取概览数据
const fetchOverview = async () => {
  try {
    const response = await getOverview()
    if (response.code === 200) {
      Object.assign(overview, response.data)
    }
  } catch (error) {
    console.error('获取概览数据失败:', error)
    ElMessage.error('获取概览数据失败')
  }
}

// 初始化商品状态分布图表
const initProductStatusChart = async () => {
  try {
    const response = await getProductStatus()
    if (response.code === 200) {
      const data = response.data

      productStatusChartInstance = echarts.init(productStatusChart.value)
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left'
        },
        series: [
          {
            name: '商品状态',
            type: 'pie',
            radius: ['40%', '70%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 10,
              borderColor: '#fff',
              borderWidth: 2
            },
            label: {
              show: false,
              position: 'center'
            },
            emphasis: {
              label: {
                show: true,
                fontSize: 20,
                fontWeight: 'bold'
              }
            },
            labelLine: {
              show: false
            },
            data: [
              { value: data.online, name: '上架', itemStyle: { color: '#67C23A' } },
              { value: data.offline, name: '下架', itemStyle: { color: '#F56C6C' } }
            ]
          }
        ]
      }
      productStatusChartInstance.setOption(option)
    }
  } catch (error) {
    console.error('初始化商品状态图表失败:', error)
  }
}

// 初始化评分分布图表
const initRatingChart = async () => {
  try {
    const response = await getCommentRating()
    if (response.code === 200) {
      const data = response.data

      ratingChartInstance = echarts.init(ratingChart.value)
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: ['1星', '2星', '3星', '4星', '5星'],
          axisTick: {
            alignWithLabel: true
          }
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '评论数',
            type: 'bar',
            barWidth: '60%',
            data: [
              { value: data.rating1, itemStyle: { color: '#F56C6C' } },
              { value: data.rating2, itemStyle: { color: '#E6A23C' } },
              { value: data.rating3, itemStyle: { color: '#909399' } },
              { value: data.rating4, itemStyle: { color: '#409EFF' } },
              { value: data.rating5, itemStyle: { color: '#67C23A' } }
            ]
          }
        ]
      }
      ratingChartInstance.setOption(option)
    }
  } catch (error) {
    console.error('初始化评分图表失败:', error)
  }
}

// 初始化热门商品（收藏）图表
const initTopFavoriteChart = async () => {
  try {
    const response = await getTopProductsByFavorite()
    if (response.code === 200) {
      const data = response.data
      const names = data.map(item => item.name)
      const values = data.map(item => item.favoriteCount)

      topFavoriteChartInstance = echarts.init(topFavoriteChart.value)
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'value'
        },
        yAxis: {
          type: 'category',
          data: names,
          axisLabel: {
            interval: 0,
            formatter: function(value) {
              return value.length > 15 ? value.substring(0, 15) + '...' : value
            }
          }
        },
        series: [
          {
            name: '收藏数',
            type: 'bar',
            data: values,
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                { offset: 0, color: '#4facfe' },
                { offset: 1, color: '#00f2fe' }
              ])
            }
          }
        ]
      }
      topFavoriteChartInstance.setOption(option)
    }
  } catch (error) {
    console.error('初始化热门商品图表失败:', error)
  }
}

// 初始化热门商品（浏览）图表
const initTopViewChart = async () => {
  try {
    const response = await getTopProductsByView()
    if (response.code === 200) {
      const data = response.data
      const names = data.map(item => item.name)
      const values = data.map(item => item.viewCount)

      topViewChartInstance = echarts.init(topViewChart.value)
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'value'
        },
        yAxis: {
          type: 'category',
          data: names,
          axisLabel: {
            interval: 0,
            formatter: function(value) {
              return value.length > 15 ? value.substring(0, 15) + '...' : value
            }
          }
        },
        series: [
          {
            name: '浏览数',
            type: 'bar',
            data: values,
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                { offset: 0, color: '#f093fb' },
                { offset: 1, color: '#f5576c' }
              ])
            }
          }
        ]
      }
      topViewChartInstance.setOption(option)
    }
  } catch (error) {
    console.error('初始化浏览排行图表失败:', error)
  }
}

// 窗口大小变化时调整图表
const handleResize = () => {
  productStatusChartInstance?.resize()
  ratingChartInstance?.resize()
  topFavoriteChartInstance?.resize()
  topViewChartInstance?.resize()
}

// 组件挂载
onMounted(async () => {
  await fetchOverview()
  await initProductStatusChart()
  await initRatingChart()
  await initTopFavoriteChart()
  await initTopViewChart()

  window.addEventListener('resize', handleResize)
})

// 组件卸载
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  productStatusChartInstance?.dispose()
  ratingChartInstance?.dispose()
  topFavoriteChartInstance?.dispose()
  topViewChartInstance?.dispose()
})
</script>

<style scoped>
.analytics-container {
  width: 100%;
  max-width: 100%;
  padding: 32px 40px;
}

.page-header {
  margin-bottom: 32px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 8px 0;
}

.page-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  margin-bottom: 32px;
}

.stat-card {
  padding: 28px;
  border-radius: 16px;
  color: white;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: transform 0.3s, box-shadow 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.25);
}

.stat-icon {
  width: 70px;
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  backdrop-filter: blur(10px);
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 36px;
  font-weight: 700;
  line-height: 1;
  margin-bottom: 8px;
}

.stat-desc {
  font-size: 13px;
  opacity: 0.8;
}

.chart-row {
  margin-bottom: 24px;
}

.chart-card {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.chart-card :deep(.el-card__header) {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.chart {
  width: 100%;
  height: 350px;
}

.chart-large {
  width: 100%;
  height: 450px;
}
</style>
