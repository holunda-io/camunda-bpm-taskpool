create table if not exists ASSOCIATION_VALUE_ENTRY
(
  ID                bigint not null,
  ASSOCIATION_KEY   varchar(255) not null,
  ASSOCIATION_VALUE varchar(255),
  SAGA_ID           varchar(255) not null,
  SAGA_TYPE         varchar(255),
  constraint PK_ASSOCIATION_VALUE primary key (ID)
);

create table if not exists DEAD_LETTER_ENTRY
(
  DEAD_LETTER_ID       varchar(255) not null,
  CAUSE_MESSAGE        varchar(255),
  CAUSE_TYPE           varchar(255),
  DIAGNOSTICS          bytea,
  ENQUEUED_AT          timestamp not null,
  LAST_TOUCHED         timestamp,
  AGGREGATE_IDENTIFIER varchar(255),
  EVENT_IDENTIFIER     varchar(255) not null,
  MESSAGE_TYPE         varchar(255) not null,
  META_DATA            bytea,
  PAYLOAD              bytea        not null,
  PAYLOAD_REVISION     varchar(255),
  PAYLOAD_TYPE         varchar(255) not null,
  SEQUENCE_NUMBER      bigint,
  TIME_STAMP           varchar(255) not null,
  TOKEN                bytea,
  TOKEN_TYPE           varchar(255),
  TYPE                 varchar(255),
  PROCESSING_GROUP     varchar(255) not null,
  PROCESSING_STARTED   timestamp,
  SEQUENCE_IDENTIFIER  varchar(255) not null,
  SEQUENCE_INDEX       bigint       not null,
  constraint PK_DEAD_LETTER primary key (DEAD_LETTER_ID),

  constraint UK_DEAD_LETTER_PROC_SEQ unique (PROCESSING_GROUP, SEQUENCE_IDENTIFIER, SEQUENCE_INDEX)
);

create table if not exists DOMAIN_EVENT_ENTRY
(
  GLOBAL_INDEX         bigint not null,
  EVENT_IDENTIFIER     varchar(255) not null,
  META_DATA            bytea,
  PAYLOAD              bytea        not null,
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

create table if not exists SAGA_ENTRY
(
  SAGA_ID         varchar(255) not null,
  REVISION        varchar(255),
  SAGA_TYPE       varchar(255),
  SERIALIZED_SAGA bytea,
  constraint PK_SAGA primary key (SAGA_ID)
);

create table if not exists SNAPSHOT_EVENT_ENTRY
(
  AGGREGATE_IDENTIFIER varchar(255) not null,
  SEQUENCE_NUMBER      bigint       not null,
  TYPE                 varchar(255) not null,
  EVENT_IDENTIFIER     varchar(255) not null,
  META_DATA            bytea,
  PAYLOAD              bytea        not null,
  PAYLOAD_REVISION     varchar(255),
  PAYLOAD_TYPE         varchar(255) not null,
  TIME_STAMP           varchar(255) not null,
  constraint PK_SNAPSHOT_EVENT primary key (AGGREGATE_IDENTIFIER, SEQUENCE_NUMBER, TYPE),

  constraint UK_SNAPSHOT_EVENT_EVENT_ID unique (EVENT_IDENTIFIER)
);

create table if not exists TOKEN_ENTRY
(
  PROCESSOR_NAME varchar(255) not null,
  SEGMENT        integer      not null,
  OWNER          varchar(255),
  timestamp      varchar(255) not null,
  TOKEN          bytea,
  TOKEN_TYPE     varchar(255),
  constraint PK_TOKEN primary key (PROCESSOR_NAME, SEGMENT)
);
create index if not exists IDX_ASSOC_VALUE_SAGA_KEY_VAL on ASSOCIATION_VALUE_ENTRY (SAGA_TYPE, ASSOCIATION_KEY, ASSOCIATION_VALUE);
create index if not exists IDX_ASSOC_VALUE_SAGA on ASSOCIATION_VALUE_ENTRY (SAGA_ID, SAGA_TYPE);
create index if not exists IDX_DLQ_PROCESSING_GROUP on DEAD_LETTER_ENTRY (PROCESSING_GROUP);
create index if not exists IDX_DLQ_PROCESSING_SEQUENCE on DEAD_LETTER_ENTRY (PROCESSING_GROUP, SEQUENCE_IDENTIFIER);











create sequence if not exists ASSOCIATION_VALUE_ENTRY_SEQ start with 1 increment by 50;
create sequence if not exists DOMAIN_EVENT_ENTRY_SEQ start with 1 increment by 50;
