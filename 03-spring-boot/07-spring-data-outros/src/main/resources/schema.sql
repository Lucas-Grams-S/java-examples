create table if not exists p(id bigint auto_increment primary key, name varchar(50), price decimal(10,2));
create table if not exists orders(id bigint auto_increment primary key, customer varchar(50), total decimal(10,2));
create table if not exists order_item(id bigint auto_increment primary key, orders bigint, sku varchar(20), quantity int, orders_key int);
