CREATE TABLE `group` (
  groupId INTEGER AUTO_INCREMENT,
  name VARCHAR(16) NOT NULL,
  description VARCHAR(80) NOT NULL,
  PRIMARY KEY (groupId),
  UNIQUE name_unique (name)
) engine=InnoDB COMMENT 'Groups';
CREATE TABLE `user` (
  userId INTEGER AUTO_INCREMENT,
  groupId INTEGER NOT NULL COMMENT 'FK to groups omitted, implied relationship',
  name VARCHAR(16) NOT NULL COMMENT 'UserName',
  email VARCHAR(40) NOT NULL,
  PRIMARY KEY (userId),
  UNIQUE email_unique (email)
) engine=InnoDB COMMENT 'Users of the system';
create view userAndGroup as select u.name as UserName, g.name as GroupName from `user` u left join `group` g on u.groupId = g.groupId;
CREATE FUNCTION no_det (s CHAR(20))
RETURNS CHAR(50) NOT DETERMINISTIC
RETURN CONCAT('Hello, ',s,'!');

CREATE FUNCTION yes_det (s CHAR(20))
RETURNS CHAR(50) DETERMINISTIC
RETURN CONCAT('Hello, ',s,'!');