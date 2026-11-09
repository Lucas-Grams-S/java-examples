alter table orders add column status varchar(20) default 'NEW' not null;
insert into orders(id, customer, total, status) values (1, '  Ana  ', 100.00, 'NEW');
