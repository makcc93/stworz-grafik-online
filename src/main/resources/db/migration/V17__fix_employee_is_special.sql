UPDATE employee SET is_special = 0 WHERE is_special IS NULL;
UPDATE employee SET is_special = 0 WHERE is_special = 1 AND special_work_norm_id IS NULL;
ALTER TABLE employee MODIFY COLUMN is_special TINYINT(1) NOT NULL DEFAULT 0;