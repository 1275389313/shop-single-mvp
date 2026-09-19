-- Phase 3 patch: confirm-return permission for refund after-sales.
-- Safe to re-run. Existing Docker volumes must import this manually (init scripts only run on first boot).

USE yami_shops;
SET NAMES utf8mb4;

INSERT INTO tz_sys_menu (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 903, 900, '确认收货退款', '', 'order:refund:receive', 2, NULL, 2
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tz_sys_menu WHERE menu_id = 903);

UPDATE tz_sys_menu SET name='确认收货退款', perms='order:refund:receive', parent_id=900, type=2, order_num=2 WHERE menu_id=903;

INSERT INTO tz_sys_role_menu (`role_id`, `menu_id`)
SELECT 1, 903 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tz_sys_role_menu WHERE role_id = 1 AND menu_id = 903);
