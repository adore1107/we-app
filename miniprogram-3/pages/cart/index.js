import Dialog from 'tdesign-miniprogram/dialog/index';
import Toast from 'tdesign-miniprogram/toast/index';
import { fetchCartGroupData } from '../../services/cart/cart';

Page({
  data: {
    favoritesData: {
      isNotEmpty: false,
      favoritesList: []
    },
    isEditMode: false, // 是否处于编辑模式
    batchData: {
      isAllSelected: false,
      selectedCount: 0,
    },
  },

  // 调用自定义tabbar的init函数，使页面与tabbar激活状态保持一致
  onShow() {
    this.getTabBar().init();
    // 页面显示时也刷新数据
    this.refreshData();
    console.log('🔍 购物车页面 onShow，当前 isEditMode =', this.data.isEditMode);
  },

  onLoad() {
    this.refreshData();
    // 监听收藏状态变化事件
    this.setupFavoriteListener();
  },

  onUnload() {
    // 页面卸载时移除事件监听
    this.removeFavoriteListener();
  },

  // 设置收藏状态监听
  setupFavoriteListener() {
    try {
      const app = getApp();
      const { EVENTS } = require('../../utils/globalEvents');

      app.globalData.globalEvents.on(EVENTS.FAVORITE_CHANGED, this.handleFavoriteChanged.bind(this));
      console.log('🔔 购物车页面已设置收藏状态监听');
    } catch (error) {
      console.error('❌ 设置收藏监听失败:', error);
    }
  },

  // 移除收藏状态监听
  removeFavoriteListener() {
    try {
      const app = getApp();
      const { EVENTS } = require('../../utils/globalEvents');

      app.globalData.globalEvents.off(EVENTS.FAVORITE_CHANGED, this.handleFavoriteChanged);
      console.log('🔕 购物车页面已移除收藏状态监听');
    } catch (error) {
      console.error('❌ 移除收藏监听失败:', error);
    }
  },

  // 处理收藏状态变化
  handleFavoriteChanged(data) {
    console.log('🎉 购物车页面收到收藏状态变化通知:', data);
    console.log('通知详情:', {
      userId: data.userId,
      productId: data.productId,
      isFavorited: data.isFavorited,
      timestamp: data.timestamp
    });

    // 立即刷新，不需要延迟，因为我们已经等待了后端API完成
    console.log('开始刷新收藏列表...');
    this.refreshData().then(() => {
      console.log('✅ 收藏列表刷新完成');
    }).catch((error) => {
      console.error('❌ 收藏列表刷新失败:', error);
    });
  },

  refreshData() {
    console.log('开始获取购物车页面收藏数据...');

    this.getFavoritesData().then((result) => {
      console.log('购物车页面获取数据响应:', result);

      // 提取API响应数据（现在API返回完整Result格式）
      let favoritesData = result.data || result;

      console.log('解析后的数据:', favoritesData);

      let isEmpty = true;

      // 确保favoritesData存在
      if (!favoritesData) {
        console.log('数据为空，设置默认空数据');
        this.setData({
          favoritesData: {
            isNotEmpty: false,
            favoritesList: []
          }
        });
        return;
      }

      // 处理收藏数据，简化逻辑
      if (favoritesData.favoritesList) {
        console.log('处理收藏列表，数量:', favoritesData.favoritesList.length);

        favoritesData.favoritesList = favoritesData.favoritesList.map((goods) => {
          // 计算显示价格
          const wholesalePriceNum = parseInt(goods.wholesalePrice || goods.price * 0.7);
          const displayPrice = (wholesalePriceNum / 100).toFixed(2);

          // 移除购物车相关字段，保留收藏需要的信息
          return {
            spuId: goods.spuId,
            skuId: goods.skuId,
            title: goods.title,
            image: goods.image,
            price: goods.price,
            wholesalePrice: wholesalePriceNum.toString(),
            displayPrice: displayPrice, // 格式化后的显示价格
            minOrderQuantity: goods.minOrderQuantity || 100, // 最小起订量
            unit: goods.unit || '件',
            specifications: goods.specifications || '常规规格',
            storeId: goods.storeId,
            storeName: goods.storeName || '浙江宋家纺织',
            favoriteTime: goods.favoriteTime || new Date().toLocaleDateString()
          };
        });
        isEmpty = favoritesData.favoritesList.length === 0;
      }

      favoritesData.isNotEmpty = !isEmpty;
      console.log('设置页面数据:', favoritesData);

      this.setData({
        favoritesData: favoritesData
      });
    }).catch(err => {
      console.error('购物车页面获取收藏数据失败:', err);
      // 设置空数据避免页面报错
      this.setData({
        favoritesData: {
          isNotEmpty: false,
          favoritesList: []
        }
      });
    });
  },

  findFavoriteGoods(spuId, skuId) {
    const { favoritesList } = this.data.favoritesData;
    return favoritesList.find(goods => goods.spuId === spuId && goods.skuId === skuId);
  },

  // 获取收藏数据（从后端API读取）
  getFavoritesData() {
    return fetchCartGroupData();
  },

  // 删除收藏商品（调用后端API）
  deleteFavoriteService({ spuId, skuId }) {
    try {
      // 使用统一的用户信息获取方式（与收藏列表一致）
      const app = getApp();
      const authManager = app.globalData.authManager;
      const currentUser = authManager.getCurrentUser();

      console.log('删除收藏，用户信息:', currentUser);

      if (!currentUser || !currentUser.id) {
        Toast({
          context: this,
          selector: '#t-toast',
          message: '请先登录'
        });
        return Promise.reject('用户未登录');
      }

      // 调用后端API删除收藏
      const { removeFavorite } = require('../../services/favorite/favorite');

      console.log('调用删除收藏API:', { userId: currentUser.id, productId: spuId });

      return removeFavorite({
        userId: currentUser.id,
        productId: spuId
      }).then(() => {
        console.log('✅ 后端删除收藏成功:', spuId);
      }).catch((error) => {
        console.error('❌ 后端删除收藏失败:', error);
        throw error;
      });

    } catch (err) {
      console.error('删除收藏失败:', err);
      return Promise.reject(err);
    }
  },

  onGoodsClick(e) {
    const { spuId, storeId } = e.detail.goods;
    wx.navigateTo({
      url: `/pages/goods/details/index?spuId=${spuId}&storeId=${storeId}`,
    });
  },

  // 处理编辑模式变化
  onEditModeChange(e) {
    const { isEditMode } = e.detail;
    this.setData({
      isEditMode,
      'batchData.isAllSelected': false,
      'batchData.selectedCount': 0,
    });
  },

  // 处理选中状态变化
  onCheckChange(e) {
    const { selectedCount, isAllSelected } = e.detail;
    this.setData({
      'batchData.selectedCount': selectedCount,
      'batchData.isAllSelected': isAllSelected,
    });
  },

  // 处理全选变化（从组件触发）
  onSelectAllChange(e) {
    const { isAllSelected, selectedCount } = e.detail;
    this.setData({
      'batchData.isAllSelected': isAllSelected,
      'batchData.selectedCount': selectedCount,
    });
  },

  // 点击页面级的全选按钮
  onToggleSelectAll() {
    // 触发组件的全选方法
    const favoritesGroup = this.selectComponent('#favorites-group');
    if (favoritesGroup) {
      favoritesGroup.toggleSelectAll();
    } else {
      console.error('找不到 favorites-group 组件');
    }
  },

  // 点击页面级的批量删除按钮
  onConfirmBatchDelete() {
    // 触发组件的批量删除方法
    const favoritesGroup = this.selectComponent('#favorites-group');
    if (favoritesGroup) {
      favoritesGroup.onBatchDelete();
    } else {
      console.error('找不到 favorites-group 组件');
    }
  },

  onGoodsDelete(e) {
    const {
      goods: { spuId, skuId, title },
    } = e.detail;

    console.log('购物车页面收到删除事件:', { spuId, skuId, title });

    Dialog.confirm({
      content: `确认取消收藏"${title.length > 8 ? `${title.slice(0, 8)}...` : title}"吗?`,
      confirmBtn: '确定',
      cancelBtn: '取消',
    }).then(() => {
      wx.showLoading({
        title: '处理中...',
        mask: true
      });

      this.deleteFavoriteService({ spuId, skuId })
        .then(() => {
          const currentUser = getApp().globalData.authManager.getCurrentUser();
          console.log('✅ 删除收藏成功，用户ID:', currentUser.id, '商品ID:', spuId, '刷新页面数据');

          // 通知其他页面收藏状态已改变
          this.notifyFavoriteChanged(currentUser.id, spuId, false);

          // 刷新当前页面数据
          return this.refreshData();
        })
        .then(() => {
          wx.hideLoading();
          Toast({ context: this, selector: '#t-toast', message: '已取消收藏' });
        })
        .catch((error) => {
          console.error('删除收藏失败:', error);
          wx.hideLoading();
          Toast({ context: this, selector: '#t-toast', message: '删除失败，请重试' });
        });
    });
  },

  // 批量删除收藏
  onBatchDelete(e) {
    const { items } = e.detail;
    console.log('批量删除收藏:', items);

    wx.showLoading({
      title: '处理中...',
      mask: true
    });

    // 创建批量删除的Promise数组
    const deletePromises = items.map(item => {
      return this.deleteFavoriteService({ spuId: item.spuId, skuId: item.skuId });
    });

    // 并行执行所有删除操作
    Promise.all(deletePromises)
      .then(() => {
        const currentUser = getApp().globalData.authManager.getCurrentUser();

        // 通知所有删除的商品状态变更
        items.forEach(item => {
          this.notifyFavoriteChanged(currentUser.id, item.spuId, false);
        });

        // 刷新当前页面数据
        return this.refreshData();
      })
      .then(() => {
        wx.hideLoading();
        Toast({
          context: this,
          selector: '#t-toast',
          message: `已取消收藏${items.length}件商品`
        });
      })
      .catch((error) => {
        console.error('批量删除收藏失败:', error);
        wx.hideLoading();
        Toast({ context: this, selector: '#t-toast', message: '删除失败，请重试' });
      });
  },

  // 通知其他页面收藏状态已改变
  notifyFavoriteChanged(productId, isFavorited) {
    try {
      // 使用全局事件通知
      const globalEvents = require('../../utils/globalEvents').default;

      globalEvents.emit('favoriteChanged', {
        productId: productId,
        isFavorited: isFavorited,
        timestamp: Date.now()
      });

      console.log('购物车页面已发送收藏状态变化通知:', { productId, isFavorited });
    } catch (error) {
      console.error('通知收藏状态变化失败:', error);
    }
  },

  // 一键询价功能
  onInquiry() {
    const { favoritesList } = this.data.favoritesData;
    if (favoritesList.length === 0) {
      Toast({
        context: this,
        selector: '#t-toast',
        message: '请先添加收藏商品',
      });
      return;
    }

    // 将收藏商品信息保存到本地存储，用于询价页面
    wx.setStorageSync('inquiry.favoritesList', JSON.stringify(favoritesList));

    wx.showModal({
      title: '批量询价',
      content: `已选择${favoritesList.length}个商品进行询价，我们将尽快为您报价`,
      confirmText: '确定询价',
      success: (res) => {
        if (res.confirm) {
          // 这里可以跳转到询价页面或直接调用询价接口
          Toast({
            context: this,
            selector: '#t-toast',
            message: '询价请求已提交，客服将在24小时内联系您',
          });
        }
      }
    });
  },
  onGotoHome() {
    wx.switchTab({ url: '/pages/home/home' });
  },
});
