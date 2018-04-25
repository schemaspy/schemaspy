CREATE DATABASE `routinesit`;

CREATE TABLE `routinesit`.issue222 (
  rid INTEGER NOT NULL
) engine=InnoDB COMMENT 'Only to avoid issue 222';

CREATE FUNCTION `routinesit`.no_det (s CHAR(20))
RETURNS CHAR(50) NOT DETERMINISTIC
RETURN CONCAT('Hello, ',s,'!');

CREATE FUNCTION `routinesit`.yes_det (s CHAR(20))
RETURNS CHAR(50) DETERMINISTIC
RETURN CONCAT('Hello, ',s,'!');

GRANT SELECT, EXECUTE on `routinesit`.* to test@`%`;
FLUSH PRIVILEGES;