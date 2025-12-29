/**
 * 全局事件管理工具
 * 用于跨页面通信
 */

class GlobalEvents {
  constructor() {
    this.events = {};
  }

  /**
   * 监听事件
   * @param {String} eventName 事件名称
   * @param {Function} callback 回调函数
   */
  on(eventName, callback) {
    if (!this.events[eventName]) {
      this.events[eventName] = [];
    }
    this.events[eventName].push(callback);
  }

  /**
   * 移除事件监听
   * @param {String} eventName 事件名称
   * @param {Function} callback 回调函数
   */
  off(eventName, callback) {
    if (!this.events[eventName]) return;

    const index = this.events[eventName].indexOf(callback);
    if (index > -1) {
      this.events[eventName].splice(index, 1);
    }
  }

  /**
   * 触发事件
   * @param {String} eventName 事件名称
   * @param {*} data 传递的数据
   */
  emit(eventName, data) {
    if (!this.events[eventName]) return;

    this.events[eventName].forEach(callback => {
      try {
        callback(data);
      } catch (error) {
        console.error(`事件 ${eventName} 回调执行失败:`, error);
      }
    });
  }
}

// 创建全局实例
const globalEvents = new GlobalEvents();

export default globalEvents;

// 导出常用事件名称
export const EVENTS = {
  FAVORITE_CHANGED: 'favoriteChanged',
  USER_LOGIN: 'userLogin',
  USER_LOGOUT: 'userLogout'
};