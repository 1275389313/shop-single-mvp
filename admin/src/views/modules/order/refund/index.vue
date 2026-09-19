<template>
  <div class="mod-order-refund">
    <avue-crud
      ref="crudRef"
      :page="page"
      :data="dataList"
      :table-loading="dataListLoading"
      :option="tableOption"
      @search-change="onSearch"
      @on-load="getDataList"
      @refresh-change="refreshChange"
    >
      <template #menu="scope">
        <el-button
          v-if="isAuth('order:refund:audit') && scope.row.refundSts === 1"
          type="primary"
          @click="openAudit(scope.row)"
        >
          审核
        </el-button>
        <el-button
          v-if="canConfirmReceive(scope.row)"
          type="success"
          @click="openReceive(scope.row)"
        >
          确认收货退款
        </el-button>
        <el-button
          v-if="isAuth('order:refund:info')"
          type="primary"
          link
          @click="openDetail(scope.row)"
        >
          详情
        </el-button>
      </template>
    </avue-crud>

    <el-dialog
      v-model="auditVisible"
      :title="auditReadonly ? '退款详情' : '退款审核'"
      width="560px"
      :close-on-click-modal="false"
    >
      <el-descriptions
        :column="1"
        border
      >
        <el-descriptions-item label="退款编号">
          {{ current.refundSn }}
        </el-descriptions-item>
        <el-descriptions-item label="订单编号">
          {{ current.orderNumber }}
        </el-descriptions-item>
        <el-descriptions-item label="退款金额">
          ￥{{ current.refundAmount }}
        </el-descriptions-item>
        <el-descriptions-item label="申请类型">
          {{ current.applyType === 2 ? '退货退款' : '仅退款' }}
        </el-descriptions-item>
        <el-descriptions-item label="售后进度">
          {{ current.flowText || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="申请原因">
          {{ current.buyerMsg || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="申请时间">
          {{ current.applyTime }}
        </el-descriptions-item>
        <el-descriptions-item
          v-if="current.sellerMsg"
          label="卖家备注"
        >
          {{ current.sellerMsg }}
        </el-descriptions-item>
        <el-descriptions-item
          v-if="current.rejectMessage"
          label="拒绝原因"
        >
          {{ current.rejectMessage }}
        </el-descriptions-item>
        <el-descriptions-item
          v-if="current.applyType === 2"
          label="退货物流"
        >
          {{ current.expressName || '-' }}
          {{ current.expressNo ? (' / ' + current.expressNo) : '' }}
        </el-descriptions-item>
        <el-descriptions-item
          v-if="current.shipTime"
          label="买家寄回时间"
        >
          {{ current.shipTime }}
        </el-descriptions-item>
        <el-descriptions-item
          v-if="current.receiveTime"
          label="商家收货时间"
        >
          {{ current.receiveTime }}
        </el-descriptions-item>
        <el-descriptions-item
          v-if="current.receiveMessage"
          label="收货备注"
        >
          {{ current.receiveMessage }}
        </el-descriptions-item>
        <el-descriptions-item
          v-if="current.refundTime"
          label="退款时间"
        >
          {{ current.refundTime }}
        </el-descriptions-item>
      </el-descriptions>
      <el-form
        v-if="!auditReadonly"
        style="margin-top: 16px;"
        label-width="90px"
      >
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditForm.refundSts">
            <el-radio :label="2">
              同意
            </el-radio>
            <el-radio :label="3">
              拒绝
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-alert
          v-if="current.applyType === 2 && auditForm.refundSts === 2"
          title="退货退款：同意后不会立刻退款，需买家填写物流、商家确认收货后才 mock 退款。"
          type="info"
          show-icon
          :closable="false"
          style="margin-bottom: 12px;"
        />
        <el-form-item :label="auditForm.refundSts === 3 ? '拒绝原因' : '卖家备注'">
          <el-input
            v-model="auditForm.sellerMsg"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
            :placeholder="auditForm.refundSts === 3 ? '请填写拒绝原因' : '选填'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditVisible = false">
          关闭
        </el-button>
        <el-button
          v-if="!auditReadonly"
          type="primary"
          :loading="auditSubmitting"
          @click="submitAudit"
        >
          提交
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="receiveVisible"
      title="确认收货并退款"
      width="520px"
      :close-on-click-modal="false"
    >
      <el-descriptions
        :column="1"
        border
      >
        <el-descriptions-item label="退款编号">
          {{ current.refundSn }}
        </el-descriptions-item>
        <el-descriptions-item label="物流公司">
          {{ current.expressName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="退货单号">
          {{ current.expressNo || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="寄回时间">
          {{ current.shipTime || '-' }}
        </el-descriptions-item>
      </el-descriptions>
      <el-form
        style="margin-top: 16px;"
        label-width="90px"
      >
        <el-form-item label="收货备注">
          <el-input
            v-model="receiveForm.receiveMessage"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
            placeholder="选填"
          />
        </el-form-item>
      </el-form>
      <el-alert
        title="确认后将标记退款成功。当前为 mock 支付，不会调用微信退款接口。"
        type="warning"
        show-icon
        :closable="false"
      />
      <template #footer>
        <el-button @click="receiveVisible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="receiveSubmitting"
          @click="submitReceive"
        >
          确认收货并退款
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { isAuth } from '@/utils'
import { ElMessage } from 'element-plus'
import { tableOption } from '@/crud/order/refund'

const dataList = ref([])
const page = reactive({
  total: 0,
  currentPage: 1,
  pageSize: 10
})
const dataListLoading = ref(false)
const searchParams = ref({})
const auditVisible = ref(false)
const auditReadonly = ref(false)
const auditSubmitting = ref(false)
const receiveVisible = ref(false)
const receiveSubmitting = ref(false)
const current = ref({})
const auditForm = reactive({
  refundId: null,
  refundSts: 2,
  sellerMsg: ''
})
const receiveForm = reactive({
  refundId: null,
  receiveMessage: ''
})

const canConfirmReceive = (row) => {
  return (isAuth('order:refund:receive') || isAuth('order:refund:audit')) && row.flowCode === 'WAIT_RECEIVE'
}

const getDataList = (pageParam, params, done) => {
  dataListLoading.value = true
  pageParam = pageParam == null ? page : pageParam
  http({
    url: http.adornUrl('/order/refund/page'),
    method: 'get',
    params: http.adornParams(Object.assign({
      current: pageParam == null ? page.currentPage : pageParam.currentPage,
      size: pageParam == null ? page.pageSize : pageParam.pageSize,
      refundSts: (params && params.refundSts) || searchParams.value.refundSts,
      orderNumber: (params && params.orderNumber) || searchParams.value.orderNumber
    }, params))
  }).then(({ data }) => {
    dataList.value = data.records
    page.total = data.total
    dataListLoading.value = false
    if (done) done()
  }).catch(() => {
    dataListLoading.value = false
    if (done) done()
  })
}

const refreshChange = () => {
  getDataList(page)
}

const onSearch = (params, done) => {
  searchParams.value = params || {}
  getDataList(page, params, done)
}

const openAudit = (row) => {
  current.value = row
  auditReadonly.value = false
  auditForm.refundId = row.refundId
  auditForm.refundSts = 2
  auditForm.sellerMsg = ''
  auditVisible.value = true
}

const openDetail = (row) => {
  http({
    url: http.adornUrl('/order/refund/info'),
    method: 'get',
    params: http.adornParams({ refundId: row.refundId })
  }).then(({ data }) => {
    current.value = data || row
    auditReadonly.value = true
    auditVisible.value = true
  }).catch(() => {
    current.value = row
    auditReadonly.value = true
    auditVisible.value = true
  })
}

const openReceive = (row) => {
  current.value = row
  receiveForm.refundId = row.refundId
  receiveForm.receiveMessage = ''
  receiveVisible.value = true
}

const submitAudit = () => {
  if (auditForm.refundSts === 3 && !String(auditForm.sellerMsg || '').trim()) {
    ElMessage.error('拒绝时请填写原因')
    return
  }
  auditSubmitting.value = true
  http({
    url: http.adornUrl('/order/refund/audit'),
    method: 'put',
    data: http.adornData({
      refundId: auditForm.refundId,
      refundSts: auditForm.refundSts,
      sellerMsg: auditForm.sellerMsg
    })
  }).then(({ data }) => {
    const isReturnGoods = (data && data.applyType === 2) || current.value.applyType === 2
    if (auditForm.refundSts === 3) {
      ElMessage.success('已拒绝')
    } else if (isReturnGoods) {
      ElMessage.success('已同意，等待买家寄回商品')
    } else {
      ElMessage.success('审核成功（mock 退款，未调用微信退款接口）')
    }
    auditVisible.value = false
    getDataList(page)
  }).finally(() => {
    auditSubmitting.value = false
  })
}

const submitReceive = () => {
  receiveSubmitting.value = true
  http({
    url: http.adornUrl('/order/refund/receive'),
    method: 'put',
    data: http.adornData({
      refundId: receiveForm.refundId,
      receiveMessage: receiveForm.receiveMessage
    })
  }).then(() => {
    ElMessage.success('已确认收货，mock 退款完成（未调用微信退款）')
    receiveVisible.value = false
    getDataList(page)
  }).finally(() => {
    receiveSubmitting.value = false
  })
}
</script>
