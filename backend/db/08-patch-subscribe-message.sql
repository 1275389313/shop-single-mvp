-- P1 WeChat mini-program subscribe message template ID placeholders.
-- Values stay empty so local mock pay/ship does not need AppSecret.
-- Fill later in 系统管理 → 参数管理, or via env WX_SUBSCRIBE_*_TEMPLATE_ID.
-- Safe to re-run. Existing Docker volumes must import this manually.

USE yami_shops;
SET NAMES utf8mb4;

INSERT INTO tz_sys_config (`param_key`, `param_value`, `remark`)
SELECT 'WX_SUBSCRIBE_PAY_TEMPLATE_ID', '', '小程序订阅消息：支付成功模板ID。留空则支付成功后不发送。不是密钥。也可改 .env 的 WX_SUBSCRIBE_PAY_TEMPLATE_ID'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tz_sys_config WHERE param_key = 'WX_SUBSCRIBE_PAY_TEMPLATE_ID');

INSERT INTO tz_sys_config (`param_key`, `param_value`, `remark`)
SELECT 'WX_SUBSCRIBE_SHIP_TEMPLATE_ID', '', '小程序订阅消息：发货通知模板ID。留空则发货后不发送。不是密钥。也可改 .env 的 WX_SUBSCRIBE_SHIP_TEMPLATE_ID'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tz_sys_config WHERE param_key = 'WX_SUBSCRIBE_SHIP_TEMPLATE_ID');
