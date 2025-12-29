import updateManager from './common/updateManager';
import authManager from './utils/auth';
import globalEvents from './utils/globalEvents';

App({
  onLaunch: function () {
    // 初始化全局事件
    this.globalData.globalEvents = globalEvents;

    // 小程序启动时自动进行用户登录
    this.initUserAuth();
  },

  onShow: function () {
    updateManager();
  },

  /**
   * 初始化用户授权 - 进入小程序立即授权登录
   */
  async initUserAuth() {
    try {
      console.log('小程序启动，开始用户授权登录...');

      // 先尝试静默登录（使用本地存储）
      const user = await authManager.autoLogin();
      if (user && user.id) {
        console.log('用户自动登录成功:', user);
        // 用户已登录，可以正常使用小程序
        return;
      } else {
        console.log('本地无用户信息或已过期，开始微信授权登录...');

        // 延迟一点时间再弹窗，让小程序先完全启动
        setTimeout(() => {
          this.forceUserAuth();
        }, 500);
      }
    } catch (error) {
      console.error('用户自动登录失败:', error);
      // 出错时也尝试强制授权
      setTimeout(() => {
        this.forceUserAuth();
      }, 500);
    }
  },

  /**
   * 强制微信用户授权 - 进入小程序时主动授权
   */
  forceUserAuth() {
    // 先显示为什么需要授权的说明
    wx.showModal({
      title: '欢迎使用宋家纺织',
      content: '为了给您提供更好的个性化服务，请授权微信用户信息。您将能够：\n\n✅ 收藏喜欢的商品\n✅ 获取个性化推荐\n✅ 享受完整的B2B采购服务',
      confirmText: '立即授权',
      cancelText: '稍后再说',
      success: (modalRes) => {
        if (modalRes.confirm) {
          // 用户同意授权，开始微信授权流程
          this.performWechatAuth();
        } else {
          // 用户选择稍后再说，显示提示
          this.showAuthReminder();
        }
      }
    });
  },

  /**
   * 执行微信授权流程
   */
  performWechatAuth() {
    // 检测开发环境：开发者工具中 getUserProfile 会报错，使用模拟数据
    const accountInfo = wx.getAccountInfoSync();
    const isDev = accountInfo.miniProgram.envVersion === 'develop';

    if (isDev) {
      // 开发环境：直接使用模拟数据，跳过 getUserProfile
      console.log('⚠️ 开发环境检测：使用模拟用户数据');
      this.performDevLogin();
      return;
    }

    // 生产/体验环境：使用真实的 getUserProfile
    if (wx.getUserProfile) {
      wx.getUserProfile({
        desc: '获取用户信息用于完善个人资料',
        success: async (res) => {
          console.log('微信授权成功:', res.userInfo);

          // 显示授权中提示
          wx.showLoading({
            title: '登录中...',
            mask: true
          });

          try {
            const authManager = getApp().globalData.authManager;

            // 获取微信登录code
            wx.login({
              success: async (loginRes) => {
                if (loginRes.code) {
                  // 调用登录API
                  const { wxLogin } = require('./services/user/auth');

                  const userInfo = {
                    openid: 'wx_' + loginRes.code,
                    nickname: res.userInfo.nickName || '微信用户',
                    avatarUrl: res.userInfo.avatarUrl || '',
                    company_name: '',
                    real_name: '',
                    position: '',
                    industry: '',
                    city: ''
                  };

                  const loginResult = await wxLogin(userInfo);
                  console.log('授权登录成功:', loginResult);

                  // 保存用户信息（现在API返回完整Result格式）
                  const user = loginResult.data;
                  if (user && user.id) {
                    await authManager.saveUserInfo(res.userInfo, user);
                    console.log('用户授权登录完成:', authManager.getCurrentUser());

                    wx.hideLoading();
                    wx.showToast({
                      title: '登录成功',
                      icon: 'success',
                      duration: 2000
                    });
                  }
                } else {
                  throw new Error('获取微信登录码失败');
                }
              },
              fail: (error) => {
                wx.hideLoading();
                console.error('微信登录失败:', error);
                this.showAuthFailed();
              }
            });

          } catch (error) {
            wx.hideLoading();
            console.error('授权登录失败:', error);
            this.showAuthFailed();
          }
        },
        fail: (error) => {
          console.log('用户拒绝授权:', error);
          this.showAuthReminder();
        }
      });
    } else {
      // 如果不支持getUserProfile，使用兼容方案
      this.showCompatAuth();
    }
  },

  /**
   * 开发环境模拟登录（完全不调用微信API，避免 access_token 错误）
   */
  async performDevLogin() {
    wx.showLoading({
      title: '开发环境登录中...',
      mask: true
    });

    try {
      const authManager = getApp().globalData.authManager;
      const { wxLogin } = require('./services/user/auth');

      // 模拟微信用户信息
      const mockWxUserInfo = {
        nickName: '开发测试用户',
        avatarUrl: 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0'
      };

      // 开发环境：生成固定的模拟 openid，不调用 wx.login()
      const timestamp = Date.now();
      const userInfo = {
        openid: 'wx_dev_user_' + timestamp,  // 使用时间戳生成唯一ID
        nickname: mockWxUserInfo.nickName,
        avatarUrl: mockWxUserInfo.avatarUrl,
        company_name: '测试公司',
        real_name: '测试用户',
        position: '测试岗位',
        industry: '纺织业',
        city: '杭州'
      };

      console.log('🔧 开发环境：使用模拟用户信息', userInfo);

      // 调用后端登录API
      const loginResult = await wxLogin(userInfo);
      console.log('✅ 开发环境登录成功:', loginResult);

      // 保存用户信息
      const user = loginResult.data;
      if (user && user.id) {
        await authManager.saveUserInfo(mockWxUserInfo, user);
        console.log('✅ 开发环境用户信息保存完成:', authManager.getCurrentUser());

        wx.hideLoading();
        wx.showToast({
          title: '开发环境登录成功',
          icon: 'success',
          duration: 2000
        });
      } else {
        throw new Error('登录返回的用户信息无效');
      }

    } catch (error) {
      wx.hideLoading();
      console.error('❌ 开发环境登录失败:', error);

      // 开发环境登录失败时，显示详细错误信息
      wx.showModal({
        title: '开发环境登录失败',
        content: '错误信息: ' + (error.message || JSON.stringify(error)),
        confirmText: '重试',
        cancelText: '取消',
        success: (res) => {
          if (res.confirm) {
            setTimeout(() => {
              this.performDevLogin();
            }, 1000);
          }
        }
      });
    }
  },

  /**
   * 显示授权提醒
   */
  showAuthReminder() {
    wx.showToast({
      title: '您可以在收藏时重新授权',
      icon: 'none',
      duration: 3000
    });
  },

  /**
   * 显示授权失败提示
   */
  showAuthFailed() {
    wx.showModal({
      title: '登录失败',
      content: '授权登录失败，请检查网络后重试',
      confirmText: '重试',
      cancelText: '稍后再说',
      success: (res) => {
        if (res.confirm) {
          // 重试授权
          setTimeout(() => {
            this.forceUserAuth();
          }, 1000);
        } else {
          this.showAuthReminder();
        }
      }
    });
  },

  /**
   * 兼容性授权方案
   */
  showCompatAuth() {
    wx.showModal({
      title: '版本较旧',
      content: '当前微信版本较旧，请升级微信后获得更好体验',
      confirmText: '继续使用',
      showCancel: false
    });
  },

  /**
   * 全局数据
   */
  globalData: {
    authManager: authManager,
    globalEvents: null // 全局事件实例
  }
});
