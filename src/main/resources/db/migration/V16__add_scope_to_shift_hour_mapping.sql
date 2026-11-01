-- =============================================================
-- Flyway V16 – zakres reguły przesunięcia godziny zmiany
--
--  * scope: START (tylko początek zmiany), END (tylko koniec zmiany), BOTH (oba)
--  * dotychczasowe reguły działały na początek i koniec, więc dostają BOTH –
--    zachowanie generowania grafiku się nie zmienia
--  * klucz główny rozszerzony o scope: ta sama godzina może mieć osobne reguły
--    dla początku i dla końca zmiany (np. 14:00 → 13:45 dla początku
--    i 14:00 → 14:15 dla końca)
--  * jeden ALTER TABLE, bo FK na config_id opiera się na indeksie klucza głównego
-- =============================================================
ALTER TABLE shift_hour_mapping
    ADD COLUMN scope VARCHAR(10) NOT NULL DEFAULT 'BOTH',
    DROP PRIMARY KEY,
    ADD PRIMARY KEY (config_id, original_hour, scope);