/* eslint-disable no-param-reassign */
import { config } from '../../config/index';
import { get } from '../../utils/api';

/** 获取商品列表 */
function mockFetchGoodsList(params) {
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
      item.desc = '';
      if (item.spuTagList) {
        item.tags = item.spuTagList.map((tag) => tag.title);
      } else {
        item.tags = [];
      }
    });
  }
  return delay().then(() => {
    return data;
  });
}

/** 获取商品列表 */
export function fetchGoodsList(params) {
  if (config.useMock) {
    return mockFetchGoodsList(params);
  }

  // 调用真实的后端API
  const { categoryId, keyword, page = 0, size = 30 } = params;

  let apiUrl = '/product/list';
  let requestParams = { page, size };

  // 如果有分类ID，使用分类商品接口
  if (categoryId) {
    apiUrl = `/product/category/${categoryId}`;
  }

  // 如果有关键词，使用搜索接口
  if (keyword) {
    apiUrl = '/product/search';
    requestParams.keyword = keyword;
  }

  console.log('调用商品列表API:', apiUrl, requestParams);
  console.log('分页信息 - 页码:', requestParams.page, '页大小:', requestParams.size);
  console.log('是否为分类查询:', !!categoryId, '分类ID:', categoryId);
  console.log('是否有关键词:', !!keyword, '关键词:', keyword);

  return get(apiUrl, requestParams);
}
