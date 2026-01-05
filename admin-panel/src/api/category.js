import request from '@/utils/request'

/**
 * 获取分类列表（管理员）
 */
export function getCategoryList() {
  return request({
    url: '/admin/category/list',
    method: 'get'
  })
}

/**
 * 获取分类详情
 * @param {Number} id - 分类ID
 */
export function getCategoryDetail(id) {
  return request({
    url: `/admin/category/detail/${id}`,
    method: 'get'
  })
}

/**
 * 添加分类
 * @param {Object} data - 分类信息
 */
export function addCategory(data) {
  return request({
    url: '/admin/category/add',
    method: 'post',
    data
  })
}

/**
 * 更新分类
 * @param {Number} id - 分类ID
 * @param {Object} data - 分类信息
 */
export function updateCategory(id, data) {
  return request({
    url: `/admin/category/update/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除分类
 * @param {Number} id - 分类ID
 */
export function deleteCategory(id) {
  return request({
    url: `/admin/category/delete/${id}`,
    method: 'delete'
  })
}

/**
 * 更新分类状态
 * @param {Number} id - 分类ID
 * @param {Boolean} status - 状态
 */
export function updateCategoryStatus(id, status) {
  return request({
    url: `/admin/category/status/${id}`,
    method: 'put',
    params: { status }
  })
}

/**
 * 更新分类排序
 * @param {Number} id - 分类ID
 * @param {Number} sortOrder - 排序值
 */
export function updateCategorySortOrder(id, sortOrder) {
  return request({
    url: `/admin/category/sort-order/${id}`,
    method: 'put',
    params: { sortOrder }
  })
}
