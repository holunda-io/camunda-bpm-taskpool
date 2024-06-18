create view PLF_VIEW_DATA_ENTRY_PAYLOAD as
(
select *
from PLF_DATA_ENTRY_PAYLOAD_ATTRIBUTES
union
(select ec.OWNING_ENTRY_ID   as ENTRY_ID,
        ec.OWNING_ENTRY_TYPE as ENTRY_TYPE,
        ep.path              as PATH,
        ep.value as VALUE
 from PLF_DATA_ENTRY_CORRELATIONS ec
   join PLF_DATA_ENTRY_PAYLOAD_ATTRIBUTES ep
 on
   ec.ENTRY_ID = ep.ENTRY_ID and ec.ENTRY_TYPE = ep.ENTRY_TYPE)
)
