-- =============================================================
-- Flyway V15 – szablony zapotrzebowania (draftu) per sklep / miesiąc / dzień tygodnia
--
--  * jeden rekord na (sklep, rok, miesiąc, dzień tygodnia) – UNIQUE
--  * szablon jest ZAPISANYM stanem, a nie średnią z draftów: ręczna zmiana
--    pojedynczego dnia (demand_draft) nie zmienia szablonu
--  * store_id ON DELETE CASCADE (tak jak demand_draft w V10)
--  * created_by_user_id / updated_by_user_id nullable + ON DELETE SET NULL
--    (ta sama zasada co w V11 i V13); historię niesie created_by_label / updated_by_label
--  * day_of_week = nazwa java.time.DayOfWeek (MONDAY..SUNDAY)
--  * hourly_demand = JSON z int[24] (IntArrayJsonConverter, jak w demand_draft)
-- =============================================================
CREATE TABLE demand_template (
    id                  BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    store_id            BIGINT NOT NULL,
    year_number         INT NOT NULL,
    month_number        INT NOT NULL,
    day_of_week         VARCHAR(10) NOT NULL,
    hourly_demand       TEXT NOT NULL,
    created_at          DATETIME NOT NULL,
    created_by_user_id  BIGINT NULL,
    created_by_label    VARCHAR(255) NOT NULL,
    updated_at          DATETIME,
    updated_by_user_id  BIGINT NULL,
    updated_by_label    VARCHAR(255) NULL,
    CONSTRAINT fk_demand_template_store FOREIGN KEY (store_id) REFERENCES store (id) ON DELETE CASCADE,
    CONSTRAINT fk_demand_template_created_by FOREIGN KEY (created_by_user_id) REFERENCES app_user (id) ON DELETE SET NULL,
    CONSTRAINT fk_demand_template_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES app_user (id) ON DELETE SET NULL,
    CONSTRAINT uq_demand_template UNIQUE (store_id, year_number, month_number, day_of_week)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;