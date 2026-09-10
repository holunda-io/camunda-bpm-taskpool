create table if not exists TOKEN_ENTRY
(
  PROCESSOR_NAME varchar(255) not null,
  SEGMENT        int4         not null,
  OWNER          varchar(255),
  timestamp      varchar(255) not null,
  TOKEN          oid,
  TOKEN_TYPE     varchar(255),
  constraint PK_TOKEN primary key (PROCESSOR_NAME, SEGMENT)
);

create table if not exists DEAD_LETTER_ENTRY
(
  DEAD_LETTER_ID       varchar(255) not null,
  CAUSE_MESSAGE        varchar(255),
  CAUSE_TYPE           varchar(255),
  DIAGNOSTICS          oid,
  ENQUEUED_AT          timestamp    not null,
  LAST_TOUCHED         timestamp,
  AGGREGATE_IDENTIFIER varchar(255),
  EVENT_IDENTIFIER     varchar(255) not null,
  MESSAGE_TYPE         varchar(255) not null,
  META_DATA            oid,
  PAYLOAD              oid          not null,
  PAYLOAD_REVISION     varchar(255),
  PAYLOAD_TYPE         varchar(255) not null,
  SEQUENCE_NUMBER      int8,
  TIME_STAMP           varchar(255) not null,
  TOKEN                oid,
  TOKEN_TYPE           varchar(255),
  TYPE                 varchar(255),
  PROCESSING_GROUP     varchar(255) not null,
  PROCESSING_STARTED   timestamp,
  SEQUENCE_IDENTIFIER  varchar(255) not null,
  SEQUENCE_INDEX       int8         not null,
  constraint PK_DEAD_LETTER primary key (DEAD_LETTER_ID),

  constraint UK_DEAD_LETTER_PROC_SEQ unique (PROCESSING_GROUP, SEQUENCE_IDENTIFIER, SEQUENCE_INDEX)
);

create index if not exists IDX_DLQ_PROCESSING_GROUP on DEAD_LETTER_ENTRY (PROCESSING_GROUP);
create index if not exists IDX_DLQ_PROCESSING_SEQUENCE on DEAD_LETTER_ENTRY (PROCESSING_GROUP, SEQUENCE_IDENTIFIER);
