CREATE DATABASE TEST;

USE TEST;

CREATE TABLE TestTable
(
 Id INT PRIMARY KEY IDENTITY(1,1) NOT NULL,
 Name NVARCHAR(40) NOT NULL,
 Description NVARCHAR(100) NOT NULL,
);

EXEC sys.sp_addextendedproperty
    @name=N'MS_Description',
    @value=N'This is column description' ,
    @level0type=N'SCHEMA',@level0name=N'dbo',
    @level1type=N'TABLE',@level1name=N'TestTable',
    @level2type=N'COLUMN',@level2name=N'Description'
