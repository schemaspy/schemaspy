-- TODO: Add Pg11 range and Pg14 multirange

drop schema if exists type_tests cascade;
create schema type_tests authorization postgres;

-- Base type (typtype='b')
create type type_tests.base_type;
create function type_tests.base_type_in(cstring) returns type_tests.base_type
    language internal as
'int4in';
create function type_tests.base_type_out(type_tests.base_type) returns cstring
    language internal as
'int4out';

create type type_tests.base_type
(
    input = type_tests.base_type_in,
    output = type_tests.base_type_out,
    element = int4
);

comment on type type_tests.base_type is 'Description for base type_tests.base_type';

-- Composite type (typtype='c')
create type type_tests.composite_type as
(
    att1 int,
    att2 varchar(25)
);
comment on column type_tests.composite_type.att1 is 'Description for column type_tests.composite_type.att1';
comment on column type_tests.composite_type.att2 is 'Description for column type_tests.composite_type.att2';
comment on type type_tests.composite_type is 'Description for composite type_tests.composite_type';

-- Domain type (typtype='d')
create domain type_tests.test_domain as int default 1 not null check ( value < 4 );
comment on domain type_tests.test_domain is 'Description for domain type_tests.test_domain';

-- Enum type (typtype='e')
create type type_tests.test_enum as enum ('a', 'b', 'c');
comment on type type_tests.test_enum is 'Description for enum type_tests.test_enum';

-- Pseudo-Type (typtype='p')
create type type_tests.test_pseudo;
comment on type type_tests.test_pseudo is 'Description for pseudo type_tests.test_pseudo';