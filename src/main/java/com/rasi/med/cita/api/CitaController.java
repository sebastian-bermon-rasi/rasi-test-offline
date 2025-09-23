package com.rasi.med.cita.api;

import com.rasi.med.cita.domain.Cita;
import com.rasi.med.cita.repo.CitaRepository;
import com.rasi.med.paciente.domain.Paciente;
import com.rasi.med.paciente.repo.PacienteRepository;
import lombok.var;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Profile("sede")
@RestController
@RequestMapping("/api/citas")
public class CitaController {

    private final CitaRepository repo;
    private final PacienteRepository pacienteRepo;
    private final CitaMapper mapper;

    public CitaController(CitaRepository repo, PacienteRepository pacienteRepo, CitaMapper mapper) {
        this.repo = repo; this.pacienteRepo = pacienteRepo; this.mapper = mapper;
    }

    @GetMapping
    public List<CitaDto> list(@RequestParam(required=false) String from,
                              @RequestParam(required=false) String to) {
        if (from != null && to != null) {
            var f = OffsetDateTime.parse(from);
            var t = OffsetDateTime.parse(to);
            return repo.findInRange(f, t).stream().map(mapper::toDto).collect(Collectors.toList());
        }
        return repo.findAll().stream()
                .filter(c -> c.getDeletedAt()==null)
                .sorted(Comparator.comparing(Cita::getFechaHora))
                .map(mapper::toDto).collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CitaDto dto) {
        Paciente p = pacienteRepo.findById(dto.pacientePublicId)
                .orElse(null);
        if (p == null || p.getDeletedAt()!=null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Paciente inexistente o eliminado");
        }
        if (repo.existsByProfesionalAndFechaHoraAndDeletedAtIsNull(dto.profesional, dto.fechaHora) ||
                repo.existsByPaciente_PublicIdAndFechaHoraAndDeletedAtIsNull(p.getPublicId(), dto.fechaHora)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflicto de agenda en ese horario");
        }
        Cita c = new Cita();
        c.setPaciente(p);
        c.setFechaHora(dto.fechaHora);
        c.setProfesional(dto.profesional);
        c.setSede(dto.sede);
        c.setMotivo(dto.motivo);
        c.setEstado(dto.estado != null ? dto.estado : "PROGRAMADA");
        Cita saved = repo.save(c);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id,
                                    @RequestHeader(value="If-Match", required=false) String ifMatch,
                                    @Valid @RequestBody CitaDto dto) {
        Cita c = repo.findById(id).orElse(null);
        if (c == null || c.getDeletedAt()!=null) return ResponseEntity.notFound().build();
        if (ifMatch == null) return ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED).body("Falta If-Match");
        int expected = Integer.parseInt(ifMatch.replace("W/","").replace("\"",""));
        if (!Objects.equals(c.getVersion(), expected)) return ResponseEntity.status(HttpStatus.CONFLICT).body("409");

        // Validar cambio de horario / profesional
        if ((dto.fechaHora != null && !dto.fechaHora.equals(c.getFechaHora())) ||
                (dto.profesional != null && !dto.profesional.equals(c.getProfesional()))) {
            String nuevoProf = dto.profesional != null ? dto.profesional : c.getProfesional();
            var nuevaHora = dto.fechaHora != null ? dto.fechaHora : c.getFechaHora();
            if (repo.existsByProfesionalAndFechaHoraAndDeletedAtIsNull(nuevoProf, nuevaHora) ||
                    repo.existsByPaciente_PublicIdAndFechaHoraAndDeletedAtIsNull(c.getPaciente().getPublicId(), nuevaHora)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflicto de agenda en ese horario");
            }
        }
        if (dto.fechaHora != null) c.setFechaHora(dto.fechaHora);
        if (dto.profesional != null) c.setProfesional(dto.profesional);
        if (dto.sede != null) c.setSede(dto.sede);
        if (dto.motivo != null) c.setMotivo(dto.motivo);
        if (dto.estado != null) c.setEstado(dto.estado);

        Cita saved = repo.save(c);
        return ResponseEntity.ok()
                .eTag("W/\"" + saved.getVersion() + "\"")
                .body(mapper.toDto(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id,
                                    @RequestHeader(value="If-Match", required=false) String ifMatch) {
        Cita c = repo.findById(id).orElse(null);
        if (c == null) return ResponseEntity.notFound().build();
        if (ifMatch == null) return ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED).body("Falta If-Match");
        int expected = Integer.parseInt(ifMatch.replace("W/","").replace("\"",""));
        if (!Objects.equals(c.getVersion(), expected)) return ResponseEntity.status(HttpStatus.CONFLICT).body("409");
        c.setDeletedAt(OffsetDateTime.now());
        c.setEstado("CANCELADA");
        repo.save(c);
        return ResponseEntity.noContent().build();
    }
}
