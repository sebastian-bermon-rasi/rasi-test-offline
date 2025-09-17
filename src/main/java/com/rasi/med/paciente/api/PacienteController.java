package com.rasi.med.paciente.api;

import com.rasi.med.paciente.domain.Paciente;
import com.rasi.med.paciente.repo.PacienteRepository;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    private final PacienteRepository repo;
    private final PacienteMapper mapper;

    public PacienteController(PacienteRepository repo, PacienteMapper mapper) {
        this.repo = repo; this.mapper = mapper;
    }

    @GetMapping
    public List<PacienteDto> list() {
        return repo.findAll().stream().filter(p->p.getDeletedAt()==null).map(mapper::toDto).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PacienteDto> get(@PathVariable UUID id) {
        Optional<Paciente> p = repo.findById(id);
        if (!p.isPresent() || p.get().getDeletedAt()!=null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(mapper.toDto(p.get()));
    }

    @PostMapping
    public ResponseEntity<PacienteDto> create(@Valid @RequestBody PacienteDto dto) {
        Paciente p = mapper.toEntity(dto);
        Paciente saved = repo.save(p);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @Valid @RequestBody PacienteDto dto,
                                    @RequestHeader(value="If-Match", required=false) String ifMatch) {
        Paciente p = repo.findById(id).orElse(null);
        if (p == null || p.getDeletedAt()!=null) return ResponseEntity.notFound().build();
        if (ifMatch == null) return ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED).body("Falta If-Match");
        int expected = Integer.parseInt(ifMatch.replace("W/","").replace("\"",""));
        if (p.getVersion()==null || p.getVersion().intValue()!=expected) return ResponseEntity.status(HttpStatus.CONFLICT).body("409");
        mapper.copy(dto, p);
        Paciente saved = repo.save(p);
        return ResponseEntity.ok().eTag("W/\"" + saved.getVersion() + "\"").body(mapper.toDto(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id,
                                    @RequestHeader(value="If-Match", required=false) String ifMatch) {
        Paciente p = repo.findById(id).orElse(null);
        if (p == null) return ResponseEntity.notFound().build();
        if (ifMatch == null) return ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED).body("Falta If-Match");
        int expected = Integer.parseInt(ifMatch.replace("W/","").replace("\"",""));
        if (p.getVersion()==null || p.getVersion().intValue()!=expected) return ResponseEntity.status(HttpStatus.CONFLICT).body("409");
        p.setDeletedAt(java.time.OffsetDateTime.now());
        repo.save(p);
        return ResponseEntity.noContent().build();
    }
}
