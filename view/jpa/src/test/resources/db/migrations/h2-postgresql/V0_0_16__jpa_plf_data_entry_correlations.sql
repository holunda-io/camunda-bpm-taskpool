create table plf_data_entry_correlations
(
    OWNING_ENTRY_TYPE varchar(255) not null,
    OWNING_ENTRY_ID   varchar(64)  not null,
    ENTRY_TYPE        varchar(255) not null,
    ENTRY_ID          varchar(64)  not null,
    primary key (OWNING_ENTRY_TYPE, OWNING_ENTRY_ID, ENTRY_TYPE, ENTRY_ID)
);

