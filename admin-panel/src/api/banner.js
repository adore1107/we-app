import request from '@/utils/request'

/**
 * 获取轮播图列表（管理员）
 * @param {String} type - 轮播图类型（可选）
 */
export function getBannerList(type) {
  return request({
    url: '/admin/banner/list',
    method: 'get',
    params: { type }
  })
}

/**
 * 获取轮播图详情
 * @param {Number} id - 轮播图ID
 */
export function getBannerDetail(id) {
  return request({
    url: `/admin/banner/detail/${id}`,
    method: 'get'
  })
}

/**
 * 添加轮播图
 * @param {Object} data - 轮播图信息
 */
export function addBanner(data) {
  return request({
    url: '/admin/banner/add',
    method: 'post',
    data
  })
}

/**
 * 更新轮播图
 * @param {Number} id - 轮播图ID
 * @param {Object} data - 轮播图信息
 */
export function updateBanner(id, data) {
  return request({
    url: `/admin/banner/update/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除轮播图
 * @param {Number} id - 轮播图ID
 */
export function deleteBanner(id) {
  return request({
    url: `/admin/banner/delete/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除轮播图
 * @param {Array} ids - 轮播图ID数组
 */
export function batchDeleteBanners(ids) {
  return request({
    url: '/admin/banner/batch-delete',
    method: 'delete',
    data: ids
  })
}

/**
 * 更新轮播图状态
 * @param {Number} id - 轮播图ID
 * @param {Boolean} status - 状态
 */
export function updateBannerStatus(id, status) {
  return request({
    url: `/admin/banner/status/${id}`,
    method: 'put',
    params: { status }
  })
}

/**
 * 更新轮播图排序
 * @param {Number} id - 轮播图ID
 * @param {Number} sortOrder - 排序值
 */
export function updateBannerSortOrder(id, sortOrder) {
  return request({
    url: `/admin/banner/sort-order/${id}`,
    method: 'put',
    params: { sortOrder }
  })
}
