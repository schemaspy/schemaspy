CREATE DATABASE `htmlorphanit`;

CREATE TABLE `htmlorphanit`.`group` (
  groupId INTEGER AUTO_INCREMENT,
  name VARCHAR(16) NOT NULL,
  description VARCHAR(80) NOT NULL,
  PRIMARY KEY (groupId),
  UNIQUE name_unique (name)
) engine=InnoDB COMMENT 'Orphan Groups';

GRANT SELECT, EXECUTE, SHOW VIEW on `htmlorphanit`.* to test@`%`;
FLUSH PRIVILEGES;