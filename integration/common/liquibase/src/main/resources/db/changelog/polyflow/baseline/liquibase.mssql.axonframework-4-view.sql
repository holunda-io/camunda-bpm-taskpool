IF OBJECT_ID(N'association_value_entry', N'U') IS NOT NULL
  RETURN;

create sequence association_value_entry_seq start with 1 increment by 50;
create sequence domain_event_entry_seq start with 1 increment by 50;

create table association_value_entry
(
  id                bigint         not null,
  association_key   nvarchar(255) not null,
  association_value nvarchar(255),
  saga_id           nvarchar(255) not null,
  saga_type         nvarchar(255),
  primary key (id)
);

create table saga_entry
(
  saga_id         nvarchar(255) not null,
  revision        nvarchar(255),
  saga_type       nvarchar(255),
  serialized_saga varbinary(max),
  primary key (saga_id)
);

create table token_entry
(
  processor_name nvarchar(255) not null,
  segment        int         not null,
  owner          nvarchar(255),
  timestamp      nvarchar(255) not null,
  token          varbinary(max),
  token_type     nvarchar(255),
  primary key (processor_name, segment)
);

CREATE TABLE dead_letter_entry
(
  dead_letter_id       nvarchar(255) NOT NULL,
  cause_message        nvarchar(255),
  cause_type           nvarchar(255),
  diagnostics          varbinary(max),
  enqueued_at          datetime2    NOT NULL,
  last_touched         datetime2,
  aggregate_identifier nvarchar(255),
  event_identifier     nvarchar(255) NOT NULL,
  message_type         nvarchar(255) NOT NULL,
  meta_data            varbinary(max),
  payload              varbinary(max)          NOT NULL,
  payload_revision     nvarchar(255),
  payload_type         nvarchar(255) NOT NULL,
  sequence_number      bigint,
  time_stamp           nvarchar(255) NOT NULL,
  token                varbinary(max),
  token_type           nvarchar(255),
  type                 nvarchar(255),
  processing_group     nvarchar(255) NOT NULL,
  processing_started   datetime2,
  sequence_identifier  nvarchar(255) NOT NULL,
  sequence_index       bigint         NOT NULL,
  PRIMARY KEY (dead_letter_id)
);

create table domain_event_entry
(
  global_index         bigint         not null,
  event_identifier     nvarchar(255) not null,
  meta_data            varbinary(max),
  payload              varbinary(max)          not null,
  payload_revision     nvarchar(255),
  payload_type         nvarchar(255) not null,
  time_stamp           nvarchar(255) not null,
  aggregate_identifier nvarchar(255) not null,
  sequence_number      bigint         not null,
  type                 nvarchar(255),
  primary key (global_index)
);

create table snapshot_event_entry
(
  aggregate_identifier nvarchar(255) not null,
  sequence_number      bigint       not null,
  type                 nvarchar(255) not null,
  event_identifier     nvarchar(255) not null,
  meta_data            varbinary(max),
  payload              varbinary(max)          not null,
  payload_revision     nvarchar(255),
  payload_type         nvarchar(255) not null,
  time_stamp           nvarchar(255) not null,
  primary key (aggregate_identifier, sequence_number, type)
);

create index IDX_association_value_entry_stakav on association_value_entry (saga_type, association_key, association_value);
create index IDX_association_value_entry_sist on association_value_entry (saga_id, saga_type);
