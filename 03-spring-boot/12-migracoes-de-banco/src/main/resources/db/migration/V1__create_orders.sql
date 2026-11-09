create table orders (
    id       bigint        primary key,
    customer varchar(80)   not null,
    total    decimal(12,2) not null
);
