<template>
  <view class="container">
    <view class="tabs">
      <text
        v-for="tab in tabs"
        :key="tab.evaluate"
        :class="{ on: evaluate === tab.evaluate }"
        @tap="switchTab(tab.evaluate)"
      >
        {{ tab.label }}{{ tab.count != null ? tab.count : '' }}
      </text>
    </view>
    <view
      v-if="list.length === 0 && !loading"
      class="empty"
    >
      暂无评价
    </view>
    <view
      v-for="item in list"
      :key="item.prodCommId"
      class="card"
    >
      <view class="head">
        <text class="name">
          {{ item.isAnonymous === 1 ? '匿名用户' : (item.nickName || '用户') }}
        </text>
        <text class="stars">
          {{ starText(item.score) }}
        </text>
        <text class="time">
          {{ item.recTime }}
        </text>
      </view>
      <view class="content">
        {{ item.content }}
      </view>
      <view
        v-if="picArr(item.pics).length"
        class="imgs"
      >
        <image
          v-for="(img, idx) in picArr(item.pics)"
          :key="idx"
          :src="img"
          mode="aspectFill"
          @tap="preview(picArr(item.pics), idx)"
        />
      </view>
      <view
        v-if="item.replyContent"
        class="reply"
      >
        掌柜回复：{{ item.replyContent }}
      </view>
    </view>
  </view>
</template>

<script setup>
const prodId = ref(0)
const evaluate = ref(-1)
const list = ref([])
const current = ref(1)
const pages = ref(1)
const loading = ref(false)
const data = ref({
  number: 0,
  praiseNumber: 0,
  secondaryNumber: 0,
  negativeNumber: 0,
  picNumber: 0
})

const tabs = computed(() => [
  { evaluate: -1, label: '全部', count: data.value.number },
  { evaluate: 0, label: '好评', count: data.value.praiseNumber },
  { evaluate: 1, label: '中评', count: data.value.secondaryNumber },
  { evaluate: 2, label: '差评', count: data.value.negativeNumber },
  { evaluate: 3, label: '有图', count: data.value.picNumber }
])

onLoad((options) => {
  prodId.value = Number(options.prodId || options.prodid || 0)
  loadSummary()
  loadList(true)
})

onReachBottom(() => {
  if (current.value < pages.value) {
    current.value += 1
    loadList(false)
  }
})

const loadSummary = () => {
  http.request({
    url: '/prodComm/prodCommData',
    method: 'GET',
    data: { prodId: prodId.value }
  }).then(({ data: d }) => {
    data.value = d || data.value
  })
}

const loadList = (reset) => {
  if (reset) {
    current.value = 1
    list.value = []
  }
  loading.value = true
  http.request({
    url: '/prodComm/prodCommPageByProd',
    method: 'GET',
    data: {
      prodId: prodId.value,
      evaluate: evaluate.value,
      current: current.value,
      size: 10
    }
  }).then(({ data: d }) => {
    const records = d?.records || []
    list.value = reset ? records : list.value.concat(records)
    pages.value = d?.pages || 1
    current.value = d?.current || 1
  }).finally(() => {
    loading.value = false
  })
}

const switchTab = (ev) => {
  evaluate.value = ev
  loadList(true)
}

const picArr = (pics) => {
  if (!pics) {
    return []
  }
  return String(pics).split(',').map(s => s.trim()).filter(Boolean)
}

const starText = (score) => {
  const n = Number(score) || 0
  return '★'.repeat(n) + '☆'.repeat(Math.max(0, 5 - n))
}

const preview = (urls, idx) => {
  uni.previewImage({
    urls,
    current: urls[idx]
  })
}
</script>

<style scoped lang="scss">
.container {
  min-height: 100vh;
  background: #f4f4f4;
}
.tabs {
  display: flex;
  background: #fff;
  padding: 16rpx 10rpx;
  position: sticky;
  top: 0;
  z-index: 2;
  text {
    flex: 1;
    text-align: center;
    font-size: 24rpx;
    color: #666;
    padding: 8rpx 0;
  }
  text.on {
    color: #eb2444;
    font-weight: 600;
  }
}
.empty {
  text-align: center;
  color: #999;
  padding: 80rpx 0;
  font-size: 28rpx;
}
.card {
  background: #fff;
  margin-top: 16rpx;
  padding: 24rpx 30rpx;
}
.head {
  display: flex;
  align-items: center;
  font-size: 24rpx;
  color: #999;
  .name {
    color: #333;
    margin-right: 12rpx;
  }
  .stars {
    color: #f5a623;
    flex: 1;
  }
}
.content {
  font-size: 28rpx;
  color: #333;
  margin-top: 12rpx;
  line-height: 40rpx;
}
.imgs {
  margin-top: 16rpx;
  display: flex;
  flex-wrap: wrap;
  image {
    width: 160rpx;
    height: 160rpx;
    margin: 0 12rpx 12rpx 0;
  }
}
.reply {
  margin-top: 12rpx;
  background: #f7f7f7;
  padding: 16rpx;
  font-size: 24rpx;
  color: #666;
}
</style>
