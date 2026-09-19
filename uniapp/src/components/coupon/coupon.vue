<template>
  <view
    class="coupon-item"
    :class="{ disabled: !usable, selected: checked }"
    @tap="onTap"
  >
    <view class="left">
      <view
        v-if="item.couponType === 2"
        class="amount discount"
      >
        <text class="num">
          {{ item.couponDiscount }}
        </text>
        <text class="unit">
          折
        </text>
      </view>
      <view
        v-else
        class="amount"
      >
        <text class="unit">
          ￥
        </text>
        <text class="num">
          {{ item.reduceAmount }}
        </text>
      </view>
      <view class="cond">
        {{ conditionText }}
      </view>
    </view>
    <view class="right">
      <view class="name">
        {{ item.couponName }}
      </view>
      <view class="sub">
        {{ item.subTitle || '' }}
      </view>
      <view class="time">
        {{ timeText }}
      </view>
      <view
        v-if="showCheck"
        class="check"
        :class="{ on: checked }"
      />
      <view
        v-else-if="actionText"
        class="action"
        @tap.stop="onAction"
      >
        {{ actionText }}
      </view>
    </view>
  </view>
</template>

<script setup>
const emit = defineEmits(['checkCoupon', 'receive', 'use'])

const props = defineProps({
  item: {
    type: Object,
    default: () => ({})
  },
  order: {
    type: [Boolean, String],
    default: false
  },
  canUse: {
    type: [Boolean, String],
    default: true
  },
  checked: {
    type: Boolean,
    default: false
  },
  /** center | mine | order */
  scene: {
    type: String,
    default: ''
  }
})

const isTrue = (v) => v === true || v === 'true'
const inOrder = computed(() => isTrue(props.order) || props.scene === 'order')
const usable = computed(() => {
  if (inOrder.value || props.scene === 'mine') {
    return isTrue(props.canUse)
  }
  return props.item.canUse !== false
})

const showCheck = computed(() => inOrder.value && usable.value)

const conditionText = computed(() => {
  const cond = Number(props.item.cashCondition || 0)
  if (cond <= 0) {
    return '无门槛'
  }
  return '满' + cond + '元可用'
})

const timeText = computed(() => {
  const start = props.item.userStartTime || props.item.startTime || ''
  const end = props.item.userEndTime || props.item.endTime || ''
  const s = String(start).slice(0, 10)
  const e = String(end).slice(0, 10)
  if (!s && !e) {
    return ''
  }
  return s + ' 至 ' + e
})

const actionText = computed(() => {
  if (inOrder.value) {
    return ''
  }
  if (props.scene === 'center') {
    return '立即领取'
  }
  if (props.scene === 'mine') {
    if (props.item.status === 1) {
      return '已使用'
    }
    if (props.item.status === 2) {
      return '已过期'
    }
    return '去使用'
  }
  return ''
})

const onTap = () => {
  if (!showCheck.value) {
    return
  }
  const couponId = props.item.couponId
  emit('checkCoupon', { couponId, detail: { couponId } })
}

const onAction = () => {
  if (props.scene === 'center') {
    emit('receive', props.item)
    return
  }
  if (props.scene === 'mine' && props.item.status === 0) {
    emit('use', props.item)
  }
}
</script>

<style scoped lang="scss">
.coupon-item {
  display: flex;
  background: #fff;
  border-radius: 8rpx;
  margin: 16rpx 0;
  overflow: hidden;
  min-height: 180rpx;
}
.coupon-item.disabled {
  opacity: 0.55;
}
.coupon-item.selected .check {
  border-color: #eb2444;
  background: #eb2444;
}
.left {
  width: 220rpx;
  background: linear-gradient(180deg, #eb2444, #ff6b6b);
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20rpx 8rpx;
}
.coupon-item.disabled .left {
  background: #bbb;
}
.amount {
  display: flex;
  align-items: baseline;
  .unit {
    font-size: 28rpx;
  }
  .num {
    font-size: 52rpx;
    font-weight: 700;
    line-height: 1;
  }
}
.amount.discount .num {
  font-size: 48rpx;
}
.cond {
  margin-top: 8rpx;
  font-size: 22rpx;
}
.right {
  flex: 1;
  position: relative;
  padding: 24rpx 24rpx 24rpx 28rpx;
}
.name {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
}
.sub {
  font-size: 24rpx;
  color: #999;
  margin-top: 8rpx;
  min-height: 24rpx;
}
.time {
  font-size: 22rpx;
  color: #aaa;
  margin-top: 16rpx;
}
.action {
  position: absolute;
  right: 24rpx;
  bottom: 24rpx;
  color: #eb2444;
  border: 1rpx solid #eb2444;
  border-radius: 28rpx;
  padding: 6rpx 20rpx;
  font-size: 24rpx;
}
.check {
  position: absolute;
  right: 24rpx;
  top: 50%;
  margin-top: -16rpx;
  width: 32rpx;
  height: 32rpx;
  border: 2rpx solid #ccc;
  border-radius: 50%;
}
.check.on::after {
  content: '';
  display: block;
  width: 16rpx;
  height: 16rpx;
  margin: 6rpx;
  background: #fff;
  border-radius: 50%;
}
</style>
