CREATE DATABASE ABC;
CREATE DATABASE ABC_TEST;
CREATE DATABASE DEF;

USE ABC;

CREATE SCHEMA A;
CREATE SCHEMA B;
CREATE SCHEMA C;

EXEC sys.sp_addextendedproperty
  @name = N'MS_Description',
  @value = N'ABC comment'

CREATE TABLE ATable
(
 Id INT PRIMARY KEY IDENTITY(1,1) NOT NULL,
 Name NVARCHAR(40) NOT NULL,
 Description NVARCHAR(100) NOT NULL,
);

EXEC sys.sp_addextendedproperty
    @name=N'MS_Description',
    @value=N'ABC Schema dbo comment' ,
    @level0type=N'SCHEMA',@level0name=N'dbo'

EXEC sys.sp_addextendedproperty
    @name=N'MS_Description',
    @value=N'ABC Schema dbo ATable comment' ,
    @level0type=N'SCHEMA',@level0name=N'dbo',
    @level1type=N'TABLE',@level1name=N'ATable'

EXEC sys.sp_addextendedproperty
    @name=N'MS_Description',
    @value=N'ABC Schema dbo ATable Column comment' ,
    @level0type=N'SCHEMA',@level0name=N'dbo',
    @level1type=N'TABLE',@level1name=N'ATable',
    @level2type=N'COLUMN',@level2name=N'Description'

CREATE TABLE A.ATable
(
 Id INT PRIMARY KEY IDENTITY(1,1) NOT NULL,
 Name NVARCHAR(40) NOT NULL,
 Description NVARCHAR(100) NOT NULL,
);

EXEC sys.sp_addextendedproperty
    @name=N'MS_Description',
    @value=N'ABC Schema A comment' ,
    @level0type=N'SCHEMA',@level0name=N'A'

EXEC sys.sp_addextendedproperty
    @name=N'MS_Description',
    @value=N'ABC Schema A ATable comment' ,
    @level0type=N'SCHEMA',@level0name=N'A',
    @level1type=N'TABLE',@level1name=N'ATable'

EXEC sys.sp_addextendedproperty
    @name=N'MS_Description',
    @value=N'ABC Schema A ATable Column comment' ,
    @level0type=N'SCHEMA',@level0name=N'A',
    @level1type=N'TABLE',@level1name=N'ATable',
    @level2type=N'COLUMN',@level2name=N'Description'

CREATE TABLE B.ATable
(
 Id INT PRIMARY KEY IDENTITY(1,1) NOT NULL,
 Name NVARCHAR(40) NOT NULL,
 Description NVARCHAR(100) NOT NULL,
);

EXEC sys.sp_addextendedproperty
    @name=N'MS_Description',
    @value=N'ABC Schema B comment' ,
    @level0type=N'SCHEMA',@level0name=N'B'

EXEC sys.sp_addextendedproperty
    @name=N'MS_Description',
    @value=N'ABC Schema B ATable comment' ,
    @level0type=N'SCHEMA',@level0name=N'B',
    @level1type=N'TABLE',@level1name=N'ATable'

EXEC sys.sp_addextendedproperty
    @name=N'MS_Description',
    @value=N'ABC Schema B ATable Column comment' ,
    @level0type=N'SCHEMA',@level0name=N'B',
    @level1type=N'TABLE',@level1name=N'ATable',
    @level2type=N'COLUMN',@level2name=N'Description'

CREATE TABLE C.ATable
(
 Id INT PRIMARY KEY IDENTITY(1,1) NOT NULL,
 Name NVARCHAR(40) NOT NULL,
 Description NVARCHAR(100) NOT NULL,
);

EXEC sys.sp_addextendedproperty
    @name=N'MS_Description',
    @value=N'ABC Schema C comment' ,
    @level0type=N'SCHEMA',@level0name=N'C'

EXEC sys.sp_addextendedproperty
    @name=N'MS_Description',
    @value=N'ABC Schema C ATable comment' ,
    @level0type=N'SCHEMA',@level0name=N'C',
    @level1type=N'TABLE',@level1name=N'ATable'

EXEC sys.sp_addextendedproperty
    @name=N'MS_Description',
    @value=N'ABC Schema C ATable Column comment' ,
    @level0type=N'SCHEMA',@level0name=N'C',
    @level1type=N'TABLE',@level1name=N'ATable',
    @level2type=N'COLUMN',@level2name=N'Description'

USE ABC_TEST;

CREATE SCHEMA A;
CREATE SCHEMA B;
CREATE SCHEMA C;

EXEC sys.sp_addextendedproperty
  @name = N'MS_Description',
  @value = N'ABC_TEST comment'

CREATE TABLE ATable
(
 Id INT PRIMARY KEY IDENTITY(1,1) NOT NULL,
 Name NVARCHAR(40) NOT NULL,
 Description NVARCHAR(100) NOT NULL,
);

EXEC sys.sp_addextendedproperty
    @name=N'MS_Description',
    @value=N'ABC_TEST Schema dbo comment' ,
    @level0type=N'SCHEMA',@level0name=N'dbo'

EXEC sys.sp_addextendedproperty
    @name=N'MS_Description',
    @value=N'ABC_TEST Schema dbo ATable comment' ,
    @level0type=N'SCHEMA',@level0name=N'dbo',
    @level1type=N'TABLE',@level1name=N'ATable'

EXEC sys.sp_addextendedproperty
    @name=N'MS_Description',
    @value=N'ABC_TEST Schema dbo ATable Column comment' ,
    @level0type=N'SCHEMA',@level0name=N'dbo',
    @level1type=N'TABLE',@level1name=N'ATable',
    @level2type=N'COLUMN',@level2name=N'Description'

CREATE TABLE A.ATable
(
 Id INT PRIMARY KEY IDENTITY(1,1) NOT NULL,
 Name NVARCHAR(40) NOT NULL,
 Description NVARCHAR(100) NOT NULL,
);

EXEC sys.sp_addextendedproperty
    @name=N'MS_Description',
    @value=N'ABC_TEST Schema A comment' ,
    @level0type=N'SCHEMA',@level0name=N'A'

EXEC sys.sp_addextendedproperty
    @name=N'MS_Description',
    @value=N'ABC_TEST Schema A ATable comment' ,
    @level0type=N'SCHEMA',@level0name=N'A',
    @level1type=N'TABLE',@level1name=N'ATable'

EXEC sys.sp_addextendedproperty
    @name=N'MS_Description',
    @value=N'ABC_TEST Schema A ATable Column comment' ,
    @level0type=N'SCHEMA',@level0name=N'A',
    @level1type=N'TABLE',@level1name=N'ATable',
    @level2type=N'COLUMN',@level2name=N'Description'

CREATE TABLE B.ATable
(
 Id INT PRIMARY KEY IDENTITY(1,1) NOT NULL,
 Name NVARCHAR(40) NOT NULL,
 Description NVARCHAR(100) NOT NULL,
);

EXEC sys.sp_addextendedproperty
    @name=N'MS_Description',
    @value=N'ABC_TEST Schema B comment' ,
    @level0type=N'SCHEMA',@level0name=N'B'

EXEC sys.sp_addextendedproperty
    @name=N'MS_Description',
    @value=N'ABC_TEST Schema B ATable comment' ,
    @level0type=N'SCHEMA',@level0name=N'B',
    @level1type=N'TABLE',@level1name=N'ATable'

EXEC sys.sp_addextendedproperty
    @name=N'MS_Description',
    @value=N'ABC_TEST Schema B ATable Column comment' ,
    @level0type=N'SCHEMA',@level0name=N'B',
    @level1type=N'TABLE',@level1name=N'ATable',
    @level2type=N'COLUMN',@level2name=N'Description'

CREATE TABLE C.ATable
(
 Id INT PRIMARY KEY IDENTITY(1,1) NOT NULL,
 Name NVARCHAR(40) NOT NULL,
 Description NVARCHAR(100) NOT NULL,
);

EXEC sys.sp_addextendedproperty
    @name=N'MS_Description',
    @value=N'ABC_TEST Schema C comment' ,
    @level0type=N'SCHEMA',@level0name=N'C'

EXEC sys.sp_addextendedproperty
    @name=N'MS_Description',
    @value=N'ABC_TEST Schema C ATable comment' ,
    @level0type=N'SCHEMA',@level0name=N'C',
    @level1type=N'TABLE',@level1name=N'ATable'

EXEC sys.sp_addextendedproperty
    @name=N'MS_Description',
    @value=N'ABC_TEST Schema C ATable Column comment' ,
    @level0type=N'SCHEMA',@level0name=N'C',
    @level1type=N'TABLE',@level1name=N'ATable',
    @level2type=N'COLUMN',@level2name=N'Description'

USE DEF;

CREATE SCHEMA D;
CREATE SCHEMA E;
CREATE SCHEMA F;

EXEC sys.sp_addextendedproperty
  @name = N'MS_Description',
  @value = N'DEF comment'

CREATE TABLE ATable
(
 Id INT PRIMARY KEY IDENTITY(1,1) NOT NULL,
 Name NVARCHAR(40) NOT NULL,
 Description NVARCHAR(100) NOT NULL,
);

EXEC sys.sp_addextendedproperty
    @name=N'MS_Description',
    @value=N'DEF Schema dbo comment' ,
    @level0type=N'SCHEMA',@level0name=N'dbo'

EXEC sys.sp_addextendedproperty
    @name=N'MS_Description',
    @value=N'DEF Schema dbo ATable comment' ,
    @level0type=N'SCHEMA',@level0name=N'dbo',
    @level1type=N'TABLE',@level1name=N'ATable'

EXEC sys.sp_addextendedproperty
    @name=N'MS_Description',
    @value=N'DEF Schema dbo ATable Column comment' ,
    @level0type=N'SCHEMA',@level0name=N'dbo',
    @level1type=N'TABLE',@level1name=N'ATable',
    @level2type=N'COLUMN',@level2name=N'Description'

CREATE TABLE D.ATable
(
 Id INT PRIMARY KEY IDENTITY(1,1) NOT NULL,
 Name NVARCHAR(40) NOT NULL,
 Description NVARCHAR(100) NOT NULL,
);

EXEC sys.sp_addextendedproperty
    @name=N'MS_Description',
    @value=N'DEF Schema D comment' ,
    @level0type=N'SCHEMA',@level0name=N'D'

EXEC sys.sp_addextendedproperty
    @name=N'MS_Description',
    @value=N'DEF Schema D ATable comment' ,
    @level0type=N'SCHEMA',@level0name=N'D',
    @level1type=N'TABLE',@level1name=N'ATable'

EXEC sys.sp_addextendedproperty
    @name=N'MS_Description',
    @value=N'DEF Schema D ATable Column comment' ,
    @level0type=N'SCHEMA',@level0name=N'D',
    @level1type=N'TABLE',@level1name=N'ATable',
    @level2type=N'COLUMN',@level2name=N'Description'

CREATE TABLE E.ATable
(
 Id INT PRIMARY KEY IDENTITY(1,1) NOT NULL,
 Name NVARCHAR(40) NOT NULL,
 Description NVARCHAR(100) NOT NULL,
);

EXEC sys.sp_addextendedproperty
    @name=N'MS_Description',
    @value=N'DEF Schema E comment' ,
    @level0type=N'SCHEMA',@level0name=N'E'

EXEC sys.sp_addextendedproperty
    @name=N'MS_Description',
    @value=N'DEF Schema E ATable comment' ,
    @level0type=N'SCHEMA',@level0name=N'E',
    @level1type=N'TABLE',@level1name=N'ATable'

EXEC sys.sp_addextendedproperty
    @name=N'MS_Description',
    @value=N'DEF Schema E ATable Column comment' ,
    @level0type=N'SCHEMA',@level0name=N'E',
    @level1type=N'TABLE',@level1name=N'ATable',
    @level2type=N'COLUMN',@level2name=N'Description'

CREATE TABLE F.ATable
(
 Id INT PRIMARY KEY IDENTITY(1,1) NOT NULL,
 Name NVARCHAR(40) NOT NULL,
 Description NVARCHAR(100) NOT NULL,
);

EXEC sys.sp_addextendedproperty
    @name=N'MS_Description',
    @value=N'DEF Schema F comment' ,
    @level0type=N'SCHEMA',@level0name=N'F'

EXEC sys.sp_addextendedproperty
    @name=N'MS_Description',
    @value=N'DEF Schema F ATable comment' ,
    @level0type=N'SCHEMA',@level0name=N'F',
    @level1type=N'TABLE',@level1name=N'ATable'

EXEC sys.sp_addextendedproperty
    @name=N'MS_Description',
    @value=N'DEF Schema F ATable Column comment' ,
    @level0type=N'SCHEMA',@level0name=N'F',
    @level1type=N'TABLE',@level1name=N'ATable',
    @level2type=N'COLUMN',@level2name=N'Description'