USE master;
CREATE DATABASE htmlit;
USE htmlit;
CREATE SCHEMA htmlit;

CREATE TABLE [htmlit].[group] (
  groupId int IDENTITY(1,1) NOT NULL,
  name VARCHAR(16) NOT NULL,
  description VARCHAR(80) NOT NULL,
  CONSTRAINT PK_groupId PRIMARY KEY (groupId),
  CONSTRAINT UQ_grp_name UNIQUE (name)
);

CREATE TABLE [htmlit].[user] (
  userId int IDENTITY(1,1) NOT NULL,
  groupId int NOT NULL,
  name VARCHAR(16) NOT NULL,
  email VARCHAR(40) NOT NULL,
  CONSTRAINT PK_userId PRIMARY KEY (userId),
  CONSTRAINT UQ_email UNIQUE (email)
);

CREATE TABLE [htmlit].[resources] (
  rid int NOT NULL,
  name VARCHAR(40) NOT NULL,
  description VARCHAR(80) NOT NULL,
  CONSTRAINT PK_rid PRIMARY KEY (rid),
  CONSTRAINT UQ_res_name UNIQUE (name)
);

CREATE TABLE [htmlit].[group_resources] (
  gid int NOT NULL,
  rid int NOT NULL,
  access VARCHAR(2) NOT NULL DEFAULT 'n' CONSTRAINT CK_access_value CHECK (access IN('n','r','rw')),
  CONSTRAINT PK_gid_rid PRIMARY KEY (gid, rid),
  CONSTRAINT FK_gid FOREIGN KEY (gid) REFERENCES htmlit.[group](groupId),
  CONSTRAINT FK_rid FOREIGN KEY (rid) REFERENCES htmlit.resources(rid)
);

CREATE VIEW htmlit.userAndGroup AS SELECT u.name AS UserName, g.name AS GroupName FROM [htmlit].[user] u JOIN [htmlit].[group] g ON u.groupId = g.groupId;

EXEC sys.sp_addextendedproperty
    @name=N'MS_Description',
    @value=N'Unicode: ộ' ,
    @level0type=N'SCHEMA',@level0name=N'htmlit',
    @level1type=N'TABLE',@level1name=N'group'