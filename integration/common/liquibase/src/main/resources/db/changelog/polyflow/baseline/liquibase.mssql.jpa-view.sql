create table PLF_DATA_ENTRY
(
  ENTRY_ID           NVARCHAR(255) not null,
  ENTRY_TYPE         NVARCHAR(255) not null,
  APPLICATION_NAME   NVARCHAR(255) not null,
  DATE_CREATED       DATETIME2    not null,
  DESCRIPTION        NVARCHAR(2048),
  FORM_KEY           NVARCHAR(255),
  DATE_LAST_MODIFIED DATETIME2    not null,
  NAME               NVARCHAR(255) not null,
  PAYLOAD            varbinary(max),
  REVISION           bigint,
  PROCESSING_TYPE    NVARCHAR(255) not null,
  STATE              NVARCHAR(255) not null,
  TYPE               NVARCHAR(255) not null,
  DATE_DELETED       DATETIME2,
  VERSION_TIMESTAMP  bigint,
  constraint PK_DATA_ENTRY primary key (ENTRY_ID, ENTRY_TYPE)
);

create table PLF_DATA_ENTRY_AUTHORIZATIONS
(
  ENTRY_ID             NVARCHAR(255) not null,
  ENTRY_TYPE           NVARCHAR(255) not null,
  AUTHORIZED_PRINCIPAL NVARCHAR(255) not null,
  constraint PK_DATA_ENTRY_AUTH primary key (ENTRY_ID, ENTRY_TYPE, AUTHORIZED_PRINCIPAL),
  constraint FK_DATA_ENTRY_AUTH_ENTRY
    foreign key (ENTRY_ID, ENTRY_TYPE)
    references PLF_DATA_ENTRY
);

create table PLF_DATA_ENTRY_PAYLOAD_ATTRIBUTES
(
  ENTRY_ID   NVARCHAR(255) not null,
  ENTRY_TYPE NVARCHAR(255) not null,
  PATH       NVARCHAR(255) not null,
  VALUE      NVARCHAR(255) not null,
  constraint PK_DATA_ENTRY_ATTR primary key (ENTRY_ID, ENTRY_TYPE, PATH, VALUE),
  constraint FK_DATA_ENTRY_ATTR_ENTRY
    foreign key (ENTRY_ID, ENTRY_TYPE)
    references PLF_DATA_ENTRY
);

create table PLF_DATA_ENTRY_PROTOCOL
(
  ID                NVARCHAR(255) not null,
  LOG_DETAILS       NVARCHAR(2048),
  LOG_MESSAGE       NVARCHAR(2048),
  PROCESSING_TYPE   NVARCHAR(255) not null,
  STATE             NVARCHAR(255) not null,
  TIME              DATETIME2    not null,
  USERNAME          NVARCHAR(255),
  ENTRY_ID          NVARCHAR(255) not null,
  ENTRY_TYPE        NVARCHAR(255) not null,
  constraint PK_DATA_ENTRY_PROTOCOL primary key (ID),
  constraint FK_DATA_ENTRY_PROTOCOL_ENTRY
    foreign key (ENTRY_ID, ENTRY_TYPE)
    references PLF_DATA_ENTRY
);

create table PLF_PROC_DEF
(
  PROC_DEF_ID             NVARCHAR(255) not null,
  APPLICATION_NAME        NVARCHAR(255) not null,
  DESCRIPTION             NVARCHAR(2048),
  NAME                    NVARCHAR(255) not null,
  PROC_DEF_KEY            NVARCHAR(255) not null,
  PROC_DEF_VERSION        int         not null,
  START_FORM_KEY          NVARCHAR(255),
  STARTABLE_FROM_TASKLIST bit,
  VERSION_TAG             NVARCHAR(255),
  constraint PK_PROC_DEF primary key (PROC_DEF_ID)
);

create table PLF_PROC_DEF_AUTHORIZATIONS
(
  PROC_DEF_ID                  NVARCHAR(255) not null,
  AUTHORIZED_STARTER_PRINCIPAL NVARCHAR(255) not null,
  constraint PK_PROC_DEF_AUTH primary key (PROC_DEF_ID, AUTHORIZED_STARTER_PRINCIPAL),
  constraint FK_PROC_DEF_AUTH_PROC_DEF
    foreign key (PROC_DEF_ID)
    references PLF_PROC_DEF
);

create table PLF_PROC_INSTANCE
(
  INSTANCE_ID         NVARCHAR(255) not null,
  BUSINESS_KEY        NVARCHAR(255),
  DELETE_REASON       NVARCHAR(2048),
  END_ACTIVITY_ID     NVARCHAR(255),
  APPLICATION_NAME    NVARCHAR(255) not null,
  SOURCE_DEF_ID       NVARCHAR(255) not null,
  SOURCE_DEF_KEY      NVARCHAR(255) not null,
  SOURCE_EXECUTION_ID NVARCHAR(255) not null,
  SOURCE_INSTANCE_ID  NVARCHAR(255) not null,
  SOURCE_NAME         NVARCHAR(255) not null,
  SOURCE_TYPE         NVARCHAR(255) not null,
  SOURCE_TENANT_ID    NVARCHAR(255),
  START_ACTIVITY_ID   NVARCHAR(255),
  START_USER_ID       NVARCHAR(255),
  RUN_STATE           NVARCHAR(255) not null,
  SUPER_INSTANCE_ID   NVARCHAR(255),
  constraint PK_PROC_INSTANCE primary key (INSTANCE_ID)
);

create table PLF_TASK
(
  TASK_ID             NVARCHAR(255) not null,
  ASSIGNEE_ID         NVARCHAR(255),
  BUSINESS_KEY        NVARCHAR(255),
  DATE_CREATED        DATETIME2    not null,
  DESCRIPTION         NVARCHAR(2048),
  DATE_DUE            DATETIME2,
  DATE_FOLLOW_UP      DATETIME2,
  FORM_KEY            NVARCHAR(255),
  NAME                NVARCHAR(255) not null,
  OWNER_ID            NVARCHAR(255),
  PAYLOAD             varbinary(max),
  PRIORITY            int,
  APPLICATION_NAME    NVARCHAR(255) not null,
  SOURCE_DEF_ID       NVARCHAR(255) not null,
  SOURCE_DEF_KEY      NVARCHAR(255) not null,
  SOURCE_EXECUTION_ID NVARCHAR(255) not null,
  SOURCE_INSTANCE_ID  NVARCHAR(255) not null,
  SOURCE_NAME         NVARCHAR(255) not null,
  SOURCE_TYPE         NVARCHAR(255) not null,
  SOURCE_TENANT_ID    NVARCHAR(255),
  TASK_DEF_KEY        NVARCHAR(255) not null,
  constraint PK_TASK primary key (TASK_ID)
);

create table PLF_TASK_AUTHORIZATIONS
(
  TASK_ID              NVARCHAR(255) not null,
  AUTHORIZED_PRINCIPAL NVARCHAR(255) not null,
  constraint PK_TASK_AUTH primary key (TASK_ID, AUTHORIZED_PRINCIPAL),
  constraint FK_TASK_AUTH_TASK
    foreign key (TASK_ID)
    references PLF_TASK
);

create table PLF_TASK_CORRELATIONS
(
  TASK_ID    NVARCHAR(255) not null,
  ENTRY_ID   NVARCHAR(255) not null,
  ENTRY_TYPE NVARCHAR(255) not null,
  constraint PK_TASK_CORRELATION primary key (TASK_ID, ENTRY_ID, ENTRY_TYPE),
  constraint FK_TASK_CORRELATION_TASK
    foreign key (TASK_ID)
    references PLF_TASK
);

create table PLF_TASK_PAYLOAD_ATTRIBUTES
(
  TASK_ID NVARCHAR(255) not null,
  PATH    NVARCHAR(255) not null,
  VALUE   NVARCHAR(255) not null,
  constraint PK_TASK_ATTR primary key (TASK_ID, PATH, VALUE),
  constraint FK_TASK_ATTR_TASK
    foreign key (TASK_ID)
    references PLF_TASK
);

create table PLF_DATA_ENTRY_CORRELATIONS
(
  OWNING_ENTRY_TYPE NVARCHAR(255) not null,
  OWNING_ENTRY_ID   NVARCHAR(64)  not null,
  ENTRY_TYPE        NVARCHAR(255) not null,
  ENTRY_ID          NVARCHAR(64)  not null,
  constraint PK_DATA_ENTRY_CORRELATION primary key (OWNING_ENTRY_TYPE, OWNING_ENTRY_ID, ENTRY_TYPE, ENTRY_ID)
);

create view PLF_VIEW_TASK_AND_DATA_ENTRY_PAYLOAD as
(
(select PC.TASK_ID, DEA.PATH, DEA.VALUE
 from PLF_TASK_CORRELATIONS PC
        join PLF_DATA_ENTRY_PAYLOAD_ATTRIBUTES DEA on PC.ENTRY_ID = DEA.ENTRY_ID and PC.ENTRY_TYPE = DEA.ENTRY_TYPE)
union
select TASK_ID, PATH, VALUE
from PLF_TASK_PAYLOAD_ATTRIBUTES);

create view PLF_VIEW_DATA_ENTRY_PAYLOAD as
(
select ENTRY_ID, ENTRY_TYPE, PATH, VALUE
from PLF_DATA_ENTRY_PAYLOAD_ATTRIBUTES
union
(select EC.OWNING_ENTRY_ID   as ENTRY_ID,
        EC.OWNING_ENTRY_TYPE as ENTRY_TYPE,
        EP.PATH              as PATH,
        EP.VALUE as VALUE
 from PLF_DATA_ENTRY_CORRELATIONS EC
   join PLF_DATA_ENTRY_PAYLOAD_ATTRIBUTES EP
 on
   EC.ENTRY_ID = EP.ENTRY_ID and EC.ENTRY_TYPE = EP.ENTRY_TYPE)
);
