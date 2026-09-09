create sequence DOMAIN_EVENT_ENTRY_SEQ start with 1 increment by 50;

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
