import { defineStore } from 'pinia'
import { login as loginApi, logout as logoutApi, getAdminInfo } from '@/api/admin'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('admin_token') || '',
    userInfo: JSON.parse(localStorage.getItem('admin_info') || 'null')
  }),

  getters: {
    // 是否已登录
    isLoggedIn: (state) => !!state.token,

    // 用户名
    username: (state) => state.userInfo?.username || '',

    // 角色
    role: (state) => state.userInfo?.role || '',

    // 真实姓名
    realName: (state) => state.userInfo?.realName || ''
  },

  actions: {
    /**
     * 登录
     */
    async login(username, password) {
      try {
        const response = await loginApi({ username, password })

        if (response.code === 200) {
          const { token, ...userInfo } = response.data

          // 保存token和用户信息
          this.token = token
          this.userInfo = userInfo

          // 持久化到localStorage
          localStorage.setItem('admin_token', token)
          localStorage.setItem('admin_info', JSON.stringify(userInfo))

          return { success: true, data: response.data }
        } else {
          return { success: false, message: response.message }
        }
      } catch (error) {
        console.error('登录失败:', error)
        return { success: false, message: error.message || '登录失败' }
      }
    },

    /**
     * 退出登录
     */
    async logout() {
      try {
        // 调用后端退出接口
        await logoutApi()
      } catch (error) {
        console.error('退出登录失败:', error)
      } finally {
        // 无论成功失败都清除本地数据
        this.clearUserData()
      }
    },

    /**
     * 清除用户数据
     */
    clearUserData() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_info')
    },

    /**
     * 获取用户信息（从后端）
     */
    async fetchUserInfo() {
      try {
        const response = await getAdminInfo()
        if (response.code === 200) {
          this.userInfo = response.data
          localStorage.setItem('admin_info', JSON.stringify(response.data))
          return { success: true, data: response.data }
        } else {
          return { success: false, message: response.message }
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
        return { success: false, message: error.message }
      }
    }
  }
})
