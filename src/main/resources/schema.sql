DROP TABLE Person;
CREATE TABLE IF NOT EXISTS Person (
    oib VARCHAR(11) NOT NULL,
    name VARCHAR(255) NOT NULL,
    lastName VARCHAR(255) NOT NULL,
    status VARCHAR(10) NOT NULL,
    CONSTRAINT pk_person PRIMARY KEY (oib)
);