<template>
  <div class="product-list-container">
    <!-- 搜索和筛选区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form" label-width="80px">
        <el-form-item label="商品名称">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入商品名称"
            clearable
            @clear="handleSearch"
            style="width: 260px;"
          />
        </el-form-item>

        <el-form-item label="分类">
          <el-select
            v-model="searchForm.categoryId"
            placeholder="请选择分类"
            clearable
            @clear="handleSearch"
            style="width: 200px;"
          >
            <el-option label="全部分类" :value="null" />
            <el-option
              v-for="category in categoryList"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            @clear="handleSearch"
            style="width: 160px;"
          >
            <el-option label="全部状态" :value="null" />
            <el-option label="已上架" :value="true" />
            <el-option label="已下架" :value="false" />
          </el-select>
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
          <el-button type="primary" icon="Plus" @click="handleAdd">
            添加商品
          </el-button>
          <el-button
            type="success"
            icon="Upload"
            :disabled="selectedIds.length === 0"
            @click="handleBatchStatus(true)"
          >
            批量上架
          </el-button>
          <el-button
            type="warning"
            icon="Download"
            :disabled="selectedIds.length === 0"
            @click="handleBatchStatus(false)"
          >
            批量下架
          </el-button>
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
            已选择 {{ selectedIds.length }} 个商品
          </el-tag>
        </el-col>
      </el-row>
    </el-card>

    <!-- 商品列表表格 -->
    <el-card>
      <el-table
        v-loading="loading"
        :data="productList"
        stripe
        border
        @selection-change="handleSelectionChange"
        @sort-change="handleSortChange"
      >
        <el-table-column type="selection" width="55" :resizable="false" />
        <el-table-column prop="id" label="ID" width="80" :resizable="false" />

        <el-table-column label="商品图片" width="100" :resizable="false">
          <template #default="{ row }">
            <el-image
              :src="row.mainImage"
              :preview-src-list="[row.mainImage]"
              fit="cover"
              class="product-image"
            />
          </template>
        </el-table-column>

        <el-table-column label="商品名称" min-width="200" show-overflow-tooltip :resizable="false">
          <template #default="{ row }">
            <el-link type="primary" @click="handleView(row)" :underline="false" class="product-name-link">
              {{ row.name }}
            </el-link>
          </template>
        </el-table-column>

        <el-table-column label="分类" width="120" :resizable="false">
          <template #default="{ row }">
            <el-tag size="small">{{ getCategoryName(row.mainCategoryId) }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="minOrderQuantity" label="起订量" width="100" :resizable="false">
          <template #default="{ row }">
            {{ row.minOrderQuantity }} {{ row.unit }}
          </template>
        </el-table-column>

        <el-table-column label="标签" width="150" :resizable="false">
          <template #default="{ row }">
            <el-tag v-if="row.isHot" type="danger" size="small">热门</el-tag>
            <el-tag v-if="row.isNew" type="success" size="small">最新</el-tag>
            <el-tag v-if="row.isRecommended" type="warning" size="small">推荐</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="150" :resizable="false">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              active-text="上架"
              inactive-text="下架"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right" :resizable="false">
          <template #default="{ row }">
            <el-button type="primary" size="small" icon="Edit" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="danger" size="small" icon="Delete" @click="handleDelete(row)">
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getProductList,
  updateProductStatus,
  deleteProduct,
  batchDeleteProducts,
  batchUpdateProductStatus
} from '@/api/product'
import { getCategoryList } from '@/api/category'

const router = useRouter()

// 搜索表单
const searchForm = reactive({
  keyword: '',
  categoryId: null,
  status: null,
  sortField: null,  // 排序字段
  sortOrder: null   // 排序方向: asc/desc
})

// 分页参数
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 数据
const loading = ref(false)
const productList = ref([])
const categoryList = ref([])
const selectedIds = ref([])

// 获取商品列表
const fetchProductList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1, // 后端从0开始
      size: pagination.size,
      keyword: searchForm.keyword || undefined,
      categoryId: searchForm.categoryId || undefined,
      status: searchForm.status !== null ? searchForm.status : undefined,
      sortField: searchForm.sortField || undefined,
      sortOrder: searchForm.sortOrder || undefined
    }

    const response = await getProductList(params)
    if (response.code === 200) {
      productList.value = response.data.records || []
      pagination.total = response.data.total || 0
    } else {
      ElMessage.error(response.message || '获取商品列表失败')
    }
  } catch (error) {
    console.error('获取商品列表失败:', error)
    ElMessage.error('获取商品列表失败')
  } finally {
    loading.value = false
  }
}

// 获取分类列表
const fetchCategoryList = async () => {
  try {
    const response = await getCategoryList()
    if (response.code === 200) {
      // 只取一级分类
      categoryList.value = response.data.filter(cat => cat.level === 1)
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

// 获取分类名称
const getCategoryName = (categoryId) => {
  const category = categoryList.value.find(cat => cat.id === categoryId)
  return category ? category.name : '未知'
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchProductList()
}

// 重置搜索
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.categoryId = null
  searchForm.status = null
  handleSearch()
}

// 分页大小改变
const handleSizeChange = () => {
  pagination.page = 1
  fetchProductList()
}

// 页码改变
const handlePageChange = () => {
  fetchProductList()
}

// 排序改变
const handleSortChange = ({ column, prop, order }) => {
  if (order === null) {
    searchForm.sortField = null
    searchForm.sortOrder = null
  } else {
    searchForm.sortField = prop
    searchForm.sortOrder = order === 'ascending' ? 'asc' : 'desc'
  }
  pagination.page = 1
  fetchProductList()
}

// 选择改变
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

// 添加商品
const handleAdd = () => {
  router.push('/product/add')
}

// 查看商品
const handleView = (row) => {
  router.push(`/product/view/${row.id}`)
}

// 编辑商品
const handleEdit = (row) => {
  router.push(`/product/edit/${row.id}`)
}

// 删除商品
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除商品"${row.name}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await deleteProduct(row.id)
      if (response.code === 200) {
        ElMessage.success('删除成功')
        fetchProductList()
      } else {
        ElMessage.error(response.message || '删除失败')
      }
    } catch (error) {
      console.error('删除商品失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {
    // 取消操作
  })
}

// 批量删除
const handleBatchDelete = () => {
  ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个商品吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await batchDeleteProducts(selectedIds.value)
      if (response.code === 200) {
        ElMessage.success('批量删除成功')
        selectedIds.value = []
        fetchProductList()
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

// 状态改变
const handleStatusChange = async (row) => {
  try {
    const response = await updateProductStatus(row.id, row.status)
    if (response.code === 200) {
      ElMessage.success(row.status ? '上架成功' : '下架成功')
    } else {
      ElMessage.error(response.message || '状态更新失败')
      // 恢复原状态
      row.status = !row.status
    }
  } catch (error) {
    console.error('更新状态失败:', error)
    ElMessage.error('状态更新失败')
    // 恢复原状态
    row.status = !row.status
  }
}

// 批量更新状态
const handleBatchStatus = (status) => {
  const action = status ? '上架' : '下架'
  ElMessageBox.confirm(`确定要${action}选中的 ${selectedIds.value.length} 个商品吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await batchUpdateProductStatus(selectedIds.value, status)
      if (response.code === 200) {
        ElMessage.success(`批量${action}成功`)
        selectedIds.value = []
        fetchProductList()
      } else {
        ElMessage.error(response.message || `批量${action}失败`)
      }
    } catch (error) {
      console.error(`批量${action}失败:`, error)
      ElMessage.error(`批量${action}失败`)
    }
  }).catch(() => {
    // 取消操作
  })
}

// 组件挂载时获取数据
onMounted(() => {
  fetchCategoryList()
  fetchProductList()
})
</script>

<style scoped>
.product-list-container {
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

.product-image {
  width: 60px;
  height: 60px;
  border-radius: 4px;
}

.pagination-container {
  margin-top: 28px;
  display: flex;
  justify-content: center;
}

.el-card:last-child :deep(.el-card__body) {
  padding: 28px;
}

/* 商品名称链接样式 */
.product-name-link {
  font-weight: 500;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.product-name-link:hover {
  color: #667eea !important;
  font-weight: 600;
}
</style>
