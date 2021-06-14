create table fotbl(
    trdate varchar(30),
    symbol varchar(30),
    expirydt varchar(30),
    instrument varchar(10),
    optiontyp varchar(10),
    strikepr decimal,
    closepr decimal,
    settlepr decimal,
    contracts int,
    valinlakh decimal,
    openint int,
    choi int,
    primary key(trdate, symbol, expirydt, instrument, optiontyp, strikepr )
);

-- in mysql shell create kafkauser and grant it all privileges
create user 'kafkauser'@'%' identified by 'kafka';
grant all privileges on *.* to 'kafkauser'@'%' with grant option;
flush privileges;
