import { wxLogin } from '../services/user/auth';

/**
 * 用户授权和登录管理
 */
class AuthManager {
  constructor() {
    this.currentUser = null;
    this.isLoggingIn = false;
  }

  /**
   * 检查用户是否已登录
   */
  isLoggedIn() {
    return this.currentUser !== null;
  }

  /**
   * 获取当前用户信息
   */
  getCurrentUser() {
    return this.currentUser;
  }

  /**
   * 自动登录 - 在小程序启动时调用
   */
  async autoLogin() {
    if (this.isLoggingIn) {
      return this.currentUser;
    }

    this.isLoggingIn = true;

    try {
      // 检查本地存储的用户信息
      const localUser = wx.getStorageSync('currentUser');
      if (localUser && localUser.id) {
        // 验证本地用户信息是否完整
        this.currentUser = localUser;
        console.log('使用本地用户信息:', this.currentUser);
        return this.currentUser;
      } else if (localUser) {
        // 本地存储的用户信息不完整，清除并重新登录
        console.log('本地用户信息不完整，清除并重新登录');
        wx.removeStorageSync('currentUser');
        this.currentUser = null;
      }

      // 对于自动登录，先检查本地存储
      // 如果没有本地用户信息，自动登录不能强制用户授权，返回null
      console.log('自动登录：未找到本地用户信息，需要用户主动授权登录');
      return null;
    } catch (error) {
      console.error('自动登录失败:', error);
      return null;
    } finally {
      this.isLoggingIn = false;
    }
  }

  /**
   * 执行微信登录
   */
  async performWxLogin() {
    try {
      console.log('开始微信登录流程...');

      // 导入登录服务
      const { performWxLogin } = require('../services/user/auth');

      // 使用用户授权的登录方法（这会触发getUserProfile）
      const user = await performWxLogin();
      console.log('微信授权登录成功:', user);

      // 验证返回的用户信息是否包含ID
      if (user && user.id) {
        // 将用户信息保存到authManager的currentUser中
        this.currentUser = user;
        wx.setStorageSync('currentUser', user);

        console.log('用户登录完成，ID:', user.id);
        return user;
      } else {
        console.error('登录返回的用户信息缺少ID:', user);
        throw new Error('登录失败：未获取到有效用户ID');
      }
    } catch (error) {
      console.error('执行微信登录失败:', error);
      throw error;
    }
  }

  /**
   * 获取用户信息授权
   */
  async getUserInfo() {
    return new Promise((resolve, reject) => {
      // 优先尝试getUserProfile（需要用户主动授权）
      if (wx.getUserProfile) {
        wx.getUserProfile({
          desc: '获取用户信息用于完善个人资料',
          success: (res) => {
            console.log('获取用户信息成功:', res.userInfo);
            resolve(res.userInfo);
          },
          fail: (error) => {
            console.log('用户拒绝授权:', error);
            // 用户拒绝时返回基本用户信息
            resolve({
              nickName: '微信用户',
              avatarUrl: ''
            });
          }
        });
      } else {
        // 如果不支持getUserProfile，返回基本信息
        resolve({
          nickName: '微信用户',
          avatarUrl: ''
        });
      }
    });
  }

  /**
   * 保存用户信息到本地和后端
   */
  async saveUserInfo(wxUserInfo, loginResult = null) {
    try {
      // 如果有登录结果，直接使用登录结果中的用户信息
      if (loginResult && loginResult.id) {
        // 合并微信用户信息和登录结果
        const userInfo = {
          ...loginResult,
          nickname: wxUserInfo.nickName || loginResult.nickname || '微信用户',
          avatarUrl: wxUserInfo.avatarUrl || loginResult.avatarUrl || '',
        };

        // 保存到本地存储
        wx.setStorageSync('currentUser', userInfo);
        this.currentUser = userInfo;

        console.log('用户信息保存成功:', userInfo);
        return userInfo;
      } else {
        // 没有有效的登录结果，抛出错误
        throw new Error('保存用户信息失败：缺少有效的登录结果和用户ID');
      }
    } catch (error) {
      console.error('保存用户信息失败:', error);
      throw error;
    }
  }

  /**
   * 退出登录
   */
  logout() {
    this.currentUser = null;
    wx.removeStorageSync('currentUser');
    console.log('用户已退出登录');
  }
}

// 创建全局实例
const authManager = new AuthManager();

export default authManager;