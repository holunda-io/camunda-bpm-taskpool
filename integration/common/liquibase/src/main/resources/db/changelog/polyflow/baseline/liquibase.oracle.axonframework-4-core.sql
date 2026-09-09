create sequence ASSOCIATION_VALUE_ENTRY_SEQ start with 1 increment by 50;
create sequence DOMAIN_EVENT_ENTRY_SEQ start with 1 increment by 50;

create table ASSOCIATION_VALUE_ENTRY
(
  ID                number(19)         not null,
  ASSOCIATION_KEY   varchar2(255) not null,
  ASSOCIATION_VALUE varchar2(255),
  SAGA_ID           varchar2(255) not null,
  SAGA_TYPE         varchar2(255),
  constraint PK_ASSOCIATION_VALUE primary key (ID)
);

create table SAGA_ENTRY
(
  SAGA_ID         varchar2(255) not null,
  REVISION        varchar2(255),
  SAGA_TYPE       varchar2(255),
  SERIALIZED_SAGA blob,
  constraint PK_SAGA primary key (SAGA_ID)
);

create table SNAPSHOT_EVENT_ENTRY
(
  AGGREGATE_IDENTIFIER varchar2(255) not null,
  SEQUENCE_NUMBER      number(19)   not null,
  TYPE                 varchar2(255) not null,
  EVENT_IDENTIFIER     varchar2(255) not null,
  META_DATA            blob,
  PAYLOAD              blob          not null,
  PAYLOAD_REVISION     varchar2(255),
  PAYLOAD_TYPE         varchar2(255) not null,
  TIME_STAMP           varchar2(255) not null,
  constraint PK_SNAPSHOT_EVENT primary key (AGGREGATE_IDENTIFIER, SEQUENCE_NUMBER, TYPE),

  constraint UK_SNAPSHOT_EVENT_EVENT_ID unique (EVENT_IDENTIFIER)
);

create table TOKEN_ENTRY
(
  PROCESSOR_NAME varchar2(255) not null,
  SEGMENT        number(10)         not null,
  OWNER          varchar2(255),
  timestamp      varchar2(255) not null,
  TOKEN          blob,
  TOKEN_TYPE     varchar2(255),
  constraint PK_TOKEN primary key (PROCESSOR_NAME, SEGMENT)
);

create table DOMAIN_EVENT_ENTRY
(
  GLOBAL_INDEX         number(19)         not null,
  EVENT_IDENTIFIER     varchar2(255) not null,
  META_DATA            blob,
  PAYLOAD              blob          not null,
  PAYLOAD_REVISION     varchar2(255),
  PAYLOAD_TYPE         varchar2(255) not null,
  TIME_STAMP           varchar2(255) not null,
  AGGREGATE_IDENTIFIER varchar2(255) not null,
  SEQUENCE_NUMBER      number(19)         not null,
  TYPE                 varchar2(255),
  constraint PK_DOMAIN_EVENT primary key (GLOBAL_INDEX),

  constraint UK_DOMAIN_EVENT_AGG_SEQ unique (AGGREGATE_IDENTIFIER, SEQUENCE_NUMBER),
  constraint UK_DOMAIN_EVENT_EVENT_ID unique (EVENT_IDENTIFIER)
);

create table DEAD_LETTER_ENTRY
(
  DEAD_LETTER_ID       varchar2(255) not null,
  CAUSE_MESSAGE        varchar2(255),
  CAUSE_TYPE           varchar2(255),
  DIAGNOSTICS          blob,
  ENQUEUED_AT          timestamp    not null,
  LAST_TOUCHED         timestamp,
  AGGREGATE_IDENTIFIER varchar2(255),
  EVENT_IDENTIFIER     varchar2(255) not null,
  MESSAGE_TYPE         varchar2(255) not null,
  META_DATA            blob,
  PAYLOAD              blob          not null,
  PAYLOAD_REVISION     varchar2(255),
  PAYLOAD_TYPE         varchar2(255) not null,
  SEQUENCE_NUMBER      number(19),
  TIME_STAMP           varchar2(255) not null,
  TOKEN                blob,
  TOKEN_TYPE           varchar2(255),
  TYPE                 varchar2(255),
  PROCESSING_GROUP     varchar2(255) not null,
  PROCESSING_STARTED   timestamp,
  SEQUENCE_IDENTIFIER  varchar2(255) not null,
  SEQUENCE_INDEX       number(19)         not null,
  constraint PK_DEAD_LETTER primary key (DEAD_LETTER_ID),

  constraint UK_DEAD_LETTER_PROC_SEQ unique (PROCESSING_GROUP, SEQUENCE_IDENTIFIER, SEQUENCE_INDEX)
);

create index IDX_ASSOC_VALUE_SAGA_KEY_VAL on ASSOCIATION_VALUE_ENTRY (SAGA_TYPE, ASSOCIATION_KEY, ASSOCIATION_VALUE);
create index IDX_ASSOC_VALUE_SAGA on ASSOCIATION_VALUE_ENTRY (SAGA_ID, SAGA_TYPE);

create index IDX_DLQ_PROCESSING_GROUP on DEAD_LETTER_ENTRY (PROCESSING_GROUP);
create index IDX_DLQ_PROCESSING_SEQUENCE on DEAD_LETTER_ENTRY (PROCESSING_GROUP, SEQUENCE_IDENTIFIER);
