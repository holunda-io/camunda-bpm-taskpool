create table if not exists DEAD_LETTER_ENTRY
(
  DEAD_LETTER_ID       varchar(255) not null,
  CAUSE_MESSAGE        varchar(255),
  CAUSE_TYPE           varchar(255),
  DIAGNOSTICS          longblob,
  ENQUEUED_AT          datetime(6) not null,
  LAST_TOUCHED         datetime(6),
  AGGREGATE_IDENTIFIER varchar(255),
  EVENT_IDENTIFIER     varchar(255) not null,
  MESSAGE_TYPE         varchar(255) not null,
  META_DATA            longblob,
  PAYLOAD              longblob     not null,
  PAYLOAD_REVISION     varchar(255),
  PAYLOAD_TYPE         varchar(255) not null,
  SEQUENCE_NUMBER      bigint,
  TIME_STAMP           varchar(255) not null,
  TOKEN                longblob,
  TOKEN_TYPE           varchar(255),
  TYPE                 varchar(255),
  PROCESSING_GROUP     varchar(255) not null,
  PROCESSING_STARTED   datetime(6),
  SEQUENCE_IDENTIFIER  varchar(255) not null,
  SEQUENCE_INDEX       bigint       not null,
  constraint PK_DEAD_LETTER primary key (DEAD_LETTER_ID),

  constraint UK_DEAD_LETTER_PROC_SEQ unique (PROCESSING_GROUP, SEQUENCE_IDENTIFIER, SEQUENCE_INDEX)
);

create table if not exists TOKEN_ENTRY
(
  PROCESSOR_NAME varchar(255) not null,
  SEGMENT        integer      not null,
  OWNER          varchar(255),
  timestamp      varchar(255) not null,
  TOKEN          longblob,
  TOKEN_TYPE     varchar(255),
  constraint PK_TOKEN primary key (PROCESSOR_NAME, SEGMENT)
);
create index if not exists IDX_DLQ_PROCESSING_GROUP on DEAD_LETTER_ENTRY (PROCESSING_GROUP);
create index if not exists IDX_DLQ_PROCESSING_SEQUENCE on DEAD_LETTER_ENTRY (PROCESSING_GROUP, SEQUENCE_IDENTIFIER);











