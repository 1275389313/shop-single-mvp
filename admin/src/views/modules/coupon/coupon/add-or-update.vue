<template>
  <el-dialog
    v-model="visible"
    :title="!dataForm.couponId ? '新增优惠券' : '修改优惠券'"
    :close-on-click-modal="false"
    width="640px"
  >
    <el-form
      ref="dataFormRef"
      :model="dataForm"
      :rules="dataRule"
      label-width="120px"
    >
      <el-form-item
        label="名称"
        prop="couponName"
      >
        <el-input
          v-model="dataForm.couponName"
          maxlength="64"
          show-word-limit
        />
      </el-form-item>
      <el-form-item
        label="副标题"
        prop="subTitle"
      >
        <el-input
          v-model="dataForm.subTitle"
          maxlength="128"
        />
      </el-form-item>
      <el-form-item
        label="类型"
        prop="couponType"
      >
        <el-radio-group v-model="dataForm.couponType">
          <el-radio :label="1">
            满减
          </el-radio>
          <el-radio :label="2">
            折扣
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item
        label="使用门槛"
        prop="cashCondition"
      >
        <el-input-number
          v-model="dataForm.cashCondition"
          :min="0"
          :precision="2"
          controls-position="right"
        />
        <span class="form-tip">满多少可用，0 为无门槛</span>
      </el-form-item>
      <el-form-item
        v-if="dataForm.couponType === 1"
        label="减免金额"
        prop="reduceAmount"
      >
        <el-input-number
          v-model="dataForm.reduceAmount"
          :min="0.01"
          :precision="2"
          controls-position="right"
        />
      </el-form-item>
      <el-form-item
        v-if="dataForm.couponType === 2"
        label="折扣（折）"
        prop="couponDiscount"
      >
        <el-input-number
          v-model="dataForm.couponDiscount"
          :min="0.1"
          :max="9.9"
          :precision="2"
          :step="0.1"
          controls-position="right"
        />
        <span class="form-tip">如 8.5 表示 8.5 折</span>
      </el-form-item>
      <el-form-item
        v-if="dataForm.couponType === 2"
        label="最多减免"
        prop="maxReduceAmount"
      >
        <el-input-number
          v-model="dataForm.maxReduceAmount"
          :min="0"
          :precision="2"
          controls-position="right"
        />
        <span class="form-tip">0 或不填表示不封顶</span>
      </el-form-item>
      <el-form-item
        label="库存"
        prop="stocks"
      >
        <el-input-number
          v-model="dataForm.stocks"
          :min="-1"
          controls-position="right"
        />
        <span class="form-tip">剩余可领数量，-1 不限</span>
      </el-form-item>
      <el-form-item
        label="每人限领"
        prop="limitNum"
      >
        <el-input-number
          v-model="dataForm.limitNum"
          :min="-1"
          controls-position="right"
        />
        <span class="form-tip">-1 不限</span>
      </el-form-item>
      <el-form-item
        label="领取时间"
        prop="startTime"
      >
        <el-date-picker
          v-model="dataForm.startTime"
          type="datetime"
          value-format="YYYY-MM-DD HH:mm:ss"
          placeholder="开始"
        />
        <span class="form-tip">至</span>
        <el-date-picker
          v-model="dataForm.endTime"
          type="datetime"
          value-format="YYYY-MM-DD HH:mm:ss"
          placeholder="结束"
        />
      </el-form-item>
      <el-form-item
        label="用券有效期"
        prop="validTimeType"
      >
        <el-radio-group v-model="dataForm.validTimeType">
          <el-radio :label="1">
            领取后 N 天
          </el-radio>
          <el-radio :label="2">
            固定时间段
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item
        v-if="dataForm.validTimeType === 1"
        label="有效天数"
        prop="validDays"
      >
        <el-input-number
          v-model="dataForm.validDays"
          :min="1"
          controls-position="right"
        />
      </el-form-item>
      <el-form-item
        v-if="dataForm.validTimeType === 2"
        label="固定有效期"
        prop="validStartTime"
      >
        <el-date-picker
          v-model="dataForm.validStartTime"
          type="datetime"
          value-format="YYYY-MM-DD HH:mm:ss"
          placeholder="开始"
        />
        <span class="form-tip">至</span>
        <el-date-picker
          v-model="dataForm.validEndTime"
          type="datetime"
          value-format="YYYY-MM-DD HH:mm:ss"
          placeholder="结束"
        />
      </el-form-item>
      <el-form-item
        label="状态"
        prop="status"
      >
        <el-radio-group v-model="dataForm.status">
          <el-radio :label="0">
            下线
          </el-radio>
          <el-radio :label="1">
            投放
          </el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="visible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          @click="onSubmit()"
        >
          确定
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { Debounce } from '@/utils/debounce'

const emit = defineEmits(['refreshDataList'])
const visible = ref(false)
const dataFormRef = ref(null)

const dataRule = {
  couponName: [{ required: true, message: '请填写名称', trigger: 'blur' }],
  couponType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  startTime: [{ required: true, message: '请填写领取开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请填写领取结束时间', trigger: 'change' }]
}

const emptyForm = () => ({
  couponId: 0,
  couponName: '',
  subTitle: '',
  couponType: 1,
  cashCondition: 0,
  reduceAmount: 10,
  couponDiscount: 8.5,
  maxReduceAmount: 0,
  stocks: 100,
  sourceStock: 100,
  limitNum: 1,
  startTime: '',
  endTime: '',
  validTimeType: 1,
  validDays: 30,
  validStartTime: '',
  validEndTime: '',
  status: 1,
  suitableProdType: 0
})

const dataForm = ref(emptyForm())

const init = (id) => {
  dataForm.value = emptyForm()
  dataForm.value.couponId = id || 0
  visible.value = true
  nextTick(() => {
    dataFormRef.value?.clearValidate()
    if (dataForm.value.couponId) {
      http({
        url: http.adornUrl('/coupon/coupon/info/' + dataForm.value.couponId),
        method: 'get',
        params: http.adornParams()
      })
        .then(({ data }) => {
          dataForm.value = Object.assign(emptyForm(), data)
        })
    }
  })
}
defineExpose({ init })

const onSubmit = Debounce(() => {
  dataFormRef.value?.validate((valid) => {
    if (!valid) {
      return
    }
    const payload = { ...dataForm.value }
    if (payload.couponId) {
      payload.sourceStock = payload.sourceStock || payload.stocks
    } else {
      payload.sourceStock = payload.stocks
      payload.couponId = undefined
    }
    http({
      url: http.adornUrl('/coupon/coupon'),
      method: payload.couponId ? 'put' : 'post',
      data: http.adornData(payload)
    })
      .then(() => {
        ElMessage({
          message: '操作成功',
          type: 'success',
          duration: 1500,
          onClose: () => {
            visible.value = false
            emit('refreshDataList')
          }
        })
      })
  })
})
</script>

<style scoped>
.form-tip {
  margin-left: 8px;
  color: #999;
  font-size: 12px;
}
</style>
