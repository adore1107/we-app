import { fetchHome } from '../../services/home/home';
import { fetchGoodsList } from '../../services/good/fetchGoods';
import Toast from 'tdesign-miniprogram/toast/index';

Page({
  data: {
    imgSrcs: [],
    tabList: [],
    goodsList: [],
    goodsListLoadStatus: 0,
    pageLoading: false,
    current: 1,
    autoplay: true,
    duration: '500',
    interval: 5000,
    navigation: { type: 'dots' },
    swiperImageProps: { mode: 'scaleToFill' },
    showBackToTop: false,  // 回到顶部按钮显示状态
  },

  goodListPagination: {
    index: 0,
    num: 20,
  },

  privateData: {
    tabIndex: 0,
  },

  onShow() {
    this.getTabBar().init();
  },

  onLoad() {
    this.init();
  },

  onReachBottom() {
    console.log('触底了！当前商品数量:', this.data.goodsList.length);
    console.log('加载状态:', this.data.goodsListLoadStatus);

    if (this.data.goodsListLoadStatus === 0) {
      console.log('开始加载更多商品...');
      this.loadGoodsList();
    } else {
      console.log('正在加载或已完成，不再请求');
    }
  },

  onPullDownRefresh() {
    this.init();
  },

  init() {
    this.loadHomePage();
  },

  loadHomePage() {
    wx.stopPullDownRefresh();

    this.setData({
      pageLoading: true,
    });
    fetchHome().then((homeData) => {
      console.log('获取首页数据:', homeData);
      this.setData({
        tabList: homeData.tabList || [],
        imgSrcs: homeData.swiper || [],
        pageLoading: false,
      });
      this.loadGoodsList(true);
    }).catch((error) => {
      console.error('加载首页数据失败:', error);
      this.setData({
        pageLoading: false,
      });
    });
  },

  tabChangeHandle(e) {
    console.log('Tab切换事件:', e);
    console.log('e.detail:', e.detail);
    console.log('e.detail类型:', typeof e.detail);

    // 正确提取value值
    let tabIndex = 0;

    if (typeof e.detail === 'object' && e.detail.value !== undefined) {
      // TDesign Tab组件返回对象格式：{value: 1, label: "热门商品"}
      tabIndex = parseInt(e.detail.value) || 0;
      console.log(`从对象中提取value: ${e.detail.value}, 解析后: ${tabIndex}`);
    } else {
      // 兼容其他可能的格式
      tabIndex = parseInt(e.detail) || 0;
      console.log(`直接解析e.detail: ${e.detail}, 解析后: ${tabIndex}`);
    }

    this.privateData.tabIndex = tabIndex;
    this.goodListPagination.index = 0; // 重置分页索引
    console.log(`最终设置tabIndex: ${this.privateData.tabIndex}, 类型: ${typeof this.privateData.tabIndex}`);

    // 立即验证当前tabIndex
    if (this.privateData.tabIndex === 1) {
      console.log('✅ 正确！准备调用热门商品接口 /product/hot');
    } else {
      console.log(`❌ 错误！当前tabIndex=${this.privateData.tabIndex}，将调用全部商品接口`);
    }

    this.loadGoodsList(true);
  },

  onReTry() {
    this.loadGoodsList();
  },

  async loadGoodsList(fresh = false) {
    console.log(`loadGoodsList: fresh=${fresh}, 当前商品数量=${this.data.goodsList.length}`);

    if (fresh) {
      wx.pageScrollTo({
        scrollTop: 0,
      });
    }

    this.setData({ goodsListLoadStatus: 1 });

    const pageSize = this.goodListPagination.num;
    let pageIndex = fresh ? 0 : this.goodListPagination.index;

    console.log(`计算分页: fresh=${fresh}, pageIndex=${pageIndex}, pageSize=${pageSize}, pagination.index=${this.goodListPagination.index}`);

    try {
      const tabIndex = this.privateData.tabIndex || 0;
      console.log(`即将请求商品: pageIndex=${pageIndex}, pageSize=${pageSize}, tabIndex=${tabIndex}`);

      const nextList = await fetchGoodsList(pageIndex, pageSize, tabIndex);
      console.log(`收到商品数据，数量: ${nextList.length}`);

      this.setData({
        goodsList: fresh ? nextList : this.data.goodsList.concat(nextList),
        goodsListLoadStatus: nextList.length < pageSize ? 2 : 0, // 如果返回的商品数量少于pageSize，说明没有更多数据了
      });

      if (!fresh) {
        this.goodListPagination.index = pageIndex + 1;
      } else {
        this.goodListPagination.index = 1;
      }
      this.goodListPagination.num = pageSize;

      console.log(`更新后商品总数: ${this.data.goodsList.length}`);
    } catch (err) {
      console.error('加载商品失败:', err);
      this.setData({ goodsListLoadStatus: 3 });
    }
  },

  goodListClickHandle(e) {
    const { index } = e.detail;
    const { spuId } = this.data.goodsList[index];
    wx.navigateTo({
      url: `/pages/goods/details/index?spuId=${spuId}`,
    });
  },

  goodListAddCartHandle() {
    Toast({
      context: this,
      selector: '#t-toast',
      message: '点击加入购物车',
    });
  },

  navToSearchPage() {
    wx.navigateTo({ url: '/pages/goods/search/index' });
  },

  navToActivityDetail({ detail }) {
    const { index: promotionID = 0 } = detail || {};
    wx.navigateTo({
      url: `/pages/promotion/promotion-detail/index?promotion_id=${promotionID}`,
    });
  },

  /**
   * 页面滚动监听
   */
  onPageScroll(e) {
    const scrollTop = e.scrollTop;
    // 当滚动超过300px时显示回到顶部按钮
    const shouldShow = scrollTop > 300;

    if (shouldShow !== this.data.showBackToTop) {
      this.setData({
        showBackToTop: shouldShow
      });
    }
  },

  /**
   * 回到顶部
   */
  backToTop() {
    wx.pageScrollTo({
      scrollTop: 0,
      duration: 300, // 滚动动画时长300ms
      success: () => {
        console.log('回到顶部成功');
      },
      fail: (err) => {
        console.error('回到顶部失败:', err);
      }
    });
  },
});
