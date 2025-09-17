// PacienteService.java
package com.rasi.med.paciente.service;

import com.rasi.med.paciente.api.PacienteDto;
import com.rasi.med.paciente.api.PacienteMapper;
import com.rasi.med.paciente.domain.Paciente;
import com.rasi.med.paciente.repo.PacienteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.OptimisticLockException;
import java.util.Optional;
import java.util.UUID;

@Service
public class PacienteService {

    private final PacienteRepository repo;
    private final PacienteMapper mapper;

    public PacienteService(PacienteRepository repo, PacienteMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    public Page<PacienteDto> list(Pageable pageable) {
        return repo.findByDeletedAtIsNull(pageable).map(mapper::toDto);
    }

    public Optional<PacienteDto> get(UUID id) {
        return repo.findById(id).filter(p -> p.getDeletedAt()==null).map(mapper::toDto);
    }

    public PacienteDto create(PacienteDto dto) {
        Paciente p = mapper.toEntity(dto);
        // En alta, deja que @PrePersist ponga UUID si viene null
        return mapper.toDto(repo.save(p));
    }

    public PacienteDto update(UUID id, int expectedVersion, PacienteDto dto) {
        Paciente p = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("No existe paciente"));
        if (p.getDeletedAt()!=null) throw new IllegalStateException("Paciente eliminado");
        if (p.getVersion() == null || p.getVersion().intValue() != expectedVersion) {
            throw new OptimisticLockException("Versión desfasada");
        }
        mapper.copy(dto, p);
        Paciente saved = repo.save(p);
        return mapper.toDto(saved);
    }

    public void softDelete(UUID id, int expectedVersion) {
        Paciente p = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("No existe paciente"));
        if (p.getVersion() == null || p.getVersion().intValue() != expectedVersion)
            throw new OptimisticLockException("Versión desfasada");
        p.setDeletedAt(java.time.OffsetDateTime.now());
        repo.save(p);
    }
}
