import {
  getSearchHistory,
  getSearchPopular,
} from '../../../services/good/fetchSearchHistory';

Page({
  data: {
    historyWords: [],
    popularWords: [],
    searchValue: '',
    categoryId: null,      // 分类ID
    categoryName: '',      // 分类名称
    dialog: {
      title: '确认删除当前历史记录',
      showCancelButton: true,
      message: '',
    },
    dialogShow: false,
  },

  deleteType: 0,
  deleteIndex: '',

  onLoad(options) {
    // 接收分类参数
    const { categoryId, categoryName } = options;
    if (categoryId && categoryName) {
      this.setData({
        categoryId: parseInt(categoryId),
        categoryName: decodeURIComponent(categoryName),
      });
      console.log('分类搜索模式 -', categoryName, 'ID:', categoryId);
    }
  },

  onShow() {
    this.queryHistory();
    this.queryPopular();
  },

  async queryHistory() {
    try {
      const data = await getSearchHistory();
      const code = 'Success';
      if (String(code).toUpperCase() === 'SUCCESS') {
        const { historyWords = [] } = data;
        this.setData({
          historyWords,
        });
      }
    } catch (error) {
      console.error(error);
    }
  },

  async queryPopular() {
    try {
      const data = await getSearchPopular();
      const code = 'Success';
      if (String(code).toUpperCase() === 'SUCCESS') {
        const { popularWords = [] } = data;
        this.setData({
          popularWords,
        });
      }
    } catch (error) {
      console.error(error);
    }
  },

  confirm() {
    const { historyWords } = this.data;
    const { deleteType, deleteIndex } = this;
    historyWords.splice(deleteIndex, 1);
    if (deleteType === 0) {
      this.setData({
        historyWords,
        dialogShow: false,
      });
    } else {
      this.setData({ historyWords: [], dialogShow: false });
    }
  },

  close() {
    this.setData({ dialogShow: false });
  },

  handleClearHistory() {
    const { dialog } = this.data;
    this.deleteType = 1;
    this.setData({
      dialog: {
        ...dialog,
        message: '确认删除所有历史记录',
      },
      dialogShow: true,
    });
  },

  deleteCurr(e) {
    const { index } = e.currentTarget.dataset;
    const { dialog } = this.data;
    this.deleteIndex = index;
    this.setData({
      dialog: {
        ...dialog,
        message: '确认删除当前历史记录',
        deleteType: 0,
      },
      dialogShow: true,
    });
  },

  handleHistoryTap(e) {
    const { historyWords } = this.data;
    const { dataset } = e.currentTarget;
    const _searchValue = historyWords[dataset.index || 0] || '';
    if (_searchValue) {
      wx.navigateTo({
        url: `/pages/goods/result/index?searchValue=${_searchValue}`,
      });
    }
  },

  handleInputChange(e) {
    const { value } = e.detail || {};
    this.setData({
      searchValue: value || ''
    });
  },

  handleSubmit(e) {
    // 优先从事件详情中获取值，如果没有则从当前状态获取
    let searchValue = '';
    if (e.detail && e.detail.value) {
      searchValue = e.detail.value;
    } else {
      searchValue = this.data.searchValue;
    }

    if (!searchValue || searchValue.trim().length === 0) return;

    // 如果有分类ID，带上分类参数
    const { categoryId, categoryName } = this.data;
    let url = `/pages/goods/result/index?searchValue=${searchValue}`;

    if (categoryId && categoryName) {
      url += `&categoryId=${categoryId}&categoryName=${encodeURIComponent(categoryName)}`;
      console.log('分类搜索 -', categoryName, '关键词:', searchValue);
    }

    wx.navigateTo({ url });
  },

  // 清除分类限制，切换到全局搜索
  clearCategory() {
    this.setData({
      categoryId: null,
      categoryName: '',
    });
    wx.showToast({
      title: '已切换到全局搜索',
      icon: 'success',
      duration: 1500
    });
  },
});
