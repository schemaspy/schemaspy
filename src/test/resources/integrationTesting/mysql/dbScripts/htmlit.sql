CREATE DATABASE `htmlit`;

CREATE TABLE `htmlit`.`group` (
  groupId INTEGER AUTO_INCREMENT,
  name VARCHAR(16) NOT NULL,
  description VARCHAR(80) NOT NULL,
  PRIMARY KEY (groupId),
  UNIQUE name_unique (name)
) engine=InnoDB COMMENT 'Groups';
CREATE TABLE `htmlit`.`user` (
  userId INTEGER AUTO_INCREMENT,
  groupId INTEGER NOT NULL COMMENT 'FK to groups omitted, implied relationship',
  name VARCHAR(16) NOT NULL COMMENT 'UserName',
  email VARCHAR(40) NOT NULL,
  PRIMARY KEY (userId),
  UNIQUE email_unique (email)
) engine=InnoDB COMMENT 'Users of the system';

CREATE VIEW `htmlit`.userAndGroup AS SELECT u.name AS UserName, g.name AS GroupName FROM `htmlit`.`user` u LEFT JOIN `htmlit`.`group` g ON u.groupId = g.groupId;

CREATE FUNCTION `htmlit`.no_det (s CHAR(20))
RETURNS CHAR(50) NOT DETERMINISTIC
RETURN CONCAT('Hello, ',s,'!');

CREATE DEFINER = 'test'@'%' FUNCTION `htmlit`.yes_det (s CHAR(20))
RETURNS CHAR(50) DETERMINISTIC COMMENT 'is deterministic'
RETURN CONCAT('Hello, ',s,'!');

GRANT SELECT, EXECUTE, SHOW VIEW on `htmlit`.* to test@`%`;
FLUSH PRIVILEGES;