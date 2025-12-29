import { config, apiBaseUrl } from '../../config/index';

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
    wx.request({
      url: `${apiBaseUrl}/product/detail/${ID}`,
      method: 'GET',
      header: {
        'content-type': 'application/json',
      },
      success: (res) => {
        console.log('商品详情API返回:', res);
        console.log('后端返回的完整data:', res.data);

        if (res.statusCode === 200 && res.data.code === 200) {
          const detailData = res.data.data;
          console.log('原始商品数据:', detailData);

          // 处理规格参数 - 将JSON字符串转换为数组格式
          let specificationParams = [];
          if (detailData.specifications) {
            try {
              console.log('规格参数JSON字符串:', detailData.specifications);
              const specObj = JSON.parse(detailData.specifications);

              // 定义参数显示名称和顺序
              const nameMapping = {
                model: '型号',
                fabric_composition: '面料参数',
                weaving_process: '织造工艺',
                finishing_process: '后处理工艺',
                width: '门幅',
                weight: '克重',
                function: '功能特性',
                certification: '认证',
                flame_retardant: '阻燃性能',
                mite_resistance: '防螨性能',
                antibacterial_rate: '抗菌率'
              };

              const orderMapping = {
                model: 1,
                fabric_composition: 2,
                weaving_process: 3,
                finishing_process: 4,
                width: 5,
                weight: 6,
                function: 7,
                certification: 8,
                flame_retardant: 9,
                mite_resistance: 10,
                antibacterial_rate: 11
              };

              specificationParams = Object.entries(specObj)
                .filter(([key, value]) => value && value.trim() !== '')
                .map(([key, value]) => ({
                  key: key,
                  name: nameMapping[key] || key,
                  value: value.toString(),
                  order: orderMapping[key] || 999
                }))
                .sort((a, b) => a.order - b.order);

              console.log('处理后的规格参数:', specificationParams);
            } catch (e) {
              console.error('解析规格参数失败:', e);
            }
          }

          // 处理图片数据 - 如果是JSON字符串则解析
          let images = [detailData.mainImage];
          console.log('图片数据开始处理:', {
            mainImage: detailData.mainImage,
            images: detailData.images,
            imagesType: typeof detailData.images
          });

          if (detailData.images && typeof detailData.images === 'string') {
            try {
              console.log('尝试解析images JSON字符串:', detailData.images);
              images = JSON.parse(detailData.images);
              console.log('解析结果:', images);
              if (!Array.isArray(images)) {
                console.log('解析结果不是数组，使用主图');
                images = [detailData.mainImage];
              }
            } catch (e) {
              console.error('解析images JSON失败:', e);
              images = [detailData.mainImage];
            }
          } else if (Array.isArray(detailData.images)) {
            console.log('images已经是数组格式:', detailData.images);
            images = detailData.images;
          }

        console.log('最终images数组:', images);
        console.log('数组长度:', images.length);

          // 处理价格字段，避免NaN
          const wholesalePrice = parseFloat(detailData.wholesalePrice) || 0;
          const retailPrice = parseFloat(detailData.retailPrice) || 0;

          const processedData = {
            ...detailData,
            title: detailData.name, // 映射字段名
            spuId: detailData.id,
            price: wholesalePrice.toFixed(2),
            originPrice: retailPrice.toFixed(2),
            minSalePrice: wholesalePrice.toFixed(2),
            maxSalePrice: wholesalePrice.toFixed(2),
            maxLinePrice: retailPrice.toFixed(2),
            images: images, // 图片数组
            specificationParams: specificationParams,
            description: detailData.description || ''
          };

          console.log('最终处理的数据:', {
            title: processedData.title,
            price: processedData.price,
            specificationParamsCount: processedData.specificationParams.length,
            imagesCount: processedData.images.length,
            description: processedData.description,
            originalDescription: detailData.description
          });

          resolve(processedData);
        } else {
          console.error('获取商品详情失败:', res.data);
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
