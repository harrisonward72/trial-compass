-- table for user tracking of trials, allows them to track if interested/contacted/enrolled/etc

CREATE TABLE tracking_statuses (
        id          BIGINT          NOT NULL AUTO_INCREMENT,
        code        VARCHAR(50)     NOT NULL,
        label       VARCHAR(100)    NOT NULL,
        sort_order  INT             NOT NULL,
        created_at  DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
        updated_at  DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
        PRIMARY KEY (id),
        CONSTRAINT uq_tracking_tracking_statuses_code UNIQUE (code)
        )   ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


INSERT INTO tracking_statuses (code, label, sort_order) VALUES
        ('INTERESTED', 'Interested', 1),
        ('CONTACTED', 'Contacted', 2),
        ('SCREENING', 'Screening', 3),
        ('ENROLLED', 'Enrolled', 4),
        ('RULED_OUT', 'Ruled out', 5);

-- for saved trials; one user one trial

CREATE TABLE saved_studies (
        id                  BIGINT      NOT NULL AUTO_INCREMENT,
        user_id             BIGINT      NOT NULL,
        tracking_status_id  BIGINT      NOT NULL,
        nct_id              VARCHAR(20) NOT NULL,
        title               VARCHAR(500)  NOT NULL,
        -- trials recruitment status the when it was saved
        last_known_status   VARCHAR(50) NULL,
        follow_up_date      DATE        NULL,
        created_at          DATETIME(6) NOT NULL,
        updated_at          DATETIME(6) NOT NULL,
        PRIMARY KEY (id),
        -- user can only save a trial once
        CONSTRAINT uq_saved_studies_user_nct UNIQUE (user_id, nct_id),
        -- must have foreign keys that point to users or statuses
        -- CASCADE will delete saved trials if a user is deleted
        CONSTRAINT fk_saved_studies_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
        CONSTRAINT fk_saved_studies_tracking
        FOREIGN KEY (tracking_status_id) REFERENCES tracking_statuses(id),

        -- speeds up searching for upcoming follow ups for saved trials
        INDEX idx_saved_studies_follow_up (user_id, follow_up_date)
        )   ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;