<template>
  <div class="user-list-container">
    <!-- 统计卡片 -->
    <el-card class="statistics-card">
      <div class="statistics-content">
        <div class="stat-item">
          <div class="stat-icon">
            <el-icon size="40" color="#409EFF"><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">用户总数</div>
            <div class="stat-value">{{ statistics.total }}</div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 搜索和筛选区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form" label-width="80px">
        <el-form-item label="OpenID">
          <el-input
            v-model="searchForm.openid"
            placeholder="请输入OpenID搜索"
            clearable
            @clear="handleSearch"
            style="width: 250px;"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleSearch">
            搜索
          </el-button>
          <el-button icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 用户列表表格 -->
    <el-card>
      <el-table
        v-loading="loading"
        :data="userList"
        stripe
        border
      >
        <el-table-column prop="openid" label="OpenID" min-width="280" show-overflow-tooltip :resizable="false" />

        <el-table-column label="创建时间" width="200" :resizable="false">
          <template #default="{ row }">
            {{ row.createdAt }}
          </template>
        </el-table-column>

        <el-table-column label="最后登录时间" width="200" :resizable="false">
          <template #default="{ row }">
            {{ row.lastLoginAt || '未登录' }}
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import { getUserList, getUserStatistics } from '@/api/user'

// 搜索表单
const searchForm = reactive({
  openid: ''
})

// 分页参数
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 统计数据
const statistics = reactive({
  total: 0
})

// 数据
const loading = ref(false)
const userList = ref([])

// 获取用户统计
const fetchUserStatistics = async () => {
  try {
    const response = await getUserStatistics()
    if (response.code === 200) {
      statistics.total = response.data.total || 0
    }
  } catch (error) {
    console.error('获取用户统计失败:', error)
  }
}

// 获取用户列表
const fetchUserList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1, // 后端从0开始
      size: pagination.size,
      openid: searchForm.openid || undefined
    }

    const response = await getUserList(params)
    if (response.code === 200) {
      userList.value = response.data.records || []
      pagination.total = response.data.total || 0
    } else {
      ElMessage.error(response.message || '获取用户列表失败')
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchUserList()
}

// 重置搜索
const handleReset = () => {
  searchForm.openid = ''
  handleSearch()
}

// 分页大小改变
const handleSizeChange = () => {
  pagination.page = 1
  fetchUserList()
}

// 页码改变
const handlePageChange = () => {
  fetchUserList()
}

// 组件挂载时获取数据
onMounted(() => {
  fetchUserStatistics()
  fetchUserList()
})
</script>

<style scoped>
.user-list-container {
  width: 100%;
  max-width: 100%;
  padding: 32px 40px;
}

.statistics-card {
  margin-bottom: 24px;
}

.statistics-card :deep(.el-card__body) {
  padding: 28px;
}

.statistics-content {
  display: flex;
  gap: 32px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;
  min-width: 280px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  line-height: 1;
}

.search-card {
  margin-bottom: 24px;
}

.search-card :deep(.el-card__body) {
  padding: 28px;
}

.search-form {
  margin-bottom: 0;
}

.search-form .el-form-item {
  margin-bottom: 0;
}

.pagination-container {
  margin-top: 28px;
  display: flex;
  justify-content: center;
}

.el-card:last-child :deep(.el-card__body) {
  padding: 28px;
}
</style>
