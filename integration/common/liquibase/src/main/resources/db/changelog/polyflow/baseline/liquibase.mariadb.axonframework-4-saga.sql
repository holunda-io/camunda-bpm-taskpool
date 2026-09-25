create table if not exists association_value_entry
(
  ID                bigint auto_increment not null,
  ASSOCIATION_KEY   varchar(255) not null,
  ASSOCIATION_VALUE varchar(255),
  SAGA_ID           varchar(255) not null,
  SAGA_TYPE         varchar(255),
  constraint PK_ASSOCIATION_VALUE primary key (ID)
);

create table if not exists saga_entry
(
  SAGA_ID         varchar(255) not null,
  REVISION        varchar(255),
  SAGA_TYPE       varchar(255),
  SERIALIZED_SAGA blob,
  constraint PK_SAGA primary key (SAGA_ID)
);

create index if not exists IDX_ASSOC_VALUE_SAGA_KEY_VAL on association_value_entry (SAGA_TYPE, ASSOCIATION_KEY, ASSOCIATION_VALUE);
create index if not exists IDX_ASSOC_VALUE_SAGA on association_value_entry (SAGA_ID, SAGA_TYPE);
create sequence if not exists association_value_entry_seq start with 1 increment by 50;
