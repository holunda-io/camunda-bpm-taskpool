CREATE TABLE plf_data_entry
(
  entry_id           varchar2(255) NOT NULL,
  entry_type         varchar2(255) NOT NULL,
  application_name   varchar2(255) NOT NULL,
  date_created       timestamp    NOT NULL,
  description        varchar2(2048),
  form_key           varchar2(255),
  date_last_modified timestamp    NOT NULL,
  name               varchar2(255) NOT NULL,
  payload            blob,
  revision           number(19),
  processing_type    varchar2(255) NOT NULL,
  state              varchar2(255) NOT NULL,
  type               varchar2(255) NOT NULL,
  date_deleted       timestamp,
  version_timestamp  number(19),
  PRIMARY KEY (entry_id, entry_type)
);

CREATE TABLE plf_data_entry_authorizations
(
  entry_id             varchar2(255) NOT NULL,
  entry_type           varchar2(255) NOT NULL,
  authorized_principal varchar2(255) NOT NULL,
  PRIMARY KEY (entry_id, entry_type, authorized_principal)
);

CREATE TABLE plf_data_entry_payload_attributes
(
  entry_id   varchar2(255) NOT NULL,
  entry_type varchar2(255) NOT NULL,
  path       varchar2(255) NOT NULL,
  value      varchar2(255) NOT NULL,
  PRIMARY KEY (entry_id, entry_type, path, value)
);

CREATE TABLE plf_data_entry_protocol
(
  id                varchar2(255) NOT NULL,
  log_details       varchar2(2048),
  log_message       varchar2(2048),
  processing_type   varchar2(255) NOT NULL,
  state             varchar2(255) NOT NULL,
  time              timestamp    NOT NULL,
  username          varchar2(255),
  entry_id          varchar2(255) NOT NULL,
  entry_type        varchar2(255) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE plf_proc_def
(
  proc_def_id             varchar2(255) NOT NULL,
  application_name        varchar2(255) NOT NULL,
  description             varchar2(2048),
  name                    varchar2(255) NOT NULL,
  proc_def_key            varchar2(255) NOT NULL,
  proc_def_version        number(10)         NOT NULL,
  start_form_key          varchar2(255),
  startable_from_tasklist number(1),
  version_tag             varchar2(255),
  PRIMARY KEY (proc_def_id)
);

CREATE TABLE plf_proc_def_authorizations
(
  proc_def_id                  varchar2(255) NOT NULL,
  authorized_starter_principal varchar2(255) NOT NULL,
  PRIMARY KEY (proc_def_id, authorized_starter_principal)
);

CREATE TABLE plf_proc_instance
(
  instance_id         varchar2(255) NOT NULL,
  business_key        varchar2(255),
  delete_reason       varchar2(2048),
  end_activity_id     varchar2(255),
  application_name    varchar2(255) NOT NULL,
  source_def_id       varchar2(255) NOT NULL,
  source_def_key      varchar2(255) NOT NULL,
  source_execution_id varchar2(255) NOT NULL,
  source_instance_id  varchar2(255) NOT NULL,
  source_name         varchar2(255) NOT NULL,
  source_type         varchar2(255) NOT NULL,
  source_tenant_id    varchar2(255),
  start_activity_id   varchar2(255),
  start_user_id       varchar2(255),
  run_state           varchar2(255) NOT NULL,
  super_instance_id   varchar2(255),
  PRIMARY KEY (instance_id)
);

CREATE TABLE plf_task
(
  task_id             varchar2(255) NOT NULL,
  assignee_id         varchar2(255),
  business_key        varchar2(255),
  date_created        timestamp    NOT NULL,
  description         varchar2(2048),
  date_due            timestamp,
  date_follow_up      timestamp,
  form_key            varchar2(255),
  name                varchar2(255) NOT NULL,
  owner_id            varchar2(255),
  payload             blob,
  priority            number(10),
  application_name    varchar2(255) NOT NULL,
  source_def_id       varchar2(255) NOT NULL,
  source_def_key      varchar2(255) NOT NULL,
  source_execution_id varchar2(255) NOT NULL,
  source_instance_id  varchar2(255) NOT NULL,
  source_name         varchar2(255) NOT NULL,
  source_type         varchar2(255) NOT NULL,
  source_tenant_id    varchar2(255),
  task_def_key        varchar2(255) NOT NULL,
  PRIMARY KEY (task_id)
);

CREATE TABLE plf_task_authorizations
(
  task_id              varchar2(255) NOT NULL,
  authorized_principal varchar2(255) NOT NULL,
  PRIMARY KEY (task_id, authorized_principal)
);

CREATE TABLE plf_task_correlations
(
  task_id    varchar2(255) NOT NULL,
  entry_id   varchar2(255) NOT NULL,
  entry_type varchar2(255) NOT NULL,
  PRIMARY KEY (task_id, entry_id, entry_type)
);

CREATE TABLE plf_task_payload_attributes
(
  task_id varchar2(255) NOT NULL,
  path    varchar2(255) NOT NULL,
  value   varchar2(255) NOT NULL,
  PRIMARY KEY (task_id, path, value)
);

create table plf_data_entry_correlations
(
  owning_entry_type varchar2(255) not null,
  owning_entry_id   varchar2(64)  not null,
  entry_type        varchar2(255) not null,
  entry_id          varchar2(64)  not null,
  primary key (owning_entry_type, owning_entry_id, entry_type, entry_id)
);

create view plf_view_task_and_data_entry_payload as
(
(select pc.task_id, dea.path, dea.value
 from plf_task_correlations pc
        join plf_data_entry_payload_attributes dea on pc.entry_id = dea.entry_id AND pc.entry_type = dea.entry_type)
union
select *
from plf_task_payload_attributes);

create view plf_view_data_entry_payload as
(
select *
from plf_data_entry_payload_attributes
union
(select ec.owning_entry_id   as entry_id,
        ec.owning_entry_type as entry_type,
        ep.path              as path,
        ep.value as value
 from plf_data_entry_correlations ec
   join plf_data_entry_payload_attributes ep
 on
   ec.entry_id = ep.entry_id and ec.entry_type = ep.entry_type)
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
