Component({
  externalClasses: ['custom-class'],

  properties: {
    category: {
      type: Array,
    },
    initActive: {
      type: Array,
      value: [],
      observer(newVal, oldVal) {
        if (newVal[0] !== oldVal[0]) {
          this.setActiveKey(newVal[0], 0);
        }
      },
    },
    isSlotRight: {
      type: Boolean,
      value: false,
    },
    level: {
      type: Number,
      value: 3,
    },
  },
  data: {
    activeKey: 0,
    subActiveKey: 0,
  },
  attached() {
    if (this.properties.initActive && this.properties.initActive.length > 0) {
      this.setData({
        activeKey: this.properties.initActive[0],
        subActiveKey: this.properties.initActive[1] || 0,
      });
    }
  },
  methods: {
    onParentChange(event) {
      this.setActiveKey(event.detail.index, 0).then(() => {
        // 获取当前选中的分类对象
        const categoryList = this.properties.category;
        const selectedCategory = categoryList[event.detail.index];
        console.log('左侧分类点击:', selectedCategory);

        this.triggerEvent('change', [this.data.activeKey, this.data.subActiveKey]);
        // 同时触发changeCategory事件，传递分类对象
        this.triggerEvent('changeCategory', {
          item: selectedCategory,
          index: event.detail.index
        });
      });
    },
    onChildChange(event) {
      this.setActiveKey(this.data.activeKey, event.detail.index).then(() => {
        this.triggerEvent('change', [this.data.activeKey, this.data.subActiveKey]);
      });
    },
    changCategory(event) {
      const { item } = event.currentTarget.dataset;
      this.triggerEvent('changeCategory', {
        item,
      });
    },
    setActiveKey(key, subKey) {
      return new Promise((resolve) => {
        this.setData(
          {
            activeKey: key,
            subActiveKey: subKey,
          },
          () => {
            resolve();
          },
        );
      });
    },
  },
});
