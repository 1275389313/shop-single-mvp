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
      @tap="toDetail(item.orderNumber)"
    >
      <view class="head">
        <text>退款编号 {{ item.refundSn }}</text>
        <text :class="['sts', item.refundSts === 3 ? 'gray' : '']">
          {{ refundFlowText(item) }}
        </text>
      </view>
      <view class="row">
        订单 {{ item.orderNumber }}
      </view>
      <view class="row">
        {{ item.applyType === 2 ? '退货退款' : '仅退款' }} · ￥{{ item.refundAmount }}
      </view>
      <view
        v-if="item.expressNo"
        class="row"
      >
        {{ item.expressName }} {{ item.expressNo }}
        <text
          class="link-inline"
          @tap.stop="toReturnTrack(item)"
        >
          查看轨迹
        </text>
      </view>
      <view class="row reason">
        {{ item.buyerMsg }}
      </view>
      <view
        v-if="canEditReturnExpress(item)"
        class="link"
        @tap.stop="toDetail(item.orderNumber)"
      >
        {{ item.expressNo ? '修改退货物流' : '填写退货物流' }}
      </view>
    </view>
  </view>
</template>

<script setup>
import { refundFlowText, canEditReturnExpress } from '@/utils/refund.js'

const list = ref([])
const current = ref(1)
const pages = ref(0)

onShow(() => {
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

const toDetail = (orderNumber) => {
  uni.navigateTo({
    url: '/pages/refund-apply/refund-apply?orderNum=' + orderNumber
  })
}

const toReturnTrack = (item) => {
  if (!item || !item.refundSn) {
    return
  }
  uni.navigateTo({
    url: '/pages/express-delivery/express-delivery?refundSn=' + item.refundSn
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
.link {
  margin-top: 16rpx;
  color: #eb2444;
  font-size: 26rpx;
}
.link-inline {
  margin-left: 12rpx;
  color: #eb2444;
}
</style>
