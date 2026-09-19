-- P1 simple admin dashboard (数据看板). Menu only; metrics are live aggregates on tz_order / tz_order_refund.
-- Safe to re-run. Existing Docker volumes must import this manually (init scripts only run on first boot).

USE yami_shops;
SET NAMES utf8mb4;

INSERT INTO tz_sys_menu (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 940, 91, '数据看板', 'order/dashboard', '', 1, '', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tz_sys_menu WHERE menu_id = 940);

INSERT INTO tz_sys_menu (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 941, 940, '查看', '', 'order:dashboard:info', 2, '', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tz_sys_menu WHERE menu_id = 941);

UPDATE tz_sys_menu SET name='数据看板', url='order/dashboard', parent_id=91, type=1, order_num=0 WHERE menu_id=940;
UPDATE tz_sys_menu SET name='查看', perms='order:dashboard:info', parent_id=940, type=2, order_num=0 WHERE menu_id=941;

INSERT INTO tz_sys_role_menu (`role_id`, `menu_id`)
SELECT 1, 940 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tz_sys_role_menu WHERE role_id = 1 AND menu_id = 940);
INSERT INTO tz_sys_role_menu (`role_id`, `menu_id`)
SELECT 1, 941 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tz_sys_role_menu WHERE role_id = 1 AND menu_id = 941);
