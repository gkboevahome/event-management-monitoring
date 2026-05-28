DROP TABLE IF EXISTS services;
CREATE TABLE IF NOT EXISTS services
(
  id          UUID                PRIMARY KEY DEFAULT gen_random_uuid(),
  name        VARCHAR(255)        NOT NULL,
  environment VARCHAR(255)        NOT NULL,
  owner       VARCHAR(255)        NOT NULL,
  version     BIGINT DEFAULT 0    NOT NULL
) ;

ALTER TABLE services
    ADD CONSTRAINT service_unq UNIQUE  (name, environment,owner) ;

DROP TABLE IF EXISTS events;
CREATE TABLE IF NOT EXISTS events
(
    id          UUID                                  PRIMARY KEY DEFAULT gen_random_uuid(),
    version     BIGINT                                DEFAULT 0 NOT NULL,
    type        VARCHAR(20)                           NOT NULL,
    message     JSONB                                  NOT NULL,
    service_id  UUID                                  NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP   NOT NULL
) ;

ALTER TABLE events
    ADD CONSTRAINT fk_entity_service FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE;

