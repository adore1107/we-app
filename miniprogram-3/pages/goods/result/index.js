/* eslint-disable no-param-reassign */
import { getSearchResult } from '../../../services/good/fetchSearchResult';
import Toast from 'tdesign-miniprogram/toast/index';

Page({
  data: {
    goodsList: [],
    sortType: 'overall', // overall=综合, latest=最新, hot=热门
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
      // 获取搜索关键词、分类ID和排序类型
      const keywords = this.data.keywords;
      const categoryId = this.data.categoryId;
      const sortType = this.data.sortType;

      // 调用搜索API，带关键词、分类ID、排序和分页
      const params = {
        keyword: keywords,
        page: pageIndex,
        size: pageSize,
        sortType: sortType, // overall=综合, latest=最新, hot=热门
      };

      // 如果有分类ID，添加到参数中
      if (categoryId) {
        params.categoryId = categoryId;
        console.log(`分类搜索 - 分类ID: ${categoryId}, 关键词: ${keywords}, 排序: ${sortType}`);
      } else {
        console.log(`全局搜索 - 关键词: ${keywords}, 排序: ${sortType}`);
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
    const { sortType } = e.detail;
    console.log('排序类型切换:', sortType);

    this.setData({
      sortType: sortType,
    });

    // 重置分页并重新加载
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

});
