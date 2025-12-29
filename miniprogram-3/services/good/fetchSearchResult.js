/* eslint-disable no-param-reassign */
import { config } from '../../config/index';
import { get } from '../../utils/api';

/** 获取搜索历史 */
function mockSearchResult(params) {
  const { delay } = require('../_utils/delay');
  const { getSearchResult } = require('../../model/search');

  const data = getSearchResult(params);

  if (data.spuList.length) {
    data.spuList.forEach((item) => {
      item.spuId = item.spuId;
      item.thumb = item.primaryImage;
      item.title = item.title;
      item.price = item.minSalePrice;
      item.originPrice = item.maxLinePrice;
      if (item.spuTagList) {
        item.tags = item.spuTagList.map((tag) => ({ title: tag.title }));
      } else {
        item.tags = [];
      }
    });
  }
  return delay().then(() => {
    return data;
  });
}

/** 搜索商品 - 连接真实API */
export function getSearchResult(params) {
  if (config.useMock) {
    return mockSearchResult(params);
  }

  // 转换参数格式 - 统一使用后端期望的参数名
  const requestParams = {
    keyword: params.keyword,
    page: params.page || params.pageNum || 0,
    size: params.pageSize || params.size || 20
  };

  // 如果有分类ID，添加到参数中（后端会同时按分类和关键词筛选）
  if (params.categoryId) {
    requestParams.categoryId = params.categoryId;
    console.log('分类搜索 - 分类ID:', params.categoryId, '关键词:', params.keyword);
  }

  const apiUrl = '/product/list';

  return get(apiUrl, requestParams).then(response => {
    // 提取API响应数据（现在API返回完整Result格式：{code, message, data}）
    const apiData = response.data || response;

    // 使用和首页完全相同的数据转换逻辑
    const records = apiData.records || [];
    const products = Array.isArray(records) ? records : [];

    const transformedProducts = products.map(product => ({
      spuId: product.id,
      thumb: product.mainImage || product.main_image,
      title: product.name,
      tags: product.tags || []
    }));

    return {
      spuList: transformedProducts,
      totalCount: apiData.total || 0
    };
  });
}
