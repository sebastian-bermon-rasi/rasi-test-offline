package com.rasi.med.sync.central;

import com.rasi.med.paciente.domain.Paciente;
import com.rasi.med.paciente.repo.PacienteRepository;
import com.rasi.med.sync.dto.PacienteSyncDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CentralPacienteService {

    private final PacienteRepository repo;

    public  CentralPacienteService(PacienteRepository repository) { this.repo = repository; }

//    @Transactional(readOnly = true)
//    public List<PacienteSyncDTO> changesSince(Instant since){
//        return repo.findChangesSince(OffsetDateTime.from(since)).stream()
//                .map(p -> new PacienteSyncDTO(p.getPublicId(), p.getNumDoc(), p.getNombre1(), p.getUpdatedAt(), p.getDeletedAt()))
//                .toList();
//    }

    @Transactional
    public ResultadoUpsert upsert(PacienteSyncDTO dto){
        Optional<Paciente> porDoc = repo.findByTipoDocAndNumDoc(dto.getTipoDoc(), dto.getNumDoc());
        if (porDoc.isPresent()){
            return ResultadoUpsert.conflicto("tipodocumento", porDoc.get().getPublicId());
        }
        Paciente destino = repo.findById(dto.getPublicId()).orElseGet(() -> {
            Paciente p = new Paciente();
            p.setPublicId(dto.getPublicId());
            return p;
        });

        // Last-write-wins por updatedAt (OffsetDateTime)
        if (destino.getUpdatedAt() == null || dto.getUpdatedAt().isAfter(destino.getUpdatedAt())) {
            destino.setTipoDoc(dto.getTipoDoc());
            destino.setNumDoc(dto.getNumDoc());
            destino.setNombre1(dto.getNombre1());
            destino.setApellido1(dto.getApellido1());
            destino.setEmail(dto.getEmail());
            destino.setDeletedAt(dto.getDeletedAt());
            destino.setUpdatedAt(dto.getUpdatedAt());
            // @Version incrementará al guardar → está bien
            repo.save(destino);
        }
        return ResultadoUpsert.ok();
    }

    @lombok.Value
    public static class ResultadoUpsert {
        boolean ok;
        String campoConflicto;
        UUID idConflicto;

        public static ResultadoUpsert ok() { return new ResultadoUpsert(true, null, null); }
        public static ResultadoUpsert conflicto(String campo, UUID id) { return new ResultadoUpsert(false, campo, id); }
    }

}
