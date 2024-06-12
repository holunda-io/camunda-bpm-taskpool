CREATE TABLE plf_view_task_and_data_entry_payload (
  task_id   VARCHAR(64) NOT NULL,
  path      VARCHAR(255) NOT NULL,
  value     VARCHAR(255) NOT NULL,
  PRIMARY KEY (task_id, path, value)
);

ALTER TABLE plf_view_task_and_data_entry_payload
  ADD CONSTRAINT FK_view_task_and_data_entry_payload_have_task
    FOREIGN KEY (task_id)
      REFERENCES plf_task;
