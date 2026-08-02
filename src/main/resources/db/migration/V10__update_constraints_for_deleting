-- =============================================================
-- Flyway V10 – kaskadowe usuwanie sklepu (store) i pracownika (employee)
--
-- Zasady:
--  * usunięcie store  -> kasuje wszystkie powiązane dane (pracownicy,
--    grafiki, urlopy, propozycje, dostawy, godziny otwarcia itd.)
--  * usunięcie employee -> kasuje wszystkie dane powiązane z pracownikiem
--    (potrzebne też dla samodzielnego usuwania pracownika, niezależnie
--    od usuwania sklepu)
--  * app_user NIGDY nie jest kasowany kaskadowo — usunięcie store
--    tylko odpina app_user.store_id (ustawia NULL)
--  * pola "referencyjne/informacyjne", które są nullable i nie są
--    danymi "należącymi" do pracownika (schedule_message.employee_id,
--    store_delivery.primary_employee_id) dostają ON DELETE SET NULL,
--    żeby nie kasować np. całej konfiguracji dostaw sklepu tylko dlatego,
--    że zwolnił się jeden pracownik
--
-- UWAGA: DROP FOREIGN KEY i ADD CONSTRAINT o tej samej nazwie muszą być
-- w OSOBNYCH instrukcjach ALTER TABLE — połączenie ich w jednej instrukcji
-- powoduje błąd InnoDB errno 121 "Duplicate key on write or update"
-- (potwierdzone na MySQL 8 / MariaDB 10.11).
-- =============================================================

-- -------------------------------------------------------------
-- Tabele zależne bezpośrednio od store
-- -------------------------------------------------------------

ALTER TABLE employee DROP FOREIGN KEY fk_employee_store;
ALTER TABLE employee ADD CONSTRAINT fk_employee_store FOREIGN KEY (store_id) REFERENCES store (id) ON DELETE CASCADE;

ALTER TABLE app_user DROP FOREIGN KEY fk_app_user_store;
ALTER TABLE app_user ADD CONSTRAINT fk_app_user_store FOREIGN KEY (store_id) REFERENCES store (id) ON DELETE SET NULL;

ALTER TABLE demand_draft DROP FOREIGN KEY fk_demand_draft_store;
ALTER TABLE demand_draft ADD CONSTRAINT fk_demand_draft_store FOREIGN KEY (store_id) REFERENCES store (id) ON DELETE CASCADE;

ALTER TABLE employee_vacation DROP FOREIGN KEY fk_emp_vacation_store;
ALTER TABLE employee_vacation ADD CONSTRAINT fk_emp_vacation_store FOREIGN KEY (store_id) REFERENCES store (id) ON DELETE CASCADE;

ALTER TABLE employee_proposal_days_off DROP FOREIGN KEY fk_emp_days_off_store;
ALTER TABLE employee_proposal_days_off ADD CONSTRAINT fk_emp_days_off_store FOREIGN KEY (store_id) REFERENCES store (id) ON DELETE CASCADE;

ALTER TABLE employee_proposal_shifts DROP FOREIGN KEY fk_emp_proposal_shifts_store;
ALTER TABLE employee_proposal_shifts ADD CONSTRAINT fk_emp_proposal_shifts_store FOREIGN KEY (store_id) REFERENCES store (id) ON DELETE CASCADE;

ALTER TABLE employee_delegation DROP FOREIGN KEY fk_emp_delegation_store;
ALTER TABLE employee_delegation ADD CONSTRAINT fk_emp_delegation_store FOREIGN KEY (store_id) REFERENCES store (id) ON DELETE CASCADE;

ALTER TABLE schedule DROP FOREIGN KEY fk_schedule_store;
ALTER TABLE schedule ADD CONSTRAINT fk_schedule_store FOREIGN KEY (store_id) REFERENCES store (id) ON DELETE CASCADE;

ALTER TABLE period_hours_correction DROP FOREIGN KEY fk_period_hours_store;
ALTER TABLE period_hours_correction ADD CONSTRAINT fk_period_hours_store FOREIGN KEY (store_id) REFERENCES store (id) ON DELETE CASCADE;

ALTER TABLE store_details DROP FOREIGN KEY fk_store_details_store;
ALTER TABLE store_details ADD CONSTRAINT fk_store_details_store FOREIGN KEY (store_id) REFERENCES store (id) ON DELETE CASCADE;

ALTER TABLE store_delivery DROP FOREIGN KEY fk_store_delivery_store;
ALTER TABLE store_delivery ADD CONSTRAINT fk_store_delivery_store FOREIGN KEY (store_id) REFERENCES store (id) ON DELETE CASCADE;

ALTER TABLE store_opening_hours DROP FOREIGN KEY fk_store_opening_hours_store;
ALTER TABLE store_opening_hours ADD CONSTRAINT fk_store_opening_hours_store FOREIGN KEY (store_id) REFERENCES store (id) ON DELETE CASCADE;

ALTER TABLE shift_hour_modification_config DROP FOREIGN KEY fk_shift_hour_mod_config_store;
ALTER TABLE shift_hour_modification_config ADD CONSTRAINT fk_shift_hour_mod_config_store FOREIGN KEY (store_id) REFERENCES store (id) ON DELETE CASCADE;

ALTER TABLE employee_monthly_hours_confirmation DROP FOREIGN KEY fk_emhc_store;
ALTER TABLE employee_monthly_hours_confirmation ADD CONSTRAINT fk_emhc_store FOREIGN KEY (store_id) REFERENCES store (id) ON DELETE CASCADE;

-- -------------------------------------------------------------
-- Tabele zależne od employee
-- (potrzebne również przy DELETE /stores/{id}/employees/{id})
-- -------------------------------------------------------------

ALTER TABLE schedule_details DROP FOREIGN KEY fk_schedule_details_employee;
ALTER TABLE schedule_details ADD CONSTRAINT fk_schedule_details_employee FOREIGN KEY (employee_id) REFERENCES employee (id) ON DELETE CASCADE;

ALTER TABLE schedule_message DROP FOREIGN KEY fk_schedule_message_employee;
ALTER TABLE schedule_message ADD CONSTRAINT fk_schedule_message_employee FOREIGN KEY (employee_id) REFERENCES employee (id) ON DELETE SET NULL;

ALTER TABLE employee_vacation DROP FOREIGN KEY fk_emp_vacation_employee;
ALTER TABLE employee_vacation ADD CONSTRAINT fk_emp_vacation_employee FOREIGN KEY (employee_id) REFERENCES employee (id) ON DELETE CASCADE;

ALTER TABLE employee_proposal_days_off DROP FOREIGN KEY fk_emp_days_off_employee;
ALTER TABLE employee_proposal_days_off ADD CONSTRAINT fk_emp_days_off_employee FOREIGN KEY (employee_id) REFERENCES employee (id) ON DELETE CASCADE;

ALTER TABLE employee_proposal_shifts DROP FOREIGN KEY fk_emp_proposal_shifts_employee;
ALTER TABLE employee_proposal_shifts ADD CONSTRAINT fk_emp_proposal_shifts_employee FOREIGN KEY (employee_id) REFERENCES employee (id) ON DELETE CASCADE;

ALTER TABLE employee_delegation DROP FOREIGN KEY fk_emp_delegation_employee;
ALTER TABLE employee_delegation ADD CONSTRAINT fk_emp_delegation_employee FOREIGN KEY (employee_id) REFERENCES employee (id) ON DELETE CASCADE;

ALTER TABLE period_hours_correction DROP FOREIGN KEY fk_period_hours_employee;
ALTER TABLE period_hours_correction ADD CONSTRAINT fk_period_hours_employee FOREIGN KEY (employee_id) REFERENCES employee (id) ON DELETE CASCADE;

ALTER TABLE store_delivery DROP FOREIGN KEY fk_store_delivery_employee;
ALTER TABLE store_delivery ADD CONSTRAINT fk_store_delivery_employee FOREIGN KEY (primary_employee_id) REFERENCES employee (id) ON DELETE SET NULL;

ALTER TABLE shift_hour_modification_excluded_employees DROP FOREIGN KEY fk_shm_excl_employee;
ALTER TABLE shift_hour_modification_excluded_employees ADD CONSTRAINT fk_shm_excl_employee FOREIGN KEY (employee_id) REFERENCES employee (id) ON DELETE CASCADE;

ALTER TABLE employee_monthly_hours_confirmation DROP FOREIGN KEY fk_emhc_employee;
ALTER TABLE employee_monthly_hours_confirmation ADD CONSTRAINT fk_emhc_employee FOREIGN KEY (employee_id) REFERENCES employee (id) ON DELETE CASCADE;

-- -------------------------------------------------------------
-- Tabele zależne od shift_hour_modification_config
-- (samo config kasuje się kaskadowo ze store, więc jego dzieci
-- też muszą się kaskadowo skasować)
-- -------------------------------------------------------------

ALTER TABLE shift_hour_mapping DROP FOREIGN KEY fk_shift_hour_mapping_config;
ALTER TABLE shift_hour_mapping ADD CONSTRAINT fk_shift_hour_mapping_config FOREIGN KEY (config_id) REFERENCES shift_hour_modification_config (id) ON DELETE CASCADE;

ALTER TABLE shift_hour_modification_excluded_employees DROP FOREIGN KEY fk_shm_excl_config;
ALTER TABLE shift_hour_modification_excluded_employees ADD CONSTRAINT fk_shm_excl_config FOREIGN KEY (config_id) REFERENCES shift_hour_modification_config (id) ON DELETE CASCADE;