import { config } from '../../config/index';
import { get } from '../../utils/api';

/** 获取商品列表 */
function mockFetchGoodCategory() {
  const { delay } = require('../_utils/delay');
  const { getCategoryList } = require('../../model/category');
  return delay().then(() => getCategoryList());
}

/** 获取分类列表（扁平结构） */
export function getCategoryList() {
  if (config.useMock) {
    return mockFetchGoodCategory();
  }
  // 调用真实的后端API
  return get('/category/list');
}

/** 获取树形分类结构（一级分类+子分类）- 推荐使用 */
export function getCategoryTree() {
  if (config.useMock) {
    return mockFetchGoodCategory();
  }
  // 调用真实的后端API
  return get('/category/tree');
}

/** 获取一级分类 */
export function getMainCategories() {
  if (config.useMock) {
    return mockFetchGoodCategory();
  }
  return get('/category/main');
}

/** 获取子分类 */
export function getSubCategories(parentId) {
  if (config.useMock) {
    return mockFetchGoodCategory();
  }
  return get(`/category/sub/${parentId}`);
}
