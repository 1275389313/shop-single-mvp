<template>
  <view class="container">
    <view
      v-if="list.length === 0"
      class="empty"
    >
      暂无退款申请
    </view>
    <view
      v-for="item in list"
      :key="item.refundId"
      class="card"
      @tap="toOrder(item.orderNumber)"
    >
      <view class="head">
        <text>退款编号 {{ item.refundSn }}</text>
        <text :class="['sts', item.refundSts === 3 ? 'gray' : '']">
          {{ item.refundSts === 1 ? '待审核' : (item.refundSts === 2 ? '已同意' : '已拒绝') }}
        </text>
      </view>
      <view class="row">
        订单 {{ item.orderNumber }}
      </view>
      <view class="row">
        {{ item.applyType === 2 ? '退货退款' : '仅退款' }} · ￥{{ item.refundAmount }}
      </view>
      <view class="row reason">
        {{ item.buyerMsg }}
      </view>
    </view>
  </view>
</template>

<script setup>
const list = ref([])
const current = ref(1)
const pages = ref(0)

onLoad(() => {
  load(1)
})

onReachBottom(() => {
  if (current.value < pages.value) {
    load(current.value + 1)
  }
})

const load = (pageNo) => {
  uni.showLoading()
  http.request({
    url: '/p/refund/page',
    method: 'GET',
    data: {
      current: pageNo,
      size: 10
    }
  }).then(({ data }) => {
    const records = data.records || []
    list.value = pageNo === 1 ? records : list.value.concat(records)
    pages.value = data.pages
    current.value = data.current
    uni.hideLoading()
  }).catch(() => {
    uni.hideLoading()
  })
}

const toOrder = (orderNumber) => {
  uni.navigateTo({
    url: '/pages/order-detail/order-detail?orderNum=' + orderNumber
  })
}
</script>

<style scoped lang="scss">
.container {
  min-height: 100%;
  background: #f4f4f4;
  padding: 20rpx;
  box-sizing: border-box;
}
.empty {
  text-align: center;
  color: #999;
  margin-top: 200rpx;
  font-size: 26rpx;
}
.card {
  background: #fff;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
}
.head {
  display: flex;
  justify-content: space-between;
  font-size: 24rpx;
  margin-bottom: 12rpx;
}
.sts {
  color: #eb2444;
}
.sts.gray {
  color: #999;
}
.row {
  font-size: 26rpx;
  color: #555;
  line-height: 40rpx;
}
.reason {
  color: #999;
}
</style>
