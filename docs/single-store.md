# 单店约束与仍带「多店味道」的代码

开源 mall4j 本身是 **B2C 单商户**，不是 mall4cloud 多商户。数据模型仍到处带着 `shop_id`，默认店铺 **shop_id = 1**（`tz_sys_user.shop_id`、`tz_shop_detail`）。

## 本仓库已做的收口

- 管理端 `ShopDetailController`：禁止新建/删除店铺；列表、改状态、按 id 查询只允许当前登录店铺
- uni-app 提交订单仍写死 `shopId: 1`（与上游一致）
- mock 微信登录把 token 里的 `shopId` 设为 1

## 刻意保留（改动面太大）

这些 **不是** 多商户切换器，但阅读代码时会看到「店铺」概念：

| 位置 | 说明 |
| --- | --- |
| 几乎所有业务表的 `shop_id` | 单店也用列做数据归属，不要删列 |
| `UserInfoInTokenBO.shopId` 注释写「租户 id」 | 开源版实际是店铺 id |
| `PayServiceImpl` 支持多个 `orderNumbers` 共用一个 `payNo` | 上游为多店合单预留；单店只会有一个订单号 |
| `ShopCartOrderMergerDto` / `OrderShopParam` | 购物车按店分组的结构，单店只有一组 |
| `tz_category.shop_id` | 平台分类可能为 0，店铺分类为 1 |
| `HotSearch` 等按 shopId 缓存 | 缓存 key 仍带 shopId |
| 管理端 pinia `userStore.shopId` | 登录后写入，无店铺切换 UI |

未发现 mall4cloud 式「切换店铺」页。若以后要彻底去掉 `shop_id`，需要改 schema 与全部 Mapper，不在 Phase 1 范围。
