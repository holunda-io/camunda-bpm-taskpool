CREATE SEQUENCE hibernate_sequence START WITH 1 INCREMENT BY 1;

CREATE TABLE association_value_entry
(
  id                BIGINT       NOT NULL,
  association_key   VARCHAR(255) NOT NULL,
  association_value VARCHAR(255),
  saga_id           VARCHAR(255) NOT NULL,
  saga_type         VARCHAR(255),
  PRIMARY KEY (id)
);

CREATE TABLE domain_event_entry
(
  global_index         BIGINT       NOT NULL,
  event_identifier     VARCHAR(255) NOT NULL,
  meta_data            BYTEA,
  payload              BYTEA        NOT NULL,
  payload_revision     VARCHAR(255),
  payload_type         VARCHAR(255) NOT NULL,
  time_stamp           VARCHAR(255) NOT NULL,
  aggregate_identifier VARCHAR(255) NOT NULL,
  sequence_number      BIGINT       NOT NULL,
  type                 VARCHAR(255),
  PRIMARY KEY (global_index)
);

CREATE TABLE saga_entry
(
  saga_id         VARCHAR(255) NOT NULL,
  revision        VARCHAR(255),
  saga_type       VARCHAR(255),
  serialized_saga BYTEA,
  PRIMARY KEY (saga_id)
);

CREATE TABLE snapshot_event_entry
(
  aggregate_identifier VARCHAR(255) NOT NULL,
  sequence_number      BIGINT       NOT NULL,
  type                 VARCHAR(255) NOT NULL,
  event_identifier     VARCHAR(255) NOT NULL,
  meta_data            BYTEA,
  payload              BYTEA        NOT NULL,
  payload_revision     VARCHAR(255),
  payload_type         VARCHAR(255) NOT NULL,
  time_stamp           VARCHAR(255) NOT NULL,
  PRIMARY KEY (aggregate_identifier, sequence_number, type)
);

CREATE TABLE token_entry
(
  processor_name VARCHAR(255) NOT NULL,
  segment        INTEGER      NOT NULL,
  owner          VARCHAR(255),
  timestamp      VARCHAR(255) NOT NULL,
  token          BYTEA,
  token_type     VARCHAR(255),
  PRIMARY KEY (processor_name, segment)
);


CREATE INDEX IDXk45eqnxkgd8hpdn6xixn8sgft ON association_value_entry (saga_type, association_key, association_value);
CREATE INDEX IDXgv5k1v2mh6frxuy5c0hgbau94 ON association_value_entry (saga_id, saga_type);

ALTER TABLE domain_event_entry
  ADD CONSTRAINT UK8s1f994p4la2ipb13me2xqm1w UNIQUE (aggregate_identifier, sequence_number);

ALTER TABLE domain_event_entry
  ADD CONSTRAINT UK_fwe6lsa8bfo6hyas6ud3m8c7x UNIQUE (event_identifier);

ALTER TABLE snapshot_event_entry
  ADD CONSTRAINT UK_e1uucjseo68gopmnd0vgdl44h UNIQUE (event_identifier);


