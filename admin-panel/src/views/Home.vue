<template>
  <div class="home-container">
    <el-row :gutter="24">
      <el-col :span="24">
        <h2 class="page-title">欢迎回来，{{ userStore.realName || userStore.username }}</h2>
        <p class="page-subtitle">这是您的数据概览和快捷操作面板</p>
      </el-col>
    </el-row>

    <el-row :gutter="32" class="stats-row">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-icon" style="background-color: #409eff;">
              <el-icon :size="30"><Goods /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">--</div>
              <div class="stat-label">商品总数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-icon" style="background-color: #67c23a;">
              <el-icon :size="30"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">--</div>
              <div class="stat-label">用户总数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-icon" style="background-color: #e6a23c;">
              <el-icon :size="30"><ChatDotRound /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">--</div>
              <div class="stat-label">评论总数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-icon" style="background-color: #f56c6c;">
              <el-icon :size="30"><MessageBox /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">--</div>
              <div class="stat-label">待处理询价</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="32">
      <el-col :span="24">
        <el-card class="action-card">
          <template #header>
            <div class="card-header">
              <span>快速操作</span>
            </div>
          </template>
          <div class="quick-actions">
            <el-button type="primary" icon="Plus" @click="handleQuickAction('add-product')">
              添加商品
            </el-button>
            <el-button type="success" icon="Edit" @click="handleQuickAction('add-banner')">
              添加轮播图
            </el-button>
            <el-button type="warning" icon="View" @click="handleQuickAction('view-inquiries')">
              查看询价
            </el-button>
            <el-button type="info" icon="DataAnalysis" @click="handleQuickAction('analytics')">
              数据分析
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="32" style="margin-top: 32px;">
      <el-col :span="24">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <span>系统信息</span>
            </div>
          </template>
          <el-descriptions :column="3" border>
            <el-descriptions-item label="当前用户">
              {{ userStore.username }}
            </el-descriptions-item>
            <el-descriptions-item label="用户角色">
              <el-tag v-if="userStore.role === 'super_admin'" type="danger">超级管理员</el-tag>
              <el-tag v-else-if="userStore.role === 'product_admin'" type="success">商品管理员</el-tag>
              <el-tag v-else-if="userStore.role === 'customer_service'" type="warning">客服</el-tag>
              <el-tag v-else type="info">{{ userStore.role }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="真实姓名">
              {{ userStore.realName || '未设置' }}
            </el-descriptions-item>
            <el-descriptions-item label="登录时间">
              {{ new Date().toLocaleString('zh-CN') }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { Goods, User, ChatDotRound, MessageBox } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

// 快速操作处理
const handleQuickAction = (action) => {
  switch (action) {
    case 'add-product':
      router.push('/product/add')
      break
    case 'add-banner':
      ElMessage.info('轮播图管理功能开发中')
      // router.push('/banner/add')
      break
    case 'view-inquiries':
      ElMessage.info('询价管理功能开发中')
      // router.push('/inquiry/list')
      break
    case 'analytics':
      ElMessage.info('数据分析功能开发中')
      // router.push('/analytics')
      break
  }
}
</script>

<style scoped>
.home-container {
  width: 100%;
  max-width: 100%;
  padding: 32px 40px;
}

.page-title {
  margin: 0 0 12px 0;
  color: #111827;
  font-size: 32px;
  font-weight: 700;
}

.page-subtitle {
  margin: 0 0 32px 0;
  color: #6B7280;
  font-size: 15px;
}

.stats-row {
  margin-bottom: 32px;
}

.stat-card {
  margin-bottom: 0;
  transition: all 0.3s;
}

.stat-card :deep(.el-card__body) {
  padding: 28px;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1) !important;
}

.stat-item {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-right: 15px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.action-card,
.info-card {
  margin-bottom: 0;
}

.action-card :deep(.el-card__body),
.info-card :deep(.el-card__body) {
  padding: 28px;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 20px;
}

.quick-actions .el-button {
  width: 100%;
  height: 52px;
  font-size: 15px;
}
</style>
