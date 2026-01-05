import request from '@/utils/request'

/**
 * 获取评论列表（管理员）
 * @param {Object} params - 查询参数
 */
export function getCommentList(params) {
  return request({
    url: '/admin/comment/list',
    method: 'get',
    params
  })
}

/**
 * 获取评论详情
 * @param {Number} id - 评论ID
 */
export function getCommentDetail(id) {
  return request({
    url: `/admin/comment/detail/${id}`,
    method: 'get'
  })
}

/**
 * 回复评论
 * @param {Number} id - 评论ID
 * @param {String} adminReply - 回复内容
 */
export function replyComment(id, adminReply) {
  return request({
    url: `/admin/comment/reply/${id}`,
    method: 'put',
    params: { adminReply }
  })
}

/**
 * 删除评论
 * @param {Number} id - 评论ID
 */
export function deleteComment(id) {
  return request({
    url: `/admin/comment/delete/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除评论
 * @param {Array} ids - 评论ID数组
 */
export function batchDeleteComments(ids) {
  return request({
    url: '/admin/comment/batch-delete',
    method: 'delete',
    data: ids
  })
}

/**
 * 更新评论状态
 * @param {Number} id - 评论ID
 * @param {Boolean} status - 状态
 */
export function updateCommentStatus(id, status) {
  return request({
    url: `/admin/comment/status/${id}`,
    method: 'put',
    params: { status }
  })
}
