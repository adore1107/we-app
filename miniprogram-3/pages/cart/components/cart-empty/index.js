Component({
  properties: {
    imgUrl: {
      type: String,
      value: 'https://tdesign.gtimg.com/miniprogram/template/retail/template/empty-collection.png', // 收藏/合集图标
    },
    tip: {
      type: String,
      value: '还没有收藏任何商品',
    },
    subTip: {
      type: String,
      value: '收藏喜欢的面料，随时查看批发价格',
    },
    btnText: {
      type: String,
      value: '去逛逛',
    },
  },
  data: {},
  methods: {
    handleClick() {
      this.triggerEvent('handleClick');
    },
  },
});
