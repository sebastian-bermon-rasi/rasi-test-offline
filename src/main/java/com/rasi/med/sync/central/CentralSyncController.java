package com.rasi.med.sync.central;

import com.rasi.med.sync.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/sync")
@RequiredArgsConstructor
@Profile("central")
public class CentralSyncController {

    private final CentralPacienteService pacientes;

    // PULL (sede descarga cambios de central)
    @GetMapping("/changes")
    public PullResponse changes(
            @RequestParam("clientId") String clientId,
            @RequestParam("since") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime since
    ){
        OffsetDateTime until = OffsetDateTime.now();
        List<PacienteSyncDTO> list = pacientes.changesSince(since);
        return PullResponse.builder()
                .since(since)
                .until(until)
                .pacientes(list)
                .hasMore(false)
                .build();
    }

    // PUSH (sede sube lote a central)
    @PostMapping("/push")
    public Map<String,Object> push(@RequestBody PushRequest body){
        List<Map<String,Object>> conflicts = new ArrayList<>();
        if (body.getPacientes() != null){
            for (ChangeOp ch : body.getPacientes()){
                PacienteSyncDTO d = ch.getData();
                if ("DELETE".equalsIgnoreCase(ch.getOperation())) {
                    if (d.getDeletedAt() == null) d.setDeletedAt(OffsetDateTime.now());
                }
                CentralPacienteService.ResultadoUpsert r = pacientes.upsert(d);
                if (!r.isOk()){
                    conflicts.add(
                            new HashMap<String,Object>(){{
                                put("entity","paciente");
                                put("field", r.getConflictField());
                                put("conflictId", r.getConflictId());
                                put("publicId", d.getPublicId());
                            }}
                    );
                }
            }
        }
        return new HashMap<String,Object>(){{
            put("accepted", conflicts.isEmpty());
            put("conflicts", conflicts);
        }};
    }
}
