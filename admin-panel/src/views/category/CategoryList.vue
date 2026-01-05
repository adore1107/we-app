<template>
  <div class="category-list-container">
    <!-- 操作按钮区域 -->
    <el-card class="toolbar-card">
      <el-button type="primary" icon="Plus" @click="handleAdd">
        添加一级分类
      </el-button>
    </el-card>

    <!-- 分类列表表格 -->
    <el-card>
      <el-table
        v-loading="loading"
        :data="categoryTree"
        row-key="id"
        border
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        default-expand-all
      >
        <el-table-column prop="id" label="ID" width="80" :resizable="false" />

        <el-table-column label="分类名称" min-width="200" :resizable="false">
          <template #default="{ row }">
            <span :style="{ fontWeight: row.level === 1 ? '600' : '400' }">
              {{ row.name }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="层级" width="100" :resizable="false">
          <template #default="{ row }">
            <el-tag :type="row.level === 1 ? 'primary' : 'success'" size="small">
              {{ row.level === 1 ? '一级分类' : '二级分类' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="图标" width="100" :resizable="false">
          <template #default="{ row }">
            <el-image
              v-if="row.icon"
              :src="row.icon"
              :preview-src-list="[row.icon]"
              fit="cover"
              class="category-icon"
            />
            <span v-else class="text-secondary">无</span>
          </template>
        </el-table-column>

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

        <el-table-column label="操作" width="260" fixed="right" :resizable="false">
          <template #default="{ row }">
            <el-button
              v-if="row.level === 1"
              type="success"
              size="small"
              icon="Plus"
              @click="handleAddSub(row)"
            >
              添加子分类
            </el-button>
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

    <!-- 添加/编辑分类对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="分类名称" prop="name">
          <el-input
            v-model="formData.name"
            placeholder="请输入分类名称"
            clearable
          />
        </el-form-item>

        <el-form-item label="分类图标" prop="icon">
          <el-input
            v-model="formData.icon"
            placeholder="请输入图标URL"
            clearable
          />
          <div v-if="formData.icon" class="icon-preview">
            <el-image :src="formData.icon" fit="cover" />
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
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getCategoryList,
  addCategory,
  updateCategory,
  deleteCategory,
  updateCategoryStatus,
  updateCategorySortOrder
} from '@/api/category'

// 数据
const loading = ref(false)
const categoryList = ref([])
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)

// 表单数据
const formData = reactive({
  id: null,
  name: '',
  icon: '',
  parentId: null,
  level: 1,
  sortOrder: 0,
  status: true
})

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
  ]
}

// 对话框标题
const dialogTitle = computed(() => {
  if (formData.id) {
    return '编辑分类'
  }
  return formData.parentId ? '添加二级分类' : '添加一级分类'
})

// 构建树形结构
const categoryTree = computed(() => {
  // 找出所有一级分类
  const mainCategories = categoryList.value.filter(cat => cat.level === 1)

  // 为每个一级分类添加子分类
  return mainCategories.map(main => ({
    ...main,
    children: categoryList.value.filter(cat => cat.parentId === main.id)
  }))
})

// 获取分类列表
const fetchCategoryList = async () => {
  loading.value = true
  try {
    const response = await getCategoryList()
    if (response.code === 200) {
      categoryList.value = response.data || []
    } else {
      ElMessage.error(response.message || '获取分类列表失败')
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
    ElMessage.error('获取分类列表失败')
  } finally {
    loading.value = false
  }
}

// 添加一级分类
const handleAdd = () => {
  resetForm()
  formData.parentId = null
  formData.level = 1
  dialogVisible.value = true
}

// 添加子分类
const handleAddSub = (row) => {
  resetForm()
  formData.parentId = row.id
  formData.level = 2
  dialogVisible.value = true
}

// 编辑分类
const handleEdit = (row) => {
  formData.id = row.id
  formData.name = row.name
  formData.icon = row.icon || ''
  formData.parentId = row.parentId
  formData.level = row.level
  formData.sortOrder = row.sortOrder
  formData.status = row.status
  dialogVisible.value = true
}

// 删除分类
const handleDelete = (row) => {
  // 如果是一级分类，检查是否有子分类
  if (row.level === 1) {
    const hasChildren = categoryList.value.some(cat => cat.parentId === row.id)
    if (hasChildren) {
      ElMessage.warning('该分类下还有子分类，无法删除')
      return
    }
  }

  ElMessageBox.confirm(`确定要删除分类"${row.name}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await deleteCategory(row.id)
      if (response.code === 200) {
        ElMessage.success('删除成功')
        fetchCategoryList()
      } else {
        ElMessage.error(response.message || '删除失败')
      }
    } catch (error) {
      console.error('删除分类失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {
    // 取消操作
  })
}

// 状态改变
const handleStatusChange = async (row) => {
  try {
    const response = await updateCategoryStatus(row.id, row.status)
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
    const response = await updateCategorySortOrder(row.id, row.sortOrder)
    if (response.code === 200) {
      ElMessage.success('排序更新成功')
      fetchCategoryList()
    } else {
      ElMessage.error(response.message || '排序更新失败')
    }
  } catch (error) {
    console.error('更新排序失败:', error)
    ElMessage.error('排序更新失败')
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      const data = {
        name: formData.name,
        icon: formData.icon || null,
        parentId: formData.parentId,
        level: formData.level,
        sortOrder: formData.sortOrder,
        status: formData.status
      }

      let response
      if (formData.id) {
        // 编辑
        response = await updateCategory(formData.id, data)
      } else {
        // 添加
        response = await addCategory(data)
      }

      if (response.code === 200) {
        ElMessage.success(formData.id ? '更新成功' : '添加成功')
        dialogVisible.value = false
        fetchCategoryList()
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
  formData.name = ''
  formData.icon = ''
  formData.parentId = null
  formData.level = 1
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

// 组件挂载时获取数据
onMounted(() => {
  fetchCategoryList()
})
</script>

<style scoped>
.category-list-container {
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

.category-icon {
  width: 50px;
  height: 50px;
  border-radius: 4px;
}

.icon-preview {
  margin-top: 12px;
}

.icon-preview .el-image {
  width: 100px;
  height: 100px;
  border-radius: 8px;
}

.text-secondary {
  color: #909399;
  font-size: 14px;
}
</style>
