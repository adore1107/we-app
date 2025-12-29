import { fetchGoodsList } from '../../../services/good/fetchGoodsList';
import Toast from 'tdesign-miniprogram/toast/index';

const initFilters = {
  overall: 1,
  sorts: '',
  layout: 0,
};

Page({
  data: {
    goodsList: [],
    layout: 0,
    sorts: '',
    overall: 1,
    show: false,
    minVal: '',
    maxVal: '',
    filter: initFilters,
    hasLoaded: false,
    loadMoreStatus: 0,
    loading: true,
    categoryId: null,  // 分类ID
    categoryName: '',  // 分类名称
  },

  pageNum: 1,
  pageSize: 30,
  total: 0,

  handleFilterChange(e) {
    const { layout, overall, sorts } = e.detail;
    this.pageNum = 1;
    this.setData({
      layout,
      sorts,
      overall,
      loadMoreStatus: 0,
    });
    this.init(true);
  },

  generalQueryData(reset = false) {
    const { filter, keywords, minVal, maxVal, categoryId } = this.data;
    const { sorts, overall } = filter;

    // 计算要请求的页码
    let requestPage;
    if (reset) {
      // 重置时请求第0页
      requestPage = 0;
    } else {
      // 非重置时请求当前页码
      requestPage = this.pageNum;
    }

    console.log('分页计算 - reset:', reset, 'this.pageNum:', this.pageNum, 'requestPage:', requestPage);

    const params = {
      sort: 0, // 0 综合，1 价格
      page: requestPage,
      size: 30,
      keyword: keywords,
    };

    // 如果有分类ID，添加到参数中
    if (categoryId) {
      params.categoryId = categoryId;
    }

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

    return params;
  },

  async init(reset = true) {
    const { loadMoreStatus, goodsList = [] } = this.data;
    const params = this.generalQueryData(reset);

    console.log('=== 前端商品列表请求开始 ===');
    console.log('是否重置:', reset);
    console.log('请求前页码(this.pageNum):', this.pageNum);
    console.log('请求参数:', params);
    console.log('当前商品列表长度:', goodsList.length);

    // 防止重复请求
    if (loadMoreStatus !== 0) {
      console.log('阻止重复请求，当前loadMoreStatus:', loadMoreStatus);
      return;
    }
    this.setData({
      loadMoreStatus: 1,
      loading: true,
    });
    try {
      const result = await fetchGoodsList(params);
      console.log('商品列表API返回结果:', result);
      console.log('当前商品列表长度:', this.data.goodsList.length);
      console.log('是否重置加载:', reset);

      // 提取API响应数据（现在API返回完整Result格式）
      const apiData = result.data || result;
      console.log('提取的API数据:', apiData);

      // 处理MyBatis-Plus IPage格式的数据
      let spuList = [];
      let totalCount = 0;

      if (apiData && apiData.records) {
        // MyBatis-Plus IPage格式
        console.log('处理IPage格式数据，记录数量:', apiData.records.length);
        spuList = apiData.records.map((product, index) => {
          console.log(`商品${index}: ID=${product.id}, 名称=${product.name}, 批发价=${product.wholesalePrice}`);
          return {
            spuId: product.id,
            thumb: product.mainImage,
            title: product.name,
            price: product.wholesalePrice / 100, // 转换为元
            originPrice: product.retailPrice / 100, // 转换为元
            desc: product.description || '',
            tags: []
          };
        });
        totalCount = apiData.total || 0;
      } else if (apiData && Array.isArray(apiData)) {
        // 直接数组格式
        console.log('处理数组格式数据，记录数量:', apiData.length);
        spuList = apiData.map((product, index) => {
          console.log(`商品${index}: ID=${product.id}, 名称=${product.name}, 批发价=${product.wholesalePrice}`);
          return {
            spuId: product.id,
            thumb: product.mainImage,
            title: product.name,
            price: product.wholesalePrice / 100,
            originPrice: product.retailPrice / 100,
            desc: product.description || '',
            tags: []
          };
        });
        totalCount = apiData.length;
      }

      if (totalCount === 0 && reset) {
        this.total = totalCount;
        this.setData({
          emptyInfo: {
            tip: '抱歉，未找到相关商品',
          },
          hasLoaded: true,
          loadMoreStatus: 0,
          loading: false,
          goodsList: [],
        });
        return;
      }

        const _goodsList = reset ? spuList : [...this.data.goodsList, ...spuList];
      const _loadMoreStatus = _goodsList.length === totalCount ? 2 : 0;

      console.log('当前商品列表长度(合并前):', this.data.goodsList.length);
      console.log('新获取的商品数量:', spuList.length);
      console.log('合并后的商品列表长度:', _goodsList.length);
      console.log('总商品数量:', totalCount);

      // 检查是否有重复的商品ID
      const existingIds = reset ? [] : this.data.goodsList.map(item => item.spuId);
      const newIds = spuList.map(item => item.spuId);
      const duplicateIds = existingIds.filter(id => newIds.includes(id));
      if (duplicateIds.length > 0) {
        console.warn('发现重复的商品ID:', duplicateIds);
      }

      // 更新页码：请求成功后才更新页码
      const oldPageNum = this.pageNum;
      if (reset) {
        // 重置时，已经请求了第0页，下次应该请求第1页
        this.pageNum = 1;
        console.log('重置分页:', oldPageNum, '->', this.pageNum, '下次将请求第', this.pageNum, '页');
      } else {
        // 非重置时，已经请求了currentPage页，下次请求currentPage+1页
        this.pageNum = (this.pageNum || 0) + 1;
        console.log('分页递增:', oldPageNum, '->', this.pageNum, '下次将请求第', this.pageNum, '页');
      }

      this.total = totalCount;
      this.setData({
        goodsList: _goodsList,
        loadMoreStatus: _loadMoreStatus,
      });
    } catch (error) {
      console.error('加载商品列表失败:', error);
      this.setData({
        loading: false,
        loadMoreStatus: 3, // 错误状态
      });
    }
    this.setData({
      hasLoaded: true,
      loading: false,
    });
  },

  onLoad(options) {
    console.log('商品列表页面参数:', options);

    // 保存分类信息
    if (options.categoryId) {
      this.setData({
        categoryId: parseInt(options.categoryId),
        categoryName: options.categoryName || ''
      });
      console.log('当前分类ID:', options.categoryId, '分类名称:', options.categoryName);
    }

    // 重置分页状态
    this.pageNum = 0; // 修改为0，与MyBatis-Plus分页保持一致
    this.total = 0;

    this.init(true);
  },

  onReachBottom() {
    const { goodsList, loadMoreStatus } = this.data;
    const { total = 0 } = this;

    console.log('=== 触底上拉加载更多 ===');
    console.log('当前商品数量:', goodsList.length);
    console.log('总商品数量:', total);
    console.log('当前页码:', this.pageNum);
    console.log('当前加载状态:', loadMoreStatus);

    // 防止重复加载
    if (loadMoreStatus !== 0) {
      console.log('正在加载中，跳过触底加载');
      return;
    }

    if (goodsList.length === total) {
      console.log('已加载全部商品，停止加载');
      this.setData({
        loadMoreStatus: 2,
      });
      return;
    }

    console.log('触发加载下一页');
    this.init(false);
  },

  handleAddCart() {
    Toast({
      context: this,
      selector: '#t-toast',
      message: '点击加购',
    });
  },

  tagClickHandle() {
    Toast({
      context: this,
      selector: '#t-toast',
      message: '点击标签',
    });
  },

  gotoGoodsDetail(e) {
    const { index } = e.detail;
    const { spuId } = this.data.goodsList[index];
    wx.navigateTo({
      url: `/pages/goods/details/index?spuId=${spuId}`,
    });
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
