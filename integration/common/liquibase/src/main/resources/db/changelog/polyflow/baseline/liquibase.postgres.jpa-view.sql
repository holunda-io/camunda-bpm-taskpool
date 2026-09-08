CREATE TABLE plf_data_entry
(
  entry_id           VARCHAR(255) NOT NULL,
  entry_type         VARCHAR(255) NOT NULL,
  application_name   VARCHAR(255) NOT NULL,
  date_created       TIMESTAMP    NOT NULL,
  description        VARCHAR(2048),
  form_key           VARCHAR(255),
  date_last_modified TIMESTAMP    NOT NULL,
  name               VARCHAR(255) NOT NULL,
  payload            OID,
  revision           INT8,
  processing_type    VARCHAR(255) NOT NULL,
  state              VARCHAR(255) NOT NULL,
  type               VARCHAR(255) NOT NULL,
  date_deleted       TIMESTAMP,
  version_timestamp  INT8,
  PRIMARY KEY (entry_id, entry_type)
);

CREATE TABLE plf_data_entry_authorizations
(
  entry_id             VARCHAR(255) NOT NULL,
  entry_type           VARCHAR(255) NOT NULL,
  authorized_principal VARCHAR(255) NOT NULL,
  PRIMARY KEY (entry_id, entry_type, authorized_principal)
);

CREATE TABLE plf_data_entry_payload_attributes
(
  entry_id   VARCHAR(255) NOT NULL,
  entry_type VARCHAR(255) NOT NULL,
  path       VARCHAR(255) NOT NULL,
  value      VARCHAR(255) NOT NULL,
  PRIMARY KEY (entry_id, entry_type, path, value)
);

CREATE TABLE plf_data_entry_protocol
(
  id                VARCHAR(255) NOT NULL,
  log_details       VARCHAR(2048),
  log_message       VARCHAR(2048),
  processing_type   VARCHAR(255) NOT NULL,
  state             VARCHAR(255) NOT NULL,
  time              TIMESTAMP    NOT NULL,
  username          VARCHAR(255),
  entry_id          VARCHAR(255) NOT NULL,
  entry_type        VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE plf_proc_def
(
  proc_def_id             VARCHAR(255) NOT NULL,
  application_name        VARCHAR(255) NOT NULL,
  description             VARCHAR(2048),
  name                    VARCHAR(255) NOT NULL,
  proc_def_key            VARCHAR(255) NOT NULL,
  proc_def_version        INT4         NOT NULL,
  start_form_key          VARCHAR(255),
  startable_from_tasklist BOOLEAN,
  version_tag             VARCHAR(255),
  PRIMARY KEY (proc_def_id)
);

CREATE TABLE plf_proc_def_authorizations
(
  proc_def_id                  VARCHAR(255) NOT NULL,
  authorized_starter_principal VARCHAR(255) NOT NULL,
  PRIMARY KEY (proc_def_id, authorized_starter_principal)
);

CREATE TABLE plf_proc_instance
(
  instance_id         VARCHAR(255) NOT NULL,
  business_key        VARCHAR(255),
  delete_reason       VARCHAR(2048),
  end_activity_id     VARCHAR(255),
  application_name    VARCHAR(255) NOT NULL,
  source_def_id       VARCHAR(255) NOT NULL,
  source_def_key      VARCHAR(255) NOT NULL,
  source_execution_id VARCHAR(255) NOT NULL,
  source_instance_id  VARCHAR(255) NOT NULL,
  source_name         VARCHAR(255) NOT NULL,
  source_type         VARCHAR(255) NOT NULL,
  source_tenant_id    VARCHAR(255),
  start_activity_id   VARCHAR(255),
  start_user_id       VARCHAR(255),
  run_state           VARCHAR(255) NOT NULL,
  super_instance_id   VARCHAR(255),
  PRIMARY KEY (instance_id)
);

CREATE TABLE plf_task
(
  task_id             VARCHAR(255) NOT NULL,
  assignee_id         VARCHAR(255),
  business_key        VARCHAR(255),
  date_created        TIMESTAMP    NOT NULL,
  description         VARCHAR(2048),
  date_due            TIMESTAMP,
  date_follow_up      TIMESTAMP,
  form_key            VARCHAR(255),
  name                VARCHAR(255) NOT NULL,
  owner_id            VARCHAR(255),
  payload             OID,
  priority            INT4,
  application_name    VARCHAR(255) NOT NULL,
  source_def_id       VARCHAR(255) NOT NULL,
  source_def_key      VARCHAR(255) NOT NULL,
  source_execution_id VARCHAR(255) NOT NULL,
  source_instance_id  VARCHAR(255) NOT NULL,
  source_name         VARCHAR(255) NOT NULL,
  source_type         VARCHAR(255) NOT NULL,
  source_tenant_id    VARCHAR(255),
  task_def_key        VARCHAR(255) NOT NULL,
  PRIMARY KEY (task_id)
);

CREATE TABLE plf_task_authorizations
(
  task_id              VARCHAR(255) NOT NULL,
  authorized_principal VARCHAR(255) NOT NULL,
  PRIMARY KEY (task_id, authorized_principal)
);

CREATE TABLE plf_task_correlations
(
  task_id    VARCHAR(255) NOT NULL,
  entry_id   VARCHAR(255) NOT NULL,
  entry_type VARCHAR(255) NOT NULL,
  PRIMARY KEY (task_id, entry_id, entry_type)
);

CREATE TABLE plf_task_payload_attributes
(
  task_id VARCHAR(255) NOT NULL,
  path    VARCHAR(255) NOT NULL,
  value   VARCHAR(255) NOT NULL,
  PRIMARY KEY (task_id, path, value)
);

create table plf_data_entry_correlations
(
  owning_entry_type varchar(255) not null,
  owning_entry_id   varchar(64)  not null,
  entry_type        varchar(255) not null,
  entry_id          varchar(64)  not null,
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
