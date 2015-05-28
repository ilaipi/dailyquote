-- db : rzfindb

-- table : t_daily_quote

CREATE DATABASE IF NOT EXISTS rzfindb DEFAULT CHARSET utf8 COLLATE utf8_general_ci;

use rzfindb;

create table IF NOT EXISTS t_daily_quote_sh(
 F01 CHAR(6),
 F02 CHAR(8),
 F03 DOUBLE(17,3),
 F04 DOUBLE(17,3),
 F05 DOUBLE(17,0),
 F06 DOUBLE(17,3),
 F07 DOUBLE(17,3),
 F08 DOUBLE(17,3),
 F09 DOUBLE(17,3),
 F10 DOUBLE(17,3),
 F11 DOUBLE(17,0),
 F13 DOUBLE(17,3),
 F15 DOUBLE(17,0),
 F16 DOUBLE(17,3),
 F17 DOUBLE(17,0),
 F18 DOUBLE(17,3),
 F19 DOUBLE(17,0),
 F21 DOUBLE(17,0),
 F22 DOUBLE(17,3),
 F23 DOUBLE(17,0),
 F24 DOUBLE(17,3),
 F25 DOUBLE(17,0),
 F26 DOUBLE(17,3),
 F27 DOUBLE(17,0),
 F28 DOUBLE(17,3),
 F29 DOUBLE(17,0),
 F30 DOUBLE(17,3),
 F31 DOUBLE(17,0),
 F32 DOUBLE(17,3),
 F33 DOUBLE(17,0),
 F88 varchar(16),
 F99 varchar(178)
)DEFAULT CHARACTER SET = utf8;

create table IF NOT EXISTS t_daily_quote_sz(
 F01 CHAR(6),
 F02 CHAR(8),
 F03 DOUBLE(17,3),
 F04 DOUBLE(17,3),
 F05 DOUBLE(17,0),
 F06 DOUBLE(17,3),
 F07 DOUBLE(17,3),
 F08 DOUBLE(17,3),
 F09 DOUBLE(17,3),
 F10 DOUBLE(17,3),
 F11 DOUBLE(17,0),
 F13 DOUBLE(17,3),
 F14 DOUBLE(17,3),
 F15 DOUBLE(17,0),
 F16 DOUBLE(17,3),
 F17 DOUBLE(17,0),
 F18 DOUBLE(17,3),
 F19 DOUBLE(17,0),
 F21 DOUBLE(17,0),
 F22 DOUBLE(17,3),
 F23 DOUBLE(17,0),
 F24 DOUBLE(17,3),
 F25 DOUBLE(17,0),
 F26 DOUBLE(17,3),
 F27 DOUBLE(17,0),
 F28 DOUBLE(17,3),
 F29 DOUBLE(17,0),
 F30 DOUBLE(17,3),
 F31 DOUBLE(17,0),
 F32 DOUBLE(17,3),
 F33 DOUBLE(17,0),
 F34 DOUBLE(17,0),
 F35 DOUBLE(17,3),
 F36 DOUBLE(17,3),
 F37 DOUBLE(17,0),
 F88 varchar(16),
 F99 varchar(128)
)DEFAULT CHARACTER SET = utf8;

create table IF NOT EXISTS t_daily_quote_minute(
 F01 CHAR(6),
 F02 CHAR(8),
 F03 DOUBLE(17,3),
 F04 DOUBLE(17,3),
 F05 DOUBLE(17,0),
 F06 DOUBLE(17,3),
 F07 DOUBLE(17,3),
 F08 DOUBLE(17,3),
 F11 DOUBLE(17,0),
 F13 DOUBLE(17,3),
 F88 varchar(16),
 F99 varchar(128)
)DEFAULT CHARACTER SET = utf8;

create table IF NOT EXISTS t_daily_quote_day(
 F01 CHAR(6),
 F02 CHAR(8),
 F03 DOUBLE(17,3),
 F04 DOUBLE(17,3),
 F05 DOUBLE(17,0),
 F06 DOUBLE(17,3),
 F07 DOUBLE(17,3),
 F08 DOUBLE(17,3),
 F11 DOUBLE(17,0),
 F13 DOUBLE(17,3),
 F88 varchar(16),
 F99 varchar(128)
)DEFAULT CHARACTER SET = utf8;

create table IF NOT EXISTS t_sz_parsed_second(
 parsed_second varchar(128),
 parse_time datetime
)DEFAULT CHARACTER SET = utf8;

create table IF NOT EXISTS t_sh_parsed_second(
 parsed_second varchar(128),
 parse_time datetime
)DEFAULT CHARACTER SET = utf8;

create table IF NOT EXISTS t_sz_minute_last_second(
 last_second varchar(128)
)DEFAULT CHARACTER SET = utf8;

create table IF NOT EXISTS t_sh_minute_last_second(
 last_second varchar(128)
)DEFAULT CHARACTER SET = utf8;

create table IF NOT EXISTS t_sz_day_last_second(
 last_second varchar(128)
)DEFAULT CHARACTER SET = utf8;

create table IF NOT EXISTS t_sh_day_last_second(
 last_second varchar(128)
)DEFAULT CHARACTER SET = utf8;

create index idx_f01_f99 on t_daily_quote_sh(F01,F99);
create index idx_f01_f99 on t_daily_quote_sz(F01,F99);
create index idx_f99 on t_daily_quote_sh(F99);
create index idx_f99 on t_daily_quote_sz(F99);
create index idx_f01 on t_daily_quote_sh(F01);
create index idx_f01 on t_daily_quote_sz(F01);

-- for minute quote procedure
create index idx_t_daily_quote_minute_f99_f88 on t_daily_quote_minute(F99,F88);

DROP PROCEDURE IF EXISTS sp_minute; 
DELIMITER //
CREATE PROCEDURE sp_minute(IN market char(8), IN lastMinute varchar(128))
BEGIN
DECLARE num int default 0;
delete from t_daily_quote_minute where F99=left(lastMinute, 12) and F88=market;
if (market='SH') then
insert into t_daily_quote_minute(F01, F02, F03, F04, F05, F06, F07, F08, F11, F13, F88, F99) 
select F01, F02, F03, F04, F05, F06, F07, F08, F11, F13, F88, left(F99, 12) from t_daily_quote_sh where F99=lastMinute;
elseif (market='SZ') then
insert into t_daily_quote_minute(F01, F02, F03, F04, F05, F06, F07, F08, F11, F13, F88, F99) 
select F01, F02, F03, F04, F05, F06, F07, F08, F11, F13, F88, left(F99, 12) from t_daily_quote_sz where F99=lastMinute;
end if;
END //
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_daily;
DELIMITER //
CREATE PROCEDURE sp_daily(IN market char(8), IN lastTime varchar(128))
BEGIN
if (market='SH') then
insert into t_daily_quote_day(F01, F02, F03, F04, F05, F06, F07, F08, F11, F13, F88, F99) 
select F01, F02, F03, F04, F05, F06, F07, F08, F11, F13, F88, left(F99, 8) from t_daily_quote_sh where F99=lastTime;
elseif (market='SZ') then
insert into t_daily_quote_day(F01, F02, F03, F04, F05, F06, F07, F08, F11, F13, F88, F99) 
select F01, F02, F03, F04, F05, F06, F07, F08, F11, F13, F88, left(F99, 8) from t_daily_quote_sz where F99=lastTime;
end if;
END //
DELIMITER ;

DROP TRIGGER IF EXISTS tri_sz_minute;
DELIMITER //
create trigger tri_sz_minute after update on t_sz_minute_last_second for each row
BEGIN
call sp_minute('SZ',NEW.last_second);
END //
DELIMITER ;

DROP TRIGGER IF EXISTS tri_sh_minute;
DELIMITER //
create trigger tri_sh_minute after update on t_sh_minute_last_second for each row
BEGIN
call sp_minute('SH',NEW.last_second);
END //
DELIMITER ;

DROP TRIGGER IF EXISTS tri_sz_daily;
DELIMITER //
create trigger tri_sz_daily after insert on t_sz_day_last_second for each row
BEGIN
call sp_daily('SZ',NEW.last_second);
END //
DELIMITER ;

DROP TRIGGER IF EXISTS tri_sh_daily;
DELIMITER //
create trigger tri_sh_daily after insert on t_sh_day_last_second for each row
BEGIN
call sp_daily('SH',NEW.last_second);
END //
DELIMITER ;

create table IF NOT EXISTS t_debug(
 msg varchar(128),
 time datetime,
 last_minute varchar(128)
)DEFAULT CHARACTER SET = utf8;













