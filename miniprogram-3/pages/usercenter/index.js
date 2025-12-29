import Toast from 'tdesign-miniprogram/toast/index';
import { getCompanyGallery } from '../../services/banner/fetchBanner';

Page({
  data: {
    activeNames: ['gel', 'tencel'], // 默认展开前2个
    companyImages: [] // 公司风采图片数据（从后端获取）
  },

  onLoad() {
    // 设置导航栏标题
    wx.setNavigationBarTitle({
      title: '关于我们'
    });

    // 加载公司风采图片
    this.loadCompanyGallery();
  },

  /**
   * 从后端加载公司风采图片
   */
  async loadCompanyGallery() {
    try {
      const result = await getCompanyGallery();
      console.log('获取公司风采图片结果:', result);

      // 提取数据（API返回格式：{code, message, data}）
      const banners = result.data || result || [];

      // 转换为页面需要的格式
      const companyImages = banners.map(banner => ({
        url: banner.imageUrl,
        title: banner.title || ''
      }));

      this.setData({
        companyImages
      });

      console.log('公司风采图片加载完成，共', companyImages.length, '张');
    } catch (error) {
      console.error('加载公司风采图片失败:', error);
      // 失败时显示提示
      Toast({
        context: this,
        selector: '#t-toast',
        message: '加载图片失败',
      });
    }
  },

  // 折叠面板变化事件
  onCollapseChange(e) {
    this.setData({
      activeNames: e.detail.value
    });
  },

  // 拨打电话
  makePhoneCall() {
    wx.makePhoneCall({
      phoneNumber: '0579-85668888',
      success: () => {
        Toast({
          context: this,
          selector: '#t-toast',
          message: '正在拨打电话...',
        });
      },
      fail: () => {
        Toast({
          context: this,
          selector: '#t-toast',
          message: '拨号失败，请手动拨打',
        });
      }
    });
  },

  // 复制邮箱
  copyEmail() {
    wx.setClipboardData({
      data: 'sales@sj-tex.com',
      success: () => {
        Toast({
          context: this,
          selector: '#t-toast',
          message: '邮箱已复制到剪贴板',
        });
      }
    });
  },

  // 打开网站
  openWebsite() {
    wx.setClipboardData({
      data: 'www.sj-tex.com',
      success: () => {
        Toast({
          context: this,
          selector: '#t-toast',
          message: '网址已复制，请在浏览器中访问',
        });
      }
    });
  },

  // 预览公司图片
  previewImage(e) {
    const index = e.currentTarget.dataset.index;
    const urls = this.data.companyImages.map(item => item.url);

    wx.previewImage({
      current: urls[index], // 当前显示图片的http链接
      urls: urls // 需要预览的图片http链接列表
    });
  }
});