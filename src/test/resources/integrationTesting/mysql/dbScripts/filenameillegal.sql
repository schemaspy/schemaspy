CREATE DATABASE `illegal`;

CREATE TABLE `illegal`.`グループ` (
  groupId INTEGER AUTO_INCREMENT,
  name VARCHAR(16) NOT NULL,
  description VARCHAR(80) NOT NULL,
  PRIMARY KEY (groupId),
  UNIQUE name_unique (name)
) engine=InnoDB COMMENT 'Groups';
CREATE TABLE `illegal`.`us/er` (
  userId INTEGER AUTO_INCREMENT,
  groupId INTEGER NOT NULL COMMENT 'FK to groups omitted, implied relationship',
  name VARCHAR(16) NOT NULL COMMENT 'UserName',
  email VARCHAR(40) NOT NULL,
  PRIMARY KEY (userId),
  UNIQUE email_unique (email)
) engine=InnoDB COMMENT 'Users of the system';

CREATE VIEW `illegal`.userAndGroup AS SELECT u.name AS UserName, g.name AS GroupName FROM `illegal`.`us/er` u LEFT JOIN `illegal`.`グループ` g ON u.groupId = g.groupId;

CREATE FUNCTION `illegal`.`いいえ_det` (s CHAR(20))
RETURNS CHAR(50) NOT DETERMINISTIC
RETURN CONCAT('Hello, ',s,'!');

CREATE DEFINER = 'test'@'%' FUNCTION `illegal`.`はい_det` (s CHAR(20))
RETURNS CHAR(50) DETERMINISTIC COMMENT 'is deterministic'
RETURN CONCAT('Hello, ',s,'!');

GRANT SELECT, EXECUTE, SHOW VIEW on `illegal`.* to test@`%`;
FLUSH PRIVILEGES;