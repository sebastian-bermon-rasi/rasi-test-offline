-- Pacientes
create table if not exists patients (
    id uuid primary key,
    document_number varchar(50) not null unique,
    name varchar(150) not null,
    updated_at timestamp not null default now(),
    deleted boolean not null default false
    );

-- Outbox (SOLO para sedes; en la central puedes omitirla si no la usas allí)
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

-- Sync tokens (SOLO central)
create table if not exists sync_tokens (
                                           id uuid primary key,
                                           client_id varchar(64) not null unique,
    last_sync_at timestamp not null default '1970-01-01'
    );

alter table if exists paciente
    add constraint uk_paciente_tipodoc_numdoc unique (tipo_doc, num_doc);
create index if not exists idx_paciente_updated_at on paciente (updated_at);
