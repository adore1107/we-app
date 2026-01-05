<template>
  <div class="product-form-container">
    <el-page-header
      @back="goBack"
      :content="isEdit ? '编辑商品' : '添加商品'"
      class="page-header"
    />

    <el-card v-loading="loading">
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <!-- 基本信息 -->
        <el-divider content-position="left">基本信息</el-divider>

        <el-form-item label="商品名称" prop="name">
          <el-input
            v-model="formData.name"
            placeholder="请输入商品名称"
            clearable
            style="width: 100%;"
          />
        </el-form-item>

        <el-form-item label="一级分类" prop="mainCategoryId">
          <el-select
            v-model="formData.mainCategoryId"
            placeholder="请选择一级分类"
            @change="handleMainCategoryChange"
            size="large"
            style="width: 100% !important;"
          >
            <el-option
              v-for="category in mainCategoryList"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="二级分类" prop="subCategoryId">
          <el-select
            v-model="formData.subCategoryId"
            placeholder="请选择二级分类（可选）"
            clearable
            :disabled="!formData.mainCategoryId"
            size="large"
            style="width: 100% !important;"
          >
            <el-option
              v-for="category in subCategoryList"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="4"
            placeholder="请输入商品描述"
          />
        </el-form-item>

        <!-- 商品图片 -->
        <el-divider content-position="left">商品图片</el-divider>

        <el-form-item label="主图" prop="mainImage">
          <el-input
            v-model="formData.mainImage"
            placeholder="请输入主图URL"
            clearable
          >
            <template #append>
              <el-button icon="Link">粘贴链接</el-button>
            </template>
          </el-input>
          <div v-if="formData.mainImage" class="image-preview">
            <el-image :src="formData.mainImage" fit="cover" />
          </div>
        </el-form-item>

        <el-form-item label="详情图" prop="images">
          <el-input
            v-model="imageInput"
            type="textarea"
            :rows="3"
            placeholder="请输入图片URL，每行一个"
            @blur="handleImageInput"
          />
          <div v-if="additionalImages.length > 0" class="image-gallery">
            <div
              v-for="(img, index) in additionalImages"
              :key="index"
              class="gallery-item"
            >
              <el-image :src="img" fit="cover" />
              <el-button
                type="danger"
                size="small"
                icon="Delete"
                circle
                class="delete-btn"
                @click="removeImage(index)"
              />
            </div>
          </div>
        </el-form-item>

        <!-- 规格参数 -->
        <el-divider content-position="left">规格参数</el-divider>

        <el-form-item label="产品型号">
          <el-input v-model="formData.specsData.model" placeholder="如：MF-TECH-2024-MC1" style="width: 100%;" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="门幅宽度">
              <el-input v-model="formData.specsData.width" placeholder="如：180cm" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="克重">
              <el-input v-model="formData.specsData.weight" placeholder="如：380g/m²" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="面料成分">
          <el-input v-model="formData.specsData.fabric_composition" placeholder="如：抗菌纤维40% 阻燃纤维35% 功能纤维25%" style="width: 100%;" />
        </el-form-item>

        <el-form-item label="主要功能">
          <el-input v-model="formData.specsData.function" type="textarea" :rows="2" placeholder="如：抗菌防螨 阻燃防水 耐磨耐洗 高强耐久" />
        </el-form-item>

        <el-form-item label="质量认证">
          <el-input v-model="formData.specsData.certification" placeholder="如：ISO9001 CE认证" style="width: 100%;" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="抗菌性能">
              <el-input v-model="formData.specsData.antibacterial_rate" placeholder="如：抗菌率>99% 大肠杆菌" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="防螨性能">
              <el-input v-model="formData.specsData.mite_resistance" placeholder="如：防螨率>92% 尘螨" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="阻燃性能">
          <el-input v-model="formData.specsData.flame_retardant" placeholder="如：B1级阻燃 阻燃时间≤2秒" style="width: 100%;" />
        </el-form-item>

        <el-form-item label="织造工艺">
          <el-input v-model="formData.specsData.weaving_process" type="textarea" :rows="2" placeholder="如：60支多轴织造 经密320根/10cm 纬密280根/10cm" />
        </el-form-item>

        <el-form-item label="后整理工艺">
          <el-input v-model="formData.specsData.finishing_process" type="textarea" :rows="2" placeholder="如：多功能整理 复合涂层处理 环保定型" />
        </el-form-item>

        <!-- B2B信息 -->
        <el-divider content-position="left">B2B信息</el-divider>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="最小起订量" prop="minOrderQuantity">
              <el-input-number
                v-model="formData.minOrderQuantity"
                :min="1"
                :max="99999"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单位" prop="unit">
              <el-input v-model="formData.unit" placeholder="如：件、米、套" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="交货周期" prop="leadTime">
          <el-input-number
            v-model="formData.leadTime"
            :min="1"
            :max="365"
          />
          <span style="margin-left: 10px;">天</span>
        </el-form-item>

        <!-- 排序和标签 -->
        <el-divider content-position="left">排序和标签</el-divider>

        <el-form-item label="排序权重" prop="sortOrder">
          <el-input-number
            v-model="formData.sortOrder"
            :min="0"
            :max="9999"
          />
          <span style="margin-left: 10px;">数值越大排序越靠前</span>
        </el-form-item>

        <el-form-item label="商品标签">
          <el-checkbox v-model="formData.isHot">热门</el-checkbox>
          <el-checkbox v-model="formData.isNew">最新</el-checkbox>
          <el-checkbox v-model="formData.isRecommended">推荐</el-checkbox>
        </el-form-item>

        <el-form-item label="上架状态">
          <el-switch
            v-model="formData.status"
            active-text="上架"
            inactive-text="下架"
          />
        </el-form-item>

        <!-- 提交按钮 -->
        <el-form-item>
          <el-button type="primary" @click="handleSubmit">保存</el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProductDetail, addProduct, updateProduct } from '@/api/product'
import { getCategoryList } from '@/api/category'

const route = useRoute()
const router = useRouter()

const formRef = ref(null)
const loading = ref(false)
const categoryList = ref([])
const imageInput = ref('')

// 是否是编辑模式
const isEdit = computed(() => !!route.params.id)
const productId = computed(() => parseInt(route.params.id))

// 一级分类列表
const mainCategoryList = computed(() => {
  return categoryList.value.filter(cat => cat.level === 1)
})

// 二级分类列表（根据选中的一级分类过滤）
const subCategoryList = computed(() => {
  if (!formData.mainCategoryId) return []
  return categoryList.value.filter(
    cat => cat.level === 2 && cat.parentId === formData.mainCategoryId
  )
})

// 额外图片列表
const additionalImages = computed(() => {
  try {
    return formData.images ? JSON.parse(formData.images) : []
  } catch (e) {
    return []
  }
})

// 表单数据
const formData = reactive({
  name: '',
  mainCategoryId: null,
  subCategoryId: null,
  mainImage: '',
  images: '[]',
  description: '',
  specsData: {
    model: '',
    width: '',
    weight: '',
    fabric_composition: '',
    function: '',
    certification: '',
    antibacterial_rate: '',
    mite_resistance: '',
    flame_retardant: '',
    weaving_process: '',
    finishing_process: ''
  },
  specifications: '{}',
  minOrderQuantity: 1,
  unit: '件',
  leadTime: 7,
  sortOrder: 0,
  isHot: false,
  isNew: false,
  isRecommended: false,
  status: true
})

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入商品名称', trigger: 'blur' }
  ],
  mainCategoryId: [
    { required: true, message: '请选择一级分类', trigger: 'change' }
  ],
  mainImage: [
    { required: true, message: '请输入主图URL', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入商品描述', trigger: 'blur' }
  ],
  minOrderQuantity: [
    { required: true, message: '请输入最小起订量', trigger: 'blur' }
  ],
  unit: [
    { required: true, message: '请输入单位', trigger: 'blur' }
  ],
  leadTime: [
    { required: true, message: '请输入交货周期', trigger: 'blur' }
  ]
}

// 获取分类列表
const fetchCategoryList = async () => {
  try {
    const response = await getCategoryList()
    if (response.code === 200) {
      categoryList.value = response.data
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

// 获取商品详情（编辑模式）
const fetchProductDetail = async () => {
  if (!isEdit.value) return

  loading.value = true
  try {
    const response = await getProductDetail(productId.value)
    if (response.code === 200) {
      const product = response.data

      // 填充表单数据
      Object.assign(formData, {
        name: product.name,
        mainCategoryId: product.mainCategoryId,
        subCategoryId: product.subCategoryId,
        mainImage: product.mainImage,
        images: product.images || '[]',
        description: product.description,
        minOrderQuantity: product.minOrderQuantity,
        unit: product.unit,
        leadTime: product.leadTime,
        sortOrder: product.sortOrder,
        isHot: product.isHot,
        isNew: product.isNew,
        isRecommended: product.isRecommended,
        status: product.status
      })

      // 解析规格参数
      try {
        const specs = JSON.parse(product.specifications || '{}')
        formData.specsData = {
          model: specs.model || '',
          width: specs.width || '',
          weight: specs.weight || '',
          fabric_composition: specs.fabric_composition || '',
          function: specs.function || '',
          certification: specs.certification || '',
          antibacterial_rate: specs.antibacterial_rate || '',
          mite_resistance: specs.mite_resistance || '',
          flame_retardant: specs.flame_retardant || '',
          weaving_process: specs.weaving_process || '',
          finishing_process: specs.finishing_process || ''
        }
      } catch (e) {
        console.error('解析规格参数失败:', e)
      }

      // 设置图片输入框
      try {
        const imgs = JSON.parse(product.images || '[]')
        imageInput.value = imgs.join('\n')
      } catch (e) {
        imageInput.value = ''
      }
    } else {
      ElMessage.error(response.message || '获取商品详情失败')
      goBack()
    }
  } catch (error) {
    console.error('获取商品详情失败:', error)
    ElMessage.error('获取商品详情失败')
    goBack()
  } finally {
    loading.value = false
  }
}

// 一级分类改变时清空二级分类
const handleMainCategoryChange = () => {
  formData.subCategoryId = null
}

// 处理图片输入
const handleImageInput = () => {
  const urls = imageInput.value
    .split('\n')
    .map(url => url.trim())
    .filter(url => url.length > 0)
  formData.images = JSON.stringify(urls)
}

// 删除图片
const removeImage = (index) => {
  const images = [...additionalImages.value]
  images.splice(index, 1)
  formData.images = JSON.stringify(images)
  imageInput.value = images.join('\n')
}

// 提交表单
const handleSubmit = async () => {
  // 验证表单
  const valid = await formRef.value.validate()
  if (!valid) return

  // 转换规格参数为JSON（只保留有值的字段）
  const specs = {}
  Object.keys(formData.specsData).forEach(key => {
    if (formData.specsData[key] && formData.specsData[key].trim()) {
      specs[key] = formData.specsData[key].trim()
    }
  })
  formData.specifications = JSON.stringify(specs)

  loading.value = true
  try {
    const data = {
      name: formData.name,
      mainCategoryId: formData.mainCategoryId,
      subCategoryId: formData.subCategoryId || null,
      mainImage: formData.mainImage,
      images: formData.images,
      description: formData.description,
      features: '[]',
      specifications: formData.specifications,
      minOrderQuantity: formData.minOrderQuantity,
      unit: formData.unit,
      leadTime: formData.leadTime,
      sortOrder: formData.sortOrder,
      isHot: formData.isHot,
      isNew: formData.isNew,
      isRecommended: formData.isRecommended,
      status: formData.status
    }

    let response
    if (isEdit.value) {
      response = await updateProduct(productId.value, data)
    } else {
      response = await addProduct(data)
    }

    if (response.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '添加成功')
      goBack()
    } else {
      ElMessage.error(response.message || (isEdit.value ? '更新失败' : '添加失败'))
    }
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error(isEdit.value ? '更新失败' : '添加失败')
  } finally {
    loading.value = false
  }
}

// 返回列表
const goBack = () => {
  router.push('/product/list')
}

// 组件挂载时获取数据
onMounted(() => {
  fetchCategoryList()
  if (isEdit.value) {
    fetchProductDetail()
  }
})
</script>

<style scoped>
.product-form-container {
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 32px 40px;
}

.page-header {
  margin-bottom: 24px;
}

.el-card :deep(.el-card__body) {
  padding: 32px;
}

.image-preview {
  margin-top: 12px;
}

.image-preview .el-image {
  width: 200px;
  height: 200px;
  border-radius: 8px;
}

.image-gallery {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 10px;
}

.gallery-item {
  position: relative;
  width: 120px;
  height: 120px;
}

.gallery-item .el-image {
  width: 100%;
  height: 100%;
  border-radius: 8px;
}

.gallery-item .delete-btn {
  position: absolute;
  top: -8px;
  right: -8px;
}


/* 分类下拉框样式优化 */
.el-form-item :deep(.el-select) {
  width: 100% !important;
  max-width: 100% !important;
  font-size: 16px;
}

.el-form-item :deep(.el-select .el-input__wrapper) {
  min-height: 56px !important;
  padding: 14px 20px !important;
  height: auto !important;
  width: 100% !important;
}

/* 确保表单项内容区域占满剩余空间 */
.el-form-item :deep(.el-form-item__content) {
  flex: 1 !important;
  width: 100% !important;
  max-width: 100% !important;
}

/* 确保所有输入框和选择框都充满宽度 */
.el-form-item :deep(.el-input),
.el-form-item :deep(.el-select),
.el-form-item :deep(.el-switch),
.el-form-item :deep(.el-input-number) {
  width: 100% !important;
  max-width: 100% !important;
}

.el-form-item :deep(.el-input__wrapper) {
  width: 100% !important;
}

.el-form-item :deep(.el-select .el-input) {
  font-size: 16px !important;
  height: auto !important;
}

.el-form-item :deep(.el-select .el-input__inner) {
  font-size: 16px !important;
  line-height: 28px !important;
  height: 28px !important;
}

.el-form-item :deep(.el-select .el-select__wrapper) {
  min-height: 56px !important;
  padding: 14px 20px !important;
}

.el-form-item :deep(.el-select .el-select__selection) {
  font-size: 16px !important;
  line-height: 28px !important;
  height: auto !important;
  display: flex !important;
  align-items: center !important;
}

.el-form-item :deep(.el-select .el-select__selected-item) {
  font-size: 16px !important;
  line-height: 28px !important;
}

.el-form-item :deep(.el-input__wrapper) {
  min-height: 48px;
  padding: 12px 16px;
}

.el-form-item :deep(.el-input__inner) {
  font-size: 16px;
  line-height: 1.6;
}
</style>

<style>
/* 下拉菜单选项样式（全局） */
.el-select-dropdown__item {
  font-size: 15px !important;
  padding: 12px 16px !important;
  line-height: 1.6 !important;
  min-height: 44px !important;
}

.el-select-dropdown__item.selected {
  font-weight: 600 !important;
  color: #4F46E5 !important;
}
</style>
