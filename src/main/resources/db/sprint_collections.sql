CREATE TABLE IF NOT EXISTS sprint_holidays (
    sprint_id BIGINT NOT NULL,
    date       DATE   NOT NULL,
    PRIMARY KEY (sprint_id, date),
    CONSTRAINT fk_sprint_holidays_sprint FOREIGN KEY (sprint_id) REFERENCES sprints (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS sprint_working_weekends (
    sprint_id BIGINT NOT NULL,
    date       DATE   NOT NULL,
    PRIMARY KEY (sprint_id, date),
    CONSTRAINT fk_sprint_working_weekends_sprint FOREIGN KEY (sprint_id) REFERENCES sprints (id) ON DELETE CASCADE
);
