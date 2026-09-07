create table plf_data_entry
(
  entry_id           varchar(255) not null,
  entry_type         varchar(255) not null,
  application_name   varchar(255) not null,
  date_created       datetime(6) not null,
  date_deleted       datetime(6),
  description        varchar(2048),
  form_key           varchar(255),
  date_last_modified datetime(6) not null,
  name               varchar(255) not null,
  payload            longtext,
  revision           bigint,
  processing_type    varchar(255) not null,
  state              varchar(255) not null,
  type               varchar(255) not null,
  primary key (entry_id, entry_type)
);

create table plf_data_entry_authorizations
(
  entry_id             varchar(255) not null,
  entry_type           varchar(255) not null,
  authorized_principal varchar(255) not null,
  primary key (entry_id, entry_type, authorized_principal)
);

create table plf_data_entry_payload_attributes
(
  entry_id   varchar(64)  not null,
  entry_type varchar(128) not null,
  path       varchar(255) not null,
  value      varchar(128) not null,
  primary key (entry_id, entry_type, path, value)
);

create table plf_data_entry_protocol
(
  id              varchar(255) not null,
  log_details     varchar(255),
  log_message     varchar(255),
  processing_type varchar(255) not null,
  state           varchar(255) not null,
  time            datetime(6) not null,
  username        varchar(255),
  entry_id        varchar(255) not null,
  entry_type      varchar(255) not null,
  primary key (id)
);

create table plf_proc_def
(
  proc_def_id             varchar(255) not null,
  application_name        varchar(255) not null,
  description             varchar(2048),
  name                    varchar(255) not null,
  proc_def_key            varchar(255) not null,
  proc_def_version        integer      not null,
  start_form_key          varchar(255),
  startable_from_tasklist bit,
  version_tag             varchar(255),
  primary key (proc_def_id)
);

create table plf_proc_def_authorizations
(
  proc_def_id                  varchar(255) not null,
  authorized_starter_principal varchar(255) not null,
  primary key (proc_def_id, authorized_starter_principal)
);

create table plf_proc_instance
(
  instance_id         varchar(255) not null,
  business_key        varchar(255),
  delete_reason       varchar(255),
  end_activity_id     varchar(255),
  application_name    varchar(255) not null,
  source_def_id       varchar(255) not null,
  source_def_key      varchar(255) not null,
  source_execution_id varchar(255) not null,
  source_instance_id  varchar(255) not null,
  source_name         varchar(255) not null,
  source_type         varchar(255) not null,
  source_tenant_id    varchar(255),
  start_activity_id   varchar(255),
  start_user_id       varchar(255),
  run_state           varchar(255) not null,
  super_instance_id   varchar(255),
  primary key (instance_id)
);

create table plf_task
(
  task_id             varchar(255) not null,
  assignee_id         varchar(255),
  business_key        varchar(255),
  date_created        datetime(6) not null,
  description         varchar(2048),
  date_due            datetime(6),
  date_follow_up      datetime(6),
  form_key            varchar(255),
  name                varchar(255) not null,
  owner_id            varchar(255),
  payload             longtext,
  priority            integer,
  application_name    varchar(255) not null,
  source_def_id       varchar(255) not null,
  source_def_key      varchar(255) not null,
  source_execution_id varchar(255) not null,
  source_instance_id  varchar(255) not null,
  source_name         varchar(255) not null,
  source_type         varchar(255) not null,
  source_tenant_id    varchar(255),
  task_def_key        varchar(255) not null,
  primary key (task_id)
);

create table plf_task_authorizations
(
  task_id              varchar(255) not null,
  authorized_principal varchar(255) not null,
  primary key (task_id, authorized_principal)
);

create table plf_task_correlations
(
  task_id    varchar(255) not null,
  entry_id   varchar(255) not null,
  entry_type varchar(255) not null,
  primary key (task_id, entry_id, entry_type)
);

create table plf_task_payload_attributes
(
  task_id varchar(255) not null,
  path    varchar(255) not null,
  value   varchar(255) not null,
  primary key (task_id, path, value)
);

create table PLF_DATA_ENTRY_CORRELATIONS
(
    OWNING_ENTRY_TYPE varchar(255) not null,
    OWNING_ENTRY_ID   varchar(64)  not null,
    ENTRY_TYPE        varchar(255) not null,
    ENTRY_ID          varchar(64)  not null,
    primary key (OWNING_ENTRY_TYPE, OWNING_ENTRY_ID, ENTRY_TYPE, ENTRY_ID)
);
alter table PLF_DATA_ENTRY add column VERSION_TIMESTAMP INT8;
create view PLF_VIEW_TASK_AND_DATA_ENTRY_PAYLOAD as
((select pc.TASK_ID, dea.PATH, dea.VALUE
  from PLF_TASK_CORRELATIONS pc
         join PLF_DATA_ENTRY_PAYLOAD_ATTRIBUTES dea on pc.ENTRY_ID = dea.ENTRY_ID and pc.ENTRY_TYPE = dea.ENTRY_TYPE)
union
select * from PLF_TASK_PAYLOAD_ATTRIBUTES);

create view PLF_VIEW_DATA_ENTRY_PAYLOAD as
(
select *
from PLF_DATA_ENTRY_PAYLOAD_ATTRIBUTES
union
(select ec.OWNING_ENTRY_ID   as ENTRY_ID,
        ec.OWNING_ENTRY_TYPE as ENTRY_TYPE,
        ep.path              as PATH,
        ep.value             as VALUE
 from PLF_DATA_ENTRY_CORRELATIONS ec
   join PLF_DATA_ENTRY_PAYLOAD_ATTRIBUTES ep
 on
   ec.ENTRY_ID = ep.ENTRY_ID and ec.ENTRY_TYPE = ep.ENTRY_TYPE)
);
alter table plf_data_entry_authorizations
  add constraint FKayik2ifqf0heiwvgs0ts1pwl5
    foreign key (entry_id, entry_type)
      references plf_data_entry (entry_id, entry_type);
alter table plf_data_entry_payload_attributes
  add constraint FKrsbl8su01epxoepmn8h761dnt
    foreign key (entry_id, entry_type)
      references plf_data_entry (entry_id, entry_type);
alter table plf_data_entry_protocol
  add constraint FKh2e52nrdrr6x8fpu7n040760h
    foreign key (entry_id, entry_type)
      references plf_data_entry (entry_id, entry_type);
alter table plf_proc_def_authorizations
  add constraint FKe2ic13vswywsbaj8u3gr0px0k
    foreign key (proc_def_id)
      references plf_proc_def (proc_def_id);
alter table plf_task_authorizations
  add constraint FKi2132ws8a4ruun3iysbog3uc7
    foreign key (task_id)
      references plf_task (task_id);
alter table plf_task_correlations
  add constraint FKe19ae2b8uib1r0jpvl4obdopl
    foreign key (task_id)
      references plf_task (task_id);
alter table plf_task_payload_attributes
  add constraint FK44yjugbfiv7y92657vp868l7l
    foreign key (task_id)
      references plf_task (task_id);
