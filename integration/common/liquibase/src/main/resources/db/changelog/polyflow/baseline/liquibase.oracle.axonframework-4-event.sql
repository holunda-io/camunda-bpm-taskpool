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

create index IDX_DLQ_PROCESSING_GROUP on DEAD_LETTER_ENTRY (PROCESSING_GROUP);
create index IDX_DLQ_PROCESSING_SEQUENCE on DEAD_LETTER_ENTRY (PROCESSING_GROUP, SEQUENCE_IDENTIFIER);
