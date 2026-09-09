create sequence ASSOCIATION_VALUE_ENTRY_SEQ start with 1 increment by 50;
create sequence DOMAIN_EVENT_ENTRY_SEQ start with 1 increment by 50;

create table ASSOCIATION_VALUE_ENTRY
(
  ID                bigint         not null,
  ASSOCIATION_KEY   NVARCHAR(255) not null,
  ASSOCIATION_VALUE NVARCHAR(255),
  SAGA_ID           NVARCHAR(255) not null,
  SAGA_TYPE         NVARCHAR(255),
  constraint PK_ASSOCIATION_VALUE primary key (ID)
);

create table SAGA_ENTRY
(
  SAGA_ID         NVARCHAR(255) not null,
  REVISION        NVARCHAR(255),
  SAGA_TYPE       NVARCHAR(255),
  SERIALIZED_SAGA varbinary(max),
  constraint PK_SAGA primary key (SAGA_ID)
);

create table SNAPSHOT_EVENT_ENTRY
(
  AGGREGATE_IDENTIFIER NVARCHAR(255) not null,
  SEQUENCE_NUMBER      bigint       not null,
  TYPE                 NVARCHAR(255) not null,
  EVENT_IDENTIFIER     NVARCHAR(255) not null,
  META_DATA            varbinary(max),
  PAYLOAD              varbinary(max)          not null,
  PAYLOAD_REVISION     NVARCHAR(255),
  PAYLOAD_TYPE         NVARCHAR(255) not null,
  TIME_STAMP           NVARCHAR(255) not null,
  constraint PK_SNAPSHOT_EVENT primary key (AGGREGATE_IDENTIFIER, SEQUENCE_NUMBER, TYPE),

  constraint UK_SNAPSHOT_EVENT_EVENT_ID unique (EVENT_IDENTIFIER)
);

create table TOKEN_ENTRY
(
  PROCESSOR_NAME NVARCHAR(255) not null,
  SEGMENT        int         not null,
  OWNER          NVARCHAR(255),
  timestamp      NVARCHAR(255) not null,
  TOKEN          varbinary(max),
  TOKEN_TYPE     NVARCHAR(255),
  constraint PK_TOKEN primary key (PROCESSOR_NAME, SEGMENT)
);

create table DOMAIN_EVENT_ENTRY
(
  GLOBAL_INDEX         bigint         not null,
  EVENT_IDENTIFIER     NVARCHAR(255) not null,
  META_DATA            varbinary(max),
  PAYLOAD              varbinary(max)          not null,
  PAYLOAD_REVISION     NVARCHAR(255),
  PAYLOAD_TYPE         NVARCHAR(255) not null,
  TIME_STAMP           NVARCHAR(255) not null,
  AGGREGATE_IDENTIFIER NVARCHAR(255) not null,
  SEQUENCE_NUMBER      bigint         not null,
  TYPE                 NVARCHAR(255),
  constraint PK_DOMAIN_EVENT primary key (GLOBAL_INDEX),

  constraint UK_DOMAIN_EVENT_AGG_SEQ unique (AGGREGATE_IDENTIFIER, SEQUENCE_NUMBER),
  constraint UK_DOMAIN_EVENT_EVENT_ID unique (EVENT_IDENTIFIER)
);

create table DEAD_LETTER_ENTRY
(
  DEAD_LETTER_ID       NVARCHAR(255) not null,
  CAUSE_MESSAGE        NVARCHAR(255),
  CAUSE_TYPE           NVARCHAR(255),
  DIAGNOSTICS          varbinary(max),
  ENQUEUED_AT          DATETIME2    not null,
  LAST_TOUCHED         DATETIME2,
  AGGREGATE_IDENTIFIER NVARCHAR(255),
  EVENT_IDENTIFIER     NVARCHAR(255) not null,
  MESSAGE_TYPE         NVARCHAR(255) not null,
  META_DATA            varbinary(max),
  PAYLOAD              varbinary(max)          not null,
  PAYLOAD_REVISION     NVARCHAR(255),
  PAYLOAD_TYPE         NVARCHAR(255) not null,
  SEQUENCE_NUMBER      bigint,
  TIME_STAMP           NVARCHAR(255) not null,
  TOKEN                varbinary(max),
  TOKEN_TYPE           NVARCHAR(255),
  TYPE                 NVARCHAR(255),
  PROCESSING_GROUP     NVARCHAR(255) not null,
  PROCESSING_STARTED   DATETIME2,
  SEQUENCE_IDENTIFIER  NVARCHAR(255) not null,
  SEQUENCE_INDEX       bigint         not null,
  constraint PK_DEAD_LETTER primary key (DEAD_LETTER_ID),

  constraint UK_DEAD_LETTER_PROC_SEQ unique (PROCESSING_GROUP, SEQUENCE_IDENTIFIER, SEQUENCE_INDEX)
);

create index IDX_ASSOC_VALUE_SAGA_KEY_VAL on ASSOCIATION_VALUE_ENTRY (SAGA_TYPE, ASSOCIATION_KEY, ASSOCIATION_VALUE);
create index IDX_ASSOC_VALUE_SAGA on ASSOCIATION_VALUE_ENTRY (SAGA_ID, SAGA_TYPE);

create index IDX_DLQ_PROCESSING_GROUP on DEAD_LETTER_ENTRY (PROCESSING_GROUP);
create index IDX_DLQ_PROCESSING_SEQUENCE on DEAD_LETTER_ENTRY (PROCESSING_GROUP, SEQUENCE_IDENTIFIER);
