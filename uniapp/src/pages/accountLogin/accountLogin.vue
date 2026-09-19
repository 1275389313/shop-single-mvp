<template>
  <view class="con">
    <image src="@/static/logo.png" />
    <!-- 登录 -->
    <view class="login-form">
      <view :class="['item',errorTips==1? 'error':'']">
        <view class="account">
          <text class="input-item">
            账号
          </text>
          <input
            type="text"
            data-type="account"
            placeholder-class="inp-palcehoder"
            placeholder="请输入用户名"
            @input="getInputVal"
          >
        </view>
        <view
          v-if="errorTips==1"
          class="error-text"
        >
          <text class="warning-icon">
            !
          </text>
          请输入账号！
        </view>
      </view>
      <view :class="['item',errorTips==2? 'error':'']">
        <view class="account">
          <text class="input-item">
            密码
          </text>
          <input
            type="password"
            data-type="password"
            placeholder-class="inp-palcehoder"
            placeholder="请输入密码"
            @input="getInputVal"
          >
        </view>
        <view
          v-if="errorTips==2"
          class="error-text"
        >
          <text class="warning-icon">
            !
          </text>
          请输入密码！
        </view>
      </view>
      <view class="operate">
        <view
          class="to-register"
          @tap="toRegitser"
        >
          还没有账号？
          <text>去注册></text>
        </view>
      </view>
    </view>

    <view>
      <button
        class="authorized-btn"
        @tap="login"
      >
        登录
      </button>
      <button
        class="authorized-btn"
        style="margin-top: 16rpx; background: #07c160;"
        @tap="wxLogin"
      >
        微信登录
      </button>
      <button
        v-if="mockWx"
        class="authorized-btn"
        style="margin-top: 16rpx; background: #07c160;"
        @tap="mockWxLogin"
      >
        模拟微信登录
      </button>
      <button
        class="to-idx-btn"
        @tap="toIndex"
      >
        回到首页
      </button>
    </view>
  </view>
</template>

<script setup>
import { encrypt } from '@/utils/crypto.js'

const principal = ref('') // 账号
const errorTips = ref(0) // 错误提示
const mockWx = ref(String(import.meta.env.VITE_APP_MOCK_WX) === 'true')
watch(
  () => principal.value,
  () => {
    errorTips.value = 0
  }
)

const credentials = ref('') // 密码
/**
 * 输入框的值
 */
const getInputVal = (e) => {
  const type = e.currentTarget.dataset.type
  if (type == 'account') {
    principal.value = e.detail.value
  } else if (type == 'password') {
    credentials.value = e.detail.value
  }
}

/**
 * 登录
 */
const login = () => {
  if (principal.value.length == 0) {
    errorTips.value = 1
  } else if (credentials.value.length == 0) {
    errorTips.value = 2
  } else {
    errorTips.value = 0
    http.request({
      url: '/login',
      method: 'post',
      data: {
        userName: principal.value,
        passWord: encrypt(credentials.value)
      }
    })
      .then(({ data }) => {
        http.loginSuccess(data, () => {
          uni.showToast({
            title: '登录成功',
            icon: 'none',
            complete: () => {
              setTimeout(() => {
                uni.switchTab({
                  url: '/pages/index/index'
                })
              }, 1000)
            }
          })
        })
      })
  }
}

/**
 * 去注册
 */
const toRegitser = () => {
  uni.navigateTo({
    url: '/pages/register/register'
  })
}

/**
 * 微信登录：DevTools / 真机走 uni.login 拿 code，后端 mock 时任意 code 即可换 token
 */
const afterLogin = (data, toastTitle) => {
  http.loginSuccess(data, () => {
    uni.showToast({
      title: toastTitle,
      icon: 'none',
      complete: () => {
        setTimeout(() => {
          uni.switchTab({
            url: '/pages/index/index'
          })
        }, 1000)
      }
    })
  })
}

const doWxLogin = (code, nickName) => {
  http.request({
    url: '/wx/login',
    method: 'post',
    data: {
      code,
      nickName: nickName || '微信用户'
    }
  }).then(({ data }) => {
    afterLogin(data, data.mock ? '模拟微信登录成功' : '登录成功')
  })
}

const wxLogin = () => {
  uni.login({
    provider: 'weixin',
    success: (loginRes) => {
      doWxLogin(loginRes.code || ('dev-' + Date.now()), '微信用户')
    },
    fail: () => {
      if (mockWx.value) {
        mockWxLogin()
        return
      }
      uni.showToast({
        title: '当前环境无法 wx.login，请用模拟微信登录或在微信开发者工具中打开',
        icon: 'none'
      })
    }
  })
}

/**
 * mock 微信登录：任意 code 即可换 token，无需真实 AppID（H5 联调用）
 */
const mockWxLogin = () => {
  doWxLogin('dev-' + Date.now(), '模拟微信用户')
}

/**
 * 回到首页
 */
const toIndex = () => {
  uni.switchTab({
    url: '/pages/index/index'
  })
}
</script>

<style scoped lang="scss">
@import "./accountLogin.scss";
</style>
