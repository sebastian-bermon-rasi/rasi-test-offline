create table if not exists paciente (
    public_id   uuid primary key,
    version     int4 not null default 0,
    tipo_doc    varchar(10) not null,
    num_doc     varchar(50) not null,
    nombre1     varchar(100) not null,
    apellido1   varchar(100) not null,
    email       varchar(150),
    updated_at  timestamptz not null,
    deleted_at  timestamptz
    );

create unique index if not exists uk_paciente_doc
    on paciente (tipo_doc, num_doc)
    where deleted_at is null;
