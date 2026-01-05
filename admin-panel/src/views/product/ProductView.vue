<template>
  <div class="product-view-container">
    <el-page-header @back="goBack" content="商品详情" class="page-header" />

    <el-card v-loading="loading">
      <template v-if="product">
        <!-- 基本信息 -->
        <el-descriptions title="基本信息" :column="2" border>
          <el-descriptions-item label="商品ID">
            {{ product.id }}
          </el-descriptions-item>
          <el-descriptions-item label="商品名称">
            {{ product.name }}
          </el-descriptions-item>
          <el-descriptions-item label="一级分类">
            {{ getCategoryName(product.mainCategoryId) }}
          </el-descriptions-item>
          <el-descriptions-item label="二级分类">
            {{ product.subCategoryId ? getSubCategoryName(product.subCategoryId) : '无' }}
          </el-descriptions-item>
          <el-descriptions-item label="最小起订量">
            {{ product.minOrderQuantity }} {{ product.unit }}
          </el-descriptions-item>
          <el-descriptions-item label="交货周期">
            {{ product.leadTime }} 天
          </el-descriptions-item>
          <el-descriptions-item label="排序权重">
            {{ product.sortOrder }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="product.status ? 'success' : 'danger'">
              {{ product.status ? '已上架' : '已下架' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 商品图片 -->
        <el-divider />
        <h3>商品图片</h3>
        <div class="image-gallery">
          <el-image
            v-for="(img, index) in productImages"
            :key="index"
            :src="img"
            :preview-src-list="productImages"
            :initial-index="index"
            fit="cover"
            class="gallery-image"
          />
        </div>

        <!-- 商品描述 -->
        <el-divider />
        <el-descriptions title="商品描述" :column="1" border>
          <el-descriptions-item label="商品描述">
            {{ product.description || '暂无描述' }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 规格参数 -->
        <el-divider />
        <h3>规格参数</h3>
        <el-table :data="productSpecifications" border stripe>
          <el-table-column prop="key" label="参数名" width="200" />
          <el-table-column prop="value" label="参数值" />
        </el-table>

        <!-- 标签和统计 -->
        <el-divider />
        <el-descriptions title="标签和统计" :column="2" border>
          <el-descriptions-item label="标签">
            <el-tag v-if="product.isHot" type="danger" style="margin-right: 5px;">热门</el-tag>
            <el-tag v-if="product.isNew" type="success" style="margin-right: 5px;">最新</el-tag>
            <el-tag v-if="product.isRecommended" type="warning">推荐</el-tag>
            <span v-if="!product.isHot && !product.isNew && !product.isRecommended">无</span>
          </el-descriptions-item>
          <el-descriptions-item label="浏览量">
            {{ product.viewCount || 0 }} 次
          </el-descriptions-item>
          <el-descriptions-item label="收藏量">
            {{ product.favoriteCount || 0 }} 次
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ product.createdAt }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间" :span="2">
            {{ product.updatedAt }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 操作按钮 -->
        <el-divider />
        <div class="action-buttons">
          <el-button type="primary" icon="Edit" @click="handleEdit">编辑商品</el-button>
          <el-button icon="Back" @click="goBack">返回列表</el-button>
        </div>
      </template>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProductDetail } from '@/api/product'
import { getCategoryList } from '@/api/category'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const product = ref(null)
const categoryList = ref([])

// 商品ID
const productId = computed(() => parseInt(route.params.id))

// 商品图片列表
const productImages = computed(() => {
  if (!product.value) return []
  const images = [product.value.mainImage]
  if (product.value.images) {
    try {
      const additionalImages = JSON.parse(product.value.images)
      images.push(...additionalImages)
    } catch (e) {
      console.error('解析图片JSON失败:', e)
    }
  }
  return images
})

// 规格参数中文映射
const specificationLabels = {
  'model': '产品型号',
  'width': '门幅宽度',
  'weight': '克重',
  'function': '主要功能',
  'certification': '质量认证',
  'flame_retardant': '阻燃性能',
  'mite_resistance': '防螨性能',
  'weaving_process': '织造工艺',
  'finishing_process': '后整理工艺',
  'antibacterial_rate': '抗菌性能',
  'fabric_composition': '面料成分'
}

// 规格参数列表
const productSpecifications = computed(() => {
  if (!product.value || !product.value.specifications) return []
  try {
    const specs = JSON.parse(product.value.specifications)
    return Object.entries(specs).map(([key, value]) => ({
      key: specificationLabels[key] || key,
      value
    }))
  } catch (e) {
    console.error('解析规格参数JSON失败:', e)
    return []
  }
})

// 获取分类名称
const getCategoryName = (categoryId) => {
  const category = categoryList.value.find(cat => cat.id === categoryId && cat.level === 1)
  return category ? category.name : '未知'
}

// 获取二级分类名称
const getSubCategoryName = (categoryId) => {
  const category = categoryList.value.find(cat => cat.id === categoryId && cat.level === 2)
  return category ? category.name : '未知'
}

// 获取商品详情
const fetchProductDetail = async () => {
  loading.value = true
  try {
    const response = await getProductDetail(productId.value)
    if (response.code === 200) {
      product.value = response.data
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

// 编辑商品
const handleEdit = () => {
  router.push(`/product/edit/${productId.value}`)
}

// 返回列表
const goBack = () => {
  router.push('/product/list')
}

// 组件挂载时获取数据
onMounted(() => {
  fetchCategoryList()
  fetchProductDetail()
})
</script>

<style scoped>
.product-view-container {
  width: 100%;
  max-width: 100%;
  padding: 32px 40px;
}

.page-header {
  margin-bottom: 24px;
}

.el-card :deep(.el-card__body) {
  padding: 32px;
}

.image-gallery {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin: 24px 0;
}

.gallery-image {
  width: 150px;
  height: 150px;
  border-radius: 8px;
  cursor: pointer;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

h3 {
  margin: 28px 0 16px 0;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
}

.el-divider {
  margin: 28px 0;
}
</style>
