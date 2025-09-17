const API = '/api/pacientes'; // se usará solo si hay red (para validación 409)

async function listar() {
    const data = await window.rasiSync.listPacientesLocalFirst();
    const tbody = document.querySelector('#tabla tbody');
    tbody.innerHTML = '';
    data.filter(p=>!p.deletedAt).forEach(p => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
      <td>${p.publicId}</td>
      <td>${p.tipoDoc||''}</td>
      <td>${p.numDoc||''}</td>
      <td>${(p.nombre1||'')+' '+(p.apellido1||'')}</td>
      <td>${p.email||''}</td>
      <td>${p.version||0}</td>
      <td>
        <button data-id="${p.publicId}" class="editar">Editar</button>
        <button data-id="${p.publicId}" class="borrar">Borrar</button>
      </td>`;
        tbody.appendChild(tr);
    });
}

async function crear() {
    const dto = {
        tipoDoc: document.getElementById('tipoDoc').value.trim(),
        numDoc:  document.getElementById('numDoc').value.trim(),
        nombre1: document.getElementById('nombre1').value.trim(),
        apellido1: document.getElementById('apellido1').value.trim(),
        email: document.getElementById('email').value.trim()
    };
    if (!dto.tipoDoc || !dto.numDoc || !dto.nombre1 || !dto.apellido1) {
        alert('Completa los campos obligatorios'); return;
    }
    if (navigator.onLine) {
        // Intentar por API para recibir validación de duplicado inmediata
        const res = await fetch(API, { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(dto) });
        if (res.status===409) { alert('Documento duplicado'); return; }
        if (res.ok) {
            const saved = await res.json();
            await window.rasiDB.pacientes.put(saved);
            await listar();
            return;
        }
    }
    // Offline o error -> guardar en outbox (el servidor validará duplicados al sincronizar)
    await window.rasiSync.createOrUpdatePacienteOffline(dto);
    await listar();
}

async function editar(publicId) {
    const email = prompt('Nuevo email:');
    if (email == null) return;
    if (navigator.onLine) {
        // Trae versión actual de red (si hay)
        const resGet = await fetch(`${API}/${publicId}`);
        if (resGet.ok) {
            const p = await resGet.json();
            const res = await fetch(`${API}/${publicId}`, {
                method:'PUT',
                headers:{'Content-Type':'application/json','If-Match': `W/"${p.version||0}"`},
                body: JSON.stringify({ email })
            });
            if (res.status===409) { alert('Conflicto de versión'); return; }
            if (res.ok) {
                const saved = await res.json();
                await window.rasiDB.pacientes.put(saved);
                await listar();
                return;
            }
        }
    }
    // Offline o error -> patch local + outbox
    await window.rasiSync.updateOrDeletePacienteOffline(publicId, { email });
    await listar();
}

async function borrar(publicId) {
    if (!confirm('¿Eliminar (soft-delete)?')) return;
    if (navigator.onLine) {
        const resGet = await fetch(`${API}/${publicId}`);
        if (resGet.ok) {
            const p = await resGet.json();
            const res = await fetch(`${API}/${publicId}`, { method:'DELETE', headers:{'If-Match': `W/"${p.version||0}"`} });
            if (res.status===409) { alert('Conflicto de versión'); return; }
            if (res.status===204) {
                await window.rasiDB.pacientes.delete(publicId);
                await listar();
                return;
            }
        }
    }
    await window.rasiSync.updateOrDeletePacienteOffline(publicId, { deletedAt: new Date().toISOString() });
    await listar();
}

document.getElementById('btnCrear')?.addEventListener('click', crear);
document.addEventListener('click', e => {
    if (e.target.classList.contains('editar')) editar(e.target.dataset.id);
    if (e.target.classList.contains('borrar')) borrar(e.target.dataset.id);
});

// Primer sync al cargar
document.addEventListener('DOMContentLoaded', async () => {
    await window.rasiSync.pullAll();
    await listar();
});
