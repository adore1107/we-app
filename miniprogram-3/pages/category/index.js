import { getCategoryTree } from '../../services/good/fetchCategoryList';
Page({
  data: {
    mainCategories: [],           // 一级分类列表
    subCategories: [],            // 当前选中的二级分类列表
    currentMainIndex: 0,          // 当前选中的一级分类索引
    currentSubIndex: 0,           // 当前选中的二级分类索引
    currentMainCategory: null,    // 当前选中的一级分类
    currentSubCategory: null,     // 当前选中的二级分类
    currentProducts: [],
    loadMoreStatus: 0,
    hasLoaded: false,
    showBackToTop: false,         // 是否显示回到顶部按钮
    scrollIntoView: '',           // 滚动到指定元素
  },

  // 分页参数
  pageNum: 0,
  pageSize: 20,

  // 预设的渐变色彩方案和对应的图标
  categoryStyles: [
    {
      start: '#667eea',
      end: '#764ba2',
      icon: '🛏️',  // 凝胶床垫
      keywords: ['床垫', '凝胶']
    },
    {
      start: '#f093fb',
      end: '#f5576c',
      icon: '🧵',  // 纱布面料
      keywords: ['纱布', '面料']
    },
    {
      start: '#4facfe',
      end: '#00f2fe',
      icon: '🛋️',  // 家纺
      keywords: ['家纺']
    },
    {
      start: '#43e97b',
      end: '#38f9d7',
      icon: '🔬',  // 功能性
      keywords: ['功能性']
    },
    {
      start: '#fa709a',
      end: '#fee140',
      icon: '✨',  // 其他通用
      keywords: []
    },
    {
      start: '#30cfd0',
      end: '#330867',
      icon: '🎨',  // 图案
      keywords: ['图案', '印花']
    },
    {
      start: '#a8edea',
      end: '#fed6e3',
      icon: '👕',  // 服装
      keywords: ['服装', '衣服']
    },
    {
      start: '#ff9a9e',
      end: '#fecfef',
      icon: '🏠',  // 家居
      keywords: ['家居', '家']
    },
    {
      start: '#fbc2eb',
      end: '#a6c1ee',
      icon: '🌟',  // 精品
      keywords: ['精品', '高端']
    },
    {
      start: '#fdcbf1',
      end: '#e6dee9',
      icon: '📦',  // 包装
      keywords: ['包装', '材料']
    },
  ],

  async init() {
    try {
      const result = await getCategoryTree();

      // 提取分类数据（API返回树形结构）
      const mainCategories = result.data || result;

      // 智能匹配图标和颜色（只为一级分类匹配）
      const mainCategoriesWithStyles = mainCategories.map((category, index) => {
        let selectedStyle = this.categoryStyles[0]; // 默认样式

        // 根据分类名称关键词匹配最合适的图标
        const categoryName = category.name.toLowerCase();
        let bestMatch = 0;
        let bestScore = 0;

        this.categoryStyles.forEach((style, styleIndex) => {
          let score = 0;
          style.keywords.forEach(keyword => {
            if (categoryName.includes(keyword.toLowerCase())) {
              score += 10; // 关键词匹配得分
            }
          });

          // 顺序匹配得分（确保不同的颜色）
          score += (index % this.categoryStyles.length === styleIndex) ? 5 : 0;

          if (score > bestScore) {
            bestScore = score;
            bestMatch = styleIndex;
          }
        });

        selectedStyle = this.categoryStyles[bestMatch];

        return {
          ...category,
          gradientStart: selectedStyle.start,
          gradientEnd: selectedStyle.end,
          categoryIcon: selectedStyle.icon,
        };
      });

      // 设置第一个一级分类为默认选中
      const firstMainCategory = mainCategoriesWithStyles[0];
      const firstSubCategories = firstMainCategory?.children || [];
      const firstSubCategory = firstSubCategories[0];

      this.setData({
        mainCategories: mainCategoriesWithStyles,
        subCategories: firstSubCategories,
        currentMainIndex: 0,
        currentSubIndex: 0,
        currentMainCategory: firstMainCategory,
        currentSubCategory: firstSubCategory,
      });

      // 如果有二级分类，加载第一个二级分类的商品；否则加载一级分类的商品
      const categoryId = firstSubCategory ? firstSubCategory.id : firstMainCategory.id;
      await this.loadCategoryProducts(categoryId);
    } catch (error) {
      console.error('获取分类数据失败:', error);
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      });
    }
  },

  // 加载分类商品
  async loadCategoryProducts(categoryId, reset = true) {
    if (!reset && this.data.loadMoreStatus !== 0) {
      return;
    }

    this.setData({
      loadMoreStatus: 1,
    });

    if (reset) {
      this.pageNum = 0;
      this.setData({
        currentProducts: [],
        hasLoaded: false,
        scrollIntoView: 'top-anchor', // 切换分类时自动滚动到顶部
      });

      // 立即清空scrollIntoView，避免影响后续滚动
      setTimeout(() => {
        this.setData({ scrollIntoView: '' });
      }, 50);
    }

    try {
      const { fetchGoodsList } = require('../../services/good/fetchGoodsList');
      const params = {
        categoryId: categoryId,
        page: reset ? 0 : this.pageNum,
        size: this.pageSize,
      };

      const result = await fetchGoodsList(params);

      // 提取API响应数据（现在API返回完整Result格式：{code, message, data}）
      const apiData = result.data || result;

      let products = [];
      if (apiData && apiData.records) {
        products = apiData.records.map(product => ({
          spuId: product.id,
          thumb: product.mainImage || product.main_image,
          title: product.name,
          tags: product.tags || [],
        }));
      }

      const newProducts = reset ? products : [...this.data.currentProducts, ...products];
      const totalCount = apiData.total || 0;
      const loadMoreStatus = newProducts.length >= totalCount ? 2 : 0;

      if (reset) {
        this.pageNum = 1;
      } else {
        this.pageNum++;
      }

      this.setData({
        currentProducts: newProducts,
        loadMoreStatus,
        hasLoaded: true,
      });
    } catch (error) {
      console.error('加载分类商品失败:', error);
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      });
      this.setData({
        loadMoreStatus: 3,
        hasLoaded: true,
      });
    }
  },

  onShow() {
    this.getTabBar().init();
  },
  onCategoryTap(event) {
    const { item } = event.currentTarget.dataset;
    console.log('点击分类:', item);

    if (item && item.id) {
      console.log('跳转到商品列表, 分类ID:', item.id, '分类名称:', item.name);
      wx.navigateTo({
        url: `/pages/goods/list/index?categoryId=${item.id}&categoryName=${encodeURIComponent(item.name || '')}`,
      });
    } else {
      console.log('没有分类ID，跳转到全部商品');
      wx.navigateTo({
        url: '/pages/goods/list/index',
      });
    }
  },

  onChange(event) {
    console.log('分类onChange事件触发:', event);
    const { item } = event.detail;
    console.log('点击分类:', item);

    if (item && item.id) {
      console.log('跳转到商品列表, 分类ID:', item.id, '分类名称:', item.name);
      wx.navigateTo({
        url: `/pages/goods/list/index?categoryId=${item.id}&categoryName=${encodeURIComponent(item.name || '')}`,
      });
    } else {
      console.log('没有分类ID，跳转到全部商品');
      wx.navigateTo({
        url: '/pages/goods/list/index',
      });
    }
  },

  // 一级分类选择事件
  onMainCategorySelect(e) {
    const { index } = e.currentTarget.dataset;
    const mainCategory = this.data.mainCategories[index];

    console.log('选择一级分类:', mainCategory, '索引:', index);

    if (index !== this.data.currentMainIndex) {
      const subCategories = mainCategory.children || [];
      const firstSubCategory = subCategories[0];

      // 先更新一级分类索引（不会引起DOM结构变化）
      this.setData({
        currentMainIndex: index,
        currentMainCategory: mainCategory,
      });

      // 使用 nextTick 延迟更新二级分类，避免同时更新导致抖动
      wx.nextTick(() => {
        this.setData({
          currentSubIndex: 0,
          subCategories: subCategories,
          currentSubCategory: firstSubCategory,
        });
      });

      // 如果有二级分类，加载第一个二级分类的商品；否则加载一级分类的商品
      const categoryId = firstSubCategory ? firstSubCategory.id : mainCategory.id;
      this.loadCategoryProducts(categoryId, true);
    }
  },

  // 二级分类选择事件
  onSubCategorySelect(e) {
    const { index } = e.currentTarget.dataset;
    const subCategory = this.data.subCategories[index];

    console.log('选择二级分类:', subCategory, '索引:', index);

    if (index !== this.data.currentSubIndex) {
      this.setData({
        currentSubIndex: index,
        currentSubCategory: subCategory,
      });

      // 加载二级分类的商品
      this.loadCategoryProducts(subCategory.id, true);
    }
  },

  // 商品点击事件
  onProductTap(e) {
    const { product } = e.currentTarget.dataset;
    console.log('点击商品:', product);

    wx.navigateTo({
      url: `/pages/goods/details/index?spuId=${product.spuId}`,
    });
  },


  // 搜索点击事件 - 在当前分类中搜索
  onSearchTap() {
    // 优先使用二级分类，否则使用一级分类
    const currentCategory = this.data.currentSubCategory || this.data.currentMainCategory;

    if (currentCategory && currentCategory.id) {
      console.log('在分类中搜索:', currentCategory.name, 'ID:', currentCategory.id);
      wx.navigateTo({
        url: `/pages/goods/search/index?categoryId=${currentCategory.id}&categoryName=${encodeURIComponent(currentCategory.name)}`,
      });
    } else {
      // 如果没有选中分类，全局搜索
      wx.navigateTo({
        url: '/pages/goods/search/index',
      });
    }
  },

  // 上拉加载更多
  onReachBottom() {
    const currentCategory = this.data.currentSubCategory || this.data.currentMainCategory;
    if (this.data.loadMoreStatus === 0 && currentCategory) {
      this.loadCategoryProducts(currentCategory.id, false);
    }
  },

  // 点击重试
  onRetryLoad() {
    const currentCategory = this.data.currentSubCategory || this.data.currentMainCategory;
    if (currentCategory && this.data.loadMoreStatus === 3) {
      this.loadCategoryProducts(currentCategory.id, true);
    }
  },

  // 监听scroll-view滚动（scroll-view组件的滚动事件）
  onPageScroll(e) {
    // scroll-view的bindscroll事件
    if (!e || !e.detail) {
      return;
    }

    const scrollTop = e.detail.scrollTop;
    // 当滚动超过300px时显示回到顶部按钮
    const shouldShow = scrollTop > 300;

    // 只更新按钮显示状态，不要更新scrollTopValue，避免循环导致抖动
    if (this.data.showBackToTop !== shouldShow) {
      this.setData({
        showBackToTop: shouldShow
      });
    }
  },

  // 回到顶部
  backToTop() {
    console.log('=== 点击回到顶部按钮 ===');

    // 直接设置为top-anchor滚动到顶部（无动画，不会抖动）
    this.setData({
      scrollIntoView: 'top-anchor'
    });

    // 50ms后清空，确保下次点击能再次触发
    setTimeout(() => {
      this.setData({
        scrollIntoView: ''
      });
    }, 50);

    console.log('已触发回到顶部');
  },

  onLoad() {
    this.init(true);
  },
});
