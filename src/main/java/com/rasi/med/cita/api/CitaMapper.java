package com.rasi.med.cita.api;

import com.rasi.med.cita.domain.Cita;
import org.springframework.stereotype.Component;

@Component
public class CitaMapper {
    public CitaDto toDto(Cita c) {
        CitaDto d = new CitaDto();
        d.publicId = c.getPublicId();
        d.version = c.getVersion();
        d.pacientePublicId = c.getPaciente().getPublicId();
        d.fechaHora = c.getFechaHora();
        d.profesional = c.getProfesional();
        d.sede = c.getSede();
        d.motivo = c.getMotivo();
        d.estado = c.getEstado();
        d.updatedAt = c.getUpdatedAt();
        d.deletedAt = c.getDeletedAt();
        return d;
    }
}
