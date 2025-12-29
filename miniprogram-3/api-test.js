// 商品详情API测试脚本
// 模拟后端API响应数据格式

const mockProductDetail = {
  "code": 200,
  "message": "获取成功",
  "data": {
    "spuId": 1,
    "title": "凝胶恒温床垫面料 Q-max≥0.2",
    "desc": [
      "https://gw.alicdn.com/bao/uploaded/i1/92042735/O1CN01hcv6GQ1W4gJcj1fEU_!!92042735.jpg_.webp",
      "http://img.alicdn.com/img/i2/120665841/O1CN01fcm6PC1t1E5dI96u1_!!0-saturn_solar.jpg"
    ],
    "primaryImage": "https://gw.alicdn.com/bao/uploaded/i1/92042735/O1CN01hcv6GQ1W4gJcj1fEU_!!92042735.jpg_.webp",
    "images": [
      "https://gw.alicdn.com/bao/uploaded/i1/92042735/O1CN01hcv6GQ1W4gJcj1fEU_!!92042735.jpg_.webp",
      "http://img.alicdn.com/img/i2/120665841/O1CN01fcm6PC1t1E5dI96u1_!!0-saturn_solar.jpg"
    ],
    "price": 8500, // 85.00元 * 100 = 8500分
    "originPrice": 12000, // 120.00元 * 100 = 12000分
    "minSalePrice": 8500,
    "maxSalePrice": 8500,
    "maxLinePrice": 12000,
    // B2B字段
    "minOrderQuantity": 100,
    "unit": "件",
    "leadTime": 7,
    "available": true,
    "isPutOnSale": 1,
    "spuStockQuantity": 999,
    "soldNum": 0,
    "skuList": [
      {
        "skuId": 1,
        "price": 8500,
        "stockInfo": {
          "stockQuantity": 999
        }
      }
    ],
    "specList": []
  }
};

console.log('商品详情API测试数据:');
console.log(JSON.stringify(mockProductDetail, null, 2));

// 测试前端数据处理逻辑
function testFrontendDataProcessing() {
  const apiResponse = mockProductDetail.data;

  console.log('\n=== 前端数据处理测试 ===');

  // 前端价格处理（分转换为元）
  const priceInYuan = apiResponse.price / 100;
  console.log(`价格处理: ${apiResponse.price}分 -> ${priceInYuan}元`);

  // B2B字段验证
  console.log(`最小起订量: ${apiResponse.minOrderQuantity} ${apiResponse.unit}`);
  console.log(`交货周期: ${apiResponse.leadTime} 天`);
  console.log(`库存状态: ${apiResponse.available ? '有货' : '无货'}`);

  // 图片验证
  console.log(`商品图片数量: ${apiResponse.images.length}`);
  console.log(`主图: ${apiResponse.primaryImage}`);

  // SKU信息验证
  console.log(`SKU数量: ${apiResponse.skuList.length}`);
  apiResponse.skuList.forEach((sku, index) => {
    console.log(`SKU ${index + 1}: ID=${sku.skuId}, 价格=${sku.price / 100}元, 库存=${sku.stockInfo.stockQuantity}`);
  });
}

// 执行测试
testFrontendDataProcessing();