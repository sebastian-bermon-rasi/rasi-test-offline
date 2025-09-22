package com.rasi.med.sync.local;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rasi.med.paciente.domain.Paciente;
import com.rasi.med.paciente.repo.PacienteRepository;
import com.rasi.med.sync.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Profile("sede")
public class LocalSyncService {

    private final OutboxService outbox;
    private final PacienteRepository repo;
    private final ObjectMapper mapper;
    private final RestTemplate rest;

    @Value("${central.baseUrl}")
    private String centralBaseUrl; // ej: https://<tu-servicio>.railway.app
    @Value("${sync.clientId:SEDE-01}")
    private String clientId;
    @Value("${sync.apiKey}")
    private String apiKey;

    // Puedes guardar este since en DB; para demo, lo mantenemos en memoria
    private OffsetDateTime lastSince = OffsetDateTime.parse("1970-01-01T00:00:00Z");

    public Map<String,Object> pushOnce(){
        List<Map<String,Object>> rows = outbox.pending(200);
        if (rows.isEmpty()) return Collections.singletonMap("pushed", 0);

        List<ChangeOp> ops = new ArrayList<>();
        List<UUID> toMark = new ArrayList<>();
        for (Map<String,Object> r : rows){
            ChangeOp op = new ChangeOp();
            op.setOperation((String) r.get("op"));
            try {
                PacienteSyncDTO dto = mapper.readValue((String) r.get("payload"), PacienteSyncDTO.class);
                op.setData(dto);
            } catch (Exception e) { throw new RuntimeException(e); }
            ops.add(op);
            toMark.add((UUID) r.get("id"));
        }

        PushRequest body = PushRequest.builder()
                .clientId(clientId)
                .batchId(UUID.randomUUID())
                .pacientes(ops)
                .build();

        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.set("X-Api-Key", apiKey);

        ResponseEntity<Map> resp = rest.exchange(
                centralBaseUrl + "/api/sync/push",
                HttpMethod.POST,
                new HttpEntity<>(body, h),
                Map.class
        );

        Map<String,Object> m = resp.getBody();
        boolean accepted = Boolean.TRUE.equals(m.get("accepted"));
        if (accepted){
            outbox.markPublished(toMark);
        }
        return m;
    }

    @Transactional
    public Map<String,Object> pullOnce(){
        HttpHeaders h = new HttpHeaders();
        h.set("X-Api-Key", apiKey);

        ResponseEntity<PullResponse> resp = rest.exchange(
                centralBaseUrl + "/api/sync/changes?clientId="+clientId+"&since="+lastSince.toString(),
                HttpMethod.GET,
                new HttpEntity<>(h),
                PullResponse.class
        );

        PullResponse pr = resp.getBody();
        if (pr == null) return Collections.singletonMap("pulled", 0);

        // aplicar
        int applied = 0;
        for (PacienteSyncDTO d : pr.getPacientes()){
            Paciente target = repo.findById(d.getPublicId()).orElseGet(() -> {
                Paciente p = new Paciente();
                p.setPublicId(d.getPublicId());
                return p;
            });
            if (target.getUpdatedAt() == null || d.getUpdatedAt().isAfter(target.getUpdatedAt())){
                target.setTipoDoc(d.getTipoDoc());
                target.setNumDoc(d.getNumDoc());
                target.setNombre1(d.getNombre1());
                target.setApellido1(d.getApellido1());
                target.setEmail(d.getEmail());
                target.setDeletedAt(d.getDeletedAt());
                target.setUpdatedAt(d.getUpdatedAt());
                repo.save(target);
                applied++;
            }
        }
        // avanzar ventana
        lastSince = pr.getUntil();
        Map<String,Object> res = new HashMap<>();
        res.put("pulled", applied);
        res.put("until", lastSince.toString());
        return res;
    }
}
