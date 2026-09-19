<template>
  <view class="container">
    <view
      v-if="existingRefund"
      class="card"
    >
      <view class="tit">
        {{ canReapply ? '上次申请' : '售后进度' }}
      </view>
      <view class="row">
        退款编号：{{ existingRefund.refundSn }}
      </view>
      <view class="row">
        类型：{{ existingRefund.applyType === 2 ? '退货退款' : '仅退款' }}
      </view>
      <view class="row">
        状态：{{ refundFlowText(existingRefund) }}
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
      <view
        v-if="existingRefund.sellerMsg && existingRefund.refundSts !== 3"
        class="row"
      >
        商家备注：{{ existingRefund.sellerMsg }}
      </view>
      <view
        v-if="existingRefund.expressNo"
        class="row"
      >
        退货物流：{{ existingRefund.expressName }} {{ existingRefund.expressNo }}
      </view>
      <view
        v-if="existingRefund.shipTime"
        class="row"
      >
        寄回时间：{{ existingRefund.shipTime }}
      </view>
      <view
        v-if="existingRefund.receiveTime"
        class="row"
      >
        商家收货：{{ existingRefund.receiveTime }}
      </view>
      <view
        v-if="existingRefund.refundTime"
        class="row"
      >
        退款时间：{{ existingRefund.refundTime }}（mock，未打微信）
      </view>
    </view>

    <view
      v-if="showExpressForm"
      class="card"
    >
      <view class="tit">
        填写退货物流
      </view>
      <view class="hint">
        商家已同意退货，请寄回商品并填写快递信息。单号可在商家确认收货前修改。
      </view>
      <view class="label">
        物流公司
      </view>
      <picker
        :range="companyNames"
        @change="onCompanyPick"
      >
        <view class="picker">
          {{ expressName || '请选择物流公司' }}
        </view>
      </picker>
      <input
        class="input"
        maxlength="50"
        placeholder="也可手动输入公司名称"
        :value="expressName"
        @input="onExpressNameInput"
      >
      <view class="label">
        快递单号
      </view>
      <input
        class="input"
        maxlength="50"
        placeholder="请填写快递单号"
        :value="expressNo"
        @input="onExpressNoInput"
      >
      <button
        class="submit"
        :disabled="expressSubmitting"
        @tap="submitExpress"
      >
        {{ existingRefund && existingRefund.expressNo ? '更新物流信息' : '提交物流信息' }}
      </button>
    </view>

    <view
      v-if="showApplyForm"
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
        退货退款：商家审核同意后，请在本页填写退货快递单号；商家确认收货后 mock 退款。
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
import { refundFlowText, canEditReturnExpress } from '@/utils/refund.js'

const FALLBACK_COMPANIES = ['顺丰快递公司', '中通速递', '圆通速递', '韵达快递', '申通快递公司', 'EMS', '京东物流']

const orderNumber = ref('')
const orderItemId = ref(0)
const actualTotal = ref(0)
const applyType = ref(1)
const buyerMsg = ref('')
const submitting = ref(false)
const existingRefund = ref(null)
const companyNames = ref(FALLBACK_COMPANIES)
const expressName = ref('')
const expressNo = ref('')
const expressSubmitting = ref(false)

const canReapply = computed(() => existingRefund.value && existingRefund.value.refundSts === 3)
const showApplyForm = computed(() => !existingRefund.value || canReapply.value)
const showExpressForm = computed(() => canEditReturnExpress(existingRefund.value))

onLoad((options) => {
  orderNumber.value = options.orderNum || options.orderNumber || ''
  orderItemId.value = options.orderItemId ? Number(options.orderItemId) : 0
  loadOrder()
  loadRefund()
  loadCompanies()
})

onShow(() => {
  if (orderNumber.value) {
    loadRefund()
  }
})

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

const syncExpressForm = (refund) => {
  expressName.value = refund && refund.expressName ? refund.expressName : ''
  expressNo.value = refund && refund.expressNo ? refund.expressNo : ''
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
    syncExpressForm(data)
  }).catch(() => {
    existingRefund.value = null
  })
}

const loadCompanies = () => {
  http.request({
    url: '/p/refund/deliveryList',
    method: 'GET',
    hasCatch: true
  }).then(({ data }) => {
    if (data && data.length) {
      companyNames.value = data
    }
  }).catch(() => {
    companyNames.value = FALLBACK_COMPANIES
  })
}

const onTypeChange = (e) => {
  applyType.value = Number(e.detail.value)
}

const onReasonInput = (e) => {
  buyerMsg.value = e.detail.value
}

const onCompanyPick = (e) => {
  const idx = Number(e.detail.value)
  expressName.value = companyNames.value[idx] || ''
}

const onExpressNameInput = (e) => {
  expressName.value = e.detail.value
}

const onExpressNoInput = (e) => {
  expressNo.value = e.detail.value
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
    buyerMsg.value = ''
    loadRefund()
  }).finally(() => {
    submitting.value = false
  })
}

const submitExpress = () => {
  if (!expressName.value.trim() || !expressNo.value.trim()) {
    uni.showToast({ title: '请填写物流公司和单号', icon: 'none' })
    return
  }
  if (!existingRefund.value || !existingRefund.value.refundSn) {
    uni.showToast({ title: '退款单不存在', icon: 'none' })
    return
  }
  expressSubmitting.value = true
  http.request({
    url: '/p/refund/express',
    method: 'PUT',
    data: {
      refundSn: existingRefund.value.refundSn,
      expressName: expressName.value.trim(),
      expressNo: expressNo.value.trim()
    }
  }).then(({ data }) => {
    uni.showToast({ title: '已提交，等待商家确认收货', icon: 'none' })
    existingRefund.value = data || existingRefund.value
    syncExpressForm(existingRefund.value)
  }).finally(() => {
    expressSubmitting.value = false
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
  margin-bottom: 16rpx;
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
.picker {
  background: #fafafa;
  padding: 20rpx 16rpx;
  font-size: 26rpx;
  color: #333;
  border-radius: 8rpx;
}
.input {
  width: 100%;
  background: #fafafa;
  padding: 16rpx;
  box-sizing: border-box;
  font-size: 26rpx;
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
