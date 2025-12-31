Component({
  externalClasses: ['wr-class'],

  options: {
    multipleSlots: true,
  },

  properties: {
    sortType: {
      type: String,
      value: 'overall',
      observer(sortType) {
        this.setData({
          sortType,
        });
      },
    },
    color: {
      type: String,
      value: '#FA550F',
    },
  },

  data: {
    sortType: 'overall', // overall=综合, latest=最新, hot=热门
  },

  methods: {
    // 综合排序
    onOverallAction() {
      this.triggerEvent('change', {
        sortType: 'overall',
      });
    },

    // 最新排序
    onLatestAction() {
      this.triggerEvent('change', {
        sortType: 'latest',
      });
    },

    // 热门排序
    onHotAction() {
      this.triggerEvent('change', {
        sortType: 'hot',
      });
    },
  },
});
