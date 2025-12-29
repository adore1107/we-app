Component({
  properties: {
    favoritesList: {
      type: Array,
      value: [],
    },
  },

  data: {
    startX: 0, // 触摸起始位置
    deleteWidth: 160, // 删除按钮宽度（rpx）
    isEditMode: false, // 是否处于编辑模式
    isAllSelected: false, // 是否全选
    selectedCount: 0, // 已选中数量
  },

  methods: {
    // 触摸开始
    onTouchStart(e) {
      const { index } = e.currentTarget.dataset;
      const touch = e.touches[0];

      this.setData({
        startX: touch.clientX,
        currentIndex: index,
      });

      // 标记当前项正在滑动
      this.updateItemSwipeStatus(index, true);
    },

    // 触摸移动
    onTouchMove(e) {
      const touch = e.touches[0];
      const { startX, currentIndex, deleteWidth } = this.data;
      const moveX = touch.clientX - startX;

      // 只允许向左滑动
      if (moveX < 0) {
        const translateX = Math.max(moveX * 0.5, -deleteWidth); // 限制最大滑动距离
        this.updateItemTranslateX(currentIndex, translateX);
      } else if (moveX > 0) {
        // 向右滑动，关闭删除按钮
        const translateX = Math.min(moveX * 0.5, 0);
        this.updateItemTranslateX(currentIndex, translateX);
      }
    },

    // 触摸结束
    onTouchEnd(e) {
      const { currentIndex, deleteWidth } = this.data;
      const item = this.properties.favoritesList[currentIndex];
      const translateX = item.translateX || 0;

      // 标记滑动结束
      this.updateItemSwipeStatus(currentIndex, false);

      // 如果滑动距离超过一半，则显示删除按钮
      if (translateX < -deleteWidth / 2) {
        this.updateItemTranslateX(currentIndex, -deleteWidth);
      } else {
        // 否则回弹
        this.updateItemTranslateX(currentIndex, 0);
      }

      // 关闭其他已打开的项
      this.closeOtherItems(currentIndex);
    },

    // 更新单个商品的滑动状态
    updateItemSwipeStatus(index, isSwiping) {
      const key = `favoritesList[${index}].isSwiping`;
      this.setData({
        [key]: isSwiping,
      });
    },

    // 更新单个商品的位移
    updateItemTranslateX(index, translateX) {
      const key = `favoritesList[${index}].translateX`;
      this.setData({
        [key]: translateX,
      });
    },

    // 关闭其他已打开的项
    closeOtherItems(currentIndex) {
      const { favoritesList } = this.properties;
      favoritesList.forEach((item, index) => {
        if (index !== currentIndex && item.translateX !== 0) {
          this.updateItemTranslateX(index, 0);
        }
      });
    },

    // 商品点击
    onGoodsClick(e) {
      const { goods, index } = e.currentTarget.dataset;
      const item = this.properties.favoritesList[index];

      // 如果处于编辑模式，切换选中状态
      if (this.data.isEditMode) {
        this.onCheckboxChange(e);
        return;
      }

      // 如果当前项已展开删除按钮，先关闭
      if (item.translateX && item.translateX < 0) {
        this.updateItemTranslateX(index, 0);
        return;
      }

      this.triggerEvent('goodsclick', { goods });
    },

    // 删除收藏
    onDelete(e) {
      // 阻止事件冒泡
      e.stopPropagation && e.stopPropagation();

      const { goods, index } = e.currentTarget.dataset;
      console.log('点击删除商品:', goods);

      // 先关闭滑动状态
      this.updateItemTranslateX(index, 0);

      // 延迟触发删除事件，让动画完成
      setTimeout(() => {
        this.triggerEvent('delete', { goods });
      }, 300);
    },

    // 预览图片
    onImagePreview(e) {
      const { url } = e.currentTarget.dataset;
      wx.previewImage({
        urls: [url],
      });
    },

    // ========== 批量管理相关方法 ==========

    // 切换编辑模式
    toggleEditMode() {
      const isEditMode = !this.data.isEditMode;

      // 切换到编辑模式时，关闭所有已展开的滑动项
      if (isEditMode) {
        this.closeAllItems();
      }

      // 退出编辑模式时，清空所有选中状态
      if (!isEditMode) {
        this.clearAllChecked();
      }

      this.setData({
        isEditMode,
        isAllSelected: false,
        selectedCount: 0,
      }, () => {
        // 通知父组件编辑模式状态变化
        this.triggerEvent('editmodechange', { isEditMode });
      });
    },

    // 切换单个商品的选中状态
    onCheckboxChange(e) {
      const { index } = e.currentTarget.dataset;
      const item = this.properties.favoritesList[index];
      const newChecked = !item.checked;

      // 更新选中状态
      this.setData({
        [`favoritesList[${index}].checked`]: newChecked,
      });

      // 更新选中数量和全选状态
      this.updateSelectedStatus();
    },

    // 切换全选
    toggleSelectAll() {
      const isAllSelected = !this.data.isAllSelected;
      const { favoritesList } = this.properties;

      favoritesList.forEach((item, index) => {
        this.setData({
          [`favoritesList[${index}].checked`]: isAllSelected,
        });
      });

      const selectedCount = isAllSelected ? favoritesList.length : 0;

      this.setData({
        isAllSelected,
        selectedCount,
      });

      // 通知父组件全选状态变化
      this.triggerEvent('selectallchange', { isAllSelected, selectedCount });
    },

    // 更新选中状态（计算selectedCount和isAllSelected）
    updateSelectedStatus() {
      const { favoritesList } = this.properties;
      const selectedCount = favoritesList.filter(item => item.checked).length;
      const isAllSelected = selectedCount === favoritesList.length && favoritesList.length > 0;

      this.setData({
        selectedCount,
        isAllSelected,
      });

      // 通知父组件选中状态变化
      this.triggerEvent('checkchange', { selectedCount, isAllSelected });
    },

    // 批量删除
    onBatchDelete() {
      const { favoritesList } = this.properties;
      const selectedItems = favoritesList.filter(item => item.checked);

      if (selectedItems.length === 0) {
        wx.showToast({
          title: '请选择要取消收藏的商品',
          icon: 'none',
        });
        return;
      }

      wx.showModal({
        title: '确认取消收藏',
        content: `确定要取消收藏这${selectedItems.length}件商品吗？`,
        confirmText: '确定',
        confirmColor: '#ff4d4f',
        success: (res) => {
          if (res.confirm) {
            // 触发批量删除事件
            this.triggerEvent('batchdelete', { items: selectedItems });

            // 退出编辑模式
            this.setData({
              isEditMode: false,
              isAllSelected: false,
              selectedCount: 0,
            });

            // 通知父组件编辑模式状态变化
            this.triggerEvent('editmodechange', { isEditMode: false });
          }
        },
      });
    },

    // 清空所有选中状态
    clearAllChecked() {
      const { favoritesList } = this.properties;
      favoritesList.forEach((item, index) => {
        this.setData({
          [`favoritesList[${index}].checked`]: false,
        });
      });
    },

    // 关闭所有已展开的滑动项
    closeAllItems() {
      const { favoritesList } = this.properties;
      favoritesList.forEach((item, index) => {
        if (item.translateX && item.translateX !== 0) {
          this.updateItemTranslateX(index, 0);
        }
      });
    },
  },
});