-- All tasks filter just for admin
INSERT INTO ACT_RU_FILTER (ID_, REV_, RESOURCE_TYPE_, NAME_, OWNER_, QUERY_, PROPERTIES_)
VALUES ('F000', 1, 'Task', 'All Tasks', 'admin', '{}', '{"showUndefinedVariable":false,"description":"All tasks (for admin use only!)","refresh":true,"priority":0}');

INSERT INTO ACT_RU_AUTHORIZATION (ID_, REV_, TYPE_, GROUP_ID_, USER_ID_, RESOURCE_TYPE_, RESOURCE_ID_, PERMS_)
VALUES ('A20', 1, 1, null, 'admin', 5, 'F000', 2147483647);
