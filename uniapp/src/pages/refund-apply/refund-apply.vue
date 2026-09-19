<template>
  <view class="container">
    <view
      v-if="existingRefund"
      class="card"
    >
      <view class="tit">
        已有退款申请
      </view>
      <view class="row">
        退款编号：{{ existingRefund.refundSn }}
      </view>
      <view class="row">
        状态：{{ refundStsText(existingRefund.refundSts) }}
      </view>
      <view class="row">
        金额：￥{{ existingRefund.refundAmount }}
      </view>
      <view class="row">
        原因：{{ existingRefund.buyerMsg }}
      </view>
      <view
        v-if="existingRefund.rejectMessage"
        class="row"
      >
        拒绝原因：{{ existingRefund.rejectMessage }}
      </view>
    </view>

    <view
      v-if="!existingRefund"
      class="card"
    >
      <view class="tit">
        申请退款
      </view>
      <view class="row">
        订单号：{{ orderNumber }}
      </view>
      <view class="row">
        实付：￥{{ actualTotal }}
      </view>
      <view class="label">
        申请类型
      </view>
      <radio-group
        class="type-group"
        @change="onTypeChange"
      >
        <label class="type-item">
          <radio
            value="1"
            :checked="applyType === 1"
            color="#eb2444"
          />
          仅退款
        </label>
        <label class="type-item">
          <radio
            value="2"
            :checked="applyType === 2"
            color="#eb2444"
          />
          退货退款
        </label>
      </radio-group>
      <view class="hint">
        退货退款：本阶段只需提交申请，回填物流单号尚未接入。
      </view>
      <view class="label">
        申请原因
      </view>
      <textarea
        class="reason"
        maxlength="200"
        placeholder="请填写退款原因"
        :value="buyerMsg"
        @input="onReasonInput"
      />
      <button
        class="submit"
        :disabled="submitting"
        @tap="submit"
      >
        提交申请
      </button>
    </view>
  </view>
</template>

<script setup>
const orderNumber = ref('')
const orderItemId = ref(0)
const actualTotal = ref(0)
const applyType = ref(1)
const buyerMsg = ref('')
const submitting = ref(false)
const existingRefund = ref(null)

onLoad((options) => {
  orderNumber.value = options.orderNum || options.orderNumber || ''
  orderItemId.value = options.orderItemId ? Number(options.orderItemId) : 0
  loadOrder()
  loadRefund()
})

const refundStsText = (sts) => {
  if (sts === 1) return '待商家审核'
  if (sts === 2) return '商家已同意（mock 退款）'
  if (sts === 3) return '商家已拒绝'
  return '未知'
}

const loadOrder = () => {
  if (!orderNumber.value) return
  http.request({
    url: '/p/myOrder/orderDetail',
    method: 'GET',
    data: { orderNumber: orderNumber.value }
  }).then(({ data }) => {
    actualTotal.value = data.actualTotal
  })
}

const loadRefund = () => {
  if (!orderNumber.value) return
  http.request({
    url: '/p/refund/byOrder',
    method: 'GET',
    data: { orderNumber: orderNumber.value },
    hasCatch: true
  }).then(({ data }) => {
    existingRefund.value = data || null
  }).catch(() => {
    existingRefund.value = null
  })
}

const onTypeChange = (e) => {
  applyType.value = Number(e.detail.value)
}

const onReasonInput = (e) => {
  buyerMsg.value = e.detail.value
}

const submit = () => {
  if (!buyerMsg.value.trim()) {
    uni.showToast({ title: '请填写申请原因', icon: 'none' })
    return
  }
  submitting.value = true
  http.request({
    url: '/p/refund/apply',
    method: 'POST',
    data: {
      orderNumber: orderNumber.value,
      applyType: applyType.value,
      orderItemId: orderItemId.value || 0,
      buyerMsg: buyerMsg.value.trim()
    }
  }).then(() => {
    uni.showToast({ title: '已提交，等待商家审核', icon: 'none' })
    setTimeout(() => {
      uni.redirectTo({
        url: '/pages/refund-list/refund-list'
      })
    }, 800)
  }).finally(() => {
    submitting.value = false
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
.card {
  background: #fff;
  border-radius: 12rpx;
  padding: 30rpx;
}
.tit {
  font-size: 32rpx;
  font-weight: bold;
  margin-bottom: 20rpx;
}
.row {
  font-size: 26rpx;
  color: #555;
  line-height: 44rpx;
}
.label {
  margin-top: 28rpx;
  font-size: 28rpx;
  margin-bottom: 12rpx;
}
.type-group {
  display: flex;
  gap: 40rpx;
}
.type-item {
  font-size: 26rpx;
  margin-right: 40rpx;
}
.hint {
  color: #999;
  font-size: 22rpx;
  margin-top: 12rpx;
}
.reason {
  width: 100%;
  min-height: 180rpx;
  background: #fafafa;
  padding: 16rpx;
  box-sizing: border-box;
  font-size: 26rpx;
}
.submit {
  margin-top: 40rpx;
  background: #eb2444;
  color: #fff;
  border-radius: 50rpx;
  font-size: 28rpx;
}
</style>
