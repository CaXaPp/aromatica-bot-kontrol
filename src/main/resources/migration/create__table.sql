CREATE TABLE employee (
                          id BIGSERIAL PRIMARY KEY,
                          username VARCHAR(255) NOT NULL UNIQUE,
                          full_name VARCHAR(255),
                          late_count_under20_min INTEGER,
                          total_late_minutes INTEGER,
                          fine_sum INTEGER,
                          disqualified BOOLEAN,
                          last_arrival TIMESTAMP
);

CREATE TABLE arrival (
                         id BIGSERIAL PRIMARY KEY,
                         username VARCHAR(255),
                         arrival_time TIMESTAMP,
                         late_minutes INTEGER,
                         photo BYTEA
);
