-- Phase 4 patch: P1 coupons (满减 / 折扣). Open-source mall4j had no coupon tables.
-- Safe to re-run. Existing Docker volumes must import this manually (init scripts only run on first boot).

USE yami_shops;
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `tz_coupon` (
  `coupon_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '优惠券ID',
  `shop_id` bigint(20) NOT NULL DEFAULT 1 COMMENT '店铺id，单店为1',
  `coupon_name` varchar(64) NOT NULL COMMENT '名称',
  `sub_title` varchar(128) DEFAULT NULL COMMENT '副标题',
  `coupon_type` tinyint(2) NOT NULL DEFAULT 1 COMMENT '1满减 2折扣',
  `cash_condition` decimal(15,2) NOT NULL DEFAULT 0.00 COMMENT '使用门槛，满多少可用，0为无门槛',
  `reduce_amount` decimal(15,2) DEFAULT NULL COMMENT '满减金额',
  `coupon_discount` decimal(5,2) DEFAULT NULL COMMENT '折扣（折），如 8.50 表示 8.5 折',
  `max_reduce_amount` decimal(15,2) DEFAULT NULL COMMENT '折扣券最多减免，NULL/0 不封顶',
  `suitable_prod_type` tinyint(2) NOT NULL DEFAULT 0 COMMENT '0全部商品（P1）',
  `stocks` int(11) NOT NULL DEFAULT 0 COMMENT '剩余库存，-1不限',
  `source_stock` int(11) NOT NULL DEFAULT 0 COMMENT '投放总量',
  `limit_num` int(11) NOT NULL DEFAULT 1 COMMENT '每人限领，-1不限',
  `start_time` datetime NOT NULL COMMENT '领取开始',
  `end_time` datetime NOT NULL COMMENT '领取结束',
  `valid_time_type` tinyint(2) NOT NULL DEFAULT 1 COMMENT '1领取后N天 2固定时间段',
  `valid_days` int(11) DEFAULT 30 COMMENT '领取后有效天数',
  `valid_start_time` datetime DEFAULT NULL COMMENT '固定有效开始',
  `valid_end_time` datetime DEFAULT NULL COMMENT '固定有效结束',
  `status` tinyint(2) NOT NULL DEFAULT 0 COMMENT '0下线 1投放',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`coupon_id`),
  KEY `idx_shop_status` (`shop_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券';

CREATE TABLE IF NOT EXISTS `tz_coupon_user` (
  `coupon_user_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户优惠券ID',
  `coupon_id` bigint(20) NOT NULL COMMENT '优惠券模板ID',
  `user_id` varchar(36) NOT NULL COMMENT '用户ID',
  `status` tinyint(2) NOT NULL DEFAULT 0 COMMENT '0未使用 1已使用 2已过期',
  `receive_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `user_start_time` datetime NOT NULL COMMENT '可用开始',
  `user_end_time` datetime NOT NULL COMMENT '可用结束',
  `use_time` datetime DEFAULT NULL,
  `order_number` varchar(50) DEFAULT NULL COMMENT '核销订单号',
  PRIMARY KEY (`coupon_user_id`),
  KEY `idx_user_status` (`user_id`,`status`),
  KEY `idx_coupon_user` (`coupon_id`,`user_id`),
  KEY `idx_order_number` (`order_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户领取的优惠券';

INSERT INTO tz_sys_menu (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 920, 63, '优惠券', 'coupon/coupon', '', 1, '', 6
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tz_sys_menu WHERE menu_id = 920);

INSERT INTO tz_sys_menu (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 921, 920, '查看', '', 'coupon:coupon:page,coupon:coupon:info', 2, '', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tz_sys_menu WHERE menu_id = 921);

INSERT INTO tz_sys_menu (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 922, 920, '新增', '', 'coupon:coupon:save', 2, '', 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tz_sys_menu WHERE menu_id = 922);

INSERT INTO tz_sys_menu (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 923, 920, '修改', '', 'coupon:coupon:update', 2, '', 2
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tz_sys_menu WHERE menu_id = 923);

INSERT INTO tz_sys_menu (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 924, 920, '删除', '', 'coupon:coupon:delete', 2, '', 3
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tz_sys_menu WHERE menu_id = 924);

UPDATE tz_sys_menu SET name='优惠券', url='coupon/coupon', parent_id=63, type=1, order_num=6 WHERE menu_id=920;
UPDATE tz_sys_menu SET name='查看', perms='coupon:coupon:page,coupon:coupon:info', parent_id=920, type=2, order_num=0 WHERE menu_id=921;
UPDATE tz_sys_menu SET name='新增', perms='coupon:coupon:save', parent_id=920, type=2, order_num=1 WHERE menu_id=922;
UPDATE tz_sys_menu SET name='修改', perms='coupon:coupon:update', parent_id=920, type=2, order_num=2 WHERE menu_id=923;
UPDATE tz_sys_menu SET name='删除', perms='coupon:coupon:delete', parent_id=920, type=2, order_num=3 WHERE menu_id=924;

INSERT INTO tz_sys_role_menu (`role_id`, `menu_id`)
SELECT 1, 920 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tz_sys_role_menu WHERE role_id = 1 AND menu_id = 920);
INSERT INTO tz_sys_role_menu (`role_id`, `menu_id`)
SELECT 1, 921 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tz_sys_role_menu WHERE role_id = 1 AND menu_id = 921);
INSERT INTO tz_sys_role_menu (`role_id`, `menu_id`)
SELECT 1, 922 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tz_sys_role_menu WHERE role_id = 1 AND menu_id = 922);
INSERT INTO tz_sys_role_menu (`role_id`, `menu_id`)
SELECT 1, 923 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tz_sys_role_menu WHERE role_id = 1 AND menu_id = 923);
INSERT INTO tz_sys_role_menu (`role_id`, `menu_id`)
SELECT 1, 924 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tz_sys_role_menu WHERE role_id = 1 AND menu_id = 924);

-- Demo coupons for mock path (skip if already present)
INSERT INTO `tz_coupon` (`coupon_id`,`shop_id`,`coupon_name`,`sub_title`,`coupon_type`,`cash_condition`,`reduce_amount`,`coupon_discount`,`max_reduce_amount`,`suitable_prod_type`,`stocks`,`source_stock`,`limit_num`,`start_time`,`end_time`,`valid_time_type`,`valid_days`,`status`)
SELECT 1, 1, '满50减10', '全店通用满减', 1, 50.00, 10.00, NULL, NULL, 0, 100, 100, 1, '2020-01-01 00:00:00', '2035-12-31 23:59:59', 1, 365, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tz_coupon WHERE coupon_id = 1);

INSERT INTO `tz_coupon` (`coupon_id`,`shop_id`,`coupon_name`,`sub_title`,`coupon_type`,`cash_condition`,`reduce_amount`,`coupon_discount`,`max_reduce_amount`,`suitable_prod_type`,`stocks`,`source_stock`,`limit_num`,`start_time`,`end_time`,`valid_time_type`,`valid_days`,`status`)
SELECT 2, 1, '全店8.5折', '满20可用，最多减30', 2, 20.00, NULL, 8.50, 30.00, 0, 100, 100, 1, '2020-01-01 00:00:00', '2035-12-31 23:59:59', 1, 365, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tz_coupon WHERE coupon_id = 2);
