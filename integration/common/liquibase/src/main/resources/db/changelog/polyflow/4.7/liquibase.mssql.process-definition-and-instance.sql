alter table PLF_PROC_DEF add STATE varchar(32) null;
update PLF_PROC_DEF set STATE = 'ACTIVE' where STATE is null;
alter table PLF_PROC_DEF alter column STATE varchar(32) not null;
alter table PLF_PROC_DEF add PAYLOAD varbinary(max) null;
alter table PLF_PROC_INSTANCE add PAYLOAD varbinary(max) null;
alter table PLF_PROC_INSTANCE add CORRELATIONS varbinary(max) null;
