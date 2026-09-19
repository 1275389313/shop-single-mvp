<template>
  <div class="mod-coupon">
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
      <template #couponType="scope">
        {{ scope.row.couponType === 2 ? '折扣' : '满减' }}
      </template>
      <template #benefit="scope">
        <span v-if="scope.row.couponType === 2">
          {{ scope.row.couponDiscount }}折
          <template v-if="scope.row.maxReduceAmount">（最多减{{ scope.row.maxReduceAmount }}）</template>
        </span>
        <span v-else>
          减{{ scope.row.reduceAmount }}
        </span>
      </template>
      <template #stocks="scope">
        {{ scope.row.stocks === -1 ? '不限' : scope.row.stocks }}
      </template>
      <template #status="scope">
        <el-tag
          v-if="scope.row.status === 1"
          type="success"
        >
          投放
        </el-tag>
        <el-tag
          v-else
          type="info"
        >
          下线
        </el-tag>
      </template>
      <template #menu-left>
        <el-button
          v-if="isAuth('coupon:coupon:save')"
          type="primary"
          icon="el-icon-plus"
          @click="onAddOrUpdate()"
        >
          新增
        </el-button>
      </template>
      <template #menu="scope">
        <el-button
          v-if="isAuth('coupon:coupon:update')"
          type="primary"
          icon="el-icon-edit"
          @click="onAddOrUpdate(scope.row.couponId)"
        >
          修改
        </el-button>
        <el-button
          v-if="isAuth('coupon:coupon:delete')"
          type="danger"
          icon="el-icon-delete"
          @click.stop="onDelete(scope.row.couponId)"
        >
          删除
        </el-button>
      </template>
    </avue-crud>
    <add-or-update
      v-if="addOrUpdateVisible"
      ref="addOrUpdateRef"
      @refresh-data-list="refreshChange"
      @close="addOrUpdateVisible=false"
    />
  </div>
</template>

<script setup>
import { isAuth } from '@/utils'
import { ElMessage, ElMessageBox } from 'element-plus'
import { tableOption } from '@/crud/coupon/coupon'
import AddOrUpdate from './add-or-update.vue'

const dataList = ref([])
const page = reactive({
  total: 0,
  currentPage: 1,
  pageSize: 10
})
const dataListLoading = ref(false)
const addOrUpdateVisible = ref(false)

const getDataList = (pageParam, params, done) => {
  dataListLoading.value = true
  http({
    url: http.adornUrl('/coupon/coupon/page'),
    method: 'get',
    params: http.adornParams(Object.assign({
      current: pageParam == null ? page.currentPage : pageParam.currentPage,
      size: pageParam == null ? page.pageSize : pageParam.pageSize
    }, params))
  })
    .then(({ data }) => {
      dataList.value = data.records
      page.total = data.total
      dataListLoading.value = false
      if (done) done()
    })
}

const addOrUpdateRef = ref(null)
const onAddOrUpdate = (id) => {
  addOrUpdateVisible.value = true
  nextTick(() => {
    addOrUpdateRef.value?.init(id)
  })
}

const onDelete = (id) => {
  ElMessageBox.confirm('确定删除该优惠券？已领取的券不能删除。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      http({
        url: http.adornUrl('/coupon/coupon/' + id),
        method: 'delete',
        data: http.adornData({})
      })
        .then(() => {
          ElMessage({
            message: '操作成功',
            type: 'success',
            duration: 1500,
            onClose: () => {
              getDataList()
            }
          })
        })
    }).catch(() => { })
}

const refreshChange = () => {
  getDataList(page)
}

const onSearch = (params, done) => {
  getDataList(page, params, done)
}
</script>
