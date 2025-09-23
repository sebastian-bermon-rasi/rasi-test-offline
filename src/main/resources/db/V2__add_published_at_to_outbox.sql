ALTER TABLE outbox
ADD COLUMN IF NOT EXISTS published_at timestamptz;

-- Si ya tienes filas publicadas, inicialízalas:
UPDATE outbox SET published_at = now() WHERE published = true AND published_at IS NULL;

create table if not exists outbox (
                                      id uuid primary key,
                                      aggregate_type varchar(64) not null,
    aggregate_id uuid not null,
    op varchar(16) not null,               -- 'UPSERT' | 'DELETE'
    payload jsonb not null,
    created_at timestamp not null default now(),
    published boolean not null default false
    );
create index if not exists idx_outbox_published on outbox(published);