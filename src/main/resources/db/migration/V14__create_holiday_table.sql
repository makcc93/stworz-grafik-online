-- =============================================================
-- Flyway V14 – dni wolne (święta) zarządzane w bazie
--
--  * jeden rekord na dzień (UNIQUE holiday_date)
--  * custom = 0: święto skopiowane z biblioteki jollyday (inicjalizacja),
--    custom = 1: dzień dodany ręcznie przez administratora (np. dzień niehandlowy)
--  * rok bez żadnego rekordu → aplikacja korzysta z jollyday (fallback),
--    rok z rekordami → baza jest jedynym źródłem prawdy
--  * created_by_user_id / updated_by_user_id nullable + ON DELETE SET NULL
--    (zasada z V11), historię niosą created_by_label / updated_by_label
-- =============================================================
CREATE TABLE holiday(
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    holiday_date DATE NOT NULL,
    name VARCHAR(100) NOT NULL,
    custom TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    created_by_user_id BIGINT NULL,
    created_by_label VARCHAR(255) NOT NULL,
    updated_at DATETIME,
    updated_by_user_id BIGINT,
    updated_by_label VARCHAR(255) NULL,
    CONSTRAINT fk_holiday_created_by FOREIGN KEY (created_by_user_id) REFERENCES app_user (id) ON DELETE SET NULL,
    CONSTRAINT fk_holiday_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES app_user (id) ON DELETE SET NULL,
    CONSTRAINT uq_holiday_date UNIQUE (holiday_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;