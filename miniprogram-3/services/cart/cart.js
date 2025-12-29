import { config } from '../../config/index';
import { get } from '../../utils/api';

/** 获取购物车mock数据 */
function mockFetchCartGroupData(params) {
  const { delay } = require('../_utils/delay');
  const { genCartGroupData } = require('../../model/cart');

  return delay().then(() => genCartGroupData(params));
}

/** 获取收藏数据 */
export function fetchCartGroupData(params) {
  if (config.useMock) {
    return mockFetchCartGroupData(params);
  }

  return new Promise((resolve, reject) => {
    // 使用统一的用户信息获取方式（与商品详情页一致）
    const app = getApp();
    const authManager = app.globalData.authManager;
    const currentUser = authManager.getCurrentUser();

    console.log('获取收藏列表，使用authManager用户信息:', currentUser);

    if (!currentUser || !currentUser.id) {
      console.log('用户未登录或无用户ID，返回空收藏列表');
      // 用户未登录，返回空数据
      resolve({
        code: 200,
        message: '获取成功',
        data: {
          isNotEmpty: false,
          favoritesList: []
        }
      });
      return;
    }

    // 调用真实的收藏列表API
    console.log(`开始调用收藏列表API: /favorite/list/${currentUser.id}`);
    get(`/favorite/list/${currentUser.id}`, {
      page: 0, // 后端支持0-based分页，会自动+1
      size: 50 // 获取足够多的收藏记录
    })
    .then((result) => {
      console.log('获取收藏列表API响应:', result);

      // 提取API响应数据（现在API返回完整Result格式）
      const favoritesData = result.data || result;
      console.log('提取的收藏数据:', favoritesData);

      // 处理收藏数据，转换为购物车需要的格式
      const records = favoritesData.records || [];
      console.log('收藏记录数量:', records.length);
      console.log('收藏记录详细信息:', records);

      const formattedData = {
        isNotEmpty: records.length > 0,
        favoritesList: []
      };

      // 如果没有收藏记录，返回空数据
      if (records.length === 0) {
        console.log('⚠️ 用户没有收藏记录，返回空列表');
        resolve({
          code: 200,
          message: '获取成功',
          data: {
            isNotEmpty: false,
            favoritesList: []
          }
        });
        return;
      }

      // 为每个收藏记录查询商品信息
      const promises = records.map((favorite, index) => {
        console.log(`获取收藏商品详情[${index}]，商品ID:`, favorite.productId, '收藏时间:', favorite.createdAt);

        return get(`/product/detail/${favorite.productId}`)
          .then((productResult) => {
            // 提取商品详情数据
            console.log(`🔍 商品[${favorite.productId}]API原始响应:`, productResult);
            const productDetail = productResult.data || productResult;
            console.log(`📦 商品[${favorite.productId}]提取的数据:`, productDetail);
            console.log(`🏷️ 商品字段检查:`, {
              name: productDetail.name,
              title: productDetail.title,
              mainImage: productDetail.mainImage,
              primaryImage: productDetail.primaryImage,
              wholesalePrice: productDetail.wholesalePrice,
              price: productDetail.price
            });

            // 合并收藏信息和商品信息
            return {
              spuId: favorite.productId,
              skuId: favorite.productId, // 使用收藏的商品ID
              title: productDetail.title || '未知商品',
              image: productDetail.primaryImage || '',
              price: productDetail.price || 0, // 已是分为单位的价格
              wholesalePrice: productDetail.price || 0, // 使用价格字段
              minOrderQuantity: productDetail.minOrderQuantity || 1,
              unit: productDetail.unit || '件',
              leadTime: productDetail.leadTime || 7,
              favoriteTime: favorite.createdAt,
              storeName: '浙江宋家纺织',
              // 购物车需要的其他字段
              quantity: 0,
              isInvalid: false,
              isValid: true,
            };
          })
          .catch((error) => {
            console.error('获取商品详情失败:', error);
            // 如果获取商品详情失败，返回基本信息
            return {
              spuId: favorite.productId,
              skuId: favorite.productId,
              title: '商品信息获取失败',
              image: '',
              price: 0,
              wholesalePrice: 0,
              minOrderQuantity: 1,
              unit: '件',
              leadTime: 7,
              favoriteTime: favorite.createdAt,
              storeName: '浙江宋家纺织',
              quantity: 0,
              isInvalid: false,
              isValid: true,
            };
          });
      });

      // 等待所有商品详情查询完成
      console.log('等待所有商品详情查询完成，收藏记录数量:', promises.length);
      return Promise.all(promises);
    })
    .then((favoriteProducts) => {
      console.log('✅ 所有商品详情查询完成，获取到收藏商品:', favoriteProducts.length);

      const formattedData = {
        isNotEmpty: favoriteProducts.length > 0,
        favoritesList: favoriteProducts
      };

      console.log('📦 格式化后的收藏数据:', {
        isNotEmpty: formattedData.isNotEmpty,
        favoritesList: formattedData.favoritesList.map(item => ({
          spuId: item.spuId,
          title: item.title,
          price: item.price
        }))
      });

      resolve({
        code: 200,
        message: '获取成功',
        data: formattedData
      });
    })
    .catch((error) => {
      console.error('获取收藏数据失败:', error);
      reject(error);
    });
  });
}
