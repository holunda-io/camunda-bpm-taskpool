create sequence if not exists ASSOCIATION_VALUE_ENTRY_SEQ start with 1 increment by 50;
create table if not exists ASSOCIATION_VALUE_ENTRY
(
  ID                int8         not null,
  ASSOCIATION_KEY   varchar(255) not null,
  ASSOCIATION_VALUE varchar(255),
  SAGA_ID           varchar(255) not null,
  SAGA_TYPE         varchar(255),
  constraint PK_ASSOCIATION_VALUE primary key (ID)
);

create table if not exists SAGA_ENTRY
(
  SAGA_ID         varchar(255) not null,
  REVISION        varchar(255),
  SAGA_TYPE       varchar(255),
  SERIALIZED_SAGA oid,
  constraint PK_SAGA primary key (SAGA_ID)
);

create index if not exists IDX_ASSOC_VALUE_SAGA_KEY_VAL on ASSOCIATION_VALUE_ENTRY (SAGA_TYPE, ASSOCIATION_KEY, ASSOCIATION_VALUE);
create index if not exists IDX_ASSOC_VALUE_SAGA on ASSOCIATION_VALUE_ENTRY (SAGA_ID, SAGA_TYPE);

