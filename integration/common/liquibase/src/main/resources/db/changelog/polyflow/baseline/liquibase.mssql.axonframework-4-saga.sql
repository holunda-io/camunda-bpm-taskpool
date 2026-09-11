create sequence ASSOCIATION_VALUE_ENTRY_SEQ start with 1 increment by 50;
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

create index IDX_ASSOC_VALUE_SAGA_KEY_VAL on ASSOCIATION_VALUE_ENTRY (SAGA_TYPE, ASSOCIATION_KEY, ASSOCIATION_VALUE);
create index IDX_ASSOC_VALUE_SAGA on ASSOCIATION_VALUE_ENTRY (SAGA_ID, SAGA_TYPE);

