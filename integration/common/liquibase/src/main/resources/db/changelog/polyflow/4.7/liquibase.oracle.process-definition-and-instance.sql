alter table PLF_PROC_DEF add (STATE varchar2(32));
update PLF_PROC_DEF set STATE = 'ACTIVE' where STATE is null;
alter table PLF_PROC_DEF modify (STATE varchar2(32) not null);
alter table PLF_PROC_DEF add (PAYLOAD blob);
alter table PLF_PROC_INSTANCE add (PAYLOAD blob);
alter table PLF_PROC_INSTANCE add (CORRELATIONS blob);
