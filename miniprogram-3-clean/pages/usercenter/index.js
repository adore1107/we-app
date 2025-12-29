import Toast from 'tdesign-miniprogram/toast/index';

Page({
  data: {
    activeNames: ['gel', 'tencel'], // 默认展开前2个
  },

  onLoad() {
    // 设置导航栏标题
    wx.setNavigationBarTitle({
      title: '关于我们'
    });
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
  }
});