import { apiBaseUrl } from '../../config/index';

/**
 * 更新商品浏览时长
 * @param {Number} userId - 用户ID
 * @param {Number} productId - 商品ID
 * @param {Number} durationSeconds - 浏览时长（秒）
 */
export function updateViewDuration(userId, productId, durationSeconds) {
  return new Promise((resolve, reject) => {
    console.log('📊 更新浏览时长:', { userId, productId, durationSeconds });

    wx.request({
      url: `${apiBaseUrl}/product/view/duration`,
      method: 'POST',
      data: {
        userId,
        productId,
        durationSeconds
      },
      header: {
        'content-type': 'application/x-www-form-urlencoded',
      },
      success: (res) => {
        if (res.statusCode === 200 && res.data.code === 200) {
          console.log('✅ 浏览时长更新成功');
          resolve(res.data);
        } else {
          console.error('❌ 浏览时长更新失败:', res.data);
          reject(new Error(res.data.message || '更新失败'));
        }
      },
      fail: (error) => {
        console.error('❌ 网络请求失败:', error);
        reject(error);
      }
    });
  });
}
