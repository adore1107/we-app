import { config } from '../../config/index';
import { get } from '../../utils/api';

/** 获取热门搜索词 */
function mockGetHotKeywords() {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        code: 200,
        data: [
          '天丝', '四件套', '床上用品', '纺织面料',
          '床上三件套', '床单', '被套', '枕套',
          '纯棉', '蚕丝', '羽绒', '床垫'
        ]
      });
    }, 200);
  });
}

/** 获取热门搜索词 */
export function getHotKeywords() {
  if (config.useMock) {
    return mockGetHotKeywords();
  }
  return get('/search/hot-keywords');
}