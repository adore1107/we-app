import request from '@/utils/request'

/**
 * 获取Dashboard统计数据
 */
export function getDashboardStats() {
  return request({
    url: '/admin/dashboard/stats',
    method: 'get'
  })
}

/**
 * 获取最近商品浏览记录
 */
export function getRecentViews(limit = 10) {
  return request({
    url: '/admin/dashboard/recent-views',
    method: 'get',
    params: { limit }
  })
}

/**
 * 获取热门商品
 */
export function getTopProducts(limit = 10) {
  return request({
    url: '/admin/dashboard/top-products',
    method: 'get',
    params: { limit }
  })
}
