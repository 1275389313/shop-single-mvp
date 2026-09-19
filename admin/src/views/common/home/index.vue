<template>
  <div class="mod-home">
    <el-card
      v-if="isAuth('prod:stockAlert:page')"
      shadow="never"
      class="stock-alert-home"
    >
      <div class="stock-alert-home__row">
        <div>
          <div class="stock-alert-home__title">
            库存预警
          </div>
          <p class="stock-alert-home__hint">
            上架 SKU 可售库存 ≤ 全局阈值 {{ globalThreshold }} 时计入。可在「产品管理 → 库存预警」改阈值。
          </p>
        </div>
        <el-badge
          :value="lowStockCount"
          :hidden="!lowStockCount"
          :max="99"
        >
          <el-button
            type="warning"
            @click="goStockAlert"
          >
            查看低库存
          </el-button>
        </el-badge>
      </div>
    </el-card>
    <p>一个基于spring boot、spring oauth2.0、mybatis、redis的轻量级、前后端分离、拥有完整sku和下单流程的完全开源商城</p>
    <p>&nbsp;</p>
    <p>该项目仅供学习参考、可供个人学习使用、如需商用联系作者进行授权，否则必将追究法律责任</p>
    <p>&nbsp;</p>
    <h2>前言</h2>
    <p>
      <code>mall4j商城</code>项目致力于为中小企业打造一个完整、易于维护的开源的电商系统，采用现阶段流行技术实现。后台管理系统包含商品管理、订单管理、运费模板、规格管理、会员管理、运营管理、内容管理、统计报表、权限管理、设置等模块。
    </p>
    <p>&nbsp;</p>
    <h2>技术选型</h2>
    <figure>
      <table
        border="1"
        cellspacing="0"
        cellpadding="5px"
      >
        <thead>
          <tr>
            <th>技术</th>
            <th>版本</th>
            <th>说明</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>Spring Boot</td>
            <td>4.x</td>
            <td>MVC核心框架</td>
          </tr>
          <tr>
            <td>MyBatis</td>
            <td>以pom为准，一直更新</td>
            <td>ORM框架</td>
          </tr>
          <tr>
            <td>MyBatisPlus</td>
            <td>以pom为准，一直更新</td>
            <td>基于mybatis，使用lambda表达式的</td>
          </tr>
          <tr>
            <td>Swagger-UI</td>
            <td>以pom为准，一直更新</td>
            <td>文档生产工具</td>
          </tr>
          <tr>
            <td>redisson</td>
            <td>以pom为准，一直更新</td>
            <td>对redis进行封装、集成分布式锁等</td>
          </tr>
          <tr>
            <td>hikari</td>
            <td>以pom为准，一直更新</td>
            <td>数据库连接池</td>
          </tr>
          <tr>
            <td>log4j2</td>
            <td>以pom为准，一直更新</td>
            <td>更快的log日志工具</td>
          </tr>
          <tr>
            <td>lombok</td>
            <td>以pom为准，一直更新</td>
            <td>简化对象封装工具</td>
          </tr>
          <tr>
            <td>hutool</td>
            <td>以pom为准，一直更新</td>
            <td>更适合国人的java工具集</td>
          </tr>
          <tr>
            <td>xxl-job</td>
            <td>以pom为准，一直更新</td>
            <td>定时任务</td>
          </tr>
        </tbody>
      </table>
    </figure>
    <p>&nbsp;</p>
    <h2>部署教程</h2>
    <p>&nbsp;</p>
    <h3>1.开发环境</h3>
    <figure>
      <table
        border="1"
        cellspacing="0"
        cellpadding="5px"
      >
        <thead>
          <tr>
            <th>工具</th>
            <th>版本</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>jdk</td>
            <td>17</td>
          </tr>
          <tr>
            <td>mysql</td>
            <td>5.7+</td>
          </tr>
          <tr>
            <td>redis</td>
            <td>3.2+</td>
          </tr>
        </tbody>
      </table>
    </figure>
    <h3>2.启动</h3>
    <ul>
      <li>推荐使用idea，安装lombok插件，使用idea导入maven项目</li>
      <li>
        将shop.sql导入到mysql中，修改
        <code>application-dev.yml</code>更改 datasource.url、user、password
      </li>
      <li>启动redis</li>
      <li>
        通过
        <code>WebApplication</code>启动项目后台接口，
        <code>ApiApplication</code> 启动项目前端接口
      </li>
    </ul>
    <p>&nbsp;</p>
  </div>
</template>

<script setup>
import { isAuth } from '@/utils'

const router = useRouter()
const lowStockCount = ref(0)
const globalThreshold = ref(10)

const goStockAlert = () => {
  router.push('/prod/stockAlert')
}

onMounted(() => {
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
})
</script>

<style lang="scss" scoped>
.mod-home {
  line-height: 1.5;
}
.stock-alert-home {
  margin-bottom: 20px;
}
.stock-alert-home__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}
.stock-alert-home__title {
  font-size: 16px;
  font-weight: 600;
}
.stock-alert-home__hint {
  margin: 6px 0 0;
  color: #909399;
  font-size: 13px;
}
</style>
