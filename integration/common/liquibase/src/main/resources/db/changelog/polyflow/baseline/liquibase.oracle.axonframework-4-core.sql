create sequence DOMAIN_EVENT_ENTRY_SEQ start with 1 increment by 50;

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
