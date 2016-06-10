drop database if exists library;
create database library;
commit;
connect library;

create table ADDRESS (
  addressId integer auto_increment,
  address1 varchar(50) not null comment 'Address line 1',
  address2 varchar(50) comment 'Address line 2 (optional)',
  city varchar(30) not null,
  state char(2) not null,
  zip varchar(10) not null comment "Dash req'd for zip+4",
  primary key(AddressId)
) engine=InnoDB COMMENT='Address details';

create table PUBLISHER (
  publisherId integer auto_increment,
  name varchar(64) not null,
  address integer,
  phone varchar(16),
  primary key(publisherId),
  foreign key(address) references ADDRESS(addressId),
  index(name)
) engine=InnoDB;

create table BOOK (
  isbn bigint,
  title varchar(128) not null,
  publisherId integer auto_increment not null,
  primary key (isbn),
  foreign key (publisherId) references PUBLISHER(publisherId),
  index (title)
) engine=InnoDB COMMENT='Book details';

create table author (
  authorId integer auto_increment,
  firstName varchar(32) not null,
  middleName varchar(32) null,
  lastName varchar(32) not null,
  primary key (authorId),
  index (lastName)
) engine=InnoDB;

create table book_author (
  isbn bigint,
  authorId integer comment 'FK intentionally omitted to show an implied relationship',
  primary key (isbn, authorId),
  foreign key (isbn) references BOOK(isbn)
) engine=InnoDB;
--  foreign key (authorId) references author(authorId)

create table LIBRARY_BRANCH (
  branchId integer auto_increment auto_increment,
  name varchar(64) not null,
  address integer not null,
  primary key(BranchId),
  foreign key(Address) references ADDRESS(AddressId)
) engine=InnoDB;

create table BOOK_LOCATION (
  isbn bigint,
  branchId integer not null,
  numCopies integer not null,
  primary key(isbn, branchId),
  foreign key(isbn) references BOOK(isbn),
  foreign key(branchId) references LIBRARY_BRANCH(branchId)
) engine=InnoDB;

create table BORROWER (
  cardNo integer auto_increment,
  firstName varchar(32) not null,
  middleName varchar(32) null,
  lastName varchar(32) not null,
  address integer,
  phone varchar(16),
  primary key(cardNo),
  foreign key(Address) references ADDRESS(AddressId),
  index(lastName, firstName)
) engine=InnoDB;

create table borrowed_book (
  isbn bigint,
  branchId integer not null,
  cardNo integer not null,
  borrowDate date not null,
  dueDate date not null,
  primary key (isbn, branchId, cardNo),
  foreign key (isbn) references BOOK(isbn),
  foreign key (branchId) references LIBRARY_BRANCH(branchId),
  foreign key (cardNo) references BORROWER(cardNo),
  index (DueDate)
) engine=InnoDB;

insert into address values(null, 'Road Road', '', 'Colorado Springs', 'CO', '80920');
insert into address values(null, 'Road Road', '', 'Colorado Springs', 'CO', '80920');
insert into address values(null, 'Road Road', '', 'Colorado Springs', 'CO', '80920');
insert into address values(null, 'Road Road', '', 'Colorado Springs', 'CO', '80920');
insert into address values(null, 'Road Road', '', 'Colorado Springs', 'CO', '80920');
insert into address values(null, 'Road Road', '', 'Colorado Springs', 'CO', '80920');
insert into address values(null, 'Road Road', '', 'Colorado Springs', 'CO', '80920');
insert into address values(null, 'Road Road', '', 'Colorado Springs', 'CO', '80920');
insert into address values(null, 'Road Road', '', 'Colorado Springs', 'CO', '80920');
insert into borrower values(null, 'firstName', '', 'lastName', 2, '123');
insert into borrower values(null, 'firstName', '', 'lastName', 1, '123');
insert into borrower values(null, 'firstName', '', 'lastName', 2, '123');
insert into borrower values(null, 'firstName', '', 'lastName', 1, '123');
insert into borrower values(null, 'firstName', '', 'lastName', 2, '123');
insert into borrower values(null, 'firstName', '', 'lastName', 1, '123');
insert into borrower values(null, 'firstName', '', 'lastName', 2, '123');
insert into borrower values(null, 'firstName', '', 'lastName', 1, '123');
insert into borrower values(null, 'firstName', '', 'lastName', 2, '123');
insert into borrower values(null, 'firstName', '', 'lastName', 1, '123');
insert into borrower values(null, 'firstName', '', 'lastName', 2, '123');
insert into borrower values(null, 'firstName', '', 'lastName', 1, '123');
insert into borrower values(null, 'firstName', '', 'lastName', 2, '123');
insert into borrower values(null, 'firstName', '', 'lastName', 1, '123');
insert into borrower values(null, 'firstName', '', 'lastName', 2, '123');
insert into borrower values(null, 'firstName', '', 'lastName', 1, '123');
insert into borrower values(null, 'firstName', '', 'lastName', 2, '123');
insert into borrower values(null, 'firstName', '', 'lastName', 1, '123');
insert into borrower values(null, 'firstName', '', 'lastName', 2, '123');
insert into borrower values(null, 'firstName', '', 'lastName', 1, '123');
insert into borrower values(null, 'firstName', '', 'lastName', 2, '123');
insert into borrower values(null, 'firstName', '', 'lastName', 1, '123');
insert into borrower values(null, 'firstName', '', 'lastName', 2, '123');
insert into borrower values(null, 'firstName', '', 'lastName', 1, '123');
insert into borrower values(null, 'firstName', '', 'lastName', 2, '123');
insert into borrower values(null, 'firstName', '', 'lastName', 1, '123');
insert into borrower values(null, 'firstName', '', 'lastName', 2, '123');
insert into borrower values(null, 'firstName', '', 'lastName', 1, '123');
insert into borrower values(null, 'firstName', '', 'lastName', 2, '123');
insert into borrower values(null, 'firstName', '', 'lastName', 1, '123');
insert into publisher values(null, 'AW', 3, '123');
insert into publisher values(null, 'AW', 3, '123');
insert into publisher values(null, 'AW', 3, '123');
insert into publisher values(null, 'AW', 3, '123');
insert into publisher values(null, 'AW', 3, '123');
insert into book values(100, 100, 1);
insert into book values(101, 100, 1);
insert into book values(102, 100, 1);
insert into book values(103, 100, 1);
insert into book values(104, 100, 1);
insert into book values(105, 100, 1);
insert into book values(106, 100, 1);
insert into book values(107, 100, 1);
insert into book values(108, 100, 1);
insert into book values(109, 100, 1);
insert into book values(110, 100, 1);
insert into book values(111, 100, 1);
insert into book values(112, 100, 1);
insert into book values(113, 100, 1);
insert into book values(114, 100, 1);
insert into book values(115, 100, 1);
insert into book values(116, 100, 1);
insert into book values(117, 100, 1);
insert into book values(118, 100, 1);
insert into book values(119, 100, 1);
insert into book values(120, 100, 1);
insert into book values(121, 100, 1);
insert into book values(122, 100, 1);
insert into book values(123, 100, 1);
insert into book values(124, 100, 1);
insert into book values(125, 100, 1);
insert into book values(126, 100, 1);
insert into book values(127, 100, 1);
insert into book values(128, 100, 1);
insert into book values(129, 100, 1);
insert into book values(130, 100, 1);
insert into book values(131, 100, 1);
insert into book values(132, 100, 1);
insert into book values(133, 100, 1);
insert into book values(134, 100, 1);
insert into book values(135, 100, 1);
insert into book values(136, 100, 1);
insert into book values(137, 100, 1);
insert into book values(138, 100, 1);
insert into book values(139, 100, 1);
insert into book values(140, 100, 1);
insert into book values(141, 100, 1);
insert into book values(142, 100, 1);
insert into book values(143, 100, 1);
insert into book values(144, 100, 1);
insert into book values(145, 100, 1);
insert into book values(146, 100, 1);
insert into book values(147, 100, 1);
insert into book values(148, 100, 1);
insert into book values(149, 100, 1);
insert into book values(150, 100, 1);
insert into book values(151, 100, 1);
insert into book values(152, 100, 1);
insert into book values(153, 100, 1);
insert into book values(154, 100, 1);
insert into book values(155, 100, 1);
insert into book values(156, 100, 1);
insert into book values(157, 100, 1);
insert into book values(158, 100, 1);
insert into book values(159, 100, 1);
insert into book values(160, 100, 1);
insert into book values(161, 100, 1);
insert into book values(162, 100, 1);
insert into book values(163, 100, 1);
insert into book values(164, 100, 1);
insert into book values(165, 100, 1);
insert into book values(166, 100, 1);
insert into book values(167, 100, 1);
insert into book values(168, 100, 1);
insert into book values(169, 100, 1);
insert into book values(170, 100, 1);
insert into book values(171, 100, 1);
insert into book values(172, 100, 1);
insert into book values(173, 100, 1);
insert into book values(174, 100, 1);
insert into book values(175, 100, 1);
insert into book values(176, 100, 1);
insert into book values(177, 100, 1);
insert into book values(178, 100, 1);
insert into book values(179, 100, 1);
insert into library_branch values(null, 'Central', 5);
insert into book_location values(100, 1, 1);
insert into book_location values(101, 1, 1);
insert into book_location values(102, 1, 1);
insert into book_location values(103, 1, 1);
insert into book_location values(104, 1, 1);
insert into book_location values(105, 1, 1);
insert into book_location values(106, 1, 1);
insert into book_location values(107, 1, 1);
insert into book_location values(108, 1, 1);
insert into book_location values(109, 1, 1);
insert into book_location values(110, 1, 1);
insert into book_location values(111, 1, 1);
insert into book_location values(112, 1, 1);
insert into book_location values(113, 1, 1);
insert into book_location values(114, 1, 1);
insert into book_location values(115, 1, 1);
insert into book_location values(116, 1, 1);
insert into book_location values(117, 1, 1);
insert into book_location values(118, 1, 1);
insert into book_location values(119, 1, 1);
insert into book_location values(120, 1, 1);
insert into book_location values(121, 1, 1);
insert into book_location values(122, 1, 1);
insert into book_location values(123, 1, 1);
insert into book_location values(124, 1, 1);
insert into book_location values(125, 1, 1);
insert into book_location values(126, 1, 1);
insert into book_location values(127, 1, 1);
insert into book_location values(128, 1, 1);
insert into book_location values(129, 1, 1);
insert into book_location values(130, 1, 1);
insert into book_location values(131, 1, 1);
insert into book_location values(132, 1, 1);
insert into book_location values(133, 1, 1);
insert into book_location values(134, 1, 1);
insert into book_location values(135, 1, 1);
insert into book_location values(136, 1, 1);
insert into book_location values(137, 1, 1);
insert into book_location values(138, 1, 1);
insert into book_location values(139, 1, 1);
insert into book_location values(140, 1, 1);
insert into book_location values(141, 1, 1);
insert into book_location values(142, 1, 1);
insert into book_location values(143, 1, 1);
insert into book_location values(144, 1, 1);
insert into book_location values(145, 1, 1);
insert into book_location values(146, 1, 1);
insert into book_location values(147, 1, 1);
insert into book_location values(148, 1, 1);
insert into book_location values(149, 1, 1);
insert into borrowed_book values(100, 1, 1, '1999-10-31', '1999-10-31');
insert into borrowed_book values(101, 1, 2, '1999-10-31', '1999-10-31');
insert into library_branch values(null, 'Central', 8);
insert into library_branch values(null, 'Central', 7);
insert into library_branch values(null, 'Central', 5);
insert into author values(null, "author1", null, "author1");
insert into author values(null, "author2", null, "author2");
insert into author values(null, "author3", null, "author3");
insert into author values(null, "author4", null, "author4");
insert into author values(null, "author5", null, "author5");
insert into author values(null, "author6", null, "author6");
insert into author values(null, "author7", null, "author7");
insert into author values(null, "author8", null, "author8");
insert into author values(null, "author9", null, "author9");
insert into book_author values(101, 1);
insert into book_author values(101, 2);
insert into book_author values(102, 3);
insert into book_author values(103, 4);
insert into book_author values(104, 5);
insert into book_author values(105, 6);
insert into book_author values(106, 7);
insert into book_author values(107, 8);


-- select branchname, count(*) from library_branch group by branchname, branchid;

-- insert into borrower values(null, 'akshai', 9, '456');

commit;