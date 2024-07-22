create table plf_data_entry_correlations
(
    owning_entry_type varchar(255) not null,
    owning_entry_id   varchar(64)  not null,
    entry_type        varchar(255) not null,
    entry_id          varchar(64)  not null,
    primary key (owning_entry_type, owning_entry_id, entry_type, entry_id)
);

create view plf_view_data_entry_payload as
(
select *
from plf_data_entry_payload_attributes
union
(select ec.owning_entry_id   as ENTRY_ID,
        ec.owning_entry_type as ENTRY_TYPE,
        ep.path              as PATH,
        ep.value             as VALUE
 from plf_data_entry_correlations ec
   join plf_data_entry_payload_attributes ep
 on
   ec.entry_id = ep.entry_id and ec.entry_type = ep.entry_type)
);
