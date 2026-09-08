create table if not exists association_value_entry
(
  id                bigint auto_increment not null,
  association_key   varchar(255) not null,
  association_value varchar(255),
  saga_id           varchar(255) not null,
  saga_type         varchar(255),
  primary key (id)
);

create table if not exists dead_letter_entry
(
  dead_letter_id       varchar(255) not null,
  cause_message        varchar(255),
  cause_type           varchar(255),
  diagnostics          longblob,
  enqueued_at          datetime(6) not null,
  last_touched         datetime(6),
  aggregate_identifier varchar(255),
  event_identifier     varchar(255) not null,
  message_type         varchar(255) not null,
  meta_data            longblob,
  payload              longblob     not null,
  payload_revision     varchar(255),
  payload_type         varchar(255) not null,
  sequence_number      bigint,
  time_stamp           varchar(255) not null,
  token                longblob,
  token_type           varchar(255),
  type                 varchar(255),
  processing_group     varchar(255) not null,
  processing_started   datetime(6),
  sequence_identifier  varchar(255) not null,
  sequence_index       bigint       not null,
  primary key (dead_letter_id)
);

create table if not exists domain_event_entry
(
  global_index         bigint auto_increment not null,
  event_identifier     varchar(255) not null,
  meta_data            longblob,
  payload              longblob     not null,
  payload_revision     varchar(255),
  payload_type         varchar(255) not null,
  time_stamp           varchar(255) not null,
  aggregate_identifier varchar(255) not null,
  sequence_number      bigint       not null,
  type                 varchar(255),
  primary key (global_index)
);

create table if not exists saga_entry
(
  saga_id         varchar(255) not null,
  revision        varchar(255),
  saga_type       varchar(255),
  serialized_saga longblob,
  primary key (saga_id)
);

create table if not exists snapshot_event_entry
(
  aggregate_identifier varchar(255) not null,
  sequence_number      bigint       not null,
  type                 varchar(255) not null,
  event_identifier     varchar(255) not null,
  meta_data            longblob,
  payload              longblob     not null,
  payload_revision     varchar(255),
  payload_type         varchar(255) not null,
  time_stamp           varchar(255) not null,
  primary key (aggregate_identifier, sequence_number, type)
);

create table if not exists token_entry
(
  processor_name varchar(255) not null,
  segment        integer      not null,
  owner          varchar(255),
  timestamp      varchar(255) not null,
  token          longblob,
  token_type     varchar(255),
  primary key (processor_name, segment)
);
create index if not exists IDXk45eqnxkgd8hpdn6xixn8sgft on association_value_entry (saga_type, association_key, association_value);
create index if not exists IDXgv5k1v2mh6frxuy5c0hgbau94 on association_value_entry (saga_id, saga_type);
create index if not exists IDXe67wcx5fiq9hl4y4qkhlcj9cg on dead_letter_entry (processing_group);
create index if not exists IDXrwucpgs6sn93ldgoeh2q9k6bn on dead_letter_entry (processing_group, sequence_identifier);

alter table dead_letter_entry
  add constraint UKhlr8io86j74qy298xf720n16v unique (processing_group, sequence_identifier, sequence_index);

alter table domain_event_entry
  add constraint UK8s1f994p4la2ipb13me2xqm1w unique (aggregate_identifier, sequence_number);

alter table domain_event_entry
  add constraint UK_fwe6lsa8bfo6hyas6ud3m8c7x unique (event_identifier);

alter table snapshot_event_entry
  add constraint UK_e1uucjseo68gopmnd0vgdl44h unique (event_identifier);







create sequence if not exists association_value_entry_seq start with 1 increment by 50;
create sequence if not exists domain_event_entry_seq start with 1 increment by 50;
