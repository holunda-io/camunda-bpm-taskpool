create table PLF_DATA_ENTRY_CORRELATIONS
(
    OWNING_ENTRY_TYPE varchar(255) not null,
    OWNING_ENTRY_ID   varchar(64)  not null,
    ENTRY_TYPE        varchar(255) not null,
    ENTRY_ID          varchar(64)  not null,
    primary key (OWNING_ENTRY_TYPE, OWNING_ENTRY_ID, ENTRY_TYPE, ENTRY_ID)
);

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
