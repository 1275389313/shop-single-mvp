-- Phase 2 patch: admin menus for refund audit + shop settings.
-- Safe to re-run. Existing Docker volumes must import this manually (init scripts only run on first boot).

USE yami_shops;

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
