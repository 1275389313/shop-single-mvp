<template>
  <div class="mod-shop-detail">
    <el-card>
      <template #header>
        店铺设置（单店，不可新建/删除店铺）
      </template>
      <el-form
        ref="dataFormRef"
        :model="dataForm"
        :rules="dataRule"
        label-width="120px"
        style="max-width: 720px;"
      >
        <el-form-item
          label="店铺名称"
          prop="shopName"
        >
          <el-input
            v-model="dataForm.shopName"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        <el-form-item
          label="联系电话"
          prop="tel"
        >
          <el-input
            v-model="dataForm.tel"
            maxlength="20"
          />
        </el-form-item>
        <el-form-item
          label="店铺状态"
          prop="shopStatus"
        >
          <el-radio-group v-model="dataForm.shopStatus">
            <el-radio :label="1">
              营业中
            </el-radio>
            <el-radio :label="0">
              停业中
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          label="营业时间"
          prop="openTime"
        >
          <el-input
            v-model="dataForm.openTime"
            placeholder="例如 09:00-21:00"
            maxlength="100"
          />
        </el-form-item>
        <el-form-item
          label="省份"
          prop="province"
        >
          <el-input v-model="dataForm.province" />
        </el-form-item>
        <el-form-item
          label="城市"
          prop="city"
        >
          <el-input v-model="dataForm.city" />
        </el-form-item>
        <el-form-item
          label="区县"
          prop="area"
        >
          <el-input v-model="dataForm.area" />
        </el-form-item>
        <el-form-item
          label="省市区代码"
          prop="pcaCode"
        >
          <el-input
            v-model="dataForm.pcaCode"
            placeholder="用于地址回显，可沿用现有值"
          />
        </el-form-item>
        <el-form-item
          label="详细地址"
          prop="shopAddress"
        >
          <el-input v-model="dataForm.shopAddress" />
        </el-form-item>
        <el-form-item
          label="店铺 Logo"
          prop="shopLogo"
        >
          <pic-upload v-model="dataForm.shopLogo" />
        </el-form-item>
        <el-form-item
          label="简介"
          prop="intro"
        >
          <el-input
            v-model="dataForm.intro"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item
          label="公告"
          prop="shopNotice"
        >
          <el-input
            v-model="dataForm.shopNotice"
            type="textarea"
            :rows="2"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        <el-form-item>
          <el-button
            v-if="isAuth('shop:shopDetail:update')"
            type="primary"
            :loading="saving"
            @click="onSubmit"
          >
            保存
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { isAuth } from '@/utils'
import { ElMessage } from 'element-plus'
import PicUpload from '@/components/pic-upload/index.vue'

const dataFormRef = ref(null)
const saving = ref(false)
const dataForm = reactive({
  shopId: null,
  shopName: '',
  tel: '',
  shopStatus: 1,
  openTime: '',
  province: '',
  city: '',
  area: '',
  pcaCode: '',
  shopAddress: '',
  shopLogo: '',
  shopPhotos: '',
  intro: '',
  shopNotice: ''
})
const dataRule = {
  shopName: [{ required: true, message: '店铺名称不能为空', trigger: 'blur' }],
  tel: [{ required: true, message: '联系电话不能为空', trigger: 'blur' }],
  openTime: [{ required: true, message: '营业时间不能为空', trigger: 'blur' }],
  province: [{ required: true, message: '省份不能为空', trigger: 'blur' }],
  city: [{ required: true, message: '城市不能为空', trigger: 'blur' }],
  area: [{ required: true, message: '区县不能为空', trigger: 'blur' }],
  pcaCode: [{ required: true, message: '省市区代码不能为空', trigger: 'blur' }],
  shopAddress: [{ required: true, message: '详细地址不能为空', trigger: 'blur' }],
  shopLogo: [{ required: true, message: '请上传店铺 Logo', trigger: 'change' }]
}

onMounted(() => {
  http({
    url: http.adornUrl('/shop/shopDetail/info'),
    method: 'get',
    params: http.adornParams()
  }).then(({ data }) => {
    Object.assign(dataForm, data || {})
  })
})

const onSubmit = () => {
  dataFormRef.value?.validate((valid) => {
    if (!valid) {
      return
    }
    saving.value = true
    http({
      url: http.adornUrl('/shop/shopDetail'),
      method: 'put',
      data: http.adornData({
        shopId: dataForm.shopId,
        shopName: dataForm.shopName,
        intro: dataForm.intro,
        shopNotice: dataForm.shopNotice,
        tel: dataForm.tel,
        shopAddress: dataForm.shopAddress,
        province: dataForm.province,
        city: dataForm.city,
        area: dataForm.area,
        pcaCode: dataForm.pcaCode,
        shopLogo: dataForm.shopLogo,
        shopPhotos: dataForm.shopPhotos,
        openTime: dataForm.openTime,
        shopStatus: dataForm.shopStatus
      })
    }).then(() => {
      ElMessage.success('保存成功')
    }).finally(() => {
      saving.value = false
    })
  })
}
</script>
