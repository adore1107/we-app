<template>
  <div class="banner-list-container">
    <!-- 筛选和操作按钮区域 -->
    <el-card class="toolbar-card">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-select
            v-model="filterType"
            placeholder="筛选类型"
            clearable
            @change="handleFilterChange"
            style="width: 200px; margin-right: 10px;"
          >
            <el-option label="全部类型" value="" />
            <el-option label="首页轮播" value="home_banner" />
            <el-option label="公司风采" value="company_gallery" />
          </el-select>
          <el-button type="primary" icon="Plus" @click="handleAdd">
            添加轮播图
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
            已选择 {{ selectedIds.length }} 个轮播图
          </el-tag>
        </el-col>
      </el-row>
    </el-card>

    <!-- 轮播图列表表格 -->
    <el-card>
      <el-table
        v-loading="loading"
        :data="bannerList"
        stripe
        border
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" :resizable="false" />

        <el-table-column label="图片" width="120" :resizable="false">
          <template #default="{ row }">
            <el-image
              :src="row.imageUrl"
              :preview-src-list="[row.imageUrl]"
              fit="cover"
              class="banner-image"
            />
          </template>
        </el-table-column>

        <el-table-column label="标题" min-width="200" show-overflow-tooltip :resizable="false">
          <template #default="{ row }">
            {{ row.title || '无标题' }}
          </template>
        </el-table-column>

        <el-table-column label="类型" width="120" :resizable="false">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)" size="small">
              {{ getTypeLabel(row.type) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="链接类型" width="120" :resizable="false">
          <template #default="{ row }">
            <el-tag size="small">{{ getLinkTypeLabel(row.linkType) }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="linkUrl" label="跳转链接" min-width="180" show-overflow-tooltip :resizable="false" />

        <el-table-column prop="sortOrder" label="排序" width="120" :resizable="false">
          <template #default="{ row }">
            <el-input-number
              v-model="row.sortOrder"
              :min="0"
              :max="9999"
              size="small"
              @change="handleSortChange(row)"
            />
          </template>
        </el-table-column>

        <el-table-column label="状态" width="150" :resizable="false">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              active-text="启用"
              inactive-text="禁用"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>

        <el-table-column label="创建时间" width="180" :resizable="false">
          <template #default="{ row }">
            {{ row.createdAt }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right" :resizable="false">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              icon="Edit"
              @click="handleEdit(row)"
            >
              编辑
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
    </el-card>

    <!-- 添加/编辑轮播图对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="formData.id ? '编辑轮播图' : '添加轮播图'"
      width="700px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="轮播图类型" prop="type">
          <el-select v-model="formData.type" placeholder="请选择轮播图类型">
            <el-option label="首页轮播" value="home_banner" />
            <el-option label="公司风采" value="company_gallery" />
          </el-select>
        </el-form-item>

        <el-form-item label="标题" prop="title">
          <el-input
            v-model="formData.title"
            placeholder="请输入标题（可选）"
            clearable
          />
        </el-form-item>

        <el-form-item label="图片URL" prop="imageUrl">
          <el-input
            v-model="formData.imageUrl"
            placeholder="请输入图片URL"
            clearable
          />
          <div v-if="formData.imageUrl" class="image-preview">
            <el-image :src="formData.imageUrl" fit="cover" />
          </div>
        </el-form-item>

        <el-form-item label="链接类型" prop="linkType">
          <el-select v-model="formData.linkType" placeholder="请选择链接类型">
            <el-option label="无链接" value="none" />
            <el-option label="商品详情" value="product" />
            <el-option label="分类页面" value="category" />
            <el-option label="自定义URL" value="url" />
          </el-select>
        </el-form-item>

        <el-form-item label="跳转链接" prop="linkUrl">
          <el-input
            v-model="formData.linkUrl"
            placeholder="请输入跳转链接（可选）"
            clearable
          />
          <div class="form-tip">
            商品详情: 输入商品ID（如：123）<br>
            分类页面: 输入分类ID（如：1）<br>
            自定义URL: 输入完整URL
          </div>
        </el-form-item>

        <el-form-item label="排序" prop="sortOrder">
          <el-input-number
            v-model="formData.sortOrder"
            :min="0"
            :max="9999"
            placeholder="数值越大越靠前"
          />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="formData.status"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getBannerList,
  addBanner,
  updateBanner,
  deleteBanner,
  batchDeleteBanners,
  updateBannerStatus,
  updateBannerSortOrder
} from '@/api/banner'

// 数据
const loading = ref(false)
const bannerList = ref([])
const selectedIds = ref([])
const filterType = ref('')
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)

// 表单数据
const formData = reactive({
  id: null,
  type: 'home_banner',
  title: '',
  imageUrl: '',
  linkUrl: '',
  linkType: 'none',
  sortOrder: 0,
  status: true
})

// 表单验证规则
const formRules = {
  type: [
    { required: true, message: '请选择轮播图类型', trigger: 'change' }
  ],
  imageUrl: [
    { required: true, message: '请输入图片URL', trigger: 'blur' },
    { type: 'url', message: '请输入正确的URL格式', trigger: 'blur' }
  ],
  linkType: [
    { required: true, message: '请选择链接类型', trigger: 'change' }
  ]
}

// 获取轮播图列表
const fetchBannerList = async () => {
  loading.value = true
  try {
    const response = await getBannerList(filterType.value || undefined)
    if (response.code === 200) {
      bannerList.value = response.data || []
    } else {
      ElMessage.error(response.message || '获取轮播图列表失败')
    }
  } catch (error) {
    console.error('获取轮播图列表失败:', error)
    ElMessage.error('获取轮播图列表失败')
  } finally {
    loading.value = false
  }
}

// 筛选类型改变
const handleFilterChange = () => {
  fetchBannerList()
}

// 添加轮播图
const handleAdd = () => {
  resetForm()
  dialogVisible.value = true
}

// 编辑轮播图
const handleEdit = (row) => {
  formData.id = row.id
  formData.type = row.type
  formData.title = row.title || ''
  formData.imageUrl = row.imageUrl
  formData.linkUrl = row.linkUrl || ''
  formData.linkType = row.linkType || 'none'
  formData.sortOrder = row.sortOrder
  formData.status = row.status
  dialogVisible.value = true
}

// 删除轮播图
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除轮播图"${row.title || 'ID:' + row.id}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await deleteBanner(row.id)
      if (response.code === 200) {
        ElMessage.success('删除成功')
        fetchBannerList()
      } else {
        ElMessage.error(response.message || '删除失败')
      }
    } catch (error) {
      console.error('删除轮播图失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {
    // 取消操作
  })
}

// 批量删除
const handleBatchDelete = () => {
  ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个轮播图吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await batchDeleteBanners(selectedIds.value)
      if (response.code === 200) {
        ElMessage.success('批量删除成功')
        selectedIds.value = []
        fetchBannerList()
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
    const response = await updateBannerStatus(row.id, row.status)
    if (response.code === 200) {
      ElMessage.success(row.status ? '启用成功' : '禁用成功')
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

// 排序改变
const handleSortChange = async (row) => {
  try {
    const response = await updateBannerSortOrder(row.id, row.sortOrder)
    if (response.code === 200) {
      ElMessage.success('排序更新成功')
      fetchBannerList()
    } else {
      ElMessage.error(response.message || '排序更新失败')
    }
  } catch (error) {
    console.error('更新排序失败:', error)
    ElMessage.error('排序更新失败')
  }
}

// 选择改变
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      const data = {
        type: formData.type,
        title: formData.title || null,
        imageUrl: formData.imageUrl,
        linkUrl: formData.linkUrl || null,
        linkType: formData.linkType,
        sortOrder: formData.sortOrder,
        status: formData.status
      }

      let response
      if (formData.id) {
        // 编辑
        response = await updateBanner(formData.id, data)
      } else {
        // 添加
        response = await addBanner(data)
      }

      if (response.code === 200) {
        ElMessage.success(formData.id ? '更新成功' : '添加成功')
        dialogVisible.value = false
        fetchBannerList()
      } else {
        ElMessage.error(response.message || '操作失败')
      }
    } catch (error) {
      console.error('提交失败:', error)
      ElMessage.error('操作失败')
    } finally {
      submitting.value = false
    }
  })
}

// 重置表单
const resetForm = () => {
  formData.id = null
  formData.type = 'home_banner'
  formData.title = ''
  formData.imageUrl = ''
  formData.linkUrl = ''
  formData.linkType = 'none'
  formData.sortOrder = 0
  formData.status = true

  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

// 关闭对话框
const handleDialogClose = () => {
  resetForm()
}

// 获取类型标签
const getTypeLabel = (type) => {
  const map = {
    'home_banner': '首页轮播',
    'company_gallery': '公司风采'
  }
  return map[type] || type
}

// 获取类型标签颜色
const getTypeTagType = (type) => {
  const map = {
    'home_banner': 'primary',
    'company_gallery': 'success'
  }
  return map[type] || ''
}

// 获取链接类型标签
const getLinkTypeLabel = (linkType) => {
  const map = {
    'none': '无链接',
    'product': '商品详情',
    'category': '分类页面',
    'url': '自定义URL'
  }
  return map[linkType] || linkType
}

// 组件挂载时获取数据
onMounted(() => {
  fetchBannerList()
})
</script>

<style scoped>
.banner-list-container {
  width: 100%;
  max-width: 100%;
  padding: 32px 40px;
}

.toolbar-card {
  margin-bottom: 24px;
}

.toolbar-card :deep(.el-card__body) {
  padding: 20px 28px;
}

.el-card:last-child :deep(.el-card__body) {
  padding: 28px;
}

.text-right {
  text-align: right;
}

.banner-image {
  width: 100px;
  height: 60px;
  border-radius: 4px;
}

.image-preview {
  margin-top: 12px;
}

.image-preview .el-image {
  width: 100%;
  max-width: 500px;
  height: 200px;
  border-radius: 8px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
  line-height: 1.6;
}
</style>
