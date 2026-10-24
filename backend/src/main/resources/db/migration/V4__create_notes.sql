-- notes a user keeps about their saved trials

 CREATE TABLE notes(
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    saved_study_id      BIGINT          NOT NULL,
    body                VARCHAR(2000)   NOT NULL,
    created_at          DATETIME(6)     NOT NULL,
    updated_at          DATETIME(6)     NOT NULL,
    PRIMARY KEY (id),

    CONSTRAINT fk_notes_saved_study FOREIGN KEY (saved_study_id) REFERENCES saved_studies (id) ON DELETE CASCADE,

    INDEX idx_notes_saved_study (saved_study_id, created_at)
 )
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;