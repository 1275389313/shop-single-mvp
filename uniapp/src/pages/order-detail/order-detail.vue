<template>
  <view class="container">
    <view class="order-detail">
      <view
        v-if="userAddrDto"
        class="delivery-addr"
      >
        <view class="user-info">
          <text class="item">
            {{ userAddrDto.receiver }}
          </text>
          <text class="item">
            {{ userAddrDto.mobile }}
          </text>
        </view>
        <view class="addr">
          {{ userAddrDto.province }}{{ userAddrDto.city }}{{ userAddrDto.area }}{{ userAddrDto.addr }}
        </view>
      </view>

      <!-- 商品信息 -->
      <view
        v-if="orderItemDtos"
        class="prod-item"
      >
        <block
          v-for="(item, index) in orderItemDtos"
          :key="index"
        >
          <view
            class="item-cont"
            :data-prodid="item.prodId"
            @tap="toProdPage"
          >
            <view class="prod-pic">
              <image :src="item.pic" />
            </view>
            <view class="prod-info">
              <view class="prodname">
                {{ item.prodName }}
              </view>
              <view class="prod-info-cont">
                <text class="number">
                  数量：{{ item.prodCount }}
                </text>
                <text class="info-item">
                  {{ item.skuName }}
                </text>
              </view>
              <view class="price-nums clearfix">
                <text class="prodprice">
                  <text class="symbol">
                    ￥
                  </text>
                  <text class="big-num">
                    {{ wxs.parsePrice(item.price)[0] }}
                  </text>
                  <text class="small-num">
                    .{{ wxs.parsePrice(item.price)[1] }}
                  </text>
                </text>
                <view class="btn-box" />
              </view>
            </view>
          </view>
        </block>
      </view>

      <!-- 订单信息 -->
      <view class="order-msg">
        <view class="msg-item">
          <view class="item">
            <text class="item-tit">
              订单编号：
            </text>
            <text class="item-txt">
              {{ orderNumber }}
            </text>
          </view>
          <view class="item">
            <text class="item-tit">
              下单时间：
            </text>
            <text class="item-txt">
              {{ createTime }}
            </text>
          </view>
        </view>
        <view class="msg-item">
          <view class="item">
            <text class="item-tit">
              支付方式：
            </text>
            <text class="item-txt">
              微信支付
            </text>
          </view>
          <view class="item">
            <text class="item-tit">
              配送方式：
            </text>
            <text class="item-txt">
              普通配送
            </text>
          </view>
          <view class="item">
            <text
              v-if="!!remarks"
              class="item-tit"
            >
              订单备注：
            </text>
            <text class="item-txt remarks">
              {{ remarks }}
            </text>
          </view>
          <view
            v-if="dvyFlowId"
            class="item"
          >
            <text class="item-tit">
              发货物流：
            </text>
            <text
              class="item-txt link"
              @tap="toShipmentTrack"
            >
              {{ dvyFlowId }} 查看轨迹
            </text>
          </view>
        </view>
      </view>

      <view class="order-msg">
        <view class="msg-item">
          <view class="item">
            <view class="item-tit">
              订单总额：
            </view>
            <view class="item-txt price">
              <text class="symbol">
                ￥
              </text>
              <text class="big-num">
                {{ wxs.parsePrice(total)[0] }}
              </text>
              <text class="small-num">
                .{{ wxs.parsePrice(total)[1] }}
              </text>
            </view>
          </view>
          <view class="item">
            <view class="item-tit">
              运费：
            </view>
            <view class="item-txt price">
              <text class="symbol">
                ￥
              </text>
              <text class="big-num">
                {{ wxs.parsePrice(transfee)[0] }}
              </text>
              <text class="small-num">
                .{{ wxs.parsePrice(transfee)[1] }}
              </text>
            </view>
          </view>
          <view class="item">
            <view class="item-tit">
              优惠券：
            </view>
            <view class="item-txt price">
              <text class="symbol">
                -￥
              </text>
              <text class="big-num">
                {{ wxs.parsePrice(reduceAmount)[0] }}
              </text>
              <text class="small-num">
                .{{ wxs.parsePrice(reduceAmount)[1] }}
              </text>
            </view>
          </view>
          <view class="item payment">
            <view class="item-txt price">
              实付款：
              <text class="symbol">
                ￥
              </text>
              <text class="big-num">
                {{ wxs.parsePrice(actualTotal)[0] }}
              </text>
              <text class="small-num">
                .{{ wxs.parsePrice(actualTotal)[1] }}
              </text>
            </view>
          </view>
        </view>
      </view>

      <view
        v-if="refund"
        class="order-msg"
      >
        <view class="msg-item">
          <view class="item">
            <text class="item-tit">
              售后状态：
            </text>
            <text class="item-txt">
              {{ refundFlowText(refund) }}
            </text>
          </view>
          <view
            v-if="refund.applyType === 2"
            class="item"
          >
            <text class="item-tit">
              申请类型：
            </text>
            <text class="item-txt">
              退货退款
            </text>
          </view>
          <view
            v-if="refund.expressNo"
            class="item"
          >
            <text class="item-tit">
              退货物流：
            </text>
            <text class="item-txt">
              {{ refund.expressName }} {{ refund.expressNo }}
            </text>
            <text
              class="item-txt link"
              @tap="toReturnTrack"
            >
              查看轨迹
            </text>
          </view>
        </view>
      </view>

      <!-- 底部栏 -->
      <view
        v-if="status==1 || status==2 || status==3 || status==5 || status==6"
        class="order-detail-footer"
      >
        <text
          v-if="status==5||status==6"
          class="dele-order"
          @tap="delOrderList"
        >
          删除订单
        </text>
        <view class="footer-box">
          <text
            v-if="status==1"
            class="apply-service"
            @tap="onCancelOrder"
          >
            取消订单
          </text>
          <text
            v-if="status==1"
            class="buy-again"
            @tap="normalPay"
          >
            付款
          </text>
          <text
            v-if="canRefund"
            class="apply-service"
            @tap="toRefundApply"
          >
            申请退款
          </text>
          <text
            v-if="canFillExpress"
            class="apply-service"
            @tap="toRefundApply"
          >
            {{ refund && refund.expressNo ? '修改退货物流' : '填写退货物流' }}
          </text>
          <text
            v-if="refund && refund.refundSts !== 3 && !canFillExpress"
            class="apply-service"
            @tap="toRefundApply"
          >
            售后详情
          </text>
          <text
            v-if="status==3 || (status==5 && dvyFlowId)"
            class="apply-service"
            @tap="toShipmentTrack"
          >
            查看物流
          </text>
          <text
            v-if="status==3"
            class="buy-again"
            @tap="onConfirmReceive"
          >
            确认收货
          </text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { refundFlowText, canEditReturnExpress } from '@/utils/refund.js'

const wxs = number()

/**
 * 生命周期函数--监听页面加载
 */
onLoad((options) => {
  loadOrderDetail(options.orderNum)
})

onShow(() => {
  if (orderNumber.value) {
    loadRefund(orderNumber.value)
  }
})

/**
 * 跳转商品详情页
 * @param e
 */
const toProdPage = (e) => {
  const prodid = e.currentTarget.dataset.prodid
  uni.navigateTo({
    url: '/pages/prod/prod?prodid=' + prodid
  })
}

const remarks = ref('')
const orderItemDtos = ref([])
const reduceAmount = ref('')
const transfee = ref('')
const status = ref(0)
const actualTotal = ref(0)
const userAddrDto = ref(null)
const orderNumber = ref('')
const createTime = ref('')
const total = ref(0) // 商品总额
const dvyFlowId = ref('')
const refund = ref(null)
const canRefund = computed(() => {
  const sts = Number(status.value)
  return (sts === 2 || sts === 3 || sts === 5) && (!refund.value || refund.value.refundSts === 3)
})
const canFillExpress = computed(() => canEditReturnExpress(refund.value))
/**
 * 加载订单数据
 */
const loadOrderDetail = (orderNum) => {
  uni.showLoading() // 加载订单详情
  http.request({
    url: '/p/myOrder/orderDetail',
    method: 'GET',
    data: {
      orderNumber: orderNum
    }
  })
    .then(({ data }) => {
      orderNumber.value = orderNum
      actualTotal.value = data.actualTotal
      userAddrDto.value = data.userAddrDto
      remarks.value = data.remarks
      orderItemDtos.value = data.orderItemDtos
      createTime.value = data.createTime
      status.value = data.status
      transfee.value = data.transfee
      reduceAmount.value = data.reduceAmount
      total.value = data.total
      dvyFlowId.value = data.dvyFlowId || ''
      uni.hideLoading()
      loadRefund(orderNum)
    })
}

const loadRefund = (orderNum) => {
  http.request({
    url: '/p/refund/byOrder',
    method: 'GET',
    data: { orderNumber: orderNum },
    hasCatch: true
  }).then(({ data }) => {
    refund.value = data || null
  }).catch(() => {
    refund.value = null
  })
}

const toRefundApply = () => {
  uni.navigateTo({
    url: '/pages/refund-apply/refund-apply?orderNum=' + orderNumber.value
  })
}

const toShipmentTrack = () => {
  uni.navigateTo({
    url: '/pages/express-delivery/express-delivery?orderNum=' + orderNumber.value
  })
}

const toReturnTrack = () => {
  if (!refund.value || !refund.value.refundSn) {
    return
  }
  uni.navigateTo({
    url: '/pages/express-delivery/express-delivery?refundSn=' + refund.value.refundSn
  })
}

const onCancelOrder = () => {
  uni.showModal({
    title: '',
    content: '要取消此订单？',
    success (res) {
      if (res.confirm) {
        http.request({
          url: '/p/myOrder/cancel/' + orderNumber.value,
          method: 'PUT',
          data: {}
        }).then(() => {
          loadOrderDetail(orderNumber.value)
        })
      }
    }
  })
}

const normalPay = () => {
  uni.showLoading({ mask: true })
  http.request({
    url: '/p/order/normalPay',
    method: 'POST',
    data: {
      orderNumbers: orderNumber.value,
      payType: 1
    }
  }).then(({ data }) => {
    uni.hideLoading()
    if (data && (data.paid === undefined || data.paid)) {
      uni.navigateTo({
        url: '/pages/pay-result/pay-result?sts=1&orderNumbers=' + orderNumber.value
      })
    } else {
      uni.showToast({ title: '支付失败！', icon: 'none' })
    }
  }).catch(() => {
    uni.hideLoading()
  })
}

const onConfirmReceive = () => {
  uni.showModal({
    title: '',
    content: '我已收到货？',
    confirmColor: '#eb2444',
    success (res) {
      if (res.confirm) {
        http.request({
          url: '/p/myOrder/receipt/' + orderNumber.value,
          method: 'PUT'
        }).then(() => {
          loadOrderDetail(orderNumber.value)
        })
      }
    }
  })
}

/**
 * 删除已完成||已取消的订单
 */
const delOrderList = () => {
  uni.showModal({
    title: '',
    content: '确定要删除此订单吗？',
    confirmColor: '#eb2444',
    success (res) {
      if (res.confirm) {
        uni.showLoading()
        http.request({
          url: '/p/myOrder/' + orderNumber.value,
          method: 'DELETE'
        })
          .then(() => {
            uni.hideLoading()
            uni.showToast({
              title: res || '删除成功',
              icon: 'none'
            })
            setTimeout(() => {
              uni.redirectTo({
                url: '/pages/orderList/orderList'
              })
            }, 1000)
          })
      }
    }
  })
}
</script>

<style scoped lang="scss">
@use './order-detail.scss';
</style>
