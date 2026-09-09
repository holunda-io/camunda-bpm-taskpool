create table if not exists DOMAIN_EVENT_ENTRY
(
  GLOBAL_INDEX         bigint auto_increment not null,
  EVENT_IDENTIFIER     varchar(255) not null,
  META_DATA            longblob,
  PAYLOAD              longblob     not null,
  PAYLOAD_REVISION     varchar(255),
  PAYLOAD_TYPE         varchar(255) not null,
  TIME_STAMP           varchar(255) not null,
  AGGREGATE_IDENTIFIER varchar(255) not null,
  SEQUENCE_NUMBER      bigint       not null,
  TYPE                 varchar(255),
  constraint PK_DOMAIN_EVENT primary key (GLOBAL_INDEX),

  constraint UK_DOMAIN_EVENT_AGG_SEQ unique (AGGREGATE_IDENTIFIER, SEQUENCE_NUMBER),
  constraint UK_DOMAIN_EVENT_EVENT_ID unique (EVENT_IDENTIFIER)
);

create table if not exists SNAPSHOT_EVENT_ENTRY
(
  AGGREGATE_IDENTIFIER varchar(255) not null,
  SEQUENCE_NUMBER      bigint       not null,
  TYPE                 varchar(255) not null,
  EVENT_IDENTIFIER     varchar(255) not null,
  META_DATA            longblob,
  PAYLOAD              longblob     not null,
  PAYLOAD_REVISION     varchar(255),
  PAYLOAD_TYPE         varchar(255) not null,
  TIME_STAMP           varchar(255) not null,
  constraint PK_SNAPSHOT_EVENT primary key (AGGREGATE_IDENTIFIER, SEQUENCE_NUMBER, TYPE),

  constraint UK_SNAPSHOT_EVENT_EVENT_ID unique (EVENT_IDENTIFIER)
);

create sequence if not exists DOMAIN_EVENT_ENTRY_SEQ start with 1 increment by 50;
