<template>
  <div class="mod-prod">
    <avue-crud
      ref="crudRef"
      :page="page"
      :data="dataList"
      :table-loading="dataListLoading"
      :permission="permission"
      :option="tableOption"
      @search-change="onSearch"
      @selection-change="selectionChange"
      @on-load="getDataList"
    >
      <template #menu-left>
        <el-button
          v-if="isAuth('prod:prod:save')"
          type="primary"
          icon="el-icon-plus"
          @click.stop="onAddOrUpdate()"
        >
          新增
        </el-button>

        <el-button
          v-if="isAuth('prod:prod:delete')"
          type="danger"
          :disabled="dataListSelections.length <= 0"
          @click="onDelete()"
        >
          批量删除
        </el-button>
        <el-badge
          v-if="isAuth('prod:stockAlert:page')"
          :value="lowStockCount"
          :hidden="!lowStockCount"
          :max="99"
        >
          <el-button
            type="warning"
            @click="goStockAlert"
          >
            库存预警
          </el-button>
        </el-badge>
      </template>

      <template #totalStocks="scope">
        <span>{{ scope.row.totalStocks }}</span>
        <el-tag
          v-if="isProdLow(scope.row)"
          type="danger"
          size="small"
          style="margin-left: 8px"
        >
          低库存
        </el-tag>
      </template>

      <template #status="scope">
        <el-tag v-if="scope.row.status === 1">
          上架
        </el-tag>
        <el-tag v-else>
          未上架
        </el-tag>
      </template>

      <template #menu="scope">
        <el-button
          v-if="isAuth('prod:prod:update')"
          type="primary"
          icon="el-icon-edit"
          @click="onAddOrUpdate(scope.row.prodId)"
        >
          修改
        </el-button>
        <el-button
          v-if="isAuth('prod:prod:delete')"
          type="danger"
          icon="el-icon-delete"
          @click="onDelete(scope.row.prodId)"
        >
          删除
        </el-button>
      </template>
    </avue-crud>
  </div>
</template>

<script setup>
import { isAuth } from '@/utils'
import { ElMessage, ElMessageBox } from 'element-plus'
import { tableOption } from '@/crud/prod/prodList.js'
const permission = reactive({
  delBtn: isAuth('prod:prod:delete')
})
const dataList = ref([])
const lowStockCount = ref(0)
const globalThreshold = ref(10)
const page = reactive({
  total: 0, // 总页数
  currentPage: 1, // 当前页数
  pageSize: 10 // 每页显示多少条
})
const dataListLoading = ref(false)
/**
 * 获取数据列表
 */
const getDataList = (pageParam, params, done) => {
  dataListLoading.value = true
  http({
    url: http.adornUrl('/prod/prod/page'),
    method: 'get',
    params: http.adornParams(
      Object.assign(
        {
          current: pageParam == null ? page.currentPage : pageParam.currentPage,
          size: pageParam == null ? page.pageSize : pageParam.pageSize
        },
        params
      )
    )
  })
    .then(({ data }) => {
      dataList.value = data.records
      for (const key in dataList.value) {
        // eslint-disable-next-line no-prototype-builtins
        if (dataList.value.hasOwnProperty(key)) {
          const element = dataList.value[key]
          element.imgs = element.imgs.split(',')[0]
        }
      }
      page.total = data.total
      dataListLoading.value = false
      if (done) done()
    })
}
const router = useRouter()
/**
 * 新增 / 修改
 * @param id
 */
const onAddOrUpdate = (id) => {
  router.push({
    path: '/prodInfo',
    query: { prodId: id }
  })
}
/**
 * 删除和批量删除
 * @param id
 */
const onDelete = (id) => {
  const prodIds = getSeleProdIds()
  if (id) {
    prodIds.push(id)
  }
  ElMessageBox.confirm(`确定进行[${id ? '删除' : '批量删除'}]操作?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      http({
        url: http.adornUrl('/prod/prod'),
        method: 'delete',
        data: http.adornData(prodIds, false)
      })
        .then(() => {
          ElMessage({
            message: '操作成功',
            type: 'success',
            duration: 1500,
            onClose: () => {
              getDataList(page)
            }
          })
        })
    })
    .catch(() => { })
}
/**
 * 条件查询
 * @param params
 * @param done
 */
const onSearch = (params, done) => {
  getDataList(page, params, done)
}

const isProdLow = (row) => {
  if (row.status !== 1) {
    return false
  }
  const stocks = row.totalStocks
  if (stocks == null || stocks < 0) {
    return false
  }
  return stocks <= globalThreshold.value
}

const goStockAlert = () => {
  router.push('/prod/stockAlert')
}

const loadStockAlert = () => {
  if (!isAuth('prod:stockAlert:page')) {
    return
  }
  http({
    url: http.adornUrl('/prod/stockAlert/config'),
    method: 'get',
    params: http.adornParams()
  }).then(({ data }) => {
    lowStockCount.value = data.count || 0
    if (data.globalThreshold != null) {
      globalThreshold.value = data.globalThreshold
    }
  }).catch(() => {})
}

onMounted(() => {
  loadStockAlert()
})

const dataListSelections = ref([])
/**
 * 多选变化
 * @param val
 */
const selectionChange = (val) => {
  dataListSelections.value = val
}
/**
 * 获取选中的商品Id列表
 */
const getSeleProdIds = () => {
  return dataListSelections.value?.map(item => {
    return item.prodId
  })
}

</script>
