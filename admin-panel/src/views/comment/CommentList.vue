<template>
  <div class="comment-list-container">
    <!-- 搜索和筛选区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form" label-width="80px">
        <el-form-item label="商品ID">
          <el-input
            v-model="searchForm.productId"
            placeholder="请输入商品ID"
            clearable
            @clear="handleSearch"
            style="width: 200px;"
          />
        </el-form-item>

        <el-form-item label="用户ID">
          <el-input
            v-model="searchForm.userId"
            placeholder="请输入用户ID"
            clearable
            @clear="handleSearch"
            style="width: 200px;"
          />
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
            <el-option label="显示" :value="true" />
            <el-option label="隐藏" :value="false" />
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
            已选择 {{ selectedIds.length }} 条评论
          </el-tag>
        </el-col>
      </el-row>
    </el-card>

    <!-- 评论列表表格 -->
    <el-card>
      <el-table
        v-loading="loading"
        :data="commentList"
        stripe
        border
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" :resizable="false" />

        <el-table-column prop="productId" label="商品ID" width="100" :resizable="false" />
        <el-table-column prop="userId" label="用户ID" width="100" :resizable="false" />

        <el-table-column label="评论内容" min-width="250" show-overflow-tooltip :resizable="false">
          <template #default="{ row }">
            {{ row.content }}
          </template>
        </el-table-column>

        <el-table-column label="评分" width="120" :resizable="false">
          <template #default="{ row }">
            <el-rate v-model="row.rating" disabled show-score text-color="#ff9900" />
          </template>
        </el-table-column>

        <el-table-column label="商家回复" min-width="200" show-overflow-tooltip :resizable="false">
          <template #default="{ row }">
            {{ row.adminReply || '未回复' }}
          </template>
        </el-table-column>

        <el-table-column label="回复时间" width="180" :resizable="false">
          <template #default="{ row }">
            {{ row.replyTime || '-' }}
          </template>
        </el-table-column>

        <el-table-column label="状态" width="150" :resizable="false">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              active-text="显示"
              inactive-text="隐藏"
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
              icon="ChatDotRound"
              @click="handleReply(row)"
            >
              回复
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

    <!-- 回复评论对话框 -->
    <el-dialog
      v-model="replyDialogVisible"
      title="回复评论"
      width="600px"
      @close="handleReplyDialogClose"
    >
      <div v-if="currentComment" class="comment-detail">
        <div class="detail-item">
          <span class="label">评论内容：</span>
          <span class="content">{{ currentComment.content }}</span>
        </div>
        <div class="detail-item">
          <span class="label">评分：</span>
          <el-rate v-model="currentComment.rating" disabled show-score text-color="#ff9900" />
        </div>
        <div class="detail-item">
          <span class="label">评论时间：</span>
          <span class="content">{{ currentComment.createdAt }}</span>
        </div>
      </div>

      <el-divider />

      <el-form ref="replyFormRef" :model="replyForm" :rules="replyRules" label-width="100px">
        <el-form-item label="商家回复" prop="adminReply">
          <el-input
            v-model="replyForm.adminReply"
            type="textarea"
            :rows="4"
            placeholder="请输入回复内容"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitReply" :loading="submitting">
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
  getCommentList,
  replyComment,
  deleteComment,
  batchDeleteComments,
  updateCommentStatus
} from '@/api/comment'

// 搜索表单
const searchForm = reactive({
  productId: '',
  userId: '',
  status: null
})

// 分页参数
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 数据
const loading = ref(false)
const commentList = ref([])
const selectedIds = ref([])
const replyDialogVisible = ref(false)
const currentComment = ref(null)
const submitting = ref(false)
const replyFormRef = ref(null)

// 回复表单
const replyForm = reactive({
  adminReply: ''
})

// 回复表单验证规则
const replyRules = {
  adminReply: [
    { required: true, message: '请输入回复内容', trigger: 'blur' },
    { min: 1, max: 500, message: '长度在 1 到 500 个字符', trigger: 'blur' }
  ]
}

// 获取评论列表
const fetchCommentList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1, // 后端从0开始
      size: pagination.size,
      productId: searchForm.productId || undefined,
      userId: searchForm.userId || undefined,
      status: searchForm.status !== null ? searchForm.status : undefined
    }

    const response = await getCommentList(params)
    if (response.code === 200) {
      commentList.value = response.data.records || []
      pagination.total = response.data.total || 0
    } else {
      ElMessage.error(response.message || '获取评论列表失败')
    }
  } catch (error) {
    console.error('获取评论列表失败:', error)
    ElMessage.error('获取评论列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchCommentList()
}

// 重置搜索
const handleReset = () => {
  searchForm.productId = ''
  searchForm.userId = ''
  searchForm.status = null
  handleSearch()
}

// 分页大小改变
const handleSizeChange = () => {
  pagination.page = 1
  fetchCommentList()
}

// 页码改变
const handlePageChange = () => {
  fetchCommentList()
}

// 选择改变
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

// 回复评论
const handleReply = (row) => {
  currentComment.value = row
  replyForm.adminReply = row.adminReply || ''
  replyDialogVisible.value = true
}

// 提交回复
const handleSubmitReply = async () => {
  if (!replyFormRef.value) return

  await replyFormRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      const response = await replyComment(currentComment.value.id, replyForm.adminReply)
      if (response.code === 200) {
        ElMessage.success('回复成功')
        replyDialogVisible.value = false
        fetchCommentList()
      } else {
        ElMessage.error(response.message || '回复失败')
      }
    } catch (error) {
      console.error('回复评论失败:', error)
      ElMessage.error('回复失败')
    } finally {
      submitting.value = false
    }
  })
}

// 关闭回复对话框
const handleReplyDialogClose = () => {
  replyForm.adminReply = ''
  currentComment.value = null
  if (replyFormRef.value) {
    replyFormRef.value.clearValidate()
  }
}

// 删除评论
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await deleteComment(row.id)
      if (response.code === 200) {
        ElMessage.success('删除成功')
        fetchCommentList()
      } else {
        ElMessage.error(response.message || '删除失败')
      }
    } catch (error) {
      console.error('删除评论失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {
    // 取消操作
  })
}

// 批量删除
const handleBatchDelete = () => {
  ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 条评论吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await batchDeleteComments(selectedIds.value)
      if (response.code === 200) {
        ElMessage.success('批量删除成功')
        selectedIds.value = []
        fetchCommentList()
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
    const response = await updateCommentStatus(row.id, row.status)
    if (response.code === 200) {
      ElMessage.success(row.status ? '显示成功' : '隐藏成功')
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

// 组件挂载时获取数据
onMounted(() => {
  fetchCommentList()
})
</script>

<style scoped>
.comment-list-container {
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

/* 评论详情 */
.comment-detail {
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.detail-item {
  margin-bottom: 12px;
  line-height: 1.8;
}

.detail-item:last-child {
  margin-bottom: 0;
}

.detail-item .label {
  font-weight: 600;
  color: #606266;
  margin-right: 8px;
}

.detail-item .content {
  color: #303133;
}
</style>
