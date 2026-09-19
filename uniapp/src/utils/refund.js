export function refundFlowText (item) {
  if (!item) return ''
  if (item.flowText) return item.flowText
  if (item.refundSts === 1) return '待商家审核'
  if (item.refundSts === 3) return '商家已拒绝'
  if (item.applyType === 2 && item.returnMoneySts !== 1) {
    return item.expressNo ? '待商家收货' : '请寄回商品'
  }
  return '退款成功'
}

export function canEditReturnExpress (item) {
  if (!item || item.applyType !== 2 || item.refundSts !== 2) return false
  if (item.returnMoneySts === 1 || item.receiveTime) return false
  return true
}
