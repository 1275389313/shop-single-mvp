# 下单 → 支付 → 回调 → 发货

开源 mall4j 的支付在用户端会直接把订单标为已付。本仓库把这条路径做成可开关的 **mock**，并补上幂等回调接口。

## 状态

`tz_order.status`（`OrderStatus`）:

| 值 | 含义 |
| --- | --- |
| 1 | 待付款 UNPAY |
| 2 | 待发货 PADYED |
| 3 | 待收货 CONSIGNMENT |
| 4 | 待评价 CONFIRM（用户确认收货后上游写成 5，见下） |
| 5 | 成功 SUCCESS |
| 6 | 关闭 CLOSE（未支付超时/取消，会回库存） |

上游 `MyOrderController.receipt` 调 `confirmOrder`，SQL 把状态写成 **5** 而不是 4。评价流程不完整，按成功单处理即可。

## 时序（mock）

```text
用户端 uni-app                         yami-shop-api                         DB / Redis
     |                                      |                                  |
     |  POST /p/order/confirm               |                                  |
     | ---------------------------------->  |  算运费、写 ConfirmOrderCache     |
     |  POST /p/order/submit                |                                  |
     | ---------------------------------->  |  扣 SKU 库存，status=1           |
     |                                      |  写 tz_order + settlement        |
     |  POST /p/order/normalPay             |                                  |
     |  {orderNumbers, payType:1}           |                                  |
     | ---------------------------------->  |  生成 payNo                      |
     |                                      |  mock.pay=true → paySuccess()    |
     |                                      |  settlement.pay_status=1         |
     |                                      |  order.status=2 is_payed=1       |
     |                                      |  发布 PaySuccessOrderEvent       |
     |                                      |                                  |
     |  (可选) POST /notice/pay/mock        |                                  |
     |  {payNo}  无需登录，幂等              |                                  |
     | ---------------------------------->  |  已支付则直接成功返回             |

管理端 mall4v                           yami-shop-admin
     |                                      |
     |  PUT /order/order/delivery           |
     |  {orderNumber, dvyId, dvyFlowId}     |
     | ---------------------------------->  |  status=3，写入物流单号           |

用户端
     |  PUT /p/myOrder/receipt/{orderNumber}
     | ---------------------------------->  |  status=5
```

未支付超时：`OrderAutoCloseScheduler` 每分钟扫描 `status=1` 且更新时间早于 N 分钟的订单，调用 `OrderService.cancelOrders`（status=6 + `returnStock` + 退回已核销优惠券）。不依赖 xxl-job。

## 接口速查

| 步骤 | 方法 | 路径 | 说明 |
| --- | --- | --- | --- |
| 登录 | POST | `/wx/login` | mock：`code` 任意；真实微信 TODO |
| 登录 | POST | `/login` | 账号密码（加密） |
| 下单确认 | POST | `/p/order/confirm` | 需登录；`couponIds` 为用户券 ID |
| 提交订单 | POST | `/p/order/submit` | 需登录，扣库存并核销优惠券 |
| 领券中心 | GET | `/coupon/list` | 可匿名 |
| 领取 | POST | `/p/coupon/receive` | `{ "couponId": 模板ID }` |
| 我的券 | GET | `/p/coupon/myList` | `status` 0未使用 1已用 2过期 |
| 优惠券管理 | | `/coupon/coupon/**` | 管理端 8085 |
| 支付 | POST | `/p/order/pay` 或 `/p/order/normalPay` | mock 时当场已付 |
| 回调 | POST | `/notice/pay/mock` | `{ "payNo": "..." }` 幂等 |
| 发货 | PUT | `/order/order/delivery` | 管理端 |
| 确认收货 | PUT | `/p/myOrder/receipt/{orderNumber}` | 用户端 |
| 退款申请 | POST | `/p/refund/apply` | 用户端（订单详情/列表入口） |
| 我的退款 | GET | `/p/refund/page` | 用户端 |
| 订单退款 | GET | `/p/refund/byOrder?orderNumber=` | 用户端 |
| 退货物流公司 | GET | `/p/refund/deliveryList` | 用户端（名称列表，不含查询 URL） |
| 填写退货物流 | PUT | `/p/refund/express` | 用户端，`OrderRefundExpressParam` |
| 退款列表 | GET | `/order/refund/page` | 管理端「退款审核」 |
| 退款审核 | PUT | `/order/refund/audit` | 管理端同意=2 / 拒绝=3 |
| 确认退货收货 | PUT | `/order/refund/receive` | 管理端，退货退款寄回后 mock 退款 |

## 售后状态机（退款 / 退货退款）

表字段 `tz_order_refund.refund_sts` 仍是审核结果：`1待审核 / 2同意 / 3拒绝`。退货等待态不另开列，由 `apply_type` + 物流字段 + `return_money_sts` 推导，接口返回 `flowCode` / `flowText`。

| flowCode | 中文 | 条件 | 下一步 |
| --- | --- | --- | --- |
| `WAIT_AUDIT` | 待商家审核 | `refund_sts=1` | 管理端审核 |
| `REJECTED` | 商家已拒绝 | `refund_sts=3` | 买家可再申请 |
| `WAIT_SHIP` | 请寄回商品 | 退货退款已同意，尚未填 `express_no` | 买家 `PUT /p/refund/express` |
| `WAIT_RECEIVE` | 待商家收货 | 已填单号，`return_money_sts≠1` | 管理端确认收货 |
| `REFUNDED` | 退款成功 | `return_money_sts=1`（仅退款审核同意，或退货确认收货） | 结束（mock，无微信退款单号） |

```text
申请 applyType=1 仅退款
  → 审核拒绝 → REJECTED
  → 审核同意 → 立即 mock 退款 REFUNDED

申请 applyType=2 退货退款
  → 审核拒绝 → REJECTED
  → 审核同意 → WAIT_SHIP（订单 refund_sts 仍为处理中）
       → 买家填物流 → WAIT_RECEIVE
            → 商家确认收货 → mock 退款 REFUNDED
```

真实微信退款未实现；`SHOP_MVP_MOCK_PAY=false` 时仍只改库并打日志 TODO。

## curl 示例（mock 支付回调）

先拿到登录 token 和下单后的 `payNo`（`normalPay` 响应里）。若走了 mock 当场支付，再调回调应直接成功（幂等）：

```bash
curl -s -X POST http://127.0.0.1:8086/notice/pay/mock \
  -H 'Content-Type: application/json' \
  -d '{"payNo":"REPLACE_PAY_NO","bizPayNo":"MOCK-TEST"}'
```

## 真实微信支付（未实现）

1. `SHOP_MVP_MOCK_PAY=false`
2. 填写 `WX_PAY_MCH_ID` / `WX_PAY_API_KEY` / `WX_PAY_NOTIFY_URL`（**必须 HTTPS**）
3. 在 `PayController` 里补预下单，在 `POST /notice/pay/wechat` 验签后调 `payService.paySuccess(outTradeNo, transactionId)`
