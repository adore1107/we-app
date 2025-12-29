Component({
  properties: {
    favoritesCount: {
      type: Number,
      value: 0,
    },
    fixed: {
      type: Boolean,
      value: true,
    },
    bottomHeight: {
      type: Number,
      value: 0,
    },
  },

  methods: {
    onInquiry() {
      this.triggerEvent('handleInquiry');
    },
  },
});