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
create INDEX msg_timestamp_idx ON messages(msg_time);
create UNIQUE INDEX space_name_idx ON spaces(name);

create user natter_api_user password 'password';
grant select, insert on spaces, messages to natter_api_user;