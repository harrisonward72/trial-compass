-- V2: lookup table for the recruitment-status checkboxes.
-- "code" is the exact value ClinicalTrials.gov uses; "label" is what users see.
CREATE TABLE recruitment_statuses (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    code       VARCHAR(50)  NOT NULL,
    label      VARCHAR(100) NOT NULL,
    sort_order INT          NOT NULL,
    -- DEFAULT lets these rows be inserted by plain SQL, with no Java involved
    created_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uq_recruitment_statuses_code UNIQUE (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO recruitment_statuses (code, label, sort_order) VALUES
    ('RECRUITING',              'Recruiting',              1),
    ('NOT_YET_RECRUITING',      'Not yet recruiting',      2),
    ('ENROLLING_BY_INVITATION', 'Enrolling by invitation', 3),
    ('ACTIVE_NOT_RECRUITING',   'Active, not recruiting',  4),
    ('COMPLETED',               'Completed',               5),
    ('SUSPENDED',               'Suspended',               6),
    ('TERMINATED',              'Terminated',              7),
    ('WITHDRAWN',               'Withdrawn',               8),
    ('UNKNOWN',                 'Unknown status',          9);