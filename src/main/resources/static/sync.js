// Helpers UUID v4
function uuidv4() {
    if (crypto?.randomUUID) return crypto.randomUUID();
    const r = crypto.getRandomValues(new Uint8Array(16));
    r[6] = (r[6] & 0x0f) | 0x40; r[8] = (r[8] & 0x3f) | 0x80;
    const h = [...r].map(b => b.toString(16).padStart(2,'0')).join('');
    return `${h.substr(0,8)}-${h.substr(8,4)}-${h.substr(12,4)}-${h.substr(16,4)}-${h.substr(20)}`;
}

// --- PULL: descargar cambios incrementales ---
async function pullEntity(entity) {
    const db = window.rasiDB;
    const row = await db.cursors.get(entity);
    const since = row?.since || '1970-01-01T00:00:00Z';
    const url = `/api/${entity}/sync/changes?since=${encodeURIComponent(since)}&limit=500`;

    try {
        const res = await fetch(url);
        if (!res.ok) throw new Error('pull failed');
        const data = await res.json();
        await db.transaction('rw', db[entity], db.cursors, async () => {
            // upserts
            for (const it of data.upserts || []) {
                if (entity === 'pacientes') {
                    await db.pacientes.put(it);
                } else if (entity === 'citas') {
                    await db.citas.put(it);
                }
            }
            // tombstones
            for (const it of data.tombstones || []) {
                if (entity === 'pacientes') await db.pacientes.delete(it.publicId);
                else if (entity === 'citas') await db.citas.delete(it.publicId);
            }
            await db.cursors.put({ entity, since: data.nextCursor || since });
        });
        if (data.hasMore) return pullEntity(entity); // seguir hasta vaciar
    } catch (e) {
        // sin red o error -> no pasa nada
    }
}

// --- PUSH: drenar outbox ---
async function drainOutbox() {
    const db = window.rasiDB;
    const items = await db.outbox.orderBy('createdAt').toArray();
    for (const op of items) {
        const endpoint = `/api/${op.entity}/sync/upsert`;
        try {
            const res = await fetch(endpoint, {
                method: 'POST',
                headers: {'Content-Type':'application/json', 'Idempotency-Key': op.opId},
                body: JSON.stringify(op.body)
            });
            if (res.ok || res.status === 409) {
                await db.outbox.delete(op.opId);
                // opcional: refrescar espejo local con respuesta
                if (res.ok) {
                    const saved = await res.json().catch(()=>null);
                    if (saved) {
                        if (op.entity==='pacientes') await db.pacientes.put(saved);
                        if (op.entity==='citas')     await db.citas.put(saved);
                    }
                }
            } else {
                // si 4xx/5xx distinto, salimos para reintentar luego
                break;
            }
        } catch (e) { break; } // sin red -> salir
    }
}

// --- Helpers de lectura/escritura local-first ---
async function listPacientesLocalFirst() {
    if (navigator.onLine) { await pullEntity('pacientes'); }
    return await window.rasiDB.pacientes.toArray();
}

async function createOrUpdatePacienteOffline(dto) {
    // si offline o servidor falla, encola upsert con publicId propio
    const body = Object.assign({}, dto);
    if (!body.publicId) body.publicId = uuidv4();
    const op = { opId: uuidv4(), entity:'pacientes', action:'upsert', body, createdAt: Date.now() };
    const db = window.rasiDB;
    await db.transaction('rw', db.pacientes, db.outbox, async () => {
        await db.pacientes.put(Object.assign({}, body, { version: body.version||0, updatedAt: new Date().toISOString() }));
        await db.outbox.put(op);
    });
    return body;
}

async function updateOrDeletePacienteOffline(publicId, patch) {
    // patch puede incluir deletedAt para soft-delete
    const db = window.rasiDB;
    const current = await db.pacientes.get(publicId);
    const next = Object.assign({}, current||{ publicId }, patch);
    const op = { opId: uuidv4(), entity:'pacientes', action:'upsert', body: next, createdAt: Date.now() };
    await db.transaction('rw', db.pacientes, db.outbox, async () => {
        await db.pacientes.put(next);
        await db.outbox.put(op);
    });
}

async function listCitasDiaLocalFirst(fromIso, toIso) {
    if (navigator.onLine) { await pullEntity('citas'); }
    const db = window.rasiDB;
    const all = await db.citas.toArray();
    return all.filter(c => {
        const t = new Date(c.fechaHora).toISOString();
        return t >= fromIso && t <= toIso && !c.deletedAt;
    }).sort((a,b)=> new Date(a.fechaHora) - new Date(b.fechaHora));
}

async function upsertCitaOffline(dto) {
    const body = Object.assign({ publicId: uuidv4() }, dto);
    const db = window.rasiDB;
    const op = { opId: uuidv4(), entity:'citas', action:'upsert', body, createdAt: Date.now() };
    await db.transaction('rw', db.citas, db.outbox, async () => {
        await db.citas.put(body);
        await db.outbox.put(op);
    });
}

async function cancelarCitaOffline(publicId) {
    const db = window.rasiDB;
    const c = await db.citas.get(publicId);
    if (!c) return;
    const patch = Object.assign({}, c, { deletedAt: new Date().toISOString(), estado:'CANCELADA' });
    const op = { opId: uuidv4(), entity:'citas', action:'upsert', body: patch, createdAt: Date.now() };
    await db.transaction('rw', db.citas, db.outbox, async () => {
        await db.citas.put(patch);
        await db.outbox.put(op);
    });
}

// Exponer utilidades globales
window.rasiSync = {
    pullAll: async () => { await pullEntity('pacientes'); await pullEntity('citas'); },
    drainOutbox,
    listPacientesLocalFirst,
    createOrUpdatePacienteOffline,
    updateOrDeletePacienteOffline,
    listCitasDiaLocalFirst,
    upsertCitaOffline,
    cancelarCitaOffline
};

// Reintentos automáticos al volver la red
window.addEventListener('online', async () => {
    await window.rasiSync.drainOutbox();
    await window.rasiSync.pullAll();
});
