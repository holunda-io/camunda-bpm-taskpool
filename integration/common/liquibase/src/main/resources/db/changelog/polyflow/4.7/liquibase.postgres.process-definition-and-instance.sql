alter table PLF_PROC_DEF add column STATE varchar(32);
update PLF_PROC_DEF set STATE = 'ACTIVE' where STATE is null;
alter table PLF_PROC_DEF alter column STATE set not null;
alter table PLF_PROC_DEF add column PAYLOAD oid;
alter table PLF_PROC_INSTANCE add column PAYLOAD oid;
alter table PLF_PROC_INSTANCE add column CORRELATIONS oid;
