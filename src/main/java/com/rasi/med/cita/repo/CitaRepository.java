package com.rasi.med.cita.repo;

import com.rasi.med.cita.domain.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface CitaRepository extends JpaRepository<Cita, UUID> {

    boolean existsByProfesionalAndFechaHoraAndDeletedAtIsNull(String profesional, OffsetDateTime fechaHora);
    boolean existsByPaciente_PublicIdAndFechaHoraAndDeletedAtIsNull(UUID pacienteId, OffsetDateTime fechaHora);

    @Query("select c from Cita c where c.deletedAt is null and c.fechaHora between :from and :to order by c.fechaHora asc")
    List<Cita> findInRange(OffsetDateTime from, OffsetDateTime to);
}
