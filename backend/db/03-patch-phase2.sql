-- Phase 2 patch: admin menus for refund audit + shop settings.
-- Safe to re-run. Existing Docker volumes must import this manually (init scripts only run on first boot).

USE yami_shops;
SET NAMES utf8mb4;

INSERT INTO tz_sys_menu (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 900, 91, '退款审核', 'order/refund', '', 1, NULL, 2
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tz_sys_menu WHERE menu_id = 900);

INSERT INTO tz_sys_menu (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 901, 900, '查看', '', 'order:refund:page,order:refund:info', 2, NULL, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tz_sys_menu WHERE menu_id = 901);

INSERT INTO tz_sys_menu (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 902, 900, '审核', '', 'order:refund:audit', 2, NULL, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tz_sys_menu WHERE menu_id = 902);

INSERT INTO tz_sys_menu (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 910, 63, '店铺设置', 'shop/shopDetail', '', 1, '', 5
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tz_sys_menu WHERE menu_id = 910);

INSERT INTO tz_sys_menu (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 911, 910, '查看', '', 'shop:shopDetail:info,shop:shopDetail:page', 2, '', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tz_sys_menu WHERE menu_id = 911);

INSERT INTO tz_sys_menu (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 912, 910, '修改', '', 'shop:shopDetail:update,shop:shopDetail:shopStatus', 2, '', 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tz_sys_menu WHERE menu_id = 912);

UPDATE tz_sys_menu SET name='退款审核', url='order/refund', parent_id=91, type=1, order_num=2 WHERE menu_id=900;
UPDATE tz_sys_menu SET name='查看', perms='order:refund:page,order:refund:info', parent_id=900, type=2 WHERE menu_id=901;
UPDATE tz_sys_menu SET name='审核', perms='order:refund:audit', parent_id=900, type=2 WHERE menu_id=902;
UPDATE tz_sys_menu SET name='店铺设置', url='shop/shopDetail', parent_id=63, type=1, order_num=5 WHERE menu_id=910;
UPDATE tz_sys_menu SET name='查看', perms='shop:shopDetail:info,shop:shopDetail:page', parent_id=910, type=2 WHERE menu_id=911;
UPDATE tz_sys_menu SET name='修改', perms='shop:shopDetail:update,shop:shopDetail:shopStatus', parent_id=910, type=2 WHERE menu_id=912;

INSERT INTO tz_sys_role_menu (`role_id`, `menu_id`)
SELECT 1, 900 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tz_sys_role_menu WHERE role_id = 1 AND menu_id = 900);
INSERT INTO tz_sys_role_menu (`role_id`, `menu_id`)
SELECT 1, 901 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tz_sys_role_menu WHERE role_id = 1 AND menu_id = 901);
INSERT INTO tz_sys_role_menu (`role_id`, `menu_id`)
SELECT 1, 902 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tz_sys_role_menu WHERE role_id = 1 AND menu_id = 902);
INSERT INTO tz_sys_role_menu (`role_id`, `menu_id`)
SELECT 1, 910 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tz_sys_role_menu WHERE role_id = 1 AND menu_id = 910);
INSERT INTO tz_sys_role_menu (`role_id`, `menu_id`)
SELECT 1, 911 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tz_sys_role_menu WHERE role_id = 1 AND menu_id = 911);
INSERT INTO tz_sys_role_menu (`role_id`, `menu_id`)
SELECT 1, 912 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tz_sys_role_menu WHERE role_id = 1 AND menu_id = 912);
