package com.rasi.med.cita.api;

import com.rasi.med.cita.domain.Cita;
import com.rasi.med.cita.repo.CitaRepository;
import com.rasi.med.paciente.domain.Paciente;
import com.rasi.med.paciente.repo.PacienteRepository;
import com.rasi.med.sync.IdempotencyOp;
import com.rasi.med.sync.IdempotencyOpRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/api/citas/sync")
@Profile("sede")
public class CitaSyncController {

    private final CitaRepository repo;
    private final PacienteRepository pacienteRepo;
    private final CitaMapper mapper;
    private final IdempotencyOpRepository idemRepo;

    public CitaSyncController(CitaRepository repo, PacienteRepository pacienteRepo, CitaMapper mapper, IdempotencyOpRepository idemRepo) {
        this.repo = repo; this.pacienteRepo = pacienteRepo; this.mapper = mapper; this.idemRepo = idemRepo;
    }

    @PostMapping("/upsert")
    public ResponseEntity<?> upsert(@RequestHeader(value="Idempotency-Key", required=false) UUID opId,
                                    @Valid @RequestBody CitaDto dto) {
        if (opId != null && idemRepo.findById(opId).isPresent()) return ResponseEntity.ok().build();
        if (dto.publicId == null) dto.publicId = java.util.UUID.randomUUID();

        Optional<Cita> existing = repo.findById(dto.publicId);
        Cita saved;
        if (!existing.isPresent()) {
            // Alta
            Paciente p = pacienteRepo.findById(dto.pacientePublicId).orElse(null);
            if (p == null || p.getDeletedAt()!=null) return ResponseEntity.badRequest().body("Paciente inválido");
            Cita c = new Cita();
            c.setPublicId(dto.publicId);
            c.setPaciente(p);
            c.setFechaHora(dto.fechaHora);
            c.setProfesional(dto.profesional);
            c.setSede(dto.sede);
            c.setMotivo(dto.motivo);
            c.setEstado(dto.estado != null ? dto.estado : "PROGRAMADA");
            saved = repo.save(c);
        } else {
            Cita c = existing.get();
            if (dto.version != null && !dto.version.equals(c.getVersion()))
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Versión desfasada");
            if (dto.pacientePublicId != null && !dto.pacientePublicId.equals(c.getPaciente().getPublicId())) {
                Paciente p = pacienteRepo.findById(dto.pacientePublicId).orElse(null);
                if (p == null) return ResponseEntity.badRequest().body("Paciente inválido");
                c.setPaciente(p);
            }
            if (dto.fechaHora != null) c.setFechaHora(dto.fechaHora);
            if (dto.profesional != null) c.setProfesional(dto.profesional);
            if (dto.sede != null) c.setSede(dto.sede);
            if (dto.motivo != null) c.setMotivo(dto.motivo);
            if (dto.estado != null) c.setEstado(dto.estado);
            if (dto.deletedAt != null) c.setDeletedAt(dto.deletedAt);
            saved = repo.save(c);
        }
        if (opId != null) {
            IdempotencyOp op = new IdempotencyOp();
            op.setOpId(opId); op.setEndpoint("/api/citas/sync/upsert");
            idemRepo.save(op);
        }
        return ResponseEntity.ok(mapper.toDto(saved));
    }
}
