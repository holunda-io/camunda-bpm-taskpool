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

create index IDX_DLQ_PROCESSING_GROUP on DEAD_LETTER_ENTRY (PROCESSING_GROUP);
create index IDX_DLQ_PROCESSING_SEQUENCE on DEAD_LETTER_ENTRY (PROCESSING_GROUP, SEQUENCE_IDENTIFIER);
