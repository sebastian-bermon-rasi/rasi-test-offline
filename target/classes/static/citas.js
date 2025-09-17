const API_P = '/api/pacientes';

function isoRangoDia(dateStr) {
    const start = new Date(dateStr + 'T00:00:00');
    const end   = new Date(dateStr + 'T23:59:59');
    return { from: start.toISOString(), to: end.toISOString() };
}

async function cargarPacientes() {
    // usar espejo local
    const data = await window.rasiDB.pacientes.toArray();
    const sel = document.getElementById('pacienteSel');
    sel.innerHTML = '';
    data.filter(p => !p.deletedAt).forEach(p => {
        const opt = document.createElement('option');
        opt.value = p.publicId;
        opt.textContent = `${p.tipoDoc}-${p.numDoc} | ${p.nombre1} ${p.apellido1}`;
        sel.appendChild(opt);
    });
}

async function agendar() {
    const pacientePublicId = document.getElementById('pacienteSel').value;
    const fecha = document.getElementById('fecha').value;
    const hora  = document.getElementById('hora').value;
    const profesional = document.getElementById('prof').value.trim();
    const sede  = document.getElementById('sede').value.trim();
    const motivo= document.getElementById('motivo').value.trim();
    if (!pacientePublicId || !fecha || !hora || !profesional || !sede) {
        alert('Completa paciente, fecha, hora, profesional y sede'); return;
    }
    const fechaHora = new Date(`${fecha}T${hora}:00`).toISOString();

    if (navigator.onLine) {
        // intenta vía API "online-only" para ver 409 de choque inmediato
        const res = await fetch('/api/citas', {
            method:'POST', headers:{'Content-Type':'application/json'},
            body: JSON.stringify({ pacientePublicId, fechaHora, profesional, sede, motivo })
        });
        if (res.status===409) { alert('Conflicto de agenda'); return; }
        if (res.ok) {
            const saved = await res.json();
            await window.rasiDB.citas.put(saved);
            await cargarDelDia(); return;
        }
    }
    // Offline o error -> outbox
    await window.rasiSync.upsertCitaOffline({ pacientePublicId, fechaHora, profesional, sede, motivo, estado:'PROGRAMADA' });
    await cargarDelDia();
}

async function cargarDelDia() {
    const dia = document.getElementById('dia').value;
    if (!dia) return;
    const {from, to} = isoRangoDia(dia);
    const data = await window.rasiSync.listCitasDiaLocalFirst(from, to);
    const tbody = document.querySelector('#tabla tbody');
    tbody.innerHTML = '';
    data.forEach(c => {
        const hora = new Date(c.fechaHora).toLocaleTimeString([], {hour:'2-digit', minute:'2-digit'});
        const tr = document.createElement('tr');
        tr.innerHTML = `
      <td>${hora}</td>
      <td>${c.pacientePublicId}</td>
      <td>${c.profesional}</td>
      <td>${c.sede}</td>
      <td>${c.estado}</td>
      <td>${c.version||0}</td>
      <td><button data-id="${c.publicId}" class="cancelar">Cancelar</button></td>`;
        tbody.appendChild(tr);
    });
}

async function cancelar(id) {
    if (!confirm('¿Cancelar esta cita?')) return;
    if (navigator.onLine) {
        // probar cancelación online si hay red
        const resGet = await fetch(`/api/citas`);
        // (para simplificar, cancelamos via outbox en ambos casos)
    }
    await window.rasiSync.cancelarCitaOffline(id);
    await cargarDelDia();
}

document.addEventListener('DOMContentLoaded', async () => {
    await window.rasiSync.pullAll();
    await cargarPacientes();
    const hoy = new Date().toISOString().slice(0,10);
    document.getElementById('fecha').value = hoy;
    document.getElementById('dia').value = hoy;
    document.getElementById('btnAgendar').addEventListener('click', agendar);
    document.getElementById('btnCargar')?.addEventListener('click', cargarDelDia);
    document.addEventListener('click', e => {
        if (e.target.classList.contains('cancelar')) cancelar(e.target.dataset.id);
    });
    await cargarDelDia();
});
