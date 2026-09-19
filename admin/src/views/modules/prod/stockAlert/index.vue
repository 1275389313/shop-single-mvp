<template>
  <div class="mod-stock-alert">
    <el-card
      shadow="never"
      class="threshold-card"
    >
      <div class="threshold-row">
        <span class="threshold-label">全局低库存阈值</span>
        <el-input-number
          v-model="globalThreshold"
          :min="0"
          :max="999999"
          :disabled="!canUpdate"
          controls-position="right"
        />
        <el-button
          v-if="canUpdate"
          type="primary"
          :loading="savingConfig"
          @click="saveConfig"
        >
          保存
        </el-button>
        <el-tag type="warning">
          上架低库存 SKU：{{ lowStockCount }}
        </el-tag>
      </div>
      <p class="threshold-hint">
        比较的是 SKU 可售库存（下单扣减的 stocks）。库存 ≤ 阈值即出现在下方列表。SKU 阈值留空跟随全局，填 -1 则该规格不预警。不发短信、不改 mock 支付。
      </p>
    </el-card>

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
      <template #stocks="scope">
        <el-tag
          :type="scope.row.stocks === 0 ? 'danger' : 'warning'"
        >
          {{ scope.row.stocks }}
        </el-tag>
      </template>
      <template #stocksArm="scope">
        <el-input-number
          v-model="scope.row.stocksArm"
          :min="-1"
          :max="999999"
          :disabled="!canUpdate"
          controls-position="right"
          placeholder="空=全局"
          style="width: 120px"
        />
      </template>
      <template #prodStatus="scope">
        <el-tag v-if="scope.row.prodStatus === 1">
          上架
        </el-tag>
        <el-tag
          v-else
          type="info"
        >
          未上架
        </el-tag>
      </template>
      <template #menu="scope">
        <el-button
          v-if="canUpdate"
          type="primary"
          @click="saveSku(scope.row)"
        >
          保存阈值
        </el-button>
        <el-button
          v-if="isAuth('prod:prod:update')"
          type="primary"
          link
          @click="editProd(scope.row.prodId)"
        >
          改商品
        </el-button>
      </template>
    </avue-crud>
  </div>
</template>

<script setup>
import { isAuth } from '@/utils'
import { ElMessage } from 'element-plus'
import { tableOption } from '@/crud/prod/stockAlert.js'

const canUpdate = isAuth('prod:stockAlert:update')
const router = useRouter()
const dataList = ref([])
const page = reactive({
  total: 0,
  currentPage: 1,
  pageSize: 10
})
const dataListLoading = ref(false)
const globalThreshold = ref(10)
const lowStockCount = ref(0)
const savingConfig = ref(false)

const loadConfig = () => {
  http({
    url: http.adornUrl('/prod/stockAlert/config'),
    method: 'get',
    params: http.adornParams()
  }).then(({ data }) => {
    globalThreshold.value = data.globalThreshold
    lowStockCount.value = data.count || 0
  })
}

const getDataList = (pageParam, params, done) => {
  dataListLoading.value = true
  http({
    url: http.adornUrl('/prod/stockAlert/page'),
    method: 'get',
    params: http.adornParams(Object.assign({
      current: pageParam == null ? page.currentPage : pageParam.currentPage,
      size: pageParam == null ? page.pageSize : pageParam.pageSize
    }, params))
  }).then(({ data }) => {
    dataList.value = data.records
    page.total = data.total
    dataListLoading.value = false
    if (done) done()
    loadConfig()
  }).catch(() => {
    dataListLoading.value = false
    if (done) done()
  })
}

const saveConfig = () => {
  savingConfig.value = true
  http({
    url: http.adornUrl('/prod/stockAlert/config'),
    method: 'put',
    data: http.adornData({ globalThreshold: globalThreshold.value })
  }).then(() => {
    ElMessage.success('已保存全局阈值')
    getDataList(page)
  }).finally(() => {
    savingConfig.value = false
  })
}

const saveSku = (row) => {
  http({
    url: http.adornUrl('/prod/stockAlert/sku'),
    method: 'put',
    data: http.adornData({
      skuId: row.skuId,
      stocksArm: row.stocksArm === undefined || row.stocksArm === '' ? null : row.stocksArm
    })
  }).then(() => {
    ElMessage.success('已保存 SKU 阈值')
    getDataList(page)
  })
}

const editProd = (prodId) => {
  router.push({
    path: '/prodInfo',
    query: { prodId }
  })
}

const refreshChange = () => {
  getDataList(page)
}

const onSearch = (params, done) => {
  getDataList(page, params, done)
}

onMounted(() => {
  loadConfig()
})
</script>

<style lang="scss" scoped>
.threshold-card {
  margin-bottom: 16px;
}
.threshold-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.threshold-label {
  font-weight: 600;
}
.threshold-hint {
  margin: 12px 0 0;
  color: #909399;
  font-size: 13px;
  line-height: 1.5;
}
</style>
