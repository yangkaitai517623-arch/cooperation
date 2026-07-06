-- 修复所有用户密码
-- 密码: admin123
-- BCrypt哈希已验证正确

UPDATE sys_user
SET password = '$2a$10$75bt.TUAAsCVdVDmT6eJUuWaUiP.uKlYDtcm7O6ciSx/a1iYlRJH.';
