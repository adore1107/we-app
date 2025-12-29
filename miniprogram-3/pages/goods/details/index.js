import Toast from 'tdesign-miniprogram/toast/index';
import { fetchGood } from '../../../services/good/fetchGood';

import { cdnBase } from '../../../config/index';

const imgPrefix = `${cdnBase}/`;

const recLeftImg = `${imgPrefix}common/rec-left.png`;
const recRightImg = `${imgPrefix}common/rec-right.png`;
const obj2Params = (obj = {}, encode = false) => {
  const result = [];
  Object.keys(obj).forEach((key) => result.push(`${key}=${encode ? encodeURIComponent(obj[key]) : obj[key]}`));

  return result.join('&');
};

Page({
  data: {
    isShowPromotionPop: false,
    activityList: [],
    recLeftImg,
    recRightImg,
    details: {},
    // 收藏相关状态
    isFavorited: false,
    favoriteButtonText: '收藏商品',
    // 用户登录状态
    userInfo: null,

    // 评论相关数据
    comments: [],
    commentStats: {
      count: 0,
      avgRating: 0
    },
    ratingStars: '☆☆☆☆☆',
    commentPage: 0, // 当前页码
    commentPageSize: 5, // 每页显示数量
    hasMoreComments: false, // 是否还有更多评论

    // 评论弹窗状态
    showDialog: false,
    rating: 5,
    commentContent: '',
    goodsTabArray: [
      {
        name: '商品',
        value: '', // 空字符串代表置顶
      },
      {
        name: '详情',
        value: 'goods-page',
      },
    ],
    storeLogo: `${imgPrefix}common/store-logo.png`,
    storeName: '云mall标准版旗舰店',
    jumpArray: [
      {
        title: '首页',
        url: '/pages/home/home',
        iconName: 'home',
      },
      {
        title: '购物车',
        url: '/pages/cart/index',
        iconName: 'cart',
        showCartNum: true,
      },
    ],
    isStock: true,
    cartNum: 0,
    soldout: false,
    buttonType: 1,
    buyNum: 1,
    selectedAttrStr: '',
    skuArray: [],
    primaryImage: '',
    specImg: '',
    isSpuSelectPopupShow: false,
    isAllSelectedSku: false,
    buyType: 0,
    outOperateStatus: false, // 是否外层加入购物车
    operateType: 0,
    selectSkuSellsPrice: 0,
    maxLinePrice: 0,
    minSalePrice: 0,
    maxSalePrice: 0,
    list: [],
    spuId: '',
    navigation: { type: 'fraction' },
    current: 0,
    autoplay: true,
    duration: 500,
    interval: 5000,
    soldNum: 0, // 已售数量
  },

  handlePopupHide() {
    this.setData({
      isSpuSelectPopupShow: false,
    });
  },

  showSkuSelectPopup(type) {
    this.setData({
      buyType: type || 0,
      outOperateStatus: type >= 1,
      isSpuSelectPopupShow: true,
    });
  },

  toInquiry() {
    // 直接显示询价确认弹窗
    wx.showModal({
      title: '批量询价',
      content: `是否要询价 ${this.data.details.title || '此商品'}？我们将尽快为您报价。`,
      confirmText: '确认询价',
      success: (res) => {
        if (res.confirm) {
          Toast({
            context: this,
            selector: '#t-toast',
            message: '询价请求已提交，客服将在24小时内联系您',
          });
        }
      }
    });
  },

  toAddFavorite() {
    // 已移除旧的本地存储收藏逻辑，统一使用下面新的收藏方法
  },

  toNav(e) {
    const { url } = e.detail;
    wx.switchTab({
      url: url,
    });
  },

  showCurImg(e) {
    const { index } = e.detail;
    const { images } = this.data.details;
    wx.previewImage({
      current: images[index],
      urls: images, // 需要预览的图片http链接列表
    });
  },

  onPageScroll({ scrollTop }) {
    const goodsTab = this.selectComponent('#goodsTab');
    goodsTab && goodsTab.onScroll(scrollTop);
  },

  chooseSpecItem(e) {
    const { specList } = this.data.details;
    const { selectedSku, isAllSelectedSku } = e.detail;
    if (!isAllSelectedSku) {
      this.setData({
        selectSkuSellsPrice: 0,
      });
    }
    this.setData({
      isAllSelectedSku,
    });
    this.getSkuItem(specList, selectedSku);
  },

  getSkuItem(specList, selectedSku) {
    const { skuArray, primaryImage } = this.data;
    const selectedSkuValues = this.getSelectedSkuValues(specList, selectedSku);
    let selectedAttrStr = ` 件  `;
    selectedSkuValues.forEach((item) => {
      selectedAttrStr += `，${item.specValue}  `;
    });
    // eslint-disable-next-line array-callback-return
    const skuItem = skuArray.filter((item) => {
      let status = true;
      (item.specInfo || []).forEach((subItem) => {
        if (!selectedSku[subItem.specId] || selectedSku[subItem.specId] !== subItem.specValueId) {
          status = false;
        }
      });
      if (status) return item;
    });
    this.selectSpecsName(selectedSkuValues.length > 0 ? selectedAttrStr : '');
    if (skuItem) {
      this.setData({
        selectItem: skuItem,
        selectSkuSellsPrice: skuItem.price || 0,
      });
    } else {
      this.setData({
        selectItem: null,
        selectSkuSellsPrice: 0,
      });
    }
    this.setData({
      specImg: skuItem && skuItem.skuImage ? skuItem.skuImage : primaryImage,
    });
  },

  // 获取已选择的sku名称
  getSelectedSkuValues(skuTree, selectedSku) {
    const normalizedTree = this.normalizeSkuTree(skuTree);
    return Object.keys(selectedSku).reduce((selectedValues, skuKeyStr) => {
      const skuValues = normalizedTree[skuKeyStr];
      const skuValueId = selectedSku[skuKeyStr];
      if (skuValueId !== '') {
        const skuValue = skuValues.filter((value) => {
          return value.specValueId === skuValueId;
        })[0];
        skuValue && selectedValues.push(skuValue);
      }
      return selectedValues;
    }, []);
  },

  normalizeSkuTree(skuTree) {
    const normalizedTree = {};
    skuTree.forEach((treeItem) => {
      normalizedTree[treeItem.specId] = treeItem.specValueList;
    });
    return normalizedTree;
  },

  selectSpecsName(selectSpecsName) {
    if (selectSpecsName) {
      this.setData({
        selectedAttrStr: selectSpecsName,
      });
    } else {
      this.setData({
        selectedAttrStr: '',
      });
    }
  },

  addCart() {
    const { isAllSelectedSku } = this.data;
    Toast({
      context: this,
      selector: '#t-toast',
      message: isAllSelectedSku ? '点击加入购物车' : '请选择规格',
      icon: '',
      duration: 1000,
    });
  },

  gotoBuy(type) {
    const { isAllSelectedSku, buyNum } = this.data;
    if (!isAllSelectedSku) {
      Toast({
        context: this,
        selector: '#t-toast',
        message: '请选择规格',
        icon: '',
        duration: 1000,
      });
      return;
    }
    this.handlePopupHide();
    const query = {
      quantity: buyNum,
      storeId: '1',
      spuId: this.data.spuId,
      goodsName: this.data.details.title,
      skuId: type === 1 ? this.data.skuList[0].skuId : this.data.selectItem.skuId,
      available: this.data.details.available,
      price: this.data.details.minSalePrice,
      specInfo: this.data.details.specList?.map((item) => ({
        specTitle: item.title,
        specValue: item.specValueList[0].specValue,
      })),
      primaryImage: this.data.details.primaryImage,
      spuId: this.data.details.spuId,
      thumb: this.data.details.primaryImage,
      title: this.data.details.title,
    };
    let urlQueryStr = obj2Params({
      goodsRequestList: JSON.stringify([query]),
    });
    urlQueryStr = urlQueryStr ? `?${urlQueryStr}` : '';
    const path = `/pages/order/order-confirm/index${urlQueryStr}`;
    wx.navigateTo({
      url: path,
    });
  },

  specsConfirm() {
    const { buyType } = this.data;
    if (buyType === 1) {
      this.gotoBuy();
    } else {
      this.addCart();
    }
    // this.handlePopupHide();
  },

  changeNum(e) {
    this.setData({
      buyNum: e.detail.buyNum,
    });
  },

  closePromotionPopup() {
    this.setData({
      isShowPromotionPop: false,
    });
  },

  promotionChange(e) {
    const { index } = e.detail;
    wx.navigateTo({
      url: `/pages/promotion/promotion-detail/index?promotion_id=${index}`,
    });
  },

  showPromotionPopup() {
    this.setData({
      isShowPromotionPop: true,
    });
  },

  getDetail(spuId) {
    console.log('开始获取商品详情:', spuId);

    wx.showLoading({
      title: '加载中...',
      mask: true
    });

    fetchGood(spuId).then((details) => {
      console.log('获取商品详情成功:', details);

      // 预处理数据，确保字段格式正确
      const processedDetails = { ...details };

      // 处理B2B字段的默认值
      processedDetails.minOrderQuantity = processedDetails.minOrderQuantity || 100;
      processedDetails.unit = processedDetails.unit || '件';
      processedDetails.leadTime = processedDetails.leadTime || 7;
      processedDetails.available = processedDetails.available !== undefined ? processedDetails.available : processedDetails.status;

      // 确保title字段存在（前端WXML使用的是title）
      processedDetails.title = processedDetails.title || processedDetails.name || '';
      processedDetails.primaryImage = processedDetails.primaryImage || processedDetails.mainImage || '';

      // 处理图片字段 - 如果images是字符串，尝试解析为JSON数组
      if (typeof processedDetails.images === 'string') {
        try {
          processedDetails.images = JSON.parse(processedDetails.images);
        } catch (e) {
          console.warn('解析images字段失败，使用主图:', e);
          processedDetails.images = [processedDetails.primaryImage || processedDetails.mainImage];
        }
      }

      // 确保images是数组
      if (!Array.isArray(processedDetails.images)) {
        processedDetails.images = [processedDetails.primaryImage || processedDetails.mainImage];
      }

      // 处理desc字段（详情图片）
      if (typeof processedDetails.desc === 'string') {
        try {
          processedDetails.desc = JSON.parse(processedDetails.desc);
        } catch (e) {
          // 如果desc不是JSON，使用images作为详情图
          processedDetails.desc = processedDetails.images;
        }
      }

      console.log('最终的商品数据:', {
        title: processedDetails.title,
        description: processedDetails.description,
        intro: processedDetails.intro,
        imagesCount: processedDetails.images.length,
        specCount: processedDetails.specificationParams?.length || 0
      });

      const skuArray = [];

      // 从后端API返回的可能是原始Product数据，需要处理
      const primaryImage = processedDetails.primaryImage || processedDetails.mainImage;
      const isPutOnSale = processedDetails.isPutOnSale !== undefined ? processedDetails.isPutOnSale : (processedDetails.status ? 1 : 0);
      const minSalePrice = processedDetails.minSalePrice || (processedDetails.wholesalePrice ? (processedDetails.wholesalePrice / 100) : 0);
      const maxSalePrice = processedDetails.maxSalePrice || (processedDetails.wholesalePrice ? (processedDetails.wholesalePrice / 100) : 0);
      const maxLinePrice = processedDetails.maxLinePrice || (processedDetails.retailPrice ? (processedDetails.retailPrice / 100) : 0);
      const soldNum = processedDetails.soldNum || processedDetails.viewCount || 0;

      // 处理SKU信息 - 创建默认SKU
      const skuList = processedDetails.skuList || [{
        skuId: processedDetails.id || processedDetails.spuId,
        price: minSalePrice * 100, // 转换为分
        stockInfo: {
          stockQuantity: processedDetails.stock || processedDetails.spuStockQuantity || 999
        }
      }];

      if (Array.isArray(skuList)) {
        skuList.forEach((item) => {
          skuArray.push({
            skuId: item.skuId,
            quantity: item.stockInfo ? item.stockInfo.stockQuantity : 999,
            specInfo: item.specInfo,
          });
        });
      } else {
        // 如果没有skuList，创建一个默认的
        skuArray.push({
          skuId: processedDetails.id || processedDetails.spuId,
          quantity: 999,
          specInfo: [],
        });
      }

      this.setData({
        details: processedDetails, // 使用处理后的数据
        activityList: [], // B2B模式不需要促销活动
        isStock: processedDetails.spuStockQuantity > 0 || processedDetails.stock > 0,
        maxSalePrice: maxSalePrice ? parseInt(maxSalePrice) : 0,
        maxLinePrice: maxLinePrice ? parseInt(maxLinePrice) : 0,
        minSalePrice: minSalePrice ? parseInt(minSalePrice) : 0,
        list: [], // B2B模式不需要促销列表
        skuArray: skuArray,
        primaryImage,
        soldout: isPutOnSale === 0,
        soldNum,
      });

      // 收藏状态检查移到onLoad中进行，避免重复调用
      wx.hideLoading();
      console.log('商品详情页数据设置完成');
    }).catch((error) => {
      console.error('获取商品详情失败:', error);
      wx.hideLoading();
      wx.showToast({
        title: '加载失败',
        icon: 'error',
        duration: 2000
      });
    });
  },

  // 检查商品收藏状态
  async checkFavoriteStatus(productId) {
    try {
      // 使用新的authManager检查用户登录状态
      const authManager = getApp().globalData.authManager;
      const currentUser = authManager.getCurrentUser();

      if (!currentUser || !currentUser.id) {
        // 用户未登录或没有ID，跳过收藏状态检查
        console.log('用户未登录或无用户ID，跳过收藏状态检查');
        this.setData({
          isFavorited: false
        });
        this.updateFavoriteButtonText(false);
        return;
      }

      console.log('检查收藏状态，用户ID:', currentUser.id, '商品ID:', productId);

      // 检查本地存储中的收藏数据作为调试信息
      const localFavorites = wx.getStorageSync('favorites') || [];
      console.log('本地存储的收藏数据:', localFavorites);
      const localFavorited = localFavorites.some(item => item.spuId === productId);
      console.log('本地存储中该商品收藏状态:', localFavorited);

      const { checkFavorite } = require('../../../services/favorite/favorite');
      const result = await checkFavorite({ userId: currentUser.id, productId });

      console.log('API返回收藏状态结果:', result);

      // 提取实际的收藏状态（API返回完整Result格式）
      const isFavorited = result.data !== undefined ? result.data : false;

      console.log('提取的收藏状态:', isFavorited);
      console.log('本地vs远程收藏状态对比:', { local: localFavorited, remote: isFavorited });

      // 更新页面收藏状态
      this.setData({
        isFavorited: isFavorited
      });
      this.updateFavoriteButtonText(isFavorited);

    } catch (error) {
      console.error('检查收藏状态失败:', error);

      // 检查是否是网络错误
      const errorMessage = error.message || error.toString();
      if (errorMessage.includes('Network') || errorMessage.includes('network')) {
        console.log('网络错误，使用本地存储作为fallback');
        // 网络错误时，检查本地存储作为fallback
        const favorites = wx.getStorageSync('favorites') || [];
        const isFavorited = favorites.some(item => item.spuId === productId);

        this.setData({
          isFavorited: isFavorited
        });
        this.updateFavoriteButtonText(isFavorited);
      } else {
        // 其他错误时默认为未收藏，并清理可能错误的本地收藏数据
        console.log('API检查失败，默认未收藏，清理本地数据:', errorMessage);

        // 清理本地收藏数据中可能错误的条目
        this.cleanupLocalFavorites(productId);

        this.setData({
          isFavorited: false
        });
        this.updateFavoriteButtonText(false);
      }
    }
  },

  // 清理本地收藏数据中的错误条目
  cleanupLocalFavorites(currentProductId) {
    try {
      const favorites = wx.getStorageSync('favorites') || [];
      console.log('清理前本地收藏数据:', favorites);

      // 清理格式不正确的收藏数据
      const cleanedFavorites = favorites.filter(item => {
        // 保留有效的收藏数据（必须有spuId）
        return item && item.spuId && typeof item.spuId !== 'undefined';
      });

      if (cleanedFavorites.length !== favorites.length) {
        console.log('清理后本地收藏数据:', cleanedFavorites);
        wx.setStorageSync('favorites', cleanedFavorites);
      }
    } catch (error) {
      console.error('清理本地收藏数据失败:', error);
    }
  },

  // B2B模式不需要评论功能

  onLoad(query) {
    const { spuId } = query;
    this.setData({
      spuId: spuId,
    });

    // 先获取商品详情，然后检查用户登录状态
    this.getDetail(spuId);

    // 异步检查用户登录状态
    this.checkUserLoginStatus().then((userInfo) => {
      console.log('登录状态检查完成，用户信息:', userInfo);
      // 登录状态检查完成后，检查收藏状态
      return this.checkFavoriteStatus(spuId);
    }).catch((error) => {
      console.error('登录状态检查失败:', error);
    });

    // 加载评论数据
    this.loadCommentStats(spuId);
    this.loadComments(spuId);
  },

  // 检查用户登录状态
  async checkUserLoginStatus() {
    try {
      // 使用我们的自动登录管理器
      const authManager = getApp().globalData.authManager;

      // 如果还没有登录信息，尝试自动登录
      if (!authManager.isLoggedIn()) {
        console.log('用户未登录，开始自动登录...');
        await authManager.autoLogin();
      }

      const userInfo = authManager.getCurrentUser();
      console.log('自动登录结果:', userInfo);

      this.setData({
        userInfo: userInfo
      });

      console.log('页面加载时用户登录状态:', userInfo ? '已登录，昵称: ' + userInfo.nickname : '未登录');

      // 如果用户已登录，返回用户信息，否则返回null
      return userInfo;
    } catch (error) {
      console.error('检查用户登录状态失败:', error);
      this.setData({
        userInfo: null
      });
      return null;
    }
  },

  // 处理收藏登录（只需要wx.login，不需要用户授权）
  async onGetUserInfoForFavorite() {
    console.log('开始微信登录流程...');

    wx.showLoading({
      title: '登录中...',
      mask: true
    });

    // 1. 获取微信登录code
    wx.login({
      success: (loginRes) => {
        if (loginRes.code) {
          console.log('微信登录成功，code:', loginRes.code);

          // 2. 直接用code作为临时openid登录（后端可以进一步处理）
          const userInfo = {
            openid: 'wx_' + loginRes.code,
            nickname: '微信用户', // 默认昵称
            avatarUrl: '', // 默认头像
          };

          const { wxLogin } = require('../../../services/user/auth');

          wxLogin(userInfo)
            .then((result) => {
              console.log('后端登录成功，API响应:', result);

              // 3. 提取用户数据（现在API返回完整Result格式）
              const user = result.data;

              // 4. 存储用户信息并更新页面状态
              wx.setStorageSync('userInfo', user);
              wx.setStorageSync('currentUser', user); // 同时保存到authManager使用的位置
              wx.setStorageSync('isLoggedIn', true);

              // 也更新authManager的currentUser
              const authManager = getApp().globalData.authManager;
              if (authManager) {
                authManager.currentUser = user;
                console.log('商品详情页已更新authManager的用户信息:', user);
              }

              this.setData({
                userInfo: user
              });

              // 5. 执行收藏操作
              this.executeFavoriteOperation(user.id, this.data.spuId);

            })
            .catch((error) => {
              console.error('后端登录失败:', error);
              wx.hideLoading();
              wx.showToast({
                title: '登录失败',
                icon: 'error'
              });
            });

        } else {
          wx.hideLoading();
          wx.showToast({
            title: '微信登录失败',
            icon: 'error'
          });
        }
      },
      fail: (error) => {
        console.error('wx.login失败:', error);
        wx.hideLoading();
        wx.showToast({
          title: '登录失败',
          icon: 'error'
        });
      }
    });
  },

  // B2B功能：拨打电话
  makePhoneCall() {
    wx.makePhoneCall({
      phoneNumber: '15736288761',
      success: () => {
        console.log('拨打电话成功');
      },
      fail: (err) => {
        console.error('拨打电话失败:', err);
        wx.showToast({
          title: '拨打电话失败',
          icon: 'error'
        });
      }
    });
  },

  // B2B功能：复制邮箱
  copyEmail() {
    wx.setClipboardData({
      data: 'sales@sj-tex.com',
      success: () => {
        wx.showToast({
          title: '邮箱已复制',
          icon: 'success'
        });
      }
    });
  },

  // B2B功能：复制网站
  copyWebsite() {
    wx.setClipboardData({
      data: 'www.sj-tex.com',
      success: () => {
        wx.showToast({
          title: '网站地址已复制',
          icon: 'success'
        });
      }
    });
  },

  // 图片预览功能
  previewImage(e) {
    const current = e.currentTarget.dataset.current || 0;
    const details = this.data.details;

    // 构建图片数组：主图 + 其他图片
    let images = [];
    if (details.primaryImage || details.mainImage) {
      images.push(details.primaryImage || details.mainImage);
    }
    if (details.images && Array.isArray(details.images)) {
      images = images.concat(details.images);
    }

    // 确保有图片可以预览
    if (images.length === 0) {
      wx.showToast({
        title: '暂无图片预览',
        icon: 'none'
      });
      return;
    }

    wx.previewImage({
      current: images[current] || images[0], // 当前显示的图片链接
      urls: images // 需要预览的图片链接列表
    });
  },

  // B2B功能：优化询价功能
  toInquiry() {
    const { details } = this.data;
    wx.showModal({
      title: '批量询价',
      content: `是否要询价 ${details.title || '此商品'}？\n最小起订量：${details.minOrderQuantity || 100} ${details.unit || '件'}`,
      confirmText: '确认询价',
      success: (res) => {
        if (res.confirm) {
          Toast({
            context: this,
            selector: '#t-toast',
            message: '询价成功，我们将尽快与您联系！',
          });

          // 这里可以调用后端API保存询价记录
          // this.saveInquiry();
        }
      }
    });
  },

  // B2B功能：真实收藏功能
  async toAddFavorite() {
    const { details, spuId } = this.data;

    // 1. 首先使用authManager检查登录状态
    const authManager = getApp().globalData.authManager;
    const currentUser = authManager.getCurrentUser();

    console.log('toAddFavorite - 当前用户:', currentUser);

    if (currentUser && currentUser.id) {
      // 已登录且有用户ID，直接执行收藏操作
      console.log('用户已登录，执行收藏操作，用户ID:', currentUser.id);
      this.executeFavoriteOperation(currentUser.id, spuId);
    } else {
      // 未登录，执行登录流程（用户已经点击了按钮，可以调用getUserProfile）
      console.log('用户未登录，执行登录流程');
      this.performLoginAndFavorite(spuId);
    }
  },

  // 执行登录并收藏
  async performLoginAndFavorite(productId) {
    // 显示加载状态
    wx.showLoading({
      title: '登录中...',
      mask: true
    });

    try {
      const { performWxLogin } = require('../../../services/user/auth');
      const userInfo = await performWxLogin();

      console.log('用户登录成功，开始收藏操作，用户ID:', userInfo.id);

      // 登录成功后执行收藏操作
      this.executeFavoriteOperation(userInfo.id, productId);

    } catch (error) {
      console.error('登录失败:', error);
      wx.hideLoading();

      if (error.message.includes('拒绝授权')) {
        wx.showToast({
          title: '需要授权才能使用收藏功能',
          icon: 'none',
          duration: 2000
        });
      } else {
        wx.showToast({
          title: '登录失败，请重试',
          icon: 'error'
        });
      }
    }
  },

  // 执行收藏操作
  executeFavoriteOperation(userId, productId) {
    // 更新加载提示
    wx.showLoading({
      title: '处理中...',
      mask: true
    });

    console.log('执行收藏操作，用户ID:', userId, '商品ID:', productId);

    // 调用真实API切换收藏状态
    this.toggleFavoriteStatus(userId, productId);
  },

  // 通知其他页面收藏状态已改变
  notifyFavoriteChanged(userId, productId, isFavorited) {
    try {
      // 使用全局事件通知
      const app = getApp();
      const { EVENTS } = require('../../../utils/globalEvents');

      console.log('准备发送收藏状态变化事件:', { userId, productId, isFavorited });

      app.globalData.globalEvents.emit(EVENTS.FAVORITE_CHANGED, {
        userId: userId,
        productId: productId,
        isFavorited: isFavorited,
        timestamp: Date.now()
      });

      console.log('已发送收藏状态变化通知:', { userId, productId, isFavorited });

      // 同时尝试直接查找购物车页面（备选方案）
      const pages = getCurrentPages();
      for (let i = 0; i < pages.length; i++) {
        const page = pages[i];

        if (page.route === 'pages/cart/index') {
          console.log('找到购物车页面，直接刷新');
          if (page.refreshData) {
            page.refreshData();
          }
          break;
        }
      }
    } catch (error) {
      console.error('通知收藏状态变化失败:', error);
    }
  },

  // 切换收藏状态的核心方法
  async toggleFavoriteStatus(userId, productId) {
    try {
      const { toggleFavorite } = require('../../../services/favorite/favorite');

      console.log('调用toggleFavorite API:', { userId, productId });
      const result = await toggleFavorite({ userId, productId });
      console.log('toggleFavorite API响应:', result);

      // 检查API响应结构
      let isFavorited;
      if (result && result.code === 200) {
        // API成功返回
        isFavorited = result.data !== null; // data为null表示取消收藏，非null表示收藏成功
        console.log('API成功，收藏状态:', isFavorited);
      } else {
        // API返回错误，解析错误信息
        console.log('API返回错误:', result);
        throw new Error(result?.message || '收藏操作失败');
      }

      // 更新页面收藏状态
      this.setData({
        isFavorited: isFavorited
      });

      // 显示提示信息
      Toast({
        context: this,
        selector: '#t-toast',
        message: isFavorited ? '已添加到收藏' : '已取消收藏',
      });

      // 更新底部按钮文字
      this.updateFavoriteButtonText(isFavorited);

      // 通知其他页面收藏状态已改变（实时同步）
      console.log('🚀 准备通知其他页面收藏状态变化:', { userId, productId, isFavorited });
      this.notifyFavoriteChanged(userId, productId, isFavorited);

      console.log('✅ 收藏操作成功:', isFavorited ? '已收藏' : '已取消收藏');

    } catch (error) {
      console.error('收藏操作失败:', error);

      // 检查是否是重复收藏的错误
      const errorMessage = error.message || error.toString();
      if (errorMessage.includes('Duplicate entry') || errorMessage.includes('已在收藏中')) {
        console.log('检测到重复收藏，同步状态为已收藏');
        // 重复收藏意味着用户已经收藏了，直接设置为已收藏状态
        this.setData({
          isFavorited: true
        });
        this.updateFavoriteButtonText(true);

        Toast({
          context: this,
          selector: '#t-toast',
          message: '已在收藏中',
        });
      } else {
        // 其他错误时使用本地存储作为fallback
        console.log('使用本地存储作为fallback');
        this.fallbackToggleFavorite(productId);
      }
    } finally {
      wx.hideLoading();
    }
  },

  // 网络失败时的本地收藏fallback
  fallbackToggleFavorite(productId) {
    let favorites = wx.getStorageSync('favorites') || [];
    const isFavorited = favorites.some(item => item.spuId === productId);

    if (isFavorited) {
      // 取消收藏
      favorites = favorites.filter(item => item.spuId !== productId);
      this.setData({ isFavorited: false });
    } else {
      // 添加收藏
      const { details } = this.data;
      const goodsInfo = {
        spuId: productId,
        title: details.title,
        image: details.primaryImage || details.images?.[0],
        minOrderQuantity: details.minOrderQuantity || 100,
        unit: details.unit || '件',
        leadTime: details.leadTime || '7-15天',
        storeName: '浙江宋家纺织',
        addedTime: new Date().getTime()
      };

      favorites.unshift(goodsInfo);
      this.setData({ isFavorited: true });
    }

    wx.setStorageSync('favorites', favorites);
    this.updateFavoriteButtonText(!isFavorited);
  },

  // 更新收藏按钮文字
  updateFavoriteButtonText(isFavorited) {
    // 这里可以更新按钮的显示状态
    this.setData({
      favoriteButtonText: isFavorited ? '已收藏' : '收藏商品'
    });
  },

  // ===== 评论功能相关方法 =====

  /**
   * 加载商品评论
   */
  async loadComments(fresh = false) {
    try {
      if (fresh) {
        this.setData({
          commentPage: 0,
          comments: []
        });
      }

      const { fetchCommentList } = require('../../services/comment/comment');
      const result = await fetchCommentList(
        this.data.spuId,
        this.data.commentPage,
        this.data.commentPageSize
      );

      if (result.code === 200 && result.data) {
        const newComments = result.data.records || [];
        const totalComments = result.data.total || 0;

        // 计算平均评分
        let averageRating = 0;
        if (newComments.length > 0) {
          const totalRating = newComments.reduce((sum, comment) => sum + (comment.rating || 5), 0);
          averageRating = (totalRating / newComments.length).toFixed(1);
        }

        this.setData({
          comments: fresh ? newComments : this.data.comments.concat(newComments),
          commentTotal: totalComments,
          averageRating: parseFloat(averageRating),
          hasMoreComments: newComments.length === this.data.commentPageSize,
          commentPage: this.data.commentPage + 1
        });

        console.log(`加载评论成功: ${newComments.length}条, 总计${totalComments}条`);
      }
    } catch (error) {
      console.error('加载评论失败:', error);
    }
  },

  /**
   * 加载更多评论
   */
  loadMoreComments() {
    this.loadComments(false);
  },

  /**
   * 显示评论弹窗
   */
  showCommentModal(e) {
    const currentUser = getApp().globalData.authManager.getCurrentUser();
    if (!currentUser) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      });
      return;
    }

    this.setData({
      showCommentDialog: true,
      selectedRating: 5,
      commentContent: '',
      replyToComment: null,
      replyToUserName: ''
    });
  },

  /**
   * 隐藏评论弹窗
   */
  hideCommentModal() {
    this.setData({
      showCommentDialog: false
    });
  },

  /**
   * 选择评分
   */
  selectRating(e) {
    const rating = parseInt(e.currentTarget.dataset.rating);
    this.setData({
      selectedRating: rating
    });
  },

  /**
   * 评论内容输入
   */
  onCommentInput(e) {
    this.setData({
      commentContent: e.detail.value
    });
  },

  /**
   * 回复评论
   */
  replyComment(e) {
    const commentId = e.currentTarget.dataset.commentId;
    const userName = e.currentTarget.dataset.userName;

    this.setData({
      showCommentDialog: true,
      replyToComment: commentId,
      replyToUserName: userName,
      selectedRating: 5,
      commentContent: ''
    });
  },

  /**
   * 提交评论
   */
  async submitComment() {
    const content = this.data.commentContent.trim();
    if (!content) {
      wx.showToast({
        title: '请输入评论内容',
        icon: 'none'
      });
      return;
    }

    try {
      const { submitComment } = require('../../services/comment/comment');
      const result = await submitComment(
        this.data.spuId,
        content,
        this.data.selectedRating,
        this.data.replyToComment || 0
      );

      if (result.code === 200) {
        wx.showToast({
          title: '评论发表成功',
          icon: 'success'
        });

        // 重新加载评论列表
        this.loadComments(true);
        this.hideCommentModal();
      } else {
        wx.showToast({
          title: result.message || '评论发表失败',
          icon: 'none'
        });
      }
    } catch (error) {
      console.error('提交评论失败:', error);
      wx.showToast({
        title: '评论发表失败',
        icon: 'none'
      });
    }
  },

  /**
   * 点赞评论
   */
  async likeComment(e) {
    const commentId = e.currentTarget.dataset.commentId;

    try {
      const { likeComment } = require('../../services/comment/comment');
      const result = await likeComment(commentId);

      if (result.code === 200) {
        // 更新点赞数
        const comments = this.data.comments.map(comment => {
          if (comment.id === commentId) {
            return {
              ...comment,
              likeCount: (comment.likeCount || 0) + 1
            };
          }
          return comment;
        });

        this.setData({ comments });
        wx.showToast({
          title: '点赞成功',
          icon: 'success'
        });
      }
    } catch (error) {
      console.error('点赞失败:', error);
      wx.showToast({
        title: '点赞失败',
        icon: 'none'
      });
    }
  },

  /**
   * 退出登录
   */
  logout() {
    wx.showModal({
      title: '确认退出',
      content: '确定要退出登录吗？退出后将无法使用个人化功能。',
      success: (res) => {
        if (res.confirm) {
          try {
            // 使用authManager退出登录
            const authManager = getApp().globalData.authManager;
            authManager.logout();

            // 清空页面用户状态
            this.setData({
              userInfo: null,
              isFavorited: false
            });

            // 更新收藏按钮文字
            this.updateFavoriteButtonText(false);

            wx.showToast({
              title: '已退出登录',
              icon: 'success'
            });

            console.log('用户已退出登录');
          } catch (error) {
            console.error('退出登录失败:', error);
            wx.showToast({
              title: '退出失败',
              icon: 'none'
            });
          }
        }
      }
    });
  },

  // ==================== 评论功能相关方法 ====================

  /**
   * 加载评论统计信息
   */
  async loadCommentStats(productId) {
    try {
      const { getCommentStats } = require('../../../services/comment/comment');
      const result = await getCommentStats(productId);

      if (result && result.data) {
        const stats = result.data;
        const avgRating = stats.avgRating || 0;

        this.setData({
          commentStats: stats,
          ratingStars: this.getRatingStars(avgRating)
        });

        console.log('评论统计加载成功:', stats);
      }
    } catch (error) {
      console.error('加载评论统计失败:', error);
    }
  },

  /**
   * 加载评论列表（支持分页）
   */
  async loadComments(productId) {
    try {
      const { getProductComments } = require('../../../services/comment/comment');
      const { commentPage, commentPageSize } = this.data;

      const result = await getProductComments(productId, commentPage, commentPageSize);

      if (result && result.data) {
        // 获取分页数据
        const records = result.data.records || [];
        const total = result.data.total || 0;

        // 处理评论数据，添加星级显示
        const newComments = records.map(comment => {
          return {
            ...comment,
            ratingStars: this.getRatingStars(comment.rating || 5)
          };
        });

        // 追加到现有评论列表
        const allComments = this.data.comments.concat(newComments);

        this.setData({
          comments: allComments,
          commentPage: commentPage + 1,
          hasMoreComments: allComments.length < total
        });

        console.log(`评论列表加载成功: 第${commentPage}页, 本次${newComments.length}条, 总计${allComments.length}/${total}条`);
      }
    } catch (error) {
      console.error('加载评论列表失败:', error);
    }
  },

  /**
   * 加载更多评论
   */
  loadMoreComments() {
    const { spuId } = this.data;
    if (spuId) {
      this.loadComments(spuId);
    }
  },

  /**
   * 收起评论（只显示最新5条）
   */
  collapseComments() {
    const { spuId } = this.data;

    // 重置评论数据
    this.setData({
      comments: [],
      commentPage: 0,
      hasMoreComments: false
    });

    // 重新加载第一页（最新5条）
    this.loadComments(spuId);

    // 滚动到评论区域
    wx.pageScrollTo({
      selector: '.comments-section',
      duration: 300
    });
  },

  /**
   * 显示评论弹窗
   */
  showCommentDialog() {
    // 检查用户是否登录
    if (!this.data.userInfo) {
      wx.showModal({
        title: '提示',
        content: '请先登录后再发表评价',
        confirmText: '去登录',
        success: (res) => {
          if (res.confirm) {
            this.onGetUserInfoForFavorite();
          }
        }
      });
      return;
    }

    this.setData({
      showDialog: true,
      rating: 5,
      commentContent: ''
    });
  },

  /**
   * 隐藏评论弹窗
   */
  hideCommentDialog() {
    this.setData({
      showDialog: false,
      rating: 5,
      commentContent: ''
    });
  },

  /**
   * 阻止弹窗滚动穿透
   */
  preventMove() {
    return false;
  },

  /**
   * 设置评分
   */
  setRating(e) {
    const { rating } = e.currentTarget.dataset;
    this.setData({
      rating: rating
    });
  },

  /**
   * 监听评论内容输入
   */
  onCommentInput(e) {
    this.setData({
      commentContent: e.detail.value
    });
  },

  /**
   * 提交评论
   */
  async submitComment() {
    const { userInfo, commentContent, rating, spuId } = this.data;

    // 验证评论内容
    if (!commentContent || commentContent.trim() === '') {
      wx.showToast({
        title: '请输入评价内容',
        icon: 'none'
      });
      return;
    }

    if (commentContent.length < 5) {
      wx.showToast({
        title: '评价内容至少5个字',
        icon: 'none'
      });
      return;
    }

    wx.showLoading({
      title: '提交中...',
      mask: true
    });

    try {
      const { addComment } = require('../../../services/comment/comment');

      const params = {
        userId: userInfo.id,
        productId: parseInt(spuId),
        content: commentContent.trim(),
        rating: rating
      };

      const result = await addComment(params);

      wx.hideLoading();

      if (result && result.code === 200) {
        wx.showToast({
          title: '评价成功',
          icon: 'success'
        });

        // 关闭弹窗
        this.hideCommentDialog();

        // 重新加载评论列表和统计（重置分页）
        this.setData({
          comments: [],
          commentPage: 0
        });
        this.loadCommentStats(spuId);
        this.loadComments(spuId);
      } else {
        wx.showToast({
          title: result.message || '评价失败',
          icon: 'none'
        });
      }
    } catch (error) {
      wx.hideLoading();
      console.error('提交评论失败:', error);
      wx.showToast({
        title: '评价失败，请重试',
        icon: 'none'
      });
    }
  },

  /**
   * 删除评论
   */
  async deleteComment(e) {
    const { id } = e.currentTarget.dataset;
    const { userInfo, spuId } = this.data;

    wx.showModal({
      title: '确认删除',
      content: '确定要删除这条评价吗？',
      success: async (res) => {
        if (res.confirm) {
          wx.showLoading({
            title: '删除中...',
            mask: true
          });

          try {
            const { deleteComment } = require('../../../services/comment/comment');
            const result = await deleteComment(id, userInfo.id);

            wx.hideLoading();

            if (result && result.code === 200) {
              wx.showToast({
                title: '删除成功',
                icon: 'success'
              });

              // 重新加载评论列表和统计（重置分页）
              this.setData({
                comments: [],
                commentPage: 0
              });
              this.loadCommentStats(spuId);
              this.loadComments(spuId);
            } else {
              wx.showToast({
                title: result.message || '删除失败',
                icon: 'none'
              });
            }
          } catch (error) {
            wx.hideLoading();
            console.error('删除评论失败:', error);
            wx.showToast({
              title: '删除失败，请重试',
              icon: 'none'
            });
          }
        }
      }
    });
  },

  /**
   * 根据评分获取星级字符串
   */
  getRatingStars(rating) {
    const fullStar = '★';
    const emptyStar = '☆';
    const fullCount = Math.round(rating);
    const emptyCount = 5 - fullCount;

    return fullStar.repeat(fullCount) + emptyStar.repeat(emptyCount);
  },
});
