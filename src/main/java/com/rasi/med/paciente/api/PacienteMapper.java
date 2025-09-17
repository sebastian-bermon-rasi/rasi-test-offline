package com.rasi.med.paciente.api;

import com.rasi.med.paciente.domain.Paciente;
import org.springframework.stereotype.Component;

@Component
public class PacienteMapper {
    public PacienteDto toDto(Paciente p) {
        PacienteDto d = new PacienteDto();
        d.publicId = p.getPublicId();
        d.version = p.getVersion();
        d.tipoDoc = p.getTipoDoc();
        d.numDoc = p.getNumDoc();
        d.nombre1 = p.getNombre1();
        d.apellido1 = p.getApellido1();
        d.email = p.getEmail();
        d.updatedAt = p.getUpdatedAt();
        d.deletedAt = p.getDeletedAt();
        return d;
    }
    public Paciente toEntity(PacienteDto d) {
        com.rasi.med.paciente.domain.Paciente p = new com.rasi.med.paciente.domain.Paciente();
        p.setPublicId(d.publicId);
        p.setVersion(d.version);
        p.setTipoDoc(d.tipoDoc);
        p.setNumDoc(d.numDoc);
        p.setNombre1(d.nombre1);
        p.setApellido1(d.apellido1);
        p.setEmail(d.email);
        return p;
    }
    public void copy(PacienteDto d, com.rasi.med.paciente.domain.Paciente p) {
        if (d.tipoDoc != null) p.setTipoDoc(d.tipoDoc);
        if (d.numDoc  != null) p.setNumDoc(d.numDoc);
        if (d.nombre1 != null) p.setNombre1(d.nombre1);
        if (d.apellido1 != null) p.setApellido1(d.apellido1);
        if (d.email   != null) p.setEmail(d.email);
    }
}
