package com.rasi.med.paciente.api;

import com.rasi.med.paciente.domain.Paciente;
import com.rasi.med.paciente.repo.PacienteRepository;
import com.rasi.med.sync.IdempotencyOp;
import com.rasi.med.sync.IdempotencyOpRepository;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/api/pacientes/sync")
public class PacienteSyncController {

    private final PacienteRepository repo;
    private final PacienteMapper mapper;
    private final IdempotencyOpRepository idemRepo;

    public PacienteSyncController(PacienteRepository repo, PacienteMapper mapper, IdempotencyOpRepository idemRepo) {
        this.repo = repo; this.mapper = mapper; this.idemRepo = idemRepo;
    }

    @PostMapping("/upsert")
    public ResponseEntity<?> upsert(@RequestHeader(value="Idempotency-Key", required=false) UUID opId,
                                    @Valid @RequestBody PacienteDto dto) {
        if (opId != null && idemRepo.findById(opId).isPresent()) {
            // ya procesado -> idempotencia
            return ResponseEntity.ok().build();
        }
        // si viene sin publicId, generarlo (pero para offline conviene enviarlo)
        if (dto.publicId == null) dto.publicId = java.util.UUID.randomUUID();

        Optional<Paciente> existing = repo.findById(dto.publicId);
        Paciente saved;
        if (!existing.isPresent()) {
            // Alta
            Paciente p = mapper.toEntity(dto);
            saved = repo.save(p);
        } else {
            // Update (optimistic locking simple: si entregan version y no coincide -> 409)
            Paciente p = existing.get();
            if (dto.version != null && !dto.version.equals(p.getVersion())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Versión desfasada");
            }
            mapper.copy(dto, p);
            if (dto.deletedAt != null) { p.setDeletedAt(dto.deletedAt); }
            saved = repo.save(p);
        }
        if (opId != null) {
            IdempotencyOp op = new IdempotencyOp();
            op.setOpId(opId); op.setEndpoint("/api/pacientes/sync/upsert");
            idemRepo.save(op);
        }
        return ResponseEntity.ok(mapper.toDto(saved));
    }
}
