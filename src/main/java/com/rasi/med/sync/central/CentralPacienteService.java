package com.rasi.med.sync.central;

import com.rasi.med.paciente.domain.Paciente;
import com.rasi.med.paciente.repo.PacienteRepository;
import com.rasi.med.sync.dto.PacienteSyncDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CentralPacienteService {

    private final PacienteRepository repo;

    @Transactional(readOnly = true)
    public List<PacienteSyncDTO> changesSince(OffsetDateTime since){
        List<Paciente> list = repo.findChangesSince(since);
        List<PacienteSyncDTO> out = new ArrayList<>(list.size());
        for (Paciente p : list) out.add(toDTO(p));
        return out;
    }

    @Transactional
    public ResultadoUpsert upsert(PacienteSyncDTO dto){
        // Reglas: unicidad (tipoDoc,numDoc)
        Optional<Paciente> byDoc = repo.findByTipoDocAndNumDoc(dto.getTipoDoc(), dto.getNumDoc());
        if (byDoc.isPresent() && !byDoc.get().getPublicId().equals(dto.getPublicId())) {
            return ResultadoUpsert.conflict("tipoDoc+numDoc", byDoc.get().getPublicId());
        }

        Paciente target = repo.findById(dto.getPublicId()).orElseGet(() -> {
            Paciente p = new Paciente();
            p.setPublicId(dto.getPublicId());
            return p;
        });

        // last-write-wins por updatedAt
        if (target.getUpdatedAt() == null || dto.getUpdatedAt().isAfter(target.getUpdatedAt())) {
            target.setTipoDoc(dto.getTipoDoc());
            target.setNumDoc(dto.getNumDoc());
            target.setNombre1(dto.getNombre1());
            target.setApellido1(dto.getApellido1());
            target.setEmail(dto.getEmail());
            target.setDeletedAt(dto.getDeletedAt());
            target.setUpdatedAt(dto.getUpdatedAt());
            repo.save(target);
        }
        return ResultadoUpsert.ok();
    }

    private PacienteSyncDTO toDTO(Paciente p){
        return PacienteSyncDTO.builder()
                .publicId(p.getPublicId())
                .tipoDoc(p.getTipoDoc())
                .numDoc(p.getNumDoc())
                .nombre1(p.getNombre1())
                .apellido1(p.getApellido1())
                .email(p.getEmail())
                .updatedAt(p.getUpdatedAt())
                .deletedAt(p.getDeletedAt())
                .build();
    }

    // ===== clase interna (Java 8) =====
    public static final class ResultadoUpsert {
        private final boolean ok;
        private final String conflictField;
        private final UUID conflictId;

        private ResultadoUpsert(boolean ok, String conflictField, UUID conflictId) {
            this.ok = ok;
            this.conflictField = conflictField;
            this.conflictId = conflictId;
        }
        public static ResultadoUpsert ok(){ return new ResultadoUpsert(true, null, null); }
        public static ResultadoUpsert conflict(String field, UUID id){ return new ResultadoUpsert(false, field, id); }

        public boolean isOk(){ return ok; }
        public String getConflictField(){ return conflictField; }
        public UUID getConflictId(){ return conflictId; }
    }
}
