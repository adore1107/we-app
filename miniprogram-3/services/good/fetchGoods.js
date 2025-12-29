import { config } from '../../config/index';
import { get } from '../../utils/api';

/** 获取商品列表 */
function mockFetchGoodsList(pageIndex = 1, pageSize = 20) {
  const { delay } = require('../_utils/delay');
  const { getGoodsList } = require('../../model/goods');
  return delay().then(() =>
    getGoodsList(pageIndex, pageSize).map((item) => {
      return {
        spuId: item.spuId,
        thumb: item.primaryImage,
        title: item.title,
        // 移除价格字段，B2B不公开显示价格
        // 只保留基本的商品信息，简洁展示
        tags: item.spuTagList ? item.spuTagList.map((tag) => tag.title) : [],
      };
    }),
  );
}

/** 获取商品真实列表 */
async function realFetchGoodsList(pageIndex = 0, pageSize = 5, tabIndex = 0) {
  try {
    let data = [];
    let apiName = '';

    console.log(`请求商品列表: pageIndex=${pageIndex}, pageSize=${pageSize}, tabIndex=${tabIndex}`);
    console.log(`tabIndex类型: ${typeof tabIndex}, tabIndex===1: ${tabIndex === 1}`);

    // 根据tabIndex调用不同的API
    if (tabIndex === 1) {
      // 热门商品 - 支持分页
      apiName = '/product/hot';
      const params = { page: pageIndex, size: pageSize };
      const result = await get(apiName, params);
      // 提取API响应数据（现在API返回完整Result格式）
      const apiData = result.data || result;
      // MyBatis-Plus返回的是IPage对象，使用records字段
      data = apiData.records || apiData || [];
      console.log(`请求热门商品: ${apiName}, params:`, params, data);
    } else if (tabIndex === 2) {
      // 新品商品 - 支持分页
      apiName = '/product/new';
      const params = { page: pageIndex, size: pageSize };
      const result = await get(apiName, params);
      // 提取API响应数据（现在API返回完整Result格式）
      const apiData = result.data || result;
      // MyBatis-Plus返回的是IPage对象，使用records字段
      data = apiData.records || apiData || [];
      console.log(`请求新品商品: ${apiName}, params:`, params, data);
    } else {
      // 精选推荐 - 使用分页接口
      apiName = '/product/list';
      const params = { page: pageIndex, size: pageSize };
      const result = await get(apiName, params);
      // 提取API响应数据（现在API返回完整Result格式）
      const apiData = result.data || result;
      // MyBatis-Plus返回的是IPage对象，使用records字段
      data = apiData.records || apiData || [];
      console.log(`请求推荐商品: ${apiName}, params:`, params, data);
    }

    // 确保data是数组
    const products = Array.isArray(data) ? data : [];
    console.log(`最终商品数组长度: ${products.length}`);

    return products.map(product => ({
      spuId: product.id,
      thumb: product.mainImage,
      title: product.name,
      // 移除价格字段，B2B不公开显示价格
      // 只保留基本的商品信息，简洁展示
      tags: product.tags || []
    }));
  } catch (error) {
    console.error('获取商品列表失败:', error);
    console.error('错误详情:', error.message);
    throw error;
  }
}

/** 获取商品列表 */
export function fetchGoodsList(pageIndex = 0, pageSize = 5, tabIndex = 0) {
  if (config.useMock) {
    return mockFetchGoodsList(pageIndex + 1, pageSize);
  }
  return realFetchGoodsList(pageIndex, pageSize, tabIndex);
}
