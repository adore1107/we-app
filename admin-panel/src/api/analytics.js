import request from '@/utils/request'

/**
 * 获取概览统计数据
 */
export function getOverview() {
  return request({
    url: '/admin/analytics/overview',
    method: 'get'
  })
}

/**
 * 获取商品分类分布
 */
export function getProductCategory() {
  return request({
    url: '/admin/analytics/product-category',
    method: 'get'
  })
}

/**
 * 获取热门商品排行（按收藏数）
 */
export function getTopProductsByFavorite() {
  return request({
    url: '/admin/analytics/top-products-favorite',
    method: 'get'
  })
}

/**
 * 获取热门商品排行（按浏览数）
 */
export function getTopProductsByView() {
  return request({
    url: '/admin/analytics/top-products-view',
    method: 'get'
  })
}

/**
 * 获取商品状态分布
 */
export function getProductStatus() {
  return request({
    url: '/admin/analytics/product-status',
    method: 'get'
  })
}

/**
 * 获取评论评分分布
 */
export function getCommentRating() {
  return request({
    url: '/admin/analytics/comment-rating',
    method: 'get'
  })
}
