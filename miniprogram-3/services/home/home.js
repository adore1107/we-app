import { config, cdnBase } from '../../config/index';
import { get } from '../../utils/api';

/** 获取首页数据 */
function mockFetchHome() {
  const { delay } = require('../_utils/delay');
  const { genSwiperImageList } = require('../../model/swiper');
  return delay().then(() => {
    return {
      swiper: genSwiperImageList(),
      tabList: [
        {
          text: '精选推荐',
          key: 0,
        },
        {
          text: '人气榜',
          key: 1,
        },
      ],
      activityImg: '/images/banner.jpg',
    };
  });
}

/** 获取首页真实数据 */
async function realFetchHome() {
  try {
    const result = await get('/home/data');

    // 提取API响应数据（现在API返回完整Result格式）
    const data = result.data || result;

    // 转换后端数据格式为前端需要的格式
    const swiper = (data.banners || []).map(banner => banner.imageUrl);

    const tabList = [
      {
        text: '精选推荐',
        key: 0,
      },
      {
        text: '热门商品',
        key: 1,
      },
      {
        text: '新品上市',
        key: 2,
      }
    ];

    return {
      swiper,
      tabList,
      activityImg: '/images/banner.jpg',
      categories: data.categories || [],
      hotProducts: data.hotProducts || [],
      newProducts: data.newProducts || [],
      recommendedProducts: data.recommendedProducts || []
    };
  } catch (error) {
    console.error('获取首页数据失败:', error);
    throw error;
  }
}

/** 获取首页数据 */
export function fetchHome() {
  if (config.useMock) {
    return mockFetchHome();
  }
  return realFetchHome();
}
