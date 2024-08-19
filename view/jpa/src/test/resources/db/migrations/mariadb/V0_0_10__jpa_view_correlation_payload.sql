create view plf_view_task_and_data_entry_payload as
((select pc.task_id, dea.path, dea.value
  from plf_task_correlations pc
         join plf_data_entry_payload_attributes dea on pc.entry_id = dea.entry_id and pc.entry_type = dea.entry_type)
union
select * from plf_task_payload_attributes);
