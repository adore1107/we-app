import { apiBaseUrl } from '../config/index';

/**
 * 封装微信小程序的请求方法
 * @param {string} url 请求路径
 * @param {object} options 请求选项
 * @returns Promise
 */
function request(url, options = {}) {
  return new Promise((resolve, reject) => {
    wx.request({
      url: `${apiBaseUrl}${url}`,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'content-type': 'application/json',
        ...options.header
      },
      success: (res) => {
        if (res.statusCode === 200) {
          const result = res.data;
          if (result.code === 200) {
            resolve(result); // 返回完整的响应，包含code、message、data
          } else {
            wx.showToast({
              title: result.message || '请求失败',
              icon: 'none'
            });
            reject(new Error(result.message || '请求失败'));
          }
        } else {
          wx.showToast({
            title: '网络请求失败',
            icon: 'none'
          });
          reject(new Error(`请求失败，状态码：${res.statusCode}`));
        }
      },
      fail: (error) => {
        wx.showToast({
          title: '网络连接失败',
          icon: 'none'
        });
        reject(error);
      }
    });
  });
}

/**
 * GET请求
 * @param {string} url 请求路径
 * @param {object} params 请求参数
 * @returns Promise
 */
export function get(url, params = {}) {
  // 将参数转换为查询字符串
  const queryString = Object.keys(params)
    .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
    .join('&');

  const fullUrl = queryString ? `${url}?${queryString}` : url;

  return request(fullUrl, { method: 'GET' });
}

/**
 * POST请求
 * @param {string} url 请求路径
 * @param {object} data 请求数据
 * @param {boolean} isFormData 是否为表单数据
 * @returns Promise
 */
export function post(url, data = {}, isFormData = false) {
  return request(url, {
    method: 'POST',
    data,
    header: {
      'content-type': isFormData ? 'application/x-www-form-urlencoded' : 'application/json'
    }
  });
}

/**
 * 表单POST请求
 * @param {string} url 请求路径
 * @param {object} data 表单数据
 * @returns Promise
 */
export function postForm(url, data = {}) {
  return request(url, {
    method: 'POST',
    data,
    header: {
      'content-type': 'application/x-www-form-urlencoded'
    }
  });
}

/**
 * DELETE请求
 * @param {string} url 请求路径
 * @param {object} params 请求参数
 * @returns Promise
 */
export function del(url, params = {}) {
  // 将参数转换为查询字符串
  const queryString = Object.keys(params)
    .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
    .join('&');

  const fullUrl = queryString ? `${url}?${queryString}` : url;

  return request(fullUrl, { method: 'DELETE' });
}