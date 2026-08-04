-- =============================================================
-- Flyway V11 – bezpieczne usuwanie app_user
--
-- Zasady:
--  * app_user NIE jest "właścicielem" żadnych danych biznesowych —
--    jest tylko wskazany w polach audytowych (created_by_user_id /
--    updated_by_user_id) w demand_draft, employee_vacation,
--    employee_proposal_days_off, employee_proposal_shifts,
--    employee_delegation, schedule.
--  * Dlatego usunięcie użytkownika NIE MOŻE kasować tych rekordów
--    (skasowałoby realne grafiki/drafty/urlopy sklepu — dane, które
--    nie "należą" do konta, tylko do sklepu). Zamiast CASCADE,
--    stosujemy ON DELETE SET NULL — dokładnie ten sam wzorzec co
--    schedule_message.employee_id / store_delivery.primary_employee_id
--    w V10.
--  * created_by_label / updated_by_label (VARCHAR NOT NULL) zostają
--    bez zmian — to zdenormalizowana migawka nazwy użytkownika
--    z momentu utworzenia/edycji, więc historia pozostaje czytelna
--    nawet po usunięciu konta ("utworzone przez: Jan Kowalski").
--
-- UWAGA: DROP FOREIGN KEY i ADD CONSTRAINT o tej samej nazwie muszą
-- być w OSOBNYCH instrukcjach ALTER TABLE (patrz komentarz w V10).
-- =============================================================

-- -------------------------------------------------------------
-- demand_draft
-- -------------------------------------------------------------

ALTER TABLE demand_draft MODIFY COLUMN created_by_user_id BIGINT NULL;

ALTER TABLE demand_draft DROP FOREIGN KEY fk_demand_draft_created_by;
ALTER TABLE demand_draft ADD CONSTRAINT fk_demand_draft_created_by FOREIGN KEY (created_by_user_id) REFERENCES app_user (id) ON DELETE SET NULL;

ALTER TABLE demand_draft DROP FOREIGN KEY fk_demand_draft_updated_by;
ALTER TABLE demand_draft ADD CONSTRAINT fk_demand_draft_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES app_user (id) ON DELETE SET NULL;

-- -------------------------------------------------------------
-- employee_vacation
-- -------------------------------------------------------------

ALTER TABLE employee_vacation MODIFY COLUMN created_by_user_id BIGINT NULL;

ALTER TABLE employee_vacation DROP FOREIGN KEY fk_emp_vacation_created_by;
ALTER TABLE employee_vacation ADD CONSTRAINT fk_emp_vacation_created_by FOREIGN KEY (created_by_user_id) REFERENCES app_user (id) ON DELETE SET NULL;

ALTER TABLE employee_vacation DROP FOREIGN KEY fk_emp_vacation_updated_by;
ALTER TABLE employee_vacation ADD CONSTRAINT fk_emp_vacation_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES app_user (id) ON DELETE SET NULL;

-- -------------------------------------------------------------
-- employee_proposal_days_off
-- -------------------------------------------------------------

ALTER TABLE employee_proposal_days_off MODIFY COLUMN created_by_user_id BIGINT NULL;

ALTER TABLE employee_proposal_days_off DROP FOREIGN KEY fk_emp_days_off_created_by;
ALTER TABLE employee_proposal_days_off ADD CONSTRAINT fk_emp_days_off_created_by FOREIGN KEY (created_by_user_id) REFERENCES app_user (id) ON DELETE SET NULL;

ALTER TABLE employee_proposal_days_off DROP FOREIGN KEY fk_emp_days_off_updated_by;
ALTER TABLE employee_proposal_days_off ADD CONSTRAINT fk_emp_days_off_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES app_user (id) ON DELETE SET NULL;

-- -------------------------------------------------------------
-- employee_proposal_shifts
-- -------------------------------------------------------------

ALTER TABLE employee_proposal_shifts MODIFY COLUMN created_by_user_id BIGINT NULL;

ALTER TABLE employee_proposal_shifts DROP FOREIGN KEY fk_emp_proposal_shifts_created_by;
ALTER TABLE employee_proposal_shifts ADD CONSTRAINT fk_emp_proposal_shifts_created_by FOREIGN KEY (created_by_user_id) REFERENCES app_user (id) ON DELETE SET NULL;

ALTER TABLE employee_proposal_shifts DROP FOREIGN KEY fk_emp_proposal_shifts_updated_by;
ALTER TABLE employee_proposal_shifts ADD CONSTRAINT fk_emp_proposal_shifts_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES app_user (id) ON DELETE SET NULL;

-- -------------------------------------------------------------
-- employee_delegation
-- -------------------------------------------------------------

ALTER TABLE employee_delegation MODIFY COLUMN created_by_user_id BIGINT NULL;

ALTER TABLE employee_delegation DROP FOREIGN KEY fk_emp_delegation_created_by;
ALTER TABLE employee_delegation ADD CONSTRAINT fk_emp_delegation_created_by FOREIGN KEY (created_by_user_id) REFERENCES app_user (id) ON DELETE SET NULL;

ALTER TABLE employee_delegation DROP FOREIGN KEY fk_emp_delegation_updated_by;
ALTER TABLE employee_delegation ADD CONSTRAINT fk_emp_delegation_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES app_user (id) ON DELETE SET NULL;

-- -------------------------------------------------------------
-- schedule
-- (created_by_user_id już jest nullable — bez MODIFY COLUMN)
-- -------------------------------------------------------------

ALTER TABLE schedule DROP FOREIGN KEY fk_schedule_created_by;
ALTER TABLE schedule ADD CONSTRAINT fk_schedule_created_by FOREIGN KEY (created_by_user_id) REFERENCES app_user (id) ON DELETE SET NULL;

ALTER TABLE schedule DROP FOREIGN KEY fk_schedule_updated_by;
ALTER TABLE schedule ADD CONSTRAINT fk_schedule_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES app_user (id) ON DELETE SET NULL;