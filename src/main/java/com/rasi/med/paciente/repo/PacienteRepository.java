package com.rasi.med.paciente.repo;

import com.rasi.med.paciente.domain.Paciente;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.*;

public interface PacienteRepository extends JpaRepository<Paciente, UUID> {

    // Por documento (para conflictos)
    Optional<Paciente> findByTipoDocAndNumDoc(String tipoDoc, String numDoc);

    // Cambios desde “since”
    @Query("select p from Paciente p where p.updatedAt > :since")
    List<Paciente> findChangesSince(@Param("since") OffsetDateTime since);
}
