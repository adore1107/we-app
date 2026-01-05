import request from '@/utils/request'

/**
 * 获取用户列表（管理员）
 * @param {Object} params - 查询参数
 */
export function getUserList(params) {
  return request({
    url: '/admin/user/list',
    method: 'get',
    params
  })
}

/**
 * 获取用户统计
 */
export function getUserStatistics() {
  return request({
    url: '/admin/user/statistics',
    method: 'get'
  })
}
