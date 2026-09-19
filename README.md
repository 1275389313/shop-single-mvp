# shop-single-mvp

单店微信小程序电商 MVP。基于开源 **mall4j** 模块化单体（Spring Boot 4 / Java 17+），**不是** mall4cloud、**不是** 多商户、**不是** 微服务。

| 目录 | 来源 |
| --- | --- |
| `backend/` | [mall4j](https://github.com/gz-yami/mall4j) 后端（不含其内置 front-end 副本） |
| `admin/` | [mall4v](https://github.com/gz-yami/mall4v) Vue3 + Element Plus 管理端 |
| `uniapp/` | [mall4uni](https://github.com/gz-yami/mall4uni) uni-app（微信小程序 / 可跑 H5） |

许可与提交号见 [docs/UPSTREAM.md](docs/UPSTREAM.md)。协议为 **AGPLv3**。

订单状态机与 mock 支付时序见 [docs/order-flow.md](docs/order-flow.md)。单店残留说明见 [docs/single-store.md](docs/single-store.md)。

## 环境要求

- JDK 17+（上游 `pom.xml` 为 17，本环境可用 21）
- Maven 3.8+
- Node.js `^20.19 || >=22.12`，包管理器 **pnpm**
- MySQL 8 + Redis 7（推荐 Docker Compose）
- 微信开发者工具（小程序）；H5 可用浏览器作联调

开源 mall4j **没有** RabbitMQ/Kafka。延迟关单用 Spring `@Scheduled`（本仓库）。上游 xxl-job 客户端仍在 admin 里但默认注释，不需要先装 xxl-job。

## 1. 基础设施

```bash
cp .env.example .env   # 按需改密码，不要把真实密钥提交进 git
docker compose up -d
```

Compose 只起 **MySQL + Redis**。首次启动会导入：

1. `backend/db/yami_shop.sql`
2. `backend/db/02-patch-phase1.sql`（`tz_user.wx_open_id` 等）
3. `backend/db/03-patch-phase2.sql`（退款审核 / 店铺设置菜单）
4. `backend/db/04-patch-phase3.sql`（确认退货收货权限）
5. `backend/db/05-patch-coupon.sql`（优惠券表 + 菜单 + 演示满减/折扣券）
6. `backend/db/06-patch-stock-alert.sql`（SKU `stocks_arm` + 全局阈值配置 + 库存预警菜单）
7. `backend/db/07-patch-dashboard.sql`（数据看板菜单）

**已有数据卷不会自动跑新 SQL。** 升级请再执行：

```bash
docker compose exec -T mysql mysql -uroot -proot --default-character-set=utf8mb4 yami_shops < backend/db/03-patch-phase2.sql
docker compose exec -T mysql mysql -uroot -proot --default-character-set=utf8mb4 yami_shops < backend/db/04-patch-phase3.sql
docker compose exec -T mysql mysql -uroot -proot --default-character-set=utf8mb4 yami_shops < backend/db/05-patch-coupon.sql
docker compose exec -T mysql mysql -uroot -proot --default-character-set=utf8mb4 yami_shops < backend/db/06-patch-stock-alert.sql
docker compose exec -T mysql mysql -uroot -proot --default-character-set=utf8mb4 yami_shops < backend/db/07-patch-dashboard.sql
```

没有 Docker 时，自行安装 MySQL/Redis，导入上述 SQL，账号默认 `root/root`，库名 `yami_shops`。

## 2. 后端

```bash
cd backend
mvn -pl yami-shop-api,yami-shop-admin -am -DskipTests package
# 用户端 API :8086
mvn -pl yami-shop-api -DskipTests spring-boot:run
# 管理端 API :8085（另开终端）
mvn -pl yami-shop-admin -DskipTests spring-boot:run
```

默认 profile：`dev`。连接串走环境变量，缺省即本机 Docker：

| 变量 | 默认 | 用途 |
| --- | --- | --- |
| `MYSQL_HOST` / `MYSQL_PORT` / `MYSQL_DATABASE` | 127.0.0.1 / 3306 / yami_shops | JDBC |
| `MYSQL_USERNAME` / `MYSQL_PASSWORD` | root / root | JDBC |
| `REDIS_HOST` / `REDIS_PORT` / `REDIS_DATABASE` | 127.0.0.1 / 6379 / 0 | 缓存与 token |
| `SHOP_MVP_MOCK_WECHAT_LOGIN` | true | mock 微信登录 |
| `SHOP_MVP_MOCK_PAY` | true | mock 支付（下单后直接已付） |
| `SHOP_MVP_ORDER_AUTO_CLOSE` | true | 未支付超时关单 + 回库存 |
| `SHOP_MVP_ORDER_AUTO_CLOSE_MINUTES` | 30 | 超时分钟 |
| `WX_APP_ID` / `WX_APP_SECRET` | 空 | 真实小程序（mock 关闭后才需要） |
| `WX_PAY_MCH_ID` / `WX_PAY_API_KEY` / `WX_PAY_NOTIFY_URL` | 空 | 真实支付；回调 URL 必须 **HTTPS** |
| `KUAIDI100_CUSTOMER` / `KUAIDI100_KEY` | 空 | 快递100即时查询；都空则返回 **模拟轨迹** |

OSS：开源版接的是 **七牛**（`backend/yami-shop-common/src/main/resources/shop.properties`）。本地默认 `uploadType=1` 写 `/tmp/shop-mvp-upload/`，图片由 API 以 `http://127.0.0.1:8086/mall4j/img/` 提供。评价晒图走这条本地上传，**不需要腾讯云 COS 密钥**。COS/S3 需自行加实现，配置钩子已在 `ImgUpload` / `Qiniu`。

管理端账号（SQL 预置）：**admin / 123456**。登录后请改密。

接口文档：启动后 Knife4j（若已启用）或看 Controller。用户端需登录的路径前缀是 `/p/**`。

## 3. 管理后台

```bash
cd admin
pnpm install
pnpm dev
```

`admin/.env.development` 里 `VITE_APP_BASE_API=http://127.0.0.1:8085`，本地图片前缀指向用户端 `http://127.0.0.1:8086/mall4j/img/`。浏览器打开 Vite 提示的地址（本仓库默认 **http://localhost:9527**）。

导入 `05-patch-coupon.sql` 后请**重新登录**管理端，菜单才会出现「门店管理 → 优惠券」。导入 `06-patch-stock-alert.sql` 后重新登录才会出现「产品管理 → 库存预警」。`04-patch-phase3.sql` 为退款审核增加「确认收货退款」权限；未导入时，拥有审核权限的账号仍可确认退货（接口兼容 `order:refund:audit`）。

## 4. uni-app / 微信小程序

```bash
cd uniapp
pnpm install
pnpm dev:mp-weixin   # 用微信开发者工具导入 dist/dev/mp-weixin
# 或
pnpm dev:h5          # H5 联调（登录页点「模拟微信登录」）
```

| 配置 | 位置 |
| --- | --- |
| 用户端 API | `uniapp/.env.development` → `VITE_APP_BASE_API`（默认 `http://127.0.0.1:8086`） |
| 小程序 AppID | `uniapp/src/manifest.json` → `mp-weixin.appid`（可空，用测试号） |
| H5 公众号 AppID | `VITE_APP_MP_APPID`（可空） |
| 模拟微信登录按钮 | `VITE_APP_MOCK_WX=true`（登录页） |

**不要把真实支付商户密钥写进前端。**

### 微信开发者工具对接本地 API（mock 闭环）

本仓库 **默认 mock 微信登录 + mock 支付**，不需要商户号、不需要真实 AppSecret、不必配置合法域名。开发者工具和 `yami-shop-api` 必须跑在**同一台机器**上，这样 `127.0.0.1:8086` 才指向本机后端。

1. 按上文启动 MySQL/Redis、`yami-shop-api`（8086）、可选 `yami-shop-admin`（8085）。确认：

   ```bash
   curl -s -X POST http://127.0.0.1:8086/wx/login \
     -H 'Content-Type: application/json' \
     -d '{"code":"devtools-demo","nickName":"工具用户"}'
   ```

   应返回 `code: "00000"` 和 `accessToken`。

2. `cd uniapp && pnpm install && pnpm dev:mp-weixin`，产物目录：`uniapp/dist/dev/mp-weixin`。

3. 打开[微信开发者工具](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html)：
   - 导入目录选 **`uniapp/dist/dev/mp-weixin`**（不是 `uniapp/` 源码根）
   - AppID：可用测试号，或填自己的；`manifest.json` 里目前为空
   - 详情 → 本地设置，勾选：
     - **不校验合法域名、web-view（业务域名）、TLS 版本以及 HTTPS 证书**
     - 建议同时勾选「启用自定义处理」以外的本地调试项保持默认即可
   - `mp-weixin.setting.urlCheck` 已是 `false`，但仍建议在工具里勾上「不校验合法域名」

4. 编译打开登录页：
   - 点 **微信登录**：走 `uni.login` 拿 `code`，再 `POST /wx/login`（mock 开启时不会请求微信 `code2session`）
   - 若 `uni.login` 失败，点 **模拟微信登录**（`code` 为任意 `dev-时间戳`）

5. mock 闭环建议路径（全部打本地 8086，支付不会出现真实收银台）：

   | 步骤 | 页面 | 接口 |
   | --- | --- | --- |
   | 登录 | 登录页 | `POST /wx/login` |
   | 首页 | 首页 Tab | `GET /indexImgs`、`GET /prod/tag/prodTagList` |
   | 分类 / 搜索 | 分类 Tab、搜索页 | 分类与 `prod` 列表接口 |
   | 商品 + SKU | 商品详情 | `GET /prod/prodInfo` |
   | 购物车 | 购物车 Tab | `/p/shopCart/**` |
   | 领券 | 首页「领优惠券」或 我的 → 领券中心 | `GET /coupon/list`、`POST /p/coupon/receive` |
   | 结算 | 提交订单（选地址，选优惠券，看运费） | `POST /p/order/confirm`（`couponIds` 为用户券 ID）→ `POST /p/order/submit`（核销） |
   | 支付 | 自动调 mock 支付 | `POST /p/order/normalPay`（当场 `status=2`） |
   | 订单 | 订单列表 / 详情 | `/p/myOrder/**`；详情可 **确认收货**、**查看物流** |
   | 评价晒图 | 确认收货后「评价晒图」 | `POST /p/file/upload`、`POST /p/prodComm`；商品详情可见 |
   | 退款申请 | 订单详情 / 列表「申请退款」 | `POST /p/refund/apply` |
   | 退货物流 | 售后页「填写退货物流」 | `PUT /p/refund/express`；填单号后可 **查看退货轨迹** |
   | 地址 | 我的 → 收货地址 | `/p/address/**` |

6. 管理端（浏览器 `http://localhost:9527`，账号 `admin / 123456`，滑块验证码）：
   - **数据看板** 首页卡片 +「订单管理 → 数据看板」：今日/近7日/近30日/累计 GMV、已付/待付/退款
   - **库存预警** 全局阈值、低于阈值的 SKU 列表；商品发布页可为 SKU 填独立阈值
   - **优惠券** 新建满减/折扣、投放、改库存与有效期
   - **订单管理** 发货（待发货订单）；已发货订单详情可看物流时间轴（无密钥时为模拟轨迹）
   - **退款审核** 同意 / 拒绝（`PUT /order/refund/audit`）。仅退款同意=立刻 mock 退款；退货退款同意=等买家寄回。
   - 买家填写物流后，**确认收货退款**（`PUT /order/refund/receive`）。详情/确认弹窗可看退货轨迹。只改库，**不会**打微信退款。
   - 可选：再调 `POST /notice/pay/mock` `{ "payNo": "..." }` 验证支付回调幂等。

7. 常见失败：
   - 开发者工具在 A 电脑、API 在 B 电脑：把 `VITE_APP_BASE_API` 改成 B 的局域网 IP，并勾选不校验域名
   - `request:fail`：API 没起来，或端口不是 8086
   - 登录后立刻过期：Redis 没起
   - 管理端看不到优惠券菜单：未导入 `05-patch-coupon.sql`，或导入后未重新登录
   - 管理端看不到库存预警菜单：未导入 `06-patch-stock-alert.sql`，或导入后未重新登录
   - 管理端看不到数据看板菜单：未导入 `07-patch-dashboard.sql`，或导入后未重新登录
   - 管理端看不到退款菜单：未导入 `03-patch-phase2.sql`，或导入后未重新登录

H5 联调：`pnpm dev:h5`（默认 **http://localhost:5173**）。H5 没有 `wx.login`，用 **模拟微信登录**。

## Mock 微信登录 + Mock 支付

默认 `shop-mvp.mock.wechat-login=true`、`shop-mvp.mock.pay=true`。

1. 打开登录页，点 **微信登录** 或 **模拟微信登录**（`POST /wx/login`，mock 时 `code` 任意）
2. 加购 → 提交订单（`POST /p/order/submit`）→ 前端会调 `POST /p/order/normalPay`
3. 订单变为待发货（status=2）。也可用 `POST /notice/pay/mock` 按 `payNo` 再回调一次，**幂等**
4. 管理端发货 → 用户确认收货（订单进入 **待评价**）→ 评价晒图（可选图，立刻出现在商品详情）
5. 用户申请退款：
   - **仅退款**：管理端同意 → 立即 mock 退款成功
   - **退货退款**：管理端同意 → 买家填写物流（`PUT /p/refund/express`）→ 可查退货轨迹 → 管理端确认收货（`PUT /order/refund/receive`）→ mock 退款成功

关闭 mock 后，真实 `code2session` / 微信预下单 **尚未实现**（会明确报错），见下方「Mock 与真实能力差距」。**不要**为了联调去配商户号。

## HTTPS

微信登录、支付回调、小程序 `request` 合法域名都要求公网 **HTTPS**。本地可用内网穿透把 8086 映射出去。生产反向代理（Nginx）终止 TLS，转发给 `yami-shop-api` / `yami-shop-admin`。回调地址填 `WX_PAY_NOTIFY_URL=https://your.domain/notice/pay/wechat`（真实支付接入后）。

## 已知限制 / 剩余缺口

已具备（开源 mall4j + 本仓库补丁）：

- 账号密码登录、mock 微信登录（开发者工具 `uni.login` 或「模拟微信登录」）
- 首页轮播/分类/标签、分类与搜索、SPU/SKU/库存、购物车
- 下单、固定/满额运费模板（管理端运费模板）、mock 支付
- 订单列表/详情、取消未支付、确认收货（待评价）、管理端发货
- 评价晒图：确认收货后评分+文字+可选图，商品详情展示；管理端「商品 → 评论管理」可隐藏
- 物流轨迹：订单发货单号 / 退货快递单号；默认模拟时间轴，可插快递100密钥
- 未支付超时关单 + 回库存（Spring 定时，不依赖 xxl-job）
- 支付回调幂等
- 退款/售后：申请（仅退款 / 退货退款）→ 审核 → 买家回填物流 → 商家确认收货并 mock 退款
- 地址 CRUD、商品上下架、Banner、店铺设置页（单店）
- 管理端操作日志（`@SysLog`，如发货、退款审核、确认退货、优惠券）
- 优惠券：管理端满减/折扣、投放与库存；买家领取、结算选择、下单核销；未支付取消退券
- 库存预警：全局阈值（`tz_sys_config`）+ 可选 SKU 阈值（`tz_sku.stocks_arm`）；管理端列表与首页/商品列表角标
- 数据看板：管理端首页卡片 +「订单管理 → 数据看板」；今日/近7日/近30日/累计 GMV、已付/待付/关闭、退款成功与处理中

明确未完成或薄弱：

- **真实微信登录**（jscode2session）与 **真实微信支付/退款**（WxJava 在开源版基本被注释）；本阶段刻意保持 mock
- 对象存储目前是七牛钩子，**腾讯云 COS** 需自写；评价与后台图默认本地上传即可跑通
- 管理端验证码依赖 anji captcha 缓存，环境不齐时可能影响登录（见上游文档）
- 生产级 HTTPS、域名、小程序审核、支付商户号均未配置
- 我的页「分销中心 / 消息 / 足迹」仍是上游未开源占位 toast
- 优惠券 P1 仅全店通用、一单一券；指定商品/品类券、叠加券未做
- 数据看板：自然日按上海时区；GMV 按 **支付时间**，样例 SQL 订单多在 2019 年所以今日/近7日/近30日经常为 0（看累计或新 mock 单）；不是财务对账、不含优惠拆分、不接微信账单
- 物流：微信物流助手 / 订阅消息未接；真实快递100对顺丰等常要手机号，退货寄件人手机未单独存（目前复用订单收货人手机）；未知公司名可能解析不出 `com` 编码；mock 轨迹按发货/寄回时间推演，不是承运商数据

### Mock 与真实能力差距

本仓库默认 **mock 到底**，不要为了联调提交真实密钥。

| 能力 | Mock（当前默认） | 真实接入（未做） |
| --- | --- | --- |
| 微信登录 | `SHOP_MVP_MOCK_WECHAT_LOGIN=true`，`code` 任意 | `jscode2session`，需 `WX_APP_ID` / `WX_APP_SECRET` |
| 支付 | `SHOP_MVP_MOCK_PAY=true`，`normalPay` 当场已付 | 微信预下单 + 收银台 + HTTPS 回调验签 |
| 退款 | 审核/确认收货只改库，`return_money_sts=1` | 微信退款 API + 退款回调；`out_refund_no` 仍为空 |
| 物流轨迹 | 无 `KUAIDI100_CUSTOMER`/`KEY` 时按发货/寄回时间生成模拟时间轴（会标明 mock） | 填快递100 customer+key 后走即时查询；微信物流助手未做 |
| 评价晒图 | 本地上传 + 可选占位图，评价立刻上架 | 七牛/COS 真实 CDN；人工审核流可把默认 status 改回 0 |
| 订阅消息 | 无 | 模板 id 配置钩子（P1 TODO） |

**P1 后续（本轮不做，仅占位）：** 订阅消息配置钩子。

## 优惠券怎么用（P1）

开源 mall4j **没有**优惠券表，只留了 `OrderParam.couponIds` 和确认/提交监听顺序位。本仓库补了 `tz_coupon` / `tz_coupon_user`，接到原有结算 UI。

1. 导入 `backend/db/05-patch-coupon.sql`，**重新登录**管理端。
2. 管理端「门店管理 → 优惠券」：新建或编辑 **满减** / **折扣**，填门槛、库存、领取时间、有效天数，状态选 **投放**。SQL 已预置「满50减10」「全店8.5折」。
3. 小程序/H5 登录后：首页「领优惠券」或「我的 → 领券中心」领取；「我的优惠券」可查看。
4. 结算页点优惠券，选一张（一单一券）。`POST /p/order/confirm` 按商品总额校验门槛并算出减免；`POST /p/order/submit` 核销。订单详情「优惠券」即 `reduce_amount`。
5. 未支付取消或超时关单会把券退回未使用（过期则标过期）。已支付不退券。

`couponIds` 传的是 **用户券 ID**（`coupon_user_id`），不是模板 ID。

## 物流轨迹怎么用（P1）

开源 mall4j 的 `GET /delivery/check` 会把 `tz_delivery.query_url` 里的 `{dvyFlowId}` 换成单号再 GET 快递100 **旧免费接口**。那个 URL 现在没有密钥基本不可用，本仓库不再直接打它。

当前行为：

1. 管理端发货写入 `tz_order.dvy_id` / `dvy_flow_id`；买家退货写入 `tz_order_refund.express_name` / `express_no`。
2. 查询走 **快递100即时查询接口**（`POST https://poll.kuaidi100.com/poll/query.do`）。`tz_delivery.query_url` 只用来解析 `type=` 公司编码（如 `shunfeng`）。
3. `.env` 里 `KUAIDI100_CUSTOMER`、`KUAIDI100_KEY` **都留空**（默认）：返回一条按发货/寄回时间推出来的模拟轨迹，`mock=true`，文案会写明「非真实运单」。不需要任何密钥即可联调。
4. 以后要接真轨迹：在 **本机 `.env` 或环境变量** 填快递100企业 customer 与 key（[即时查询](https://api.kuaidi100.com/document/5f0ffb5ebc8da837cbd8aefc)），**不要提交进 git**。重启 `yami-shop-api` / `yami-shop-admin`。顺丰等公司可能还要手机号，本仓库会带上订单收货人手机；退货寄件人手机没有单独存。

接口：

| 端 | 方法 | 路径 | 说明 |
| --- | --- | --- | --- |
| 买家 | GET | `/p/delivery/check?orderNumber=` 或 `/delivery/check?orderNumber=` | 正向发货；须登录且是自己的单 |
| 买家 | GET | `/p/refund/delivery?refundSn=` | 退货；须登录且是自己的退款单 |
| 管理端 | GET | `/order/order/delivery/check?orderNumber=` | 权限 `order:order:info` |
| 管理端 | GET | `/order/refund/delivery?refundId=` | 权限 `order:refund:info` |

uni-app：订单列表/详情「查看物流」；售后详情/列表在已填退货单号后「查看轨迹」。管理端订单详情、退款详情/确认收货弹窗展示时间轴。

未发货或未填退货单号时返回空 `data` 和说明，不报错。

## 评价晒图怎么用（P1）

复用 mall4j 的 `tz_prod_comm` / `ProdCommController`，补上登录鉴权、收货校验和本地晒图。

1. mock 支付 → 管理端发货 → 买家确认收货，订单变为 **待评价**（status=4）。历史已写成成功(5) 且未评的商品仍可评。
2. 订单详情/列表点 **评价晒图**：1–5 分 + 文字 + 最多 9 张图（可点「使用占位图」）。`POST /p/prodComm`。
3. 新评价 **立刻上架**（`status=1`），商品详情和「查看全部」能看到分数、内容和图片。
4. 图片走本地上传 `POST /p/file/upload`（`/tmp/shop-mvp-upload/`，`GET /mall4j/img/**`），**不要配 COS/七牛真实密钥**。
5. 管理端「商品 → 评论管理」（开源 mall4j 已有菜单）：**隐藏** 后商品页不再展示（status=-1），**显示** 可恢复。

| 端 | 方法 | 路径 |
| --- | --- | --- |
| 买家发表 | POST | `/p/prodComm` |
| 买家上传 | POST | `/p/file/upload` |
| 占位图 | POST | `/p/file/placeholder` |
| 商品评价 | GET | `/prodComm/prodCommPageByProd?prodId=&evaluate=-1` |
| 管理隐藏 | PUT | `/prod/prodComm/status?prodCommId=&status=-1` |

## 库存预警怎么用（P1）

开源 mall4j 的 `ProductDto.stocksArm` 只是 DTO 残留，表上没有库存预警。本仓库比较的是现有 **`tz_sku.stocks`**（下单扣减的可售库存，`-1` 无限不算预警），没有新业务表。

1. 导入 `backend/db/06-patch-stock-alert.sql`，**重新登录**管理端。
2. 「产品管理 → 库存预警」：改**全局阈值**（默认 10，存在 `tz_sys_config.STOCK_ALERT_THRESHOLD`）。可售库存 ≤ 阈值的启用 SKU 出现在列表里。
3. 列表或商品发布页的 SKU「预警阈值」：留空跟随全局；填数字覆盖；`-1` 该规格不预警。
4. 管理端首页和商品列表有低库存数量角标（只计**上架**商品的 SKU）。商品列表「低库存」标签按 SPU `total_stocks` 对比全局阈值，精确名单以预警页 SKU 为准。

不发短信、不订阅消息。mock 支付与库存扣减逻辑未改。

| 端 | 方法 | 路径 | 说明 |
| --- | --- | --- | --- |
| 管理端 | GET | `/prod/stockAlert/config` | 全局阈值 + 上架低库存数量 |
| 管理端 | PUT | `/prod/stockAlert/config` | `{ "globalThreshold": 10 }` |
| 管理端 | GET | `/prod/stockAlert/page` | 低库存 SKU 分页，`prodName` / `prodStatus` |
| 管理端 | PUT | `/prod/stockAlert/sku` | `{ "skuId", "stocksArm" }` |

## 数据看板怎么用（P1）

开源 mall4j 文案里有「统计报表」，仓库里 **没有** 对应管理端接口。本仓库用本店 `tz_order` / `tz_order_refund` 做一次聚合，**不读任何密钥**，不改 mock 支付。

1. 导入 `backend/db/07-patch-dashboard.sql`，**重新登录**管理端。
2. 首页卡片，或「订单管理 → 数据看板」：今日 / 近7日 / 近30日 / 累计。
3. 口径：
   - **GMV**：窗口内 `pay_time` 且 `is_payed=1` 的 `actual_total` 之和（含 mock 支付，**不**按退款冲减）
   - **下单数**：窗口内 `create_time`（含未付、关闭）
   - **待付款 / 关闭**：当前 `status=1` / `status=6` 且下单时间落在窗口
   - **退款成功**：`return_money_sts=1`，按 `refund_time`；**处理中**：`return_money_sts=0`，按 `apply_time`
   - **净额** = GMV − 退款成功金额（处理中未扣）
4. 近30日柱状/折线用已有 echarts。初始化样例订单多在 **2019**，今日窗口常为 0；看「累计」或走一遍 mock 下单支付。

| 端 | 方法 | 路径 | 说明 |
| --- | --- | --- | --- |
| 管理端 | GET | `/order/dashboard` | 权限 `order:dashboard:info` |

## 开发约定

- 不要提交 `.env`、真实 AppSecret、商户密钥、证书、快递查询密钥
- 新增功能优先落在现有模块，不要拆微服务
- 保持单店：不要恢复「创建第二个店铺」接口
