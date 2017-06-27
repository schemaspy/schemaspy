CREATE TABLE groups (
  gid INTEGER AUTO_INCREMENT,
  name VARCHAR(16) NOT NULL,
  description VARCHAR(80) NOT NULL,
  PRIMARY KEY (gid),
  UNIQUE name_unique (name)
) engine=InnoDB COMMENT 'Groups';
CREATE TABLE users (
  uid INTEGER AUTO_INCREMENT,
  gid INTEGER NOT NULL,
  name VARCHAR(16) NOT NULL COMMENT 'UserName',
  email VARCHAR(40) NOT NULL,
  PRIMARY KEY (uid, gid),
  UNIQUE email_unique (email),
  FOREIGN KEY groups_gid_fk (gid) REFERENCES groups(gid)
) engine=InnoDB COMMENT 'Users of the system';
CREATE TABLE resources (
  rid INTEGER NOT NULL,
  name VARCHAR(40) NOT NULL,
  description VARCHAR(80) NOT NULL,
  PRIMARY KEY (rid),
  UNIQUE name_unique(name)
) engine=InnoDB COMMENT 'Resources';
CREATE TABLE group_resources (
  gid INTEGER NOT NULL,
  rid INTEGER NOT NULL,
  access ENUM('n','r','rw') DEFAULT 'n',
  PRIMARY KEY (gid, rid),
  FOREIGN KEY groups_gid_fk(gid) REFERENCES groups(gid),
  FOREIGN KEY resource_rid_fk(rid) REFERENCES resources(rid)
) engine=InnoDB COMMENT 'Group access to resource';