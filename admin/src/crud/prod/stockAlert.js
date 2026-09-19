export const tableOption = {
  searchMenuSpan: 6,
  columnBtn: false,
  border: true,
  index: true,
  indexLabel: '序号',
  stripe: true,
  menuAlign: 'center',
  menuWidth: 280,
  align: 'center',
  addBtn: false,
  editBtn: false,
  delBtn: false,
  column: [
    {
      label: '商品',
      prop: 'prodName',
      search: true,
      minWidth: 160
    },
    {
      label: 'SKU',
      prop: 'skuName',
      minWidth: 120
    },
    {
      label: '可售库存',
      prop: 'stocks',
      slot: true,
      width: 110
    },
    {
      label: '预警阈值',
      prop: 'effectiveThreshold',
      width: 100
    },
    {
      label: 'SKU阈值',
      prop: 'stocksArm',
      slot: true,
      width: 150
    },
    {
      label: '商品状态',
      prop: 'prodStatus',
      search: true,
      slot: true,
      type: 'select',
      width: 110,
      dicData: [
        { label: '未上架', value: 0 },
        { label: '上架', value: 1 }
      ]
    }
  ]
}
