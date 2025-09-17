// Dexie schema
(function(){
    const db = new Dexie('rasi-offline');
    db.version(1).stores({
        pacientes: 'publicId, updatedAt, tipoDoc, numDoc',
        citas:     'publicId, fechaHora, pacientePublicId, updatedAt',
        outbox:    'opId, createdAt',  // {opId, entity, action, body, createdAt}
        cursors:   '&entity'           // {entity, since}
    });
    window.rasiDB = db;
})();
