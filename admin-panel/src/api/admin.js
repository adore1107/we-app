import request from '@/utils/request'

/**
 * 管理员登录
 * @param {Object} data - 登录信息 { username, password }
 */
export function login(data) {
  return request({
    url: '/admin/login',
    method: 'post',
    data
  })
}

/**
 * 验证Token是否有效
 */
export function verifyToken() {
  return request({
    url: '/admin/verify',
    method: 'get'
  })
}

/**
 * 获取当前管理员信息
 */
export function getAdminInfo() {
  return request({
    url: '/admin/info',
    method: 'get'
  })
}

/**
 * 退出登录
 */
export function logout() {
  return request({
    url: '/admin/logout',
    method: 'post'
  })
}
