create sequence if not exists association_value_entry_seq start with 1 increment by 50;
create sequence if not exists domain_event_entry_seq start with 1 increment by 50;

create table if not exists association_value_entry
(
  id                int8         not null,
  association_key   varchar(255) not null,
  association_value varchar(255),
  saga_id           varchar(255) not null,
  saga_type         varchar(255),
  primary key (id)
);

create table if not exists saga_entry
(
  saga_id         varchar(255) not null,
  revision        varchar(255),
  saga_type       varchar(255),
  serialized_saga oid,
  primary key (saga_id)
);

create table if not exists token_entry
(
  processor_name varchar(255) not null,
  segment        int4         not null,
  owner          varchar(255),
  timestamp      varchar(255) not null,
  token          oid,
  token_type     varchar(255),
  primary key (processor_name, segment)
);

CREATE TABLE IF NOT EXISTS dead_letter_entry
(
  dead_letter_id       VARCHAR(255) NOT NULL,
  cause_message        VARCHAR(255),
  cause_type           VARCHAR(255),
  diagnostics          oid,
  enqueued_at          TIMESTAMP    NOT NULL,
  last_touched         TIMESTAMP,
  aggregate_identifier VARCHAR(255),
  event_identifier     VARCHAR(255) NOT NULL,
  message_type         VARCHAR(255) NOT NULL,
  meta_data            oid,
  payload              oid          NOT NULL,
  payload_revision     VARCHAR(255),
  payload_type         VARCHAR(255) NOT NULL,
  sequence_number      INT8,
  time_stamp           VARCHAR(255) NOT NULL,
  token                oid,
  token_type           VARCHAR(255),
  type                 VARCHAR(255),
  processing_group     VARCHAR(255) NOT NULL,
  processing_started   TIMESTAMP,
  sequence_identifier  VARCHAR(255) NOT NULL,
  sequence_index       INT8         NOT NULL,
  PRIMARY KEY (dead_letter_id)
);

create table if not exists domain_event_entry
(
  global_index         INT8         not null,
  event_identifier     varchar(255) not null,
  meta_data            oid,
  payload              oid          not null,
  payload_revision     varchar(255),
  payload_type         varchar(255) not null,
  time_stamp           varchar(255) not null,
  aggregate_identifier varchar(255) not null,
  sequence_number      INT8         not null,
  type                 varchar(255),
  primary key (global_index)
);

create table if not exists snapshot_event_entry
(
  aggregate_identifier varchar(255) not null,
  sequence_number      bigint       not null,
  type                 varchar(255) not null,
  event_identifier     varchar(255) not null,
  meta_data            oid,
  payload              oid          not null,
  payload_revision     varchar(255),
  payload_type         varchar(255) not null,
  time_stamp           varchar(255) not null,
  primary key (aggregate_identifier, sequence_number, type)
);

create index if not exists IDX_association_value_entry_stakav on association_value_entry (saga_type, association_key, association_value);
create index if not exists IDX_association_value_entry_sist on association_value_entry (saga_id, saga_type);
