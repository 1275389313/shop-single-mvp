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

**已有数据卷不会自动跑新 SQL。** Phase 1 之后升级请再执行一次：

```bash
docker compose exec -T mysql mysql -uroot -proot --default-character-set=utf8mb4 yami_shops < backend/db/03-patch-phase2.sql
docker compose exec -T mysql mysql -uroot -proot --default-character-set=utf8mb4 yami_shops < backend/db/04-patch-phase3.sql
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

OSS：开源版接的是 **七牛**（`backend/yami-shop-common/src/main/resources/shop.properties`）。本地默认 `uploadType=1` 写 `/tmp/shop-mvp-upload/`。COS/S3 需自行加实现，配置钩子已在 `ImgUpload` / `Qiniu`。

管理端账号（SQL 预置）：**admin / 123456**。登录后请改密。

接口文档：启动后 Knife4j（若已启用）或看 Controller。用户端需登录的路径前缀是 `/p/**`。

## 3. 管理后台

```bash
cd admin
pnpm install
pnpm dev
```

`admin/.env.development` 里 `VITE_APP_BASE_API=http://127.0.0.1:8085`。浏览器打开 Vite 提示的地址（本仓库默认 **http://localhost:9527**）。

导入 `03-patch-phase2.sql` 后请**重新登录**管理端，菜单才会出现「订单管理 → 退款审核」「门店管理 → 店铺设置」。`04-patch-phase3.sql` 为退款审核增加「确认收货退款」权限；未导入时，拥有审核权限的账号仍可确认退货（接口兼容 `order:refund:audit`）。

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
   | 结算 | 提交订单（选地址，看运费） | `POST /p/order/confirm` → `POST /p/order/submit` |
   | 支付 | 自动调 mock 支付 | `POST /p/order/normalPay`（当场 `status=2`） |
   | 订单 | 订单列表 / 详情 | `/p/myOrder/**`；详情可 **确认收货** |
   | 退款申请 | 订单详情 / 列表「申请退款」 | `POST /p/refund/apply` |
   | 退货物流 | 售后页「填写退货物流」 | `PUT /p/refund/express` |
   | 地址 | 我的 → 收货地址 | `/p/address/**` |

6. 管理端（浏览器 `http://localhost:9527`，账号 `admin / 123456`，滑块验证码）：
   - **订单管理** 发货（待发货订单）
   - **退款审核** 同意 / 拒绝（`PUT /order/refund/audit`）。仅退款同意=立刻 mock 退款；退货退款同意=等买家寄回。
   - 买家填写物流后，**确认收货退款**（`PUT /order/refund/receive`）。只改库，**不会**打微信退款。
   - 可选：再调 `POST /notice/pay/mock` `{ "payNo": "..." }` 验证支付回调幂等。

7. 常见失败：
   - 开发者工具在 A 电脑、API 在 B 电脑：把 `VITE_APP_BASE_API` 改成 B 的局域网 IP，并勾选不校验域名
   - `request:fail`：API 没起来，或端口不是 8086
   - 登录后立刻过期：Redis 没起
   - 管理端看不到退款菜单：未导入 `03-patch-phase2.sql`，或导入后未重新登录

H5 联调：`pnpm dev:h5`（默认占 80 端口，需权限）。H5 没有 `wx.login`，用 **模拟微信登录**。

## Mock 微信登录 + Mock 支付

默认 `shop-mvp.mock.wechat-login=true`、`shop-mvp.mock.pay=true`。

1. 打开登录页，点 **微信登录** 或 **模拟微信登录**（`POST /wx/login`，mock 时 `code` 任意）
2. 加购 → 提交订单（`POST /p/order/submit`）→ 前端会调 `POST /p/order/normalPay`
3. 订单变为待发货（status=2）。也可用 `POST /notice/pay/mock` 按 `payNo` 再回调一次，**幂等**
4. 管理端发货 → 用户确认收货
5. 用户申请退款：
   - **仅退款**：管理端同意 → 立即 mock 退款成功
   - **退货退款**：管理端同意 → 买家填写物流（`PUT /p/refund/express`）→ 管理端确认收货（`PUT /order/refund/receive`）→ mock 退款成功

关闭 mock 后，真实 `code2session` / 微信预下单 **尚未实现**（会明确报错），见下方「Mock 与真实能力差距」。**不要**为了联调去配商户号。

## HTTPS

微信登录、支付回调、小程序 `request` 合法域名都要求公网 **HTTPS**。本地可用内网穿透把 8086 映射出去。生产反向代理（Nginx）终止 TLS，转发给 `yami-shop-api` / `yami-shop-admin`。回调地址填 `WX_PAY_NOTIFY_URL=https://your.domain/notice/pay/wechat`（真实支付接入后）。

## 已知限制 / 剩余缺口

已具备（开源 mall4j + 本仓库补丁）：

- 账号密码登录、mock 微信登录（开发者工具 `uni.login` 或「模拟微信登录」）
- 首页轮播/分类/标签、分类与搜索、SPU/SKU/库存、购物车
- 下单、固定/满额运费模板（管理端运费模板）、mock 支付
- 订单列表/详情、取消未支付、确认收货、管理端发货
- 未支付超时关单 + 回库存（Spring 定时，不依赖 xxl-job）
- 支付回调幂等
- 退款/售后：申请（仅退款 / 退货退款）→ 审核 → 买家回填物流 → 商家确认收货并 mock 退款
- 地址 CRUD、商品上下架、Banner、店铺设置页（单店）
- 管理端操作日志（`@SysLog`，如发货、退款审核、确认退货）

明确未完成或薄弱：

- **真实微信登录**（jscode2session）与 **真实微信支付/退款**（WxJava 在开源版基本被注释）；本阶段刻意保持 mock
- 确认收货后上游把状态写成 5（成功），待评价(4) 与评价闭环不完整
- 对象存储目前是七牛钩子，**腾讯云 COS** 需自写；本地文件上传即可跑通
- 管理端验证码依赖 anji captcha 缓存，环境不齐时可能影响登录（见上游文档）
- 生产级 HTTPS、域名、小程序审核、支付商户号均未配置
- 我的页「分销中心 / 优惠券 / 消息 / 足迹」仍是上游未开源占位 toast

### Mock 与真实能力差距

本仓库默认 **mock 到底**，不要为了联调提交真实密钥。

| 能力 | Mock（当前默认） | 真实接入（未做） |
| --- | --- | --- |
| 微信登录 | `SHOP_MVP_MOCK_WECHAT_LOGIN=true`，`code` 任意 | `jscode2session`，需 `WX_APP_ID` / `WX_APP_SECRET` |
| 支付 | `SHOP_MVP_MOCK_PAY=true`，`normalPay` 当场已付 | 微信预下单 + 收银台 + HTTPS 回调验签 |
| 退款 | 审核/确认收货只改库，`return_money_sts=1` | 微信退款 API + 退款回调；`out_refund_no` 仍为空 |
| 物流轨迹 | 退货只存公司名+单号；正向物流仍走上游 `/delivery/check`（快递 100 占位 URL，无密钥） | 需快递 100 / 微信物流密钥，见配置占位，勿提交 |
| 订阅消息 | 无 | 模板 id 配置钩子（P1 TODO） |

**P1 后续（本轮不做，仅占位）：** 优惠券、物流轨迹（API + 假数据即可）、带图评价、库存预警、简单仪表盘、订阅消息配置钩子。

## 开发约定

- 不要提交 `.env`、真实 AppSecret、商户密钥、证书、快递查询密钥
- 新增功能优先落在现有模块，不要拆微服务
- 保持单店：不要恢复「创建第二个店铺」接口
