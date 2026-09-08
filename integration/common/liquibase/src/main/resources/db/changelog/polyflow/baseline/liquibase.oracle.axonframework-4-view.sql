create sequence association_value_entry_seq start with 1 increment by 50;
create sequence domain_event_entry_seq start with 1 increment by 50;

create table association_value_entry
(
  id                number(19)         not null,
  association_key   varchar2(255) not null,
  association_value varchar2(255),
  saga_id           varchar2(255) not null,
  saga_type         varchar2(255),
  primary key (id)
);

create table saga_entry
(
  saga_id         varchar2(255) not null,
  revision        varchar2(255),
  saga_type       varchar2(255),
  serialized_saga blob,
  primary key (saga_id)
);

create table token_entry
(
  processor_name varchar2(255) not null,
  segment        number(10)         not null,
  owner          varchar2(255),
  timestamp      varchar2(255) not null,
  token          blob,
  token_type     varchar2(255),
  primary key (processor_name, segment)
);

CREATE TABLE dead_letter_entry
(
  dead_letter_id       varchar2(255) NOT NULL,
  cause_message        varchar2(255),
  cause_type           varchar2(255),
  diagnostics          blob,
  enqueued_at          timestamp    NOT NULL,
  last_touched         timestamp,
  aggregate_identifier varchar2(255),
  event_identifier     varchar2(255) NOT NULL,
  message_type         varchar2(255) NOT NULL,
  meta_data            blob,
  payload              blob          NOT NULL,
  payload_revision     varchar2(255),
  payload_type         varchar2(255) NOT NULL,
  sequence_number      number(19),
  time_stamp           varchar2(255) NOT NULL,
  token                blob,
  token_type           varchar2(255),
  type                 varchar2(255),
  processing_group     varchar2(255) NOT NULL,
  processing_started   timestamp,
  sequence_identifier  varchar2(255) NOT NULL,
  sequence_index       number(19)         NOT NULL,
  PRIMARY KEY (dead_letter_id)
);

create table domain_event_entry
(
  global_index         number(19)         not null,
  event_identifier     varchar2(255) not null,
  meta_data            blob,
  payload              blob          not null,
  payload_revision     varchar2(255),
  payload_type         varchar2(255) not null,
  time_stamp           varchar2(255) not null,
  aggregate_identifier varchar2(255) not null,
  sequence_number      number(19)         not null,
  type                 varchar2(255),
  primary key (global_index)
);

create table snapshot_event_entry
(
  aggregate_identifier varchar2(255) not null,
  sequence_number      bigint       not null,
  type                 varchar2(255) not null,
  event_identifier     varchar2(255) not null,
  meta_data            blob,
  payload              blob          not null,
  payload_revision     varchar2(255),
  payload_type         varchar2(255) not null,
  time_stamp           varchar2(255) not null,
  primary key (aggregate_identifier, sequence_number, type)
);

create index IDX_association_value_entry_stakav on association_value_entry (saga_type, association_key, association_value);
create index IDX_association_value_entry_sist on association_value_entry (saga_id, saga_type);
