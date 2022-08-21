create TABLE spaces(
    space_id INT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    owner VARCHAR(30) NOT NULL
);
create sequence space_id_seq;
create TABLE messages(
    space_id INT NOT NULL REFERENCES spaces(space_id),
    msg_id INT PRIMARY KEY,
    author VARCHAR(30) NOT NULL,
    msg_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    msg_text VARCHAR(1024) NOT NULL
);
create sequence msg_id_seq;
CREATE TABLE users(
    user_id VARCHAR(30) PRIMARY KEY,
    pw_hash VARCHAR(255) NOT NULL
);
create table audit_log (
    audit_id int null,
    method varchar(10) not null,
    path varchar(100) not null,
    user_id varchar(30) null,
    status int null,
    audit_time timestamp not null
);
create sequence audit_id_seq;
create index msg_timestamp_idx ON messages(msg_time);
create unique index space_name_idx ON spaces(name);
create user natter_api_user password 'password';
grant select,
    insert on spaces,
    messages to natter_api_user;
grant select,
    insert on users to natter_api_user;
grant select,
    insert on audit_log to natter_api_user;