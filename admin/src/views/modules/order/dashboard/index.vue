<template>
  <div
    v-loading="loading"
    class="mod-dashboard"
  >
    <el-card
      shadow="never"
      class="head-card"
    >
      <div class="head-row">
        <div>
          <div class="title">
            数据看板
          </div>
          <p class="hint">
            开源 mall4j 没有统计报表接口，这里直接汇总本店 <code>tz_order</code> / <code>tz_order_refund</code>。
            时区 {{ overview?.timezone || 'Asia/Shanghai' }}。
            <el-tag
              v-if="overview?.mockPay"
              type="warning"
              size="small"
              class="mock-tag"
            >
              mock 支付中，GMV 含联调已付订单
            </el-tag>
          </p>
        </div>
        <el-button
          type="primary"
          :loading="loading"
          @click="load"
        >
          刷新
        </el-button>
      </div>
      <el-alert
        :title="overview?.note || 'GMV 按支付时间；待付款/关闭按下单时间看当前状态。'"
        type="info"
        :closable="false"
        show-icon
      />
    </el-card>

    <el-row
      :gutter="16"
      class="stat-row"
    >
      <el-col
        v-for="item in todayCards"
        :key="item.key"
        :xs="24"
        :sm="12"
        :md="6"
      >
        <el-card
          shadow="never"
          class="stat-card"
        >
          <el-statistic
            :title="item.title"
            :value="item.value"
            :precision="item.money ? 2 : 0"
            :prefix="item.money ? '¥' : ''"
          />
        </el-card>
      </el-col>
    </el-row>

    <el-card
      shadow="never"
      class="table-card"
    >
      <template #header>
        今日 / 近7日 / 近30日 / 累计
      </template>
      <el-table
        :data="overview?.ranges || []"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column
          prop="label"
          label="区间"
          width="90"
        />
        <el-table-column
          label="GMV"
          min-width="120"
        >
          <template #default="scope">
            ¥{{ formatMoney(scope.row.gmv) }}
          </template>
        </el-table-column>
        <el-table-column
          label="净额"
          min-width="120"
        >
          <template #default="scope">
            ¥{{ formatMoney(scope.row.netGmv) }}
          </template>
        </el-table-column>
        <el-table-column
          prop="paidOrderCount"
          label="已支付单"
          width="100"
        />
        <el-table-column
          prop="orderCount"
          label="下单数"
          width="90"
        />
        <el-table-column
          prop="unpaidOrderCount"
          label="待付款"
          width="90"
        />
        <el-table-column
          label="待付金额"
          min-width="110"
        >
          <template #default="scope">
            ¥{{ formatMoney(scope.row.unpaidAmount) }}
          </template>
        </el-table-column>
        <el-table-column
          prop="closedOrderCount"
          label="已关闭"
          width="90"
        />
        <el-table-column
          prop="refundCount"
          label="退款成功"
          width="100"
        />
        <el-table-column
          label="退款金额"
          min-width="110"
        >
          <template #default="scope">
            ¥{{ formatMoney(scope.row.refundAmount) }}
          </template>
        </el-table-column>
        <el-table-column
          prop="pendingRefundCount"
          label="退款处理中"
          width="110"
        />
        <el-table-column
          label="处理中金额"
          min-width="110"
        >
          <template #default="scope">
            ¥{{ formatMoney(scope.row.pendingRefundAmount) }}
          </template>
        </el-table-column>
      </el-table>
      <p class="table-hint">
        初始化 SQL 里的样例订单多在 2019 年，今日/近7日/近30日可能为 0；看「累计」或走一遍 mock 下单支付。
        不接订阅消息、真实微信结算或 COS。
      </p>
    </el-card>

    <el-card
      shadow="never"
      class="chart-card"
    >
      <template #header>
        近30日已付 GMV / 退款成功
      </template>
      <div
        ref="chartRef"
        class="chart"
      />
    </el-card>
  </div>
</template>

<script setup>
import * as echarts from 'echarts'

const loading = ref(false)
const overview = ref(null)
const chartRef = ref(null)
let chart

const formatMoney = (n) => {
  return Number(n || 0).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

const rangeOf = (code) => {
  return (overview.value?.ranges || []).find(item => item.code === code) || {}
}

const todayCards = computed(() => {
  const today = rangeOf('TODAY')
  return [
    { key: 'gmv', title: '今日 GMV', value: today.gmv || 0, money: true },
    { key: 'paid', title: '今日已支付单', value: today.paidOrderCount || 0, money: false },
    { key: 'unpaid', title: '今日待付款', value: today.unpaidOrderCount || 0, money: false },
    { key: 'refund', title: '今日退款成功', value: today.refundAmount || 0, money: true }
  ]
})

const renderChart = () => {
  if (!chartRef.value) {
    return
  }
  if (!chart) {
    chart = echarts.init(chartRef.value)
  }
  const daily = overview.value?.daily || []
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['GMV', '退款'] },
    grid: { left: 56, right: 24, top: 40, bottom: 32 },
    xAxis: {
      type: 'category',
      data: daily.map(item => (item.statDate || '').slice(5))
    },
    yAxis: { type: 'value', min: 0 },
    series: [
      { name: 'GMV', type: 'bar', data: daily.map(item => item.gmv || 0) },
      { name: '退款', type: 'line', data: daily.map(item => item.refundAmount || 0) }
    ]
  })
  chart.resize()
}

const onResize = () => {
  chart?.resize()
}

const load = () => {
  loading.value = true
  http({
    url: http.adornUrl('/order/dashboard'),
    method: 'get',
    params: http.adornParams()
  }).then(({ data }) => {
    overview.value = data
    nextTick(renderChart)
  }).catch(() => {}).finally(() => {
    loading.value = false
  })
}

onMounted(() => {
  load()
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  if (chart) {
    chart.dispose()
    chart = null
  }
})
</script>

<style lang="scss" scoped>
.mod-dashboard {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.head-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 12px;
}
.title {
  font-size: 18px;
  font-weight: 600;
}
.hint {
  margin: 6px 0 0;
  color: #909399;
  font-size: 13px;
  line-height: 1.6;
}
.mock-tag {
  margin-left: 8px;
}
.stat-row {
  margin: 0 !important;
}
.stat-card {
  margin-bottom: 16px;
}
.table-hint {
  margin: 12px 0 0;
  color: #909399;
  font-size: 13px;
  line-height: 1.6;
}
.chart {
  width: 100%;
  height: 320px;
}
</style>
