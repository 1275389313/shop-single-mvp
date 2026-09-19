-- P1 stock alerts (库存预警). Reuses tz_sku.stocks / tz_prod; adds optional per-SKU threshold.
-- Safe to re-run. Existing Docker volumes must import this manually (init scripts only run on first boot).

USE yami_shops;
SET NAMES utf8mb4;

SET @exist := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = 'yami_shops' AND TABLE_NAME = 'tz_sku' AND COLUMN_NAME = 'stocks_arm'
);
SET @sql := IF(@exist = 0,
  'ALTER TABLE tz_sku ADD COLUMN stocks_arm int(11) DEFAULT NULL COMMENT ''SKU库存预警阈值，NULL跟随全局，-1关闭预警'' AFTER actual_stocks',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

INSERT INTO tz_sys_config (`param_key`, `param_value`, `remark`)
SELECT 'STOCK_ALERT_THRESHOLD', '10', '全局库存预警阈值。SKU 可售库存 stocks <= 该值时预警；SKU.stocks_arm 非空则覆盖（-1 关闭）'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tz_sys_config WHERE param_key = 'STOCK_ALERT_THRESHOLD');

INSERT INTO tz_sys_menu (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 930, 34, '库存预警', 'prod/stockAlert', '', 1, '', 4
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tz_sys_menu WHERE menu_id = 930);

INSERT INTO tz_sys_menu (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 931, 930, '查看', '', 'prod:stockAlert:page,prod:stockAlert:info', 2, '', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tz_sys_menu WHERE menu_id = 931);

INSERT INTO tz_sys_menu (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 932, 930, '修改阈值', '', 'prod:stockAlert:update', 2, '', 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tz_sys_menu WHERE menu_id = 932);

UPDATE tz_sys_menu SET name='库存预警', url='prod/stockAlert', parent_id=34, type=1, order_num=4 WHERE menu_id=930;
UPDATE tz_sys_menu SET name='查看', perms='prod:stockAlert:page,prod:stockAlert:info', parent_id=930, type=2, order_num=0 WHERE menu_id=931;
UPDATE tz_sys_menu SET name='修改阈值', perms='prod:stockAlert:update', parent_id=930, type=2, order_num=1 WHERE menu_id=932;

INSERT INTO tz_sys_role_menu (`role_id`, `menu_id`)
SELECT 1, 930 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tz_sys_role_menu WHERE role_id = 1 AND menu_id = 930);
INSERT INTO tz_sys_role_menu (`role_id`, `menu_id`)
SELECT 1, 931 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tz_sys_role_menu WHERE role_id = 1 AND menu_id = 931);
INSERT INTO tz_sys_role_menu (`role_id`, `menu_id`)
SELECT 1, 932 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tz_sys_role_menu WHERE role_id = 1 AND menu_id = 932);
