/* eslint-disable no-param-reassign */
import { getSearchResult } from '../../../services/good/fetchSearchResult';
import Toast from 'tdesign-miniprogram/toast/index';

const initFilters = {
  overall: 1,
  sorts: '',
};

Page({
  data: {
    goodsList: [],
    sorts: '',
    overall: 1,
    show: false,
    minVal: '',
    maxVal: '',
    minSalePriceFocus: false,
    maxSalePriceFocus: false,
    filter: initFilters,
    hasLoaded: false,
    keywords: '',
    loadMoreStatus: 0,
    loading: true,
  },

  total: 0,
  pageNum: 0, // 从0开始，和首页保持一致
  pageSize: 30,
  goodListPagination: {
    index: 0,
    num: 20, // 每页20个，和首页一致
  },

  onLoad(options) {
    const { searchValue = '', categoryId = '', categoryName = '' } = options || {};

    // 如果有分类参数，说明是分类内搜索
    if (categoryId && categoryName) {
      console.log(`分类搜索 - 分类: ${decodeURIComponent(categoryName)}, 关键词: ${searchValue}`);
      this.setData({
        keywords: searchValue,
        categoryId: parseInt(categoryId),
        categoryName: decodeURIComponent(categoryName),
      }, () => {
        this.loadGoodsList(true);
      });
    } else {
      console.log(`全局搜索 - 关键词: ${searchValue}`);
      this.setData({
        keywords: searchValue,
      }, () => {
        this.loadGoodsList(true);
      });
    }
  },

  generalQueryData(reset = false) {
    const { filter = {}, keywords = '', minVal = '', maxVal = '' } = this.data;
    const { pageNum, pageSize } = this;
    const { sorts = '', overall = 1 } = filter;
    const params = {
      sort: 0, // 0 综合，1 价格
      page: reset ? 1 : pageNum, // 重置时用第1页，否则用当前页码
      size: 30,
      keyword: keywords,
    };

    if (sorts) {
      params.sort = 1;
      params.sortType = sorts === 'desc' ? 1 : 0;
    }
    if (overall) {
      params.sort = 0;
    } else {
      params.sort = 1;
    }
    params.minPrice = minVal ? minVal * 100 : 0;
    params.maxPrice = maxVal ? maxVal * 100 : undefined;
    if (reset) return params;
    return {
      ...params,
      pageNum: pageNum + 1,
      pageSize,
    };
  },

  async loadGoodsList(fresh = false) {
    if (fresh) {
      wx.pageScrollTo({
        scrollTop: 0,
      });
    }

    this.setData({ goodsListLoadStatus: 1 });

    const pageSize = this.goodListPagination.num;
    let pageIndex = fresh ? 0 : this.goodListPagination.index;

    try {
      // 获取搜索关键词和分类ID
      const keywords = this.data.keywords;
      const categoryId = this.data.categoryId;

      // 调用搜索API，带关键词、分类ID和分页
      const params = {
        keyword: keywords,
        page: pageIndex,
        size: pageSize
      };

      // 如果有分类ID，添加到参数中
      if (categoryId) {
        params.categoryId = categoryId;
        console.log(`分类搜索 - 分类ID: ${categoryId}, 关键词: ${keywords}`);
      } else {
        console.log(`全局搜索 - 关键词: ${keywords}`);
      }

      const nextList = await getSearchResult(params);

      this.setData({
        goodsList: fresh ? nextList.spuList : this.data.goodsList.concat(nextList.spuList),
        goodsListLoadStatus: (nextList.spuList?.length || 0) < pageSize ? 2 : 0, // 如果返回的商品数量少于pageSize，说明没有更多数据了
      });

      if (!fresh) {
        this.goodListPagination.index = pageIndex + 1;
      } else {
        this.goodListPagination.index = 1;
      }
    } catch (error) {
      console.error('搜索商品列表失败:', error);
      this.setData({
        goodsListLoadStatus: 0,
      });
    }
  },

  handleCartTap() {
    wx.switchTab({
      url: '/pages/cart/index',
    });
  },

  handleSubmit() {
    // 重置分页
    this.goodListPagination = { index: 0, num: 20 };
    this.setData(
      {
        goodsList: [],
        goodsListLoadStatus: 0,
      },
      () => {
        this.loadGoodsList(true);
      },
    );
  },

  onReachBottom() {
    console.log('搜索页触底了！当前商品数量:', this.data.goodsList.length);
    console.log('加载状态:', this.data.goodsListLoadStatus);

    if (this.data.goodsListLoadStatus === 0) {
      console.log('开始加载更多搜索商品...');
      this.loadGoodsList();
    } else {
      console.log('正在加载或已完成，不再请求');
    }
  },

  handleAddCart() {
    Toast({
      context: this,
      selector: '#t-toast',
      message: '点击加购',
    });
  },

  gotoGoodsDetail(e) {
    const { index } = e.detail;
    const { spuId } = this.data.goodsList[index];
    wx.navigateTo({
      url: `/pages/goods/details/index?spuId=${spuId}`,
    });
  },

  handleFilterChange(e) {
    const { overall, sorts } = e.detail;
    const { total } = this;
    const _filter = {
      sorts,
      overall,
    };
    this.setData({
      filter: _filter,
      sorts,
      overall,
    });

    this.pageNum = 1;
    this.setData(
      {
        goodsList: [],
        loadMoreStatus: 0,
      },
      () => {
        total && this.init(true);
      },
    );
  },

  showFilterPopup() {
    this.setData({
      show: true,
    });
  },

  showFilterPopupClose() {
    this.setData({
      show: false,
    });
  },

  onMinValAction(e) {
    const { value } = e.detail;
    this.setData({ minVal: value });
  },

  onMaxValAction(e) {
    const { value } = e.detail;
    this.setData({ maxVal: value });
  },

  reset() {
    this.setData({ minVal: '', maxVal: '' });
  },

  confirm() {
    const { minVal, maxVal } = this.data;
    let message = '';
    if (minVal && !maxVal) {
      message = `价格最小是${minVal}`;
    } else if (!minVal && maxVal) {
      message = `价格范围是0-${minVal}`;
    } else if (minVal && maxVal && minVal <= maxVal) {
      message = `价格范围${minVal}-${this.data.maxVal}`;
    } else {
      message = '请输入正确范围';
    }
    if (message) {
      Toast({
        context: this,
        selector: '#t-toast',
        message,
      });
    }
    this.pageNum = 1;
    this.setData(
      {
        show: false,
        minVal: '',
        goodsList: [],
        loadMoreStatus: 0,
        maxVal: '',
      },
      () => {
        this.init();
      },
    );
  },
});
