<template>
  <div class="favorite-list-container">
    <!-- 搜索和筛选区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form" label-width="80px">
        <el-form-item label="用户ID">
          <el-input
            v-model="searchForm.userId"
            placeholder="请输入用户ID"
            clearable
            @clear="handleSearch"
            style="width: 200px;"
          />
        </el-form-item>

        <el-form-item label="商品ID">
          <el-input
            v-model="searchForm.productId"
            placeholder="请输入商品ID"
            clearable
            @clear="handleSearch"
            style="width: 200px;"
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

    <!-- 操作按钮区域 -->
    <el-card class="toolbar-card">
      <el-row :gutter="10">
        <el-col :span="12">
          <el-button
            type="danger"
            icon="Delete"
            :disabled="selectedIds.length === 0"
            @click="handleBatchDelete"
          >
            批量删除
          </el-button>
        </el-col>
        <el-col :span="12" class="text-right">
          <el-tag v-if="selectedIds.length > 0" type="info">
            已选择 {{ selectedIds.length }} 条收藏
          </el-tag>
        </el-col>
      </el-row>
    </el-card>

    <!-- 收藏列表表格 -->
    <el-card>
      <el-table
        v-loading="loading"
        :data="favoriteList"
        stripe
        border
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" :resizable="false" />

        <el-table-column prop="userId" label="用户ID" width="100" :resizable="false" />

        <el-table-column prop="productId" label="商品ID" width="100" :resizable="false" />

        <el-table-column label="商品图片" width="120" :resizable="false">
          <template #default="{ row }">
            <el-image
              v-if="row.productImage"
              :src="row.productImage"
              :preview-src-list="[row.productImage]"
              fit="cover"
              class="product-image"
            />
            <span v-else class="text-muted">无图片</span>
          </template>
        </el-table-column>

        <el-table-column label="商品名称" min-width="200" show-overflow-tooltip :resizable="false">
          <template #default="{ row }">
            {{ row.productName || '商品已删除' }}
          </template>
        </el-table-column>

        <el-table-column label="商品状态" width="120" :resizable="false">
          <template #default="{ row }">
            <el-tag v-if="row.productStatus === true" type="success" size="small">上架</el-tag>
            <el-tag v-else-if="row.productStatus === false" type="danger" size="small">下架</el-tag>
            <span v-else class="text-muted">未知</span>
          </template>
        </el-table-column>

        <el-table-column label="收藏时间" width="180" :resizable="false">
          <template #default="{ row }">
            {{ row.createdAt }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right" :resizable="false">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              icon="View"
              @click="handleViewDetail(row)"
            >
              详情
            </el-button>
            <el-button
              type="danger"
              size="small"
              icon="Delete"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
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

    <!-- 收藏详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="收藏详情"
      width="700px"
      @close="handleDetailDialogClose"
    >
      <div v-if="currentFavorite" class="favorite-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="收藏ID">
            {{ currentFavorite.id }}
          </el-descriptions-item>
          <el-descriptions-item label="用户ID">
            {{ currentFavorite.userId }}
          </el-descriptions-item>
          <el-descriptions-item label="商品ID">
            {{ currentFavorite.productId }}
          </el-descriptions-item>
          <el-descriptions-item label="收藏时间">
            {{ currentFavorite.createdAt }}
          </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">商品详细信息</el-divider>

        <div v-if="currentFavorite.productName" class="product-detail">
          <div class="detail-row">
            <div class="detail-label">商品图片：</div>
            <div class="detail-value">
              <el-image
                v-if="currentFavorite.productImage"
                :src="currentFavorite.productImage"
                :preview-src-list="[currentFavorite.productImage]"
                fit="cover"
                class="detail-image"
              />
              <span v-else class="text-muted">无图片</span>
            </div>
          </div>

          <div class="detail-row">
            <div class="detail-label">商品名称：</div>
            <div class="detail-value">{{ currentFavorite.productName }}</div>
          </div>

          <div class="detail-row">
            <div class="detail-label">商品价格：</div>
            <div class="detail-value">
              <span class="price">¥{{ currentFavorite.productPrice.toFixed(2) }}</span>
            </div>
          </div>

          <div class="detail-row">
            <div class="detail-label">库存数量：</div>
            <div class="detail-value">{{ currentFavorite.productStock }}</div>
          </div>

          <div class="detail-row">
            <div class="detail-label">商品状态：</div>
            <div class="detail-value">
              <el-tag v-if="currentFavorite.productStatus" type="success" size="small">上架</el-tag>
              <el-tag v-else type="danger" size="small">下架</el-tag>
            </div>
          </div>
        </div>
        <div v-else class="product-deleted">
          <el-alert
            title="商品已被删除"
            type="warning"
            :closable="false"
            show-icon
          />
        </div>
      </div>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getFavoriteList,
  getFavoriteDetail,
  deleteFavorite,
  batchDeleteFavorites
} from '@/api/favorite'

// 搜索表单
const searchForm = reactive({
  userId: '',
  productId: ''
})

// 分页参数
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 数据
const loading = ref(false)
const favoriteList = ref([])
const selectedIds = ref([])
const detailDialogVisible = ref(false)
const currentFavorite = ref(null)

// 获取收藏列表
const fetchFavoriteList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1, // 后端从0开始
      size: pagination.size,
      userId: searchForm.userId || undefined,
      productId: searchForm.productId || undefined
    }

    const response = await getFavoriteList(params)
    if (response.code === 200) {
      favoriteList.value = response.data.records || []
      pagination.total = response.data.total || 0
    } else {
      ElMessage.error(response.message || '获取收藏列表失败')
    }
  } catch (error) {
    console.error('获取收藏列表失败:', error)
    ElMessage.error('获取收藏列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchFavoriteList()
}

// 重置搜索
const handleReset = () => {
  searchForm.userId = ''
  searchForm.productId = ''
  handleSearch()
}

// 分页大小改变
const handleSizeChange = () => {
  pagination.page = 1
  fetchFavoriteList()
}

// 页码改变
const handlePageChange = () => {
  fetchFavoriteList()
}

// 选择改变
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

// 查看详情
const handleViewDetail = async (row) => {
  try {
    const response = await getFavoriteDetail(row.id)
    if (response.code === 200) {
      currentFavorite.value = response.data
      detailDialogVisible.value = true
    } else {
      ElMessage.error(response.message || '获取详情失败')
    }
  } catch (error) {
    console.error('获取收藏详情失败:', error)
    ElMessage.error('获取详情失败')
  }
}

// 关闭详情对话框
const handleDetailDialogClose = () => {
  currentFavorite.value = null
}

// 删除收藏
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除这条收藏记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await deleteFavorite(row.id)
      if (response.code === 200) {
        ElMessage.success('删除成功')
        fetchFavoriteList()
      } else {
        ElMessage.error(response.message || '删除失败')
      }
    } catch (error) {
      console.error('删除收藏失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {
    // 取消操作
  })
}

// 批量删除
const handleBatchDelete = () => {
  ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 条收藏记录吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await batchDeleteFavorites(selectedIds.value)
      if (response.code === 200) {
        ElMessage.success('批量删除成功')
        selectedIds.value = []
        fetchFavoriteList()
      } else {
        ElMessage.error(response.message || '批量删除失败')
      }
    } catch (error) {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }).catch(() => {
    // 取消操作
  })
}

// 组件挂载时获取数据
onMounted(() => {
  fetchFavoriteList()
})
</script>

<style scoped>
.favorite-list-container {
  width: 100%;
  max-width: 100%;
  padding: 32px 40px;
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

.toolbar-card {
  margin-bottom: 24px;
}

.toolbar-card :deep(.el-card__body) {
  padding: 20px 28px;
}

.text-right {
  text-align: right;
}

.pagination-container {
  margin-top: 28px;
  display: flex;
  justify-content: center;
}

.el-card:last-child :deep(.el-card__body) {
  padding: 28px;
}

.product-image {
  width: 100px;
  height: 60px;
  border-radius: 4px;
}

.text-muted {
  color: #909399;
  font-size: 14px;
}

.price {
  color: #f56c6c;
  font-weight: 600;
  font-size: 16px;
}

/* 详情样式 */
.favorite-detail {
  padding: 16px 0;
}

.product-detail {
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.detail-row {
  display: flex;
  margin-bottom: 16px;
  align-items: flex-start;
}

.detail-row:last-child {
  margin-bottom: 0;
}

.detail-label {
  width: 120px;
  font-weight: 600;
  color: #606266;
  flex-shrink: 0;
}

.detail-value {
  flex: 1;
  color: #303133;
}

.detail-image {
  width: 200px;
  height: 120px;
  border-radius: 8px;
}

.product-deleted {
  padding: 16px;
}
</style>
