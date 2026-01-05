import request from '@/utils/request'

/**
 * 获取商品列表（分页）
 * @param {Object} params - 查询参数 { page, size, keyword, categoryId, status, sortType }
 */
export function getProductList(params) {
  return request({
    url: '/admin/product/list',
    method: 'get',
    params
  })
}

/**
 * 获取商品详情
 * @param {Number} id - 商品ID
 */
export function getProductDetail(id) {
  return request({
    url: `/admin/product/detail/${id}`,
    method: 'get'
  })
}

/**
 * 添加商品
 * @param {Object} data - 商品信息
 */
export function addProduct(data) {
  return request({
    url: '/admin/product/add',
    method: 'post',
    data
  })
}

/**
 * 更新商品
 * @param {Number} id - 商品ID
 * @param {Object} data - 商品信息
 */
export function updateProduct(id, data) {
  return request({
    url: `/admin/product/update/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除商品
 * @param {Number} id - 商品ID
 */
export function deleteProduct(id) {
  return request({
    url: `/admin/product/delete/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除商品
 * @param {Array} ids - 商品ID数组
 */
export function batchDeleteProducts(ids) {
  return request({
    url: '/admin/product/batch-delete',
    method: 'delete',
    data: ids
  })
}

/**
 * 更新商品状态（上下架）
 * @param {Number} id - 商品ID
 * @param {Boolean} status - 状态
 */
export function updateProductStatus(id, status) {
  return request({
    url: `/admin/product/status/${id}`,
    method: 'put',
    params: { status }
  })
}

/**
 * 批量更新商品状态
 * @param {Array} ids - 商品ID数组
 * @param {Boolean} status - 状态
 */
export function batchUpdateProductStatus(ids, status) {
  return request({
    url: '/admin/product/batch-status',
    method: 'put',
    data: ids,
    params: { status }
  })
}

/**
 * 获取热门商品
 */
export function getHotProducts() {
  return request({
    url: '/product/hot',
    method: 'get'
  })
}

/**
 * 获取推荐商品
 */
export function getRecommendedProducts() {
  return request({
    url: '/product/recommended',
    method: 'get'
  })
}
