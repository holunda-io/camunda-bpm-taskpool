create table PLF_DATA_ENTRY
(
  ENTRY_ID           varchar(255) not null,
  ENTRY_TYPE         varchar(255) not null,
  APPLICATION_NAME   varchar(255) not null,
  DATE_CREATED       timestamp    not null,
  DATE_DELETED       timestamp,
  DESCRIPTION        varchar(2048),
  FORM_KEY           varchar(255),
  DATE_LAST_MODIFIED timestamp    not null,
  NAME               varchar(255) not null,
  PAYLOAD            bytea,
  REVISION           bigint,
  PROCESSING_TYPE    varchar(255) not null,
  STATE              varchar(255) not null,
  TYPE               varchar(255) not null,
  VERSION_TIMESTAMP  bigint,
  constraint PK_DATA_ENTRY primary key (ENTRY_ID, ENTRY_TYPE)
);

create table PLF_DATA_ENTRY_AUTHORIZATIONS
(
  ENTRY_ID             varchar(255) not null,
  ENTRY_TYPE           varchar(255) not null,
  AUTHORIZED_PRINCIPAL varchar(255) not null,
  constraint PK_DATA_ENTRY_AUTH primary key (ENTRY_ID, ENTRY_TYPE, AUTHORIZED_PRINCIPAL),
  constraint FK_DATA_ENTRY_AUTH_ENTRY
    foreign key (ENTRY_ID, ENTRY_TYPE)
      references PLF_DATA_ENTRY (ENTRY_ID, ENTRY_TYPE)
);

create table PLF_DATA_ENTRY_PAYLOAD_ATTRIBUTES
(
  ENTRY_ID   varchar(64)  not null,
  ENTRY_TYPE varchar(128) not null,
  PATH       varchar(255) not null,
  "VALUE"    varchar(128) not null,
  constraint PK_DATA_ENTRY_ATTR primary key (ENTRY_ID, ENTRY_TYPE, PATH, "VALUE"),
  constraint FK_DATA_ENTRY_ATTR_ENTRY
    foreign key (ENTRY_ID, ENTRY_TYPE)
      references PLF_DATA_ENTRY (ENTRY_ID, ENTRY_TYPE)
);

create table PLF_DATA_ENTRY_PROTOCOL
(
  ID              varchar(255) not null,
  LOG_DETAILS     varchar(255),
  LOG_MESSAGE     varchar(255),
  PROCESSING_TYPE varchar(255) not null,
  STATE           varchar(255) not null,
  TIME            timestamp    not null,
  USERNAME        varchar(255),
  ENTRY_ID        varchar(255) not null,
  ENTRY_TYPE      varchar(255) not null,
  constraint PK_DATA_ENTRY_PROTOCOL primary key (ID),
  constraint FK_DATA_ENTRY_PROTOCOL_ENTRY
    foreign key (ENTRY_ID, ENTRY_TYPE)
      references PLF_DATA_ENTRY (ENTRY_ID, ENTRY_TYPE)
);

create table PLF_PROC_DEF
(
  PROC_DEF_ID             varchar(255) not null,
  APPLICATION_NAME        varchar(255) not null,
  DESCRIPTION             varchar(2048),
  NAME                    varchar(255) not null,
  PROC_DEF_KEY            varchar(255) not null,
  PROC_DEF_VERSION        integer      not null,
  START_FORM_KEY          varchar(255),
  STARTABLE_FROM_TASKLIST boolean,
  VERSION_TAG             varchar(255),
  constraint PK_PROC_DEF primary key (PROC_DEF_ID)
);

create table PLF_PROC_DEF_AUTHORIZATIONS
(
  PROC_DEF_ID                  varchar(255) not null,
  AUTHORIZED_STARTER_PRINCIPAL varchar(255) not null,
  constraint PK_PROC_DEF_AUTH primary key (PROC_DEF_ID, AUTHORIZED_STARTER_PRINCIPAL),
  constraint FK_PROC_DEF_AUTH_PROC_DEF
    foreign key (PROC_DEF_ID)
      references PLF_PROC_DEF (PROC_DEF_ID)
);

create table PLF_PROC_INSTANCE
(
  INSTANCE_ID         varchar(255) not null,
  BUSINESS_KEY        varchar(255),
  DELETE_REASON       varchar(255),
  END_ACTIVITY_ID     varchar(255),
  APPLICATION_NAME    varchar(255) not null,
  SOURCE_DEF_ID       varchar(255) not null,
  SOURCE_DEF_KEY      varchar(255) not null,
  SOURCE_EXECUTION_ID varchar(255) not null,
  SOURCE_INSTANCE_ID  varchar(255) not null,
  SOURCE_NAME         varchar(255) not null,
  SOURCE_TYPE         varchar(255) not null,
  SOURCE_TENANT_ID    varchar(255),
  START_ACTIVITY_ID   varchar(255),
  START_USER_ID       varchar(255),
  RUN_STATE           varchar(255) not null,
  SUPER_INSTANCE_ID   varchar(255),
  constraint PK_PROC_INSTANCE primary key (INSTANCE_ID)
);

create table PLF_TASK
(
  TASK_ID             varchar(255) not null,
  ASSIGNEE_ID         varchar(255),
  BUSINESS_KEY        varchar(255),
  DATE_CREATED        timestamp    not null,
  DESCRIPTION         varchar(2048),
  DATE_DUE            timestamp,
  DATE_FOLLOW_UP      timestamp,
  FORM_KEY            varchar(255),
  NAME                varchar(255) not null,
  OWNER_ID            varchar(255),
  PAYLOAD             bytea,
  PRIORITY            integer,
  APPLICATION_NAME    varchar(255) not null,
  SOURCE_DEF_ID       varchar(255) not null,
  SOURCE_DEF_KEY      varchar(255) not null,
  SOURCE_EXECUTION_ID varchar(255) not null,
  SOURCE_INSTANCE_ID  varchar(255) not null,
  SOURCE_NAME         varchar(255) not null,
  SOURCE_TYPE         varchar(255) not null,
  SOURCE_TENANT_ID    varchar(255),
  TASK_DEF_KEY        varchar(255) not null,
  constraint PK_TASK primary key (TASK_ID)
);

create table PLF_TASK_AUTHORIZATIONS
(
  TASK_ID              varchar(255) not null,
  AUTHORIZED_PRINCIPAL varchar(255) not null,
  constraint PK_TASK_AUTH primary key (TASK_ID, AUTHORIZED_PRINCIPAL),
  constraint FK_TASK_AUTH_TASK
    foreign key (TASK_ID)
      references PLF_TASK (TASK_ID)
);

create table PLF_TASK_CORRELATIONS
(
  TASK_ID    varchar(255) not null,
  ENTRY_ID   varchar(255) not null,
  ENTRY_TYPE varchar(255) not null,
  constraint PK_TASK_CORRELATION primary key (TASK_ID, ENTRY_ID, ENTRY_TYPE),
  constraint FK_TASK_CORRELATION_TASK
    foreign key (TASK_ID)
      references PLF_TASK (TASK_ID)
);

create table PLF_TASK_PAYLOAD_ATTRIBUTES
(
  TASK_ID varchar(255) not null,
  PATH    varchar(255) not null,
  "VALUE" varchar(255) not null,
  constraint PK_TASK_ATTR primary key (TASK_ID, PATH, "VALUE"),
  constraint FK_TASK_ATTR_TASK
    foreign key (TASK_ID)
      references PLF_TASK (TASK_ID)
);

create table PLF_DATA_ENTRY_CORRELATIONS
(
  OWNING_ENTRY_TYPE varchar(255) not null,
  OWNING_ENTRY_ID   varchar(64)  not null,
  ENTRY_TYPE        varchar(255) not null,
  ENTRY_ID          varchar(64)  not null,
  constraint PK_DATA_ENTRY_CORRELATION primary key (OWNING_ENTRY_TYPE, OWNING_ENTRY_ID, ENTRY_TYPE, ENTRY_ID)
);

create view PLF_VIEW_TASK_AND_DATA_ENTRY_PAYLOAD as
(
(select PC.TASK_ID, DEA.PATH, DEA."VALUE"
 from PLF_TASK_CORRELATIONS PC
        join PLF_DATA_ENTRY_PAYLOAD_ATTRIBUTES DEA on PC.ENTRY_ID = DEA.ENTRY_ID and PC.ENTRY_TYPE = DEA.ENTRY_TYPE)
union
select TASK_ID, PATH, "VALUE" from PLF_TASK_PAYLOAD_ATTRIBUTES);

create view PLF_VIEW_DATA_ENTRY_PAYLOAD as
(
select ENTRY_ID, ENTRY_TYPE, PATH, "VALUE"
from PLF_DATA_ENTRY_PAYLOAD_ATTRIBUTES
union
(select EC.OWNING_ENTRY_ID   as ENTRY_ID,
        EC.OWNING_ENTRY_TYPE as ENTRY_TYPE,
        EP.PATH              as PATH,
        EP."VALUE"           as "VALUE"
 from PLF_DATA_ENTRY_CORRELATIONS EC
        join PLF_DATA_ENTRY_PAYLOAD_ATTRIBUTES EP
             on
               EC.ENTRY_ID = EP.ENTRY_ID and EC.ENTRY_TYPE = EP.ENTRY_TYPE)
);
