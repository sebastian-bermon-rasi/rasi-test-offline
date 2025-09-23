create table if not exists sync_token (
    client_id   varchar(100) primary key,
    last_since  timestamptz not null
    );
