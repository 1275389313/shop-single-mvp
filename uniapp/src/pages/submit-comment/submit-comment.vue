<template>
  <view class="container">
    <view class="prod">
      <image
        v-if="pic"
        class="pic"
        :src="pic"
      />
      <text class="name">
        {{ prodName }}
      </text>
    </view>

    <view class="block">
      <view class="label">
        评分
      </view>
      <view class="stars">
        <text
          v-for="n in 5"
          :key="n"
          class="star"
          :class="{ on: score >= n }"
          @tap="score = n"
        >
          ★
        </text>
        <text class="score-txt">
          {{ score }}分
        </text>
      </view>
    </view>

    <view class="block">
      <view class="label">
        评价内容
      </view>
      <textarea
        v-model="content"
        class="area"
        maxlength="500"
        placeholder="商品怎么样？晒图更有参考价值"
      />
    </view>

    <view class="block">
      <view class="label">
        晒图（选填，最多9张）
      </view>
      <view class="pics">
        <view
          v-for="(p, idx) in pics"
          :key="idx"
          class="pic-item"
        >
          <image
            :src="p.url"
            mode="aspectFill"
          />
          <text
            class="del"
            @tap="removePic(idx)"
          >
            ×
          </text>
        </view>
        <view
          v-if="pics.length < 9"
          class="pic-item add"
          @tap="choosePics"
        >
          +
        </view>
      </view>
      <view
        class="placeholder-btn"
        @tap="addPlaceholder"
      >
        使用占位图（无需真实 COS）
      </view>
    </view>

    <view
      class="anon"
      @tap="isAnonymous = isAnonymous ? 0 : 1"
    >
      <text class="chk">
        {{ isAnonymous ? '☑' : '☐' }}
      </text>
      匿名评价
    </view>

    <view
      class="submit"
      @tap="submit"
    >
      发表评价
    </view>
  </view>
</template>

<script setup>
const orderItemId = ref(0)
const prodId = ref(0)
const prodName = ref('')
const pic = ref('')
const score = ref(5)
const content = ref('')
const pics = ref([])
const isAnonymous = ref(0)
const submitting = ref(false)

onLoad((options) => {
  orderItemId.value = Number(options.orderItemId || 0)
  prodId.value = Number(options.prodId || 0)
  prodName.value = decodeURIComponent(options.prodName || '')
  pic.value = options.pic ? decodeURIComponent(options.pic) : ''
  if (!orderItemId.value) {
    uni.showToast({ title: '缺少订单项', icon: 'none' })
  }
})

const choosePics = () => {
  const left = 9 - pics.value.length
  uni.chooseImage({
    count: left,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (res) => {
      const paths = res.tempFilePaths || []
      uni.showLoading({ title: '上传中', mask: true })
      try {
        for (const p of paths) {
          const { data } = await http.upload(p)
          pics.value.push({
            filePath: data.filePath,
            url: data.url
          })
        }
      } finally {
        uni.hideLoading()
      }
    }
  })
}

const addPlaceholder = () => {
  if (pics.value.length >= 9) {
    return
  }
  http.request({
    url: '/p/file/placeholder',
    method: 'POST',
    data: {}
  }).then(({ data }) => {
    pics.value.push({
      filePath: data.filePath,
      url: data.url
    })
  })
}

const removePic = (idx) => {
  pics.value.splice(idx, 1)
}

const submit = () => {
  if (!orderItemId.value || submitting.value) {
    return
  }
  if (!content.value.trim() && pics.value.length === 0) {
    uni.showToast({ title: '请填写内容或上传晒图', icon: 'none' })
    return
  }
  submitting.value = true
  http.request({
    url: '/p/prodComm',
    method: 'POST',
    data: {
      orderItemId: orderItemId.value,
      prodId: prodId.value || undefined,
      score: score.value,
      content: content.value.trim(),
      pics: pics.value.map(p => p.filePath).join(','),
      isAnonymous: isAnonymous.value
    }
  }).then(() => {
    uni.showToast({ title: '评价成功', icon: 'none' })
    setTimeout(() => {
      if (prodId.value) {
        uni.redirectTo({
          url: '/pages/prod/prod?prodid=' + prodId.value
        })
      } else {
        uni.navigateBack()
      }
    }, 600)
  }).catch(() => {
    submitting.value = false
  })
}
</script>

<style scoped lang="scss">
.container {
  min-height: 100vh;
  background: #f4f4f4;
  padding-bottom: 40rpx;
}
.prod {
  display: flex;
  align-items: center;
  background: #fff;
  padding: 24rpx 30rpx;
  .pic {
    width: 96rpx;
    height: 96rpx;
    margin-right: 16rpx;
    background: #eee;
  }
  .name {
    flex: 1;
    font-size: 28rpx;
    color: #333;
  }
}
.block {
  background: #fff;
  margin-top: 16rpx;
  padding: 24rpx 30rpx;
}
.label {
  font-size: 28rpx;
  color: #333;
  margin-bottom: 16rpx;
}
.stars {
  display: flex;
  align-items: center;
}
.star {
  font-size: 48rpx;
  color: #ddd;
  margin-right: 12rpx;
}
.star.on {
  color: #f5a623;
}
.score-txt {
  font-size: 26rpx;
  color: #999;
  margin-left: 8rpx;
}
.area {
  width: 100%;
  min-height: 180rpx;
  font-size: 28rpx;
  color: #333;
}
.pics {
  display: flex;
  flex-wrap: wrap;
}
.pic-item {
  width: 160rpx;
  height: 160rpx;
  margin: 0 16rpx 16rpx 0;
  position: relative;
  background: #fafafa;
  image {
    width: 160rpx;
    height: 160rpx;
  }
  .del {
    position: absolute;
    top: 0;
    right: 0;
    width: 40rpx;
    height: 40rpx;
    line-height: 36rpx;
    text-align: center;
    background: rgba(0, 0, 0, 0.45);
    color: #fff;
    font-size: 28rpx;
  }
}
.pic-item.add {
  border: 2rpx dashed #ccc;
  color: #999;
  font-size: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
.placeholder-btn {
  font-size: 24rpx;
  color: #3a86b9;
  margin-top: 8rpx;
}
.anon {
  background: #fff;
  margin-top: 16rpx;
  padding: 24rpx 30rpx;
  font-size: 28rpx;
  color: #333;
}
.chk {
  margin-right: 12rpx;
}
.submit {
  margin: 40rpx 30rpx 0;
  height: 88rpx;
  line-height: 88rpx;
  text-align: center;
  background: #eb2444;
  color: #fff;
  border-radius: 8rpx;
  font-size: 30rpx;
}
</style>
