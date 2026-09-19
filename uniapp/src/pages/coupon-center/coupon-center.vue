<template>
  <view class="container">
    <view
      v-if="list.length === 0 && !loading"
      class="empty"
    >
      暂无可领优惠券
    </view>
    <view
      v-for="item in list"
      :key="item.couponId"
    >
      <coupon
        :item="item"
        scene="center"
        @receive="onReceive"
      />
    </view>
  </view>
</template>

<script setup>
import Coupon from '@/components/coupon/coupon.vue'

const list = ref([])
const current = ref(1)
const pages = ref(1)
const loading = ref(false)

onLoad(() => {
  loadList(true)
})

onReachBottom(() => {
  if (current.value < pages.value) {
    current.value += 1
    loadList(false)
  }
})

const loadList = (reset) => {
  if (reset) {
    current.value = 1
    list.value = []
  }
  loading.value = true
  http.request({
    url: '/coupon/list',
    method: 'GET',
    data: {
      current: current.value,
      size: 10
    }
  })
    .then(({ data }) => {
      const records = data?.records || []
      list.value = reset ? records : list.value.concat(records)
      pages.value = data?.pages || 1
    })
    .finally(() => {
      loading.value = false
    })
}

const onReceive = (item) => {
  http.request({
    url: '/p/coupon/receive',
    method: 'POST',
    data: {
      couponId: item.couponId
    }
  })
    .then(() => {
      uni.showToast({
        title: '领取成功',
        icon: 'none'
      })
    })
}
</script>

<style scoped lang="scss">
.container {
  min-height: 100vh;
  background: #f4f4f4;
  padding: 16rpx 24rpx 40rpx;
  box-sizing: border-box;
}
.empty {
  text-align: center;
  color: #999;
  padding-top: 200rpx;
  font-size: 28rpx;
}
</style>
