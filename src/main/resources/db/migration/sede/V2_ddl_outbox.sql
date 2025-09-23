create table if not exists outbox (
    id             uuid primary key,
    aggregate_type varchar(50) not null,
    aggregate_id   uuid not null,
    op             varchar(20) not null,
    payload        jsonb not null,
    published      boolean not null default false,
    created_at     timestamptz not null default now()
    );

create index if not exists idx_outbox_published_created
    on outbox (published, created_at);
