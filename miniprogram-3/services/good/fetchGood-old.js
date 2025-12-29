import { config, apiBaseUrl } from '../../config/index';

console.log('=== fetchGood.js 最新版本已加载 ===');

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
    console.log('开始请求商品详情，ID:', ID);
    wx.request({
      url: `${apiBaseUrl}/product/detail/${ID}`,
      method: 'GET',
      header: {
        'content-type': 'application/json',
      },
      success: (res) => {
        console.log('API响应:', res);

        if (res.statusCode === 200 && res.data.code === 200) {
          const detailData = res.data.data; // ProductDetailDTO格式

          console.log('后端返回的ProductDetailDTO:', detailData);
          console.log('关键字段检查:', {
            title: detailData.title,
            description: detailData.description,
            images: detailData.images,
            imagesType: typeof detailData.images,
            imagesLength: detailData.images?.length,
            firstImage: detailData.images?.[0],
            firstImageTrimmed: detailData.images?.[0]?.trim(),
            specificationParams: detailData.specificationParams
          });

        // 专门检查图片URL的问题
        if (detailData.images && detailData.images.length > 0) {
            console.log('=== 图片URL详细检查 ===');
            for (let i = 0; i < Math.min(detailData.images.length, 3); i++) {
                const img = detailData.images[i];
                console.log(`图片[${i}]:`);
                console.log('  原始值:', JSON.stringify(img));
                console.log('  长度:', img.length);
                console.log('  是否以引号开头:', img.startsWith('"'));
                console.log('  是否以引号结尾:', img.endsWith('"'));
                console.log('  去掉引号后:', img.replace(/^"|"$/g, ''));
            }
        }

          // 简单处理，直接使用后端返回的数据
          const processedData = {
            ...detailData,
            intro: detailData.description || '暂无描述', // 设置简介
            // 价格转换为元
            price: detailData.price ? (detailData.price / 100).toFixed(2) : '0.00',
            originPrice: detailData.originPrice ? (detailData.originPrice / 100).toFixed(2) : '0.00',
          };

          console.log('最终返回给页面的数据:', {
            title: processedData.title,
            intro: processedData.intro,
            description: processedData.description,
            imagesCount: processedData.images?.length || 0,
            specCount: processedData.specificationParams?.length || 0,
            imagesArray: processedData.images,
            firstImage: processedData.images?.[0],
            firstImageType: typeof processedData.images?.[0]
          });

          resolve(processedData);
        } else {
          console.error('API返回错误:', res.data);
          reject(new Error(res.data.message || '获取商品详情失败'));
        }
      },
      fail: (error) => {
        console.error('网络请求失败:', error);
        // 网络失败时使用模拟数据
        console.log('使用模拟数据作为fallback');
        return mockFetchGood(ID).then(resolve).catch(reject);
      }
    });
  });
}