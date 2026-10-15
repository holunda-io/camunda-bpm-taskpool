alter table plf_proc_def add column STATE varchar(32);
update plf_proc_def set STATE = 'ACTIVE' where STATE is null;
alter table plf_proc_def modify column STATE varchar(32) not null;
alter table plf_proc_def add column PAYLOAD tinytext;
alter table plf_proc_instance add column PAYLOAD tinytext;
alter table plf_proc_instance add column CORRELATIONS tinytext;
