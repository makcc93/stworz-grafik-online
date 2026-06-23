-- =============================================================
-- Flyway V1 – StworzGrafik – tworzenie wszystkich tabel (MySQL)
-- Kolejność: od tabel bez FK → do tabel zależnych
-- =============================================================

-- -------------------------------------------------------------
-- 1. region
-- -------------------------------------------------------------
CREATE TABLE region (
    id     BIGINT       NOT NULL AUTO_INCREMENT,
    name   VARCHAR(255),
    enable TINYINT(1)   NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------------
-- 2. branch
--    FK → region
-- -------------------------------------------------------------
CREATE TABLE branch (
    id        BIGINT       NOT NULL AUTO_INCREMENT,
    name      VARCHAR(255),
    enable    TINYINT(1)   NOT NULL DEFAULT 1,
    region_id BIGINT       NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_branch_region FOREIGN KEY (region_id) REFERENCES region (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------------
-- 3. store
--    FK → branch
-- -------------------------------------------------------------
CREATE TABLE store (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    store_code       VARCHAR(255),
    name             VARCHAR(255),
    location         VARCHAR(255),
    branch_id        BIGINT,
    enable           TINYINT(1)   NOT NULL DEFAULT 1,
    store_manager_id BIGINT,
    created_at       DATETIME     NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_store_branch FOREIGN KEY (branch_id) REFERENCES branch (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------------
-- 4. positions
-- -------------------------------------------------------------
CREATE TABLE positions (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------------
-- 5. special_work_norm
-- -------------------------------------------------------------
CREATE TABLE special_work_norm (
    id               BIGINT         NOT NULL AUTO_INCREMENT,
    name             VARCHAR(255)   NOT NULL UNIQUE,
    max_daily_hours  DECIMAL(4, 2)  NOT NULL,
    weekly_norm      DECIMAL(4, 2)  NOT NULL,
    description      VARCHAR(255),
    active           TINYINT(1),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------------
-- 6. employee
--    FK → store, positions, special_work_norm
-- -------------------------------------------------------------
CREATE TABLE employee (
    id                   BIGINT       NOT NULL AUTO_INCREMENT,
    first_name           VARCHAR(255),
    last_name            VARCHAR(255),
    sap                  BIGINT,
    store_id             BIGINT       NOT NULL,
    positions_id          BIGINT       NOT NULL,
    enable               TINYINT(1)   NOT NULL DEFAULT 1,
    can_operate_checkout TINYINT(1)   NOT NULL DEFAULT 0,
    can_operate_credit   TINYINT(1)   NOT NULL DEFAULT 0,
    can_open_close_store TINYINT(1)   NOT NULL DEFAULT 0,
    can_operate_delivery TINYINT(1)   NOT NULL DEFAULT 0,
    seller               TINYINT(1)   NOT NULL DEFAULT 0,
    manager              TINYINT(1)   NOT NULL DEFAULT 0,
    cashier              TINYINT(1)   NOT NULL DEFAULT 0,
    warehouseman         TINYINT(1)   NOT NULL DEFAULT 0,
    pok                  TINYINT(1)   NOT NULL DEFAULT 0,
    is_special           TINYINT(1),
    special_work_norm_id BIGINT,
    etat_numerator       INT,
    etat_denominator     INT,
    created_at           DATETIME     NOT NULL,
    updated_at           DATETIME,
    PRIMARY KEY (id),
    CONSTRAINT fk_employee_store            FOREIGN KEY (store_id)             REFERENCES store (id),
    CONSTRAINT fk_employee_positions         FOREIGN KEY (positions_id)          REFERENCES positions (id),
    CONSTRAINT fk_employee_special_work_norm FOREIGN KEY (special_work_norm_id) REFERENCES special_work_norm (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------------
-- 7. app_user
--    FK → store, branch, region
-- -------------------------------------------------------------
CREATE TABLE app_user (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    login          VARCHAR(255) NOT NULL UNIQUE,
    password       VARCHAR(255) NOT NULL,
    role           VARCHAR(50)  NOT NULL,  -- STORE_MANAGER | DIRECTOR | ADMIN
    store_id       BIGINT,
    branch_id      BIGINT,
    region_id      BIGINT,
    director_scope VARCHAR(50),            -- BRANCH | REGION | NETWORK
    enabled        TINYINT(1),
    created_at     DATETIME,
    updated_at     DATETIME,
    PRIMARY KEY (id),
    CONSTRAINT fk_app_user_store  FOREIGN KEY (store_id)  REFERENCES store (id),
    CONSTRAINT fk_app_user_branch FOREIGN KEY (branch_id) REFERENCES branch (id),
    CONSTRAINT fk_app_user_region FOREIGN KEY (region_id) REFERENCES region (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------------
-- 8. billing_period_config
-- -------------------------------------------------------------
CREATE TABLE billing_period_config (
    id               BIGINT NOT NULL AUTO_INCREMENT,
    start_month      INT    NOT NULL UNIQUE,
    duration_months  INT    NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------------
-- 9. shift
-- -------------------------------------------------------------
CREATE TABLE shift (
    id         BIGINT NOT NULL AUTO_INCREMENT,
    start_hour TIME   NOT NULL,
    end_hour   TIME   NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------------
-- 10. shift_type_config
-- -------------------------------------------------------------
CREATE TABLE shift_type_config (
    id             BIGINT         NOT NULL AUTO_INCREMENT,
    code           VARCHAR(50),   -- WORK | WORK_BY_PROPOSAL | VACATION | DAY_OFF | SICK_LEAVE
    name_pl        VARCHAR(255),
    default_hours  DECIMAL(5, 2),
    counts_as_work TINYINT(1),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------------
-- 11. demand_draft
--    FK → store, FK → app_user (created_by / updated_by)
--    UNIQUE (store_id, draft_date)
-- -------------------------------------------------------------
CREATE TABLE demand_draft (
    id                  BIGINT NOT NULL AUTO_INCREMENT,
    store_id            BIGINT NOT NULL,
    draft_date          DATE,
    hourly_demand       TEXT,            -- JSON z int[] przez IntArrayJsonConverter
    created_at          DATETIME NOT NULL,
    updated_at          DATETIME,
    created_by_user_id  BIGINT NOT NULL,
    created_by_label    VARCHAR(255) NOT NULL,
    updated_by_user_id  BIGINT,
    updated_by_label    VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT uk_store_draft_date UNIQUE (store_id, draft_date),
    CONSTRAINT fk_demand_draft_store FOREIGN KEY (store_id) REFERENCES store (id),
    CONSTRAINT fk_demand_draft_created_by FOREIGN KEY (created_by_user_id) REFERENCES app_user (id),
    CONSTRAINT fk_demand_draft_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES app_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------------
-- 12. employee_vacation
--    FK → store, employee, app_user
-- -------------------------------------------------------------
CREATE TABLE employee_vacation (
    id               BIGINT NOT NULL AUTO_INCREMENT,
    store_id         BIGINT NOT NULL,
    employee_id      BIGINT NOT NULL,
    year_number      INT,
    month_number     INT,
    monthly_vacation TEXT,
    created_at       DATETIME,
    updated_at       DATETIME,
    created_by_user_id  BIGINT NOT NULL,
    created_by_label    VARCHAR(255) NOT NULL,
    updated_by_user_id  BIGINT,
    updated_by_label    VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT fk_emp_vacation_store    FOREIGN KEY (store_id)    REFERENCES store (id),
    CONSTRAINT fk_emp_vacation_employee FOREIGN KEY (employee_id) REFERENCES employee (id),
    CONSTRAINT fk_emp_vacation_created_by FOREIGN KEY (created_by_user_id) REFERENCES app_user (id),
    CONSTRAINT fk_emp_vacation_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES app_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------------
-- 13. employee_proposal_days_off
--    FK → store, employee, app_user
-- -------------------------------------------------------------
CREATE TABLE employee_proposal_days_off (
    id              BIGINT NOT NULL AUTO_INCREMENT,
    store_id        BIGINT NOT NULL,
    employee_id     BIGINT NOT NULL,
    year_number     INT,
    month_number    INT,
    monthly_days_off TEXT,
    created_at      DATETIME,
    updated_at      DATETIME,
    created_by_user_id  BIGINT NOT NULL,
    created_by_label    VARCHAR(255) NOT NULL,
    updated_by_user_id  BIGINT,
    updated_by_label    VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT fk_emp_days_off_store    FOREIGN KEY (store_id)    REFERENCES store (id),
    CONSTRAINT fk_emp_days_off_employee FOREIGN KEY (employee_id) REFERENCES employee (id),
    CONSTRAINT fk_emp_days_off_created_by FOREIGN KEY (created_by_user_id) REFERENCES app_user (id),
    CONSTRAINT fk_emp_days_off_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES app_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------------
-- 14. employee_proposal_shifts
--    FK → store, employee, app_user
-- -------------------------------------------------------------
CREATE TABLE employee_proposal_shifts (
    id                   BIGINT NOT NULL AUTO_INCREMENT,
    store_id             BIGINT NOT NULL,
    employee_id          BIGINT NOT NULL,
    date                 DATE   NOT NULL,
    daily_proposal_shift TEXT,
    created_at           DATETIME,
    updated_at           DATETIME,
    created_by_user_id  BIGINT NOT NULL,
    created_by_label    VARCHAR(255) NOT NULL,
    updated_by_user_id  BIGINT,
    updated_by_label    VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT fk_emp_proposal_shifts_store    FOREIGN KEY (store_id)    REFERENCES store (id),
    CONSTRAINT fk_emp_proposal_shifts_employee FOREIGN KEY (employee_id) REFERENCES employee (id),
    CONSTRAINT fk_emp_proposal_shifts_created_by FOREIGN KEY (created_by_user_id) REFERENCES app_user (id),
    CONSTRAINT fk_emp_proposal_shifts_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES app_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------------
-- 15. employee_delegation
--    FK → store, employee, app_user
-- -------------------------------------------------------------
CREATE TABLE employee_delegation (
    id                  BIGINT NOT NULL AUTO_INCREMENT,
    store_id            BIGINT NOT NULL,
    employee_id         BIGINT NOT NULL,
    year_number         INT,
    month_number        INT,
    monthly_delegation  TEXT,
    created_at          DATETIME,
    updated_at          DATETIME,
    created_by_user_id  BIGINT NOT NULL,
    created_by_label    VARCHAR(255) NOT NULL,
    updated_by_user_id  BIGINT,
    updated_by_label    VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT fk_emp_delegation_store    FOREIGN KEY (store_id)    REFERENCES store (id),
    CONSTRAINT fk_emp_delegation_employee FOREIGN KEY (employee_id) REFERENCES employee (id),
    CONSTRAINT fk_emp_delegation_created_by FOREIGN KEY (created_by_user_id) REFERENCES app_user (id),
    CONSTRAINT fk_emp_delegation_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES app_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------------
-- 16. schedule
--    FK → store, app_user
-- -------------------------------------------------------------
CREATE TABLE schedule (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    store_id          BIGINT       NOT NULL,
    year_number       INT,
    month_number      INT,
    name              VARCHAR(255),
    schedule_status   VARCHAR(50),  -- DONE | IN_PROGRESS | DELETED | FAILED | ARCHIVED
    created_at        DATETIME,
    created_by_user_id BIGINT,
    created_by_label    VARCHAR(255) NOT NULL,
    updated_at        DATETIME,
    updated_by_user_id BIGINT,
    updated_by_label    VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT fk_schedule_store FOREIGN KEY (store_id) REFERENCES store (id),
    CONSTRAINT fk_schedule_created_by FOREIGN KEY (created_by_user_id) REFERENCES app_user (id),
    CONSTRAINT fk_schedule_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES app_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------------
-- 17. schedule_message
--    FK → schedule, employee
-- -------------------------------------------------------------
CREATE TABLE schedule_message (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    schedule_id    BIGINT        NOT NULL,
    message_type   VARCHAR(50)   NOT NULL,  -- INFO | WARNING | ERROR
    message_code   VARCHAR(100)  NOT NULL,  -- enum ScheduleMessageCode
    message_text   TEXT,
    employee_id    BIGINT,
    message_date   DATE,
    created_at     DATETIME,
    PRIMARY KEY (id),
    CONSTRAINT fk_schedule_message_schedule FOREIGN KEY (schedule_id) REFERENCES schedule (id) ON DELETE CASCADE,
    CONSTRAINT fk_schedule_message_employee FOREIGN KEY (employee_id) REFERENCES employee (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------------
-- 18. schedule_details
--    FK → schedule, employee, shift, shift_type_config
-- -------------------------------------------------------------
CREATE TABLE schedule_details (
    id                  BIGINT NOT NULL AUTO_INCREMENT,
    schedule_id         BIGINT NOT NULL,
    employee_id         BIGINT NOT NULL,
    date                DATE,
    shift_id            BIGINT NOT NULL,
    shift_type_config_id BIGINT NOT NULL,
    created_at          DATETIME,
    updated_at          DATETIME,
    PRIMARY KEY (id),
    CONSTRAINT uq_schedule_employee_date UNIQUE (schedule_id, employee_id, date),
    CONSTRAINT fk_schedule_details_schedule          FOREIGN KEY (schedule_id)         REFERENCES schedule (id) ON DELETE CASCADE,
    CONSTRAINT fk_schedule_details_employee          FOREIGN KEY (employee_id)         REFERENCES employee (id),
    CONSTRAINT fk_schedule_details_shift             FOREIGN KEY (shift_id)            REFERENCES shift (id),
    CONSTRAINT fk_schedule_details_shift_type_config FOREIGN KEY (shift_type_config_id) REFERENCES shift_type_config (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------------
-- 19. period_hours_correction
--    FK → store, employee
-- -------------------------------------------------------------
CREATE TABLE period_hours_correction (
    id              BIGINT         NOT NULL AUTO_INCREMENT,
    store_id        BIGINT         NOT NULL,
    employee_id     BIGINT         NOT NULL,
    year            INT            NOT NULL,
    month           INT            NOT NULL,
    corrected_hours DECIMAL(6, 2)  NOT NULL,
    created_at      DATETIME,
    updated_at      DATETIME,
    PRIMARY KEY (id),
    CONSTRAINT fk_period_hours_store    FOREIGN KEY (store_id)    REFERENCES store (id),
    CONSTRAINT fk_period_hours_employee FOREIGN KEY (employee_id) REFERENCES employee (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------------
-- 20. store_details  (OneToOne → store)
--    Embeddable: OptimalStaffing → kolumny inline
-- -------------------------------------------------------------
CREATE TABLE store_details (
    id                    BIGINT NOT NULL AUTO_INCREMENT,
    store_id              BIGINT NOT NULL UNIQUE,
    optimal_store_managers INT,
    optimal_sales_managers INT,
    optimal_sellers        INT,
    optimal_cashiers       INT,
    optimal_storemen       INT,
    optimal_pok            INT,
    created_at             DATETIME,
    created_by_user_id     BIGINT,
    updated_at             DATETIME,
    updated_by_user_id     BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT fk_store_details_store FOREIGN KEY (store_id) REFERENCES store (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------------
-- 21. store_delivery  (OneToOne → store)
--    Embeddable: StoreWeeklyDeliverySchedule → JSON TEXT
--    FK → employee (primary_employee_id)
-- -------------------------------------------------------------
CREATE TABLE store_delivery (
    id                         BIGINT      NOT NULL AUTO_INCREMENT,
    store_id                   BIGINT      NOT NULL UNIQUE,
    has_dedicated_warehouseman TINYINT(1),
    primary_employee_id        BIGINT,
    weekly_schedule            TEXT,        -- JSON: Map<DayOfWeek, DayDeliveryConfig>
    created_at                 DATETIME,
    created_by_user_id         BIGINT,
    updated_at                 DATETIME,
    updated_by_user_id         BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT fk_store_delivery_store    FOREIGN KEY (store_id)          REFERENCES store (id),
    CONSTRAINT fk_store_delivery_employee FOREIGN KEY (primary_employee_id) REFERENCES employee (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------------
-- 22. store_opening_hours
--    FK → store
-- -------------------------------------------------------------
CREATE TABLE store_opening_hours (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    store_id    BIGINT      NOT NULL,
    day_of_week VARCHAR(20) NOT NULL,  -- enum DayOfWeek jako STRING
    open_time   TIME        NOT NULL,
    close_time  TIME        NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_store_opening_hours_store FOREIGN KEY (store_id) REFERENCES store (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------------
-- 23. shift_hour_modification_config  (OneToOne → store)
-- -------------------------------------------------------------
CREATE TABLE shift_hour_modification_config (
    id       BIGINT NOT NULL AUTO_INCREMENT,
    store_id BIGINT NOT NULL UNIQUE,
    PRIMARY KEY (id),
    CONSTRAINT fk_shift_hour_mod_config_store FOREIGN KEY (store_id) REFERENCES store (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------------
-- 24. shift_hour_mapping
--    @ElementCollection dla Map<LocalTime, LocalTime>
--    FK → shift_hour_modification_config
-- -------------------------------------------------------------
CREATE TABLE shift_hour_mapping (
    config_id     BIGINT NOT NULL,
    original_hour TIME   NOT NULL,
    modified_hour TIME   NOT NULL,
    PRIMARY KEY (config_id, original_hour),
    CONSTRAINT fk_shift_hour_mapping_config FOREIGN KEY (config_id) REFERENCES shift_hour_modification_config (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------------
-- 25. shift_hour_modification_excluded_employees
--    @ManyToMany: ShiftHourModificationConfig ↔ Employee
-- -------------------------------------------------------------
CREATE TABLE shift_hour_modification_excluded_employees (
    config_id   BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    PRIMARY KEY (config_id, employee_id),
    CONSTRAINT fk_shm_excl_config   FOREIGN KEY (config_id)   REFERENCES shift_hour_modification_config (id),
    CONSTRAINT fk_shm_excl_employee FOREIGN KEY (employee_id) REFERENCES employee (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;