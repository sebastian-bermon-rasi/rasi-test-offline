// PacienteRepository.java
package com.rasi.med.paciente.repo;

import com.rasi.med.paciente.domain.Paciente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PacienteRepository extends JpaRepository<Paciente, UUID> {

    Optional<Paciente> findByTipoDocAndNumDocAndDeletedAtIsNull(String tipoDoc, String numDoc);

    Page<Paciente> findByDeletedAtIsNull(Pageable pageable);

    List<Paciente> findByUpdatedAtAfterOrDeletedAtAfter(OffsetDateTime since1, OffsetDateTime since2);
}
