package com.rasi.med.paciente.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rasi.med.paciente.domain.Paciente;
import com.rasi.med.paciente.repo.PacienteRepository;
import com.rasi.med.sync.dto.PacienteSyncDTO;
import com.rasi.med.sync.local.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Profile("sede")
public class PacienteService {

    private final PacienteRepository repo;
    private final OutboxService outbox;
    private final ObjectMapper mapper;

    @Transactional
    public Paciente save(Paciente p){
        Paciente saved = repo.save(p);

        PacienteSyncDTO dto = PacienteSyncDTO.builder()
                .publicId(saved.getPublicId())
                .tipoDoc(saved.getTipoDoc())
                .numDoc(saved.getNumDoc())
                .nombre1(saved.getNombre1())
                .apellido1(saved.getApellido1())
                .email(saved.getEmail())
                .updatedAt(saved.getUpdatedAt())
                .deletedAt(saved.getDeletedAt())
                .build();

        try {
            outbox.enqueue("paciente", saved.getPublicId(), "UPSERT", mapper.writeValueAsString(dto));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return saved;
    }

    @Transactional
    public void softDelete(Paciente p){
        p.setDeletedAt(OffsetDateTime.now());
        repo.save(p);

        PacienteSyncDTO dto = PacienteSyncDTO.builder()
                .publicId(p.getPublicId())
                .tipoDoc(p.getTipoDoc())
                .numDoc(p.getNumDoc())
                .nombre1(p.getNombre1())
                .apellido1(p.getApellido1())
                .email(p.getEmail())
                .updatedAt(p.getUpdatedAt())
                .deletedAt(p.getDeletedAt())
                .build();

        try {
            outbox.enqueue("paciente", p.getPublicId(), "DELETE", mapper.writeValueAsString(dto));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
