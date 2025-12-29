import { config, apiBaseUrl } from '../../config/index';

console.log('=== fetchGood.js 已更新 - 支持JSON images字段 ===');

/** 获取商品列表 */
function mockFetchGood(ID = 0) {
  const { delay } = require('../_utils/delay');
  const { genGood } = require('../../model/good');
  return delay().then(() => genGood(ID));
}

/** 获取商品详情 */
export function fetchGood(ID = 0) {
  if (config.useMock) {
    return mockFetchGood(ID);
  }

  return new Promise((resolve, reject) => {
    console.log('🚀 请求商品详情，ID:', ID);

    wx.request({
      url: `${apiBaseUrl}/product/detail/${ID}`,
      method: 'GET',
      header: {
        'content-type': 'application/json',
      },
      success: (res) => {
        console.log('📦 API响应状态:', res.statusCode);

        if (res.statusCode === 200 && res.data.code === 200) {
          const detailData = res.data.data; // ProductDetailDTO格式

          console.log('📋 后端返回数据:', {
            title: detailData.title,
            description: detailData.description,
            imagesCount: detailData.images?.length || 0,
            firstImage: detailData.images?.[0],
            specCount: detailData.specificationParams?.length || 0
          });

          // 简单处理，后端已经返回正确的数组格式
          const processedData = {
            ...detailData,
            intro: detailData.description || '暂无描述',
            // 价格转换为元
            price: detailData.price ? (detailData.price / 100).toFixed(2) : '0.00',
            originPrice: detailData.originPrice ? (detailData.originPrice / 100).toFixed(2) : '0.00',
          };

          console.log('✅ 数据处理完成，发送到页面');
          resolve(processedData);
        } else {
          console.error('❌ API返回错误:', res.data);
          reject(new Error(res.data.message || '获取商品详情失败'));
        }
      },
      fail: (error) => {
        console.error('❌ 网络请求失败:', error);
        // 网络失败时使用模拟数据
        console.log('🔄 使用模拟数据');
        return mockFetchGood(ID).then(resolve).catch(reject);
      }
    });
  });
}