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
  column: [
    {
      label: '名称',
      prop: 'couponName',
      search: true
    },
    {
      label: '类型',
      prop: 'couponType',
      search: true,
      slot: true,
      type: 'select',
      dicData: [
        { label: '满减', value: 1 },
        { label: '折扣', value: 2 }
      ]
    },
    {
      label: '门槛',
      prop: 'cashCondition'
    },
    {
      label: '优惠',
      prop: 'benefit',
      slot: true
    },
    {
      label: '剩余库存',
      prop: 'stocks',
      slot: true
    },
    {
      label: '每人限领',
      prop: 'limitNum'
    },
    {
      label: '领取开始',
      prop: 'startTime'
    },
    {
      label: '领取结束',
      prop: 'endTime'
    },
    {
      label: '状态',
      prop: 'status',
      search: true,
      slot: true,
      type: 'select',
      dicData: [
        { label: '下线', value: 0 },
        { label: '投放', value: 1 }
      ]
    }
  ]
}
