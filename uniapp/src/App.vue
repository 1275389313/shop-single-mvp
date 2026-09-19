<script setup>
import { prefetchSubscribeTmplIds } from '@/utils/subscribe-message.js'
http.getCartCount()
prefetchSubscribeTmplIds()
</script>

<style lang="scss" scoped>
@import "./app.css";
/* 隐藏头部 */
uni-page-head {
  display: none;
}
/* 轮播图指示点 */
uni-swiper .uni-swiper-dots-horizontal {
  bottom: 20px !important;
}
</style>
