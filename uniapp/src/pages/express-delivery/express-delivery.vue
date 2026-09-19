<template>
  <!-- 物流信息 -->
  <view class="container">
    <view class="wrapper">
      <view
        v-if="mockTip"
        class="deliveryTip"
      >
        {{ mockTip }}
      </view>
      <view
        class="deliveryInfo"
        style="background:url(http://jiales.gz-yami.com/delivery-bg.png) center center no-repeat #fff;"
      >
        <view
          class="icon-express"
          style="background:url(http://jiales.gz-yami.com/delivery-car.png) no-repeat;background-size:100% 100%;"
        />
        <view class="infoWarp">
          <view class="companyname">
            <text class="key">
              物流公司：
            </text>
            <text class="value">
              {{ companyName }}
            </text>
          </view>
          <view class="expno">
            <text class="key">
              运单编号：
            </text>
            <text class="value">
              {{ dvyFlowId }}
            </text>
          </view>
          <view
            v-if="stateText"
            class="expno"
          >
            <text class="key">
              物流状态：
            </text>
            <text class="value">
              {{ stateText }}
            </text>
          </view>
        </view>
      </view>
      <view
        v-if="dvyData.length"
        class="deliveryDetail"
      >
        <block
          v-for="(item, index) in dvyData"
          :key="index"
        >
          <view :class="'detailItem ' + (index==0?'lastest':'')">
            <view class="dot">
              <image src="@/static/images/icon/delive-dot.png" />
              <image src="@/static/images/icon/dot.png" />
            </view>
            <view class="detail">
              <view class="desc">
                {{ item.context }}
              </view>
              <view class="time">
                {{ item.time }}
              </view>
            </view>
          </view>
        </block>
      </view>
      <view
        v-else
        class="empty-space"
      >
        {{ emptyText }}
      </view>
    </view>
  </view>
</template>

<script setup>
const companyName = ref('')
const dvyFlowId = ref('')
const dvyData = ref([])
const stateText = ref('')
const mockTip = ref('')
const emptyText = ref('暂无配送信息')

onLoad((options) => {
  const refundSn = options.refundSn
  const orderNum = options.orderNum
  uni.setNavigationBarTitle({
    title: refundSn ? '退货物流' : '物流查询'
  })
  uni.showLoading()
  const req = refundSn
    ? {
        url: '/p/refund/delivery',
        method: 'GET',
        data: { refundSn }
      }
    : {
        url: '/p/delivery/check',
        method: 'GET',
        data: { orderNumber: orderNum }
      }
  http.request(req)
    .then(({ data }) => {
      applyTrack(data)
      uni.hideLoading()
    })
    .catch(() => {
      uni.hideLoading()
      emptyText.value = '物流查询失败'
    })
})

const applyTrack = (data) => {
  if (!data) {
    return
  }
  companyName.value = data.companyName || ''
  dvyFlowId.value = data.dvyFlowId || ''
  dvyData.value = data.data || []
  stateText.value = data.stateText || ''
  if (data.mock) {
    mockTip.value = data.message || '模拟轨迹（未配置快递100密钥）'
  } else if (data.message && !(data.data && data.data.length)) {
    emptyText.value = data.message
  } else if (data.message && data.source === 'kuaidi100') {
    mockTip.value = ''
  }
  if (!(data.data && data.data.length) && data.message) {
    emptyText.value = data.message
  }
}
</script>

<style scoped lang="scss">
@use './express-delivery.scss';
</style>
