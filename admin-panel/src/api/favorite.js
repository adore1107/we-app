import request from '@/utils/request'

/**
 * 获取收藏列表（管理员）
 * @param {Object} params - 查询参数
 */
export function getFavoriteList(params) {
  return request({
    url: '/admin/favorite/list',
    method: 'get',
    params
  })
}

/**
 * 获取收藏详情
 * @param {Number} id - 收藏ID
 */
export function getFavoriteDetail(id) {
  return request({
    url: `/admin/favorite/detail/${id}`,
    method: 'get'
  })
}

/**
 * 删除收藏
 * @param {Number} id - 收藏ID
 */
export function deleteFavorite(id) {
  return request({
    url: `/admin/favorite/delete/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除收藏
 * @param {Array} ids - 收藏ID数组
 */
export function batchDeleteFavorites(ids) {
  return request({
    url: '/admin/favorite/batch-delete',
    method: 'delete',
    data: ids
  })
}

/**
 * 获取收藏统计
 */
export function getFavoriteStatistics() {
  return request({
    url: '/admin/favorite/statistics',
    method: 'get'
  })
}
