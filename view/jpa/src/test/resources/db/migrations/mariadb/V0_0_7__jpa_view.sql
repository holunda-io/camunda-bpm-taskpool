
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
  version_timestamp  bigint,
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

ALTER TABLE plf_data_entry_authorizations
  ADD CONSTRAINT FK_authorizations_have_data_entry
    FOREIGN KEY (entry_id, entry_type)
      REFERENCES plf_data_entry;

ALTER TABLE plf_data_entry_payload_attributes
  ADD CONSTRAINT FK_payload_attributes_have_data_entry
    FOREIGN KEY (entry_id, entry_type)
      REFERENCES plf_data_entry;

ALTER TABLE plf_data_entry_protocol
  ADD CONSTRAINT FK_protocol_have_data_entry
    FOREIGN KEY (entry_id, entry_type)
      REFERENCES plf_data_entry;

ALTER TABLE plf_proc_def_authorizations
  ADD CONSTRAINT FK_authorizations_have_proc_def
    FOREIGN KEY (proc_def_id)
      REFERENCES plf_proc_def;

ALTER TABLE plf_task_authorizations
  ADD CONSTRAINT FK_authorizations_have_task
    FOREIGN KEY (task_id)
      REFERENCES plf_task;

ALTER TABLE plf_task_correlations
  ADD CONSTRAINT FK_correlation_have_task
    FOREIGN KEY (task_id)
      REFERENCES plf_task;

ALTER TABLE plf_task_payload_attributes
  ADD CONSTRAINT FK_payload_attributes_have_task
    FOREIGN KEY (task_id)
      REFERENCES plf_task;
