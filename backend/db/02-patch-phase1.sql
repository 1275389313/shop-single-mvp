-- Phase 1 patch on top of upstream yami_shop.sql
-- Safe to re-run (IF NOT EXISTS / ignore duplicate column errors by checking information_schema).

USE yami_shops;

SET @exist := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = 'yami_shops' AND TABLE_NAME = 'tz_user' AND COLUMN_NAME = 'wx_open_id'
);
SET @sql := IF(@exist = 0,
  'ALTER TABLE tz_user ADD COLUMN wx_open_id varchar(64) DEFAULT NULL COMMENT ''微信openid（mock 或真实）'' AFTER user_mobile',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exist := (
  SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = 'yami_shops' AND TABLE_NAME = 'tz_user' AND INDEX_NAME = 'uk_wx_open_id'
);
SET @sql := IF(@exist = 0,
  'ALTER TABLE tz_user ADD UNIQUE KEY uk_wx_open_id (wx_open_id)',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exist := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = 'yami_shops' AND TABLE_NAME = 'tz_order_refund' AND COLUMN_NAME = 'reject_message'
);
SET @sql := IF(@exist = 0,
  'ALTER TABLE tz_order_refund ADD COLUMN reject_message varchar(300) DEFAULT NULL COMMENT ''拒绝原因'' AFTER receive_message',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
