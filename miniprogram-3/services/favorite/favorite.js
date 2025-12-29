import { config, apiBaseUrl } from '../../config/index';
import { post, get, del } from '../../utils/api';

/**
 * 收藏相关API服务
 */

/**
 * 切换收藏状态（收藏/取消收藏）
 * @param {Object} params - { userId, productId }
 * @returns {Promise}
 */
export function toggleFavorite(params) {
  if (config.useMock) {
    return mockToggleFavorite(params);
  }
  return post('/favorite/toggle', params, true); // 使用表单数据格式
}

/**
 * 添加收藏
 * @param {Object} params - { userId, productId }
 * @returns {Promise}
 */
export function addFavorite(params) {
  if (config.useMock) {
    return mockAddFavorite(params);
  }
  return post('/favorite/add', params, true); // 使用表单数据格式
}

/**
 * 取消收藏
 * @param {Object} params - { userId, productId }
 * @returns {Promise}
 */
export function removeFavorite(params) {
  if (config.useMock) {
    return mockRemoveFavorite(params);
  }
  return post('/favorite/remove', params, true); // 使用表单POST请求
}

/**
 * 检查是否已收藏
 * @param {Object} params - { userId, productId }
 * @returns {Promise<boolean>}
 */
export function checkFavorite(params) {
  if (config.useMock) {
    return mockCheckFavorite(params);
  }
  return get('/favorite/check', params);
}

/**
 * 获取用户收藏列表（带商品信息）
 * @param {Object} params - { userId, page, size }
 * @returns {Promise}
 */
export function getFavoriteList(params) {
  if (config.useMock) {
    return mockGetFavoriteList(params);
  }
  return get(`/favorite/list/${params.userId}`, {
    page: params.page || 0,
    size: params.size || 10
  });
}

/**
 * 获取用户收藏数量
 * @param {Number} userId
 * @returns {Promise}
 */
export function getFavoriteCount(userId) {
  if (config.useMock) {
    return Promise.resolve({ code: 200, data: 5 });
  }
  return get(`/favorite/count/${userId}`);
}

/**
 * 清空用户收藏
 * @param {Number} userId
 * @returns {Promise}
 */
export function clearFavorites(userId) {
  if (config.useMock) {
    return Promise.resolve({ code: 200, message: '清空成功' });
  }
  return del(`/favorite/clear/${userId}`);
}

// ================== Mock函数 ==================

/**
 * 模拟切换收藏状态
 */
function mockToggleFavorite(params) {
  return new Promise((resolve) => {
    setTimeout(() => {
      const mockData = {
        code: 200,
        message: '操作成功',
        data: {
          id: Date.now(),
          userId: params.userId,
          productId: params.productId,
          createdAt: new Date().toISOString()
        }
      };
      resolve(mockData);
    }, 300);
  });
}

/**
 * 模拟添加收藏
 */
function mockAddFavorite(params) {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        code: 200,
        message: '收藏成功',
        data: {
          id: Date.now(),
          userId: params.userId,
          productId: params.productId,
          createdAt: new Date().toISOString()
        }
      });
    }, 300);
  });
}

/**
 * 模拟取消收藏
 */
function mockRemoveFavorite(params) {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        code: 200,
        message: '取消收藏成功'
      });
    }, 300);
  });
}

/**
 * 模拟检查收藏状态
 */
function mockCheckFavorite(params) {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        code: 200,
        message: '检查成功',
        data: Math.random() > 0.5 // 随机返回收藏状态
      });
    }, 200);
  });
}

/**
 * 模拟获取收藏列表
 */
function mockGetFavoriteList(params) {
  return new Promise((resolve) => {
    setTimeout(() => {
      const mockFavorites = [
        {
          id: 1,
          userId: params.userId,
          productId: 1,
          createdAt: '2024-01-01T10:00:00',
          product: {
            id: 1,
            name: '凝胶恒温床垫面料 Q-max≥0.2',
            mainImage: 'https://gw.alicdn.com/bao/uploaded/i1/92042735/O1CN01hcv6GQ1W4gJcj1fEU_!!92042735.jpg_.webp',
            wholesalePrice: 85.00,
            minOrderQuantity: 100
          }
        },
        {
          id: 2,
          userId: params.userId,
          productId: 2,
          createdAt: '2024-01-02T10:00:00',
          product: {
            id: 2,
            name: '80支天丝四件套 高支高密',
            mainImage: 'http://img.alicdn.com/img/i2/109580487/O1CN01J1edho1FT5vWoWpJN_!!4611686018427384007-0-saturn_solar.jpg',
            wholesalePrice: 380.00,
            minOrderQuantity: 200
          }
        }
      ];

      resolve({
        code: 200,
        message: '获取成功',
        data: {
          records: mockFavorites,
          total: mockFavorites.length,
          size: params.size || 10,
          current: params.page || 0,
          pages: 1
        }
      });
    }, 400);
  });
}