import { post, get, del } from '../../utils/api';

/**
 * 评论相关API
 */

/**
 * 添加评论
 * @param {Object} params - { userId, productId, content, rating }
 * @returns {Promise}
 */
export function addComment(params) {
  return post('/comment/add', params, true); // 使用表单数据
}

/**
 * 删除评论
 * @param {Number} commentId - 评论ID
 * @param {Number} userId - 用户ID
 * @returns {Promise}
 */
export function deleteComment(commentId, userId) {
  return del(`/comment/delete/${commentId}`, { userId });
}

/**
 * 获取商品评论列表（分页）
 * @param {Number} productId - 商品ID
 * @param {Number} page - 页码（从0开始）
 * @param {Number} size - 每页数量
 * @returns {Promise}
 */
export function getProductComments(productId, page = 0, size = 10) {
  return get(`/comment/product/${productId}`, { page, size });
}

/**
 * 获取商品所有评论（不分页）
 * @param {Number} productId - 商品ID
 * @returns {Promise}
 */
export function getAllProductComments(productId) {
  return get(`/comment/product/${productId}/all`);
}

/**
 * 获取用户评论列表
 * @param {Number} userId - 用户ID
 * @returns {Promise}
 */
export function getUserComments(userId) {
  return get(`/comment/user/${userId}`);
}

/**
 * 获取商品评论统计
 * @param {Number} productId - 商品ID
 * @returns {Promise} - { count, avgRating }
 */
export function getCommentStats(productId) {
  return get(`/comment/product/${productId}/stats`);
}

/**
 * 商家回复评论
 * @param {Number} commentId - 评论ID
 * @param {String} adminReply - 回复内容
 * @returns {Promise}
 */
export function replyComment(commentId, adminReply) {
  return post(`/comment/reply/${commentId}`, { adminReply }, true);
}
