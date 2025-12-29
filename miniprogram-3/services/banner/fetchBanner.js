import { get } from '../../utils/api';

/**
 * 获取所有轮播图
 */
export function getBannerList() {
  return get('/banner/list');
}

/**
 * 根据类型获取轮播图
 * @param {string} type 类型：home_banner-首页轮播，company_gallery-公司风采
 */
export function getBannersByType(type) {
  return get(`/banner/type/${type}`);
}

/**
 * 获取首页轮播图
 */
export function getHomeBanners() {
  return getBannersByType('home_banner');
}

/**
 * 获取公司风采图片
 */
export function getCompanyGallery() {
  return getBannersByType('company_gallery');
}
