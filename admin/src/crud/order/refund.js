export const tableOption = {
  searchMenuSpan: 6,
  columnBtn: false,
  border: true,
  index: true,
  indexLabel: '序号',
  stripe: true,
  menuAlign: 'center',
  align: 'center',
  addBtn: false,
  editBtn: false,
  delBtn: false,
  menuWidth: 180,
  column: [
    {
      label: '退款编号',
      prop: 'refundSn',
      overHidden: true
    },
    {
      label: '订单编号',
      prop: 'orderNumber',
      search: true,
      overHidden: true
    },
    {
      label: '退款金额',
      prop: 'refundAmount'
    },
    {
      label: '申请类型',
      prop: 'applyType',
      type: 'select',
      dicData: [
        { label: '仅退款', value: 1 },
        { label: '退货退款', value: 2 }
      ]
    },
    {
      label: '审核状态',
      prop: 'refundSts',
      search: true,
      type: 'select',
      dicData: [
        { label: '待审核', value: 1 },
        { label: '已同意', value: 2 },
        { label: '已拒绝', value: 3 }
      ]
    },
    {
      label: '退款处理',
      prop: 'returnMoneySts',
      type: 'select',
      dicData: [
        { label: '处理中', value: 0 },
        { label: '成功', value: 1 },
        { label: '失败', value: -1 }
      ]
    },
    {
      label: '申请原因',
      prop: 'buyerMsg',
      overHidden: true
    },
    {
      label: '申请时间',
      prop: 'applyTime',
      type: 'datetime'
    }
  ]
}
