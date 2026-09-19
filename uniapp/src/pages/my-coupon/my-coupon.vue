<template>
  <view class="container">
    <view class="tabs">
      <view
        class="tab"
        :class="{ on: sts === 0 }"
        @tap="changeSts(0)"
      >
        未使用
      </view>
      <view
        class="tab"
        :class="{ on: sts === 1 }"
        @tap="changeSts(1)"
      >
        已使用
      </view>
      <view
        class="tab"
        :class="{ on: sts === 2 }"
        @tap="changeSts(2)"
      >
        已过期
      </view>
    </view>
    <view
      v-if="list.length === 0 && !loading"
      class="empty"
    >
      暂无优惠券
    </view>
    <view
      v-for="item in list"
      :key="item.couponUserId"
    >
      <coupon
        :item="item"
        scene="mine"
        :can-use="sts === 0"
        @use="onUse"
      />
    </view>
  </view>
</template>

<script setup>
import Coupon from '@/components/coupon/coupon.vue'

const sts = ref(0)
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

const changeSts = (value) => {
  sts.value = value
  loadList(true)
}

const loadList = (reset) => {
  if (reset) {
    current.value = 1
    list.value = []
  }
  loading.value = true
  http.request({
    url: '/p/coupon/myList',
    method: 'GET',
    data: {
      status: sts.value,
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

const onUse = (item) => {
  uni.navigateTo({
    url: '/pages/prod-classify/prod-classify?sts=4&tagid=' + item.couponId
  })
}
</script>

<style scoped lang="scss">
.container {
  min-height: 100vh;
  background: #f4f4f4;
  padding: 0 24rpx 40rpx;
  box-sizing: border-box;
}
.tabs {
  display: flex;
  background: #fff;
  margin: 0 -24rpx 16rpx;
}
.tab {
  flex: 1;
  text-align: center;
  padding: 24rpx 0;
  font-size: 28rpx;
  color: #666;
}
.tab.on {
  color: #eb2444;
  font-weight: 600;
  border-bottom: 4rpx solid #eb2444;
}
.empty {
  text-align: center;
  color: #999;
  padding-top: 200rpx;
  font-size: 28rpx;
}
</style>
