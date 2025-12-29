import { config, apiBaseUrl } from '../../config/index';
import { post, get } from '../../utils/api';

/**
 * 用户认证相关API - 真实数据库连接
 */

/**
 * 微信登录
 * @param {Object} userInfo - { openid, nickname, avatarUrl }
 * @returns {Promise}
 */
export function wxLogin(userInfo) {
  return post('/user/login', userInfo, true); // 使用表单数据，真实API
}

/**
 * 获取用户信息
 * @param {Number} userId
 * @returns {Promise}
 */
export function getUserProfile(userId) {
  return get(`/user/profile/${userId}`); // 真实API
}

/**
 * 执行微信小程序登录完整流程（必须在用户点击事件中调用）
 * @returns {Promise} 返回真实的用户信息（包含数据库ID）
 */
export function performWxLogin() {
  return new Promise((resolve, reject) => {
    // 1. 调用 wx.login() 获取 code
    wx.login({
      success: (loginRes) => {
        if (loginRes.code) {
          console.log('微信登录成功，code:', loginRes.code);

          // 2. 获取用户信息（只能在用户点击事件中调用）
          wx.getUserProfile({
            desc: '获取用户信息用于完善个人资料',
            success: (profileRes) => {
              console.log('获取用户信息成功:', profileRes.userInfo);

              // 构造用户信息，注意这里暂时用code作为openid的替代
              // 实际生产环境应该通过后端用code换取真实openid
              const userInfo = {
                openid: 'wx_' + loginRes.code, // 临时使用code生成openid
                nickname: profileRes.userInfo.nickName,
                avatarUrl: profileRes.userInfo.avatarUrl,
              };

              // 3. 调用后端真实登录接口，存入数据库
              wxLogin(userInfo)
                .then((result) => {
                  console.log('后端登录成功，API响应:', result);

                  // 提取用户数据（现在API返回完整Result格式）
                  const user = result.data;

                  // 4. 存储用户信息到本地（包含真实的数据库ID）
                  wx.setStorageSync('userInfo', user);
                  wx.setStorageSync('currentUser', user); // 同时保存到authManager使用的位置
                  wx.setStorageSync('isLoggedIn', true);

                  // 也更新authManager的currentUser
                  const app = getApp();
                  if (app && app.globalData && app.globalData.authManager) {
                    app.globalData.authManager.currentUser = user;
                    console.log('已更新authManager的用户信息:', user);
                  }

                  resolve(user);
                })
                .catch((error) => {
                  console.error('后端登录失败:', error);
                  wx.showToast({
                    title: '登录失败',
                    icon: 'error'
                  });
                  reject(error);
                });

            },
            fail: (error) => {
              console.error('获取用户信息失败，用户可能拒绝了授权:', error);

              // 用户拒绝授权
              wx.showToast({
                title: '需要授权才能使用收藏功能',
                icon: 'none',
                duration: 2000
              });

              reject(new Error('用户拒绝授权'));
            }
          });

        } else {
          reject(new Error('微信登录失败，未获取到code'));
        }
      },
      fail: (error) => {
        console.error('wx.login失败:', error);
        wx.showToast({
          title: '微信登录失败',
          icon: 'error'
        });
        reject(error);
      }
    });
  });
}

/**
 * 检查是否已登录
 * @returns {Object|null} 返回真实用户信息或null
 */
export function checkLoginStatus() {
  try {
    const userInfo = wx.getStorageSync('userInfo');
    const isLoggedIn = wx.getStorageSync('isLoggedIn');

    // 确保有用户ID（证明是从数据库获取的真实用户）
    if (userInfo && isLoggedIn && userInfo.id) {
      console.log('用户已登录，用户ID:', userInfo.id);
      return userInfo;
    }

    console.log('用户未登录或信息不完整');
    return null;
  } catch (error) {
    console.error('检查登录状态失败:', error);
    return null;
  }
}

/**
 * 确保用户已登录，如果未登录则抛出错误
 * @returns {Promise} 返回用户信息
 */
export function ensureLoggedIn() {
  return new Promise((resolve, reject) => {
    // 使用统一的authManager获取用户信息（避免ID不一致问题）
    const app = getApp();
    const authManager = app.globalData?.authManager;
    const currentUser = authManager?.getCurrentUser();

    console.log('ensureLoggedIn检查，用户信息:', currentUser);

    if (currentUser && currentUser.id) {
      // 已登录，直接返回用户信息
      resolve(currentUser);
    } else {
      // 未登录，抛出错误，让调用方处理登录
      console.log('用户未登录，抛出错误');
      const error = new Error('用户未登录');
      error.code = 'NOT_LOGGED_IN';
      reject(error);
    }
  });
}

/**
 * 退出登录
 */
export function logout() {
  try {
    wx.removeStorageSync('userInfo');
    wx.removeStorageSync('isLoggedIn');
    console.log('用户已退出登录');
    return true;
  } catch (error) {
    console.error('退出登录失败:', error);
    return false;
  }
}

/**
 * 获取当前用户ID（确保用户已登录）
 * @returns {Promise<Number>} 用户ID
 */
export function getCurrentUserId() {
  return ensureLoggedIn()
    .then(user => {
      if (!user || !user.id) {
        throw new Error('无法获取用户ID');
      }
      return user.id;
    });
}