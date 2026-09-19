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
const current = ref({})
const auditForm = reactive({
  refundId: null,
  refundSts: 2,
  sellerMsg: ''
})

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
  current.value = row
  auditReadonly.value = true
  auditVisible.value = true
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
  }).then(() => {
    ElMessage.success('审核成功（mock 退款，未调用微信退款接口）')
    auditVisible.value = false
    getDataList(page)
  }).finally(() => {
    auditSubmitting.value = false
  })
}
</script>
