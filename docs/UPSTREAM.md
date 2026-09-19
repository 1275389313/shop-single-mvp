# 上游来源与许可

本仓库是基于开源 **Mall4j** 栈的单店微信小程序电商 MVP 二开，**不是** mall4cloud / 微服务 / 多商户版。

开源版协议为 **GNU Affero General Public License v3.0 (AGPLv3)**。闭源商用需自行评估并遵守上游协议；商业授权说明见 [Mall4j 官网](https://www.mall4j.com)。

## 引入的上游仓库

| 本仓库目录 | 上游 | 引入提交 | 许可 |
| --- | --- | --- | --- |
| `backend/` | [gz-yami/mall4j](https://github.com/gz-yami/mall4j) | `f19b355fe50485b8c41507e6e646fa431fe064a8` (2026-09-14) | AGPLv3，完整文本见 `backend/LICENSE` |
| `admin/` | [gz-yami/mall4v](https://github.com/gz-yami/mall4v) | `1b558119e82dfb4f37ce1f90c626dd3e2a5c88c5` (2026-09-08) | AGPLv3，完整文本见 `admin/LICENSE` |
| `uniapp/` | [gz-yami/mall4uni](https://github.com/gz-yami/mall4uni) | `24d91aa6830573ba0e6dceb8f55bb45a04793018` (2026-09-14) | AGPLv3，完整文本见 `uniapp/LICENSE` |

核验日期：2026-09-19。引入时上游开源版定位为 **B2C 单商户** Spring Boot 4 + Vue3 模块化单体，不是 mall4cloud。

## 布局说明

上游 `mall4j` 仓库内还附带 `front-end/mall4v`、`front-end/mall4uni`、`front-end/mall4m` 副本。本仓库：

- 后端模块、SQL、上游文档放在 `backend/`（**不重复拷贝** 其 `front-end/`，避免和独立前端仓库漂移）
- 管理端以独立仓库 **mall4v** 为准，放在 `admin/`
- 用户端以独立仓库 **mall4uni** 为准，放在 `uniapp/`
- 不引入原生小程序仓库 mall4m（非本 MVP 目标）

## 版权与归属

- 原作者：广州市蓝海创新科技有限公司 / Mall4j
- 官网：https://www.mall4j.com
- 各子目录保留上游 `LICENSE`、`README.md` 原文
- 本仓库新增的适配代码（mock 登录/支付、定时关单、退款钩子、Docker 编排、文档）同样按 AGPLv3 分发

## 特别鸣谢（上游 README）

- WxJava: https://github.com/Wechat-Group/WxJava
- Sa-Token: https://gitee.com/dromara/sa-token
