package com.rasi.med.sync;

import com.rasi.med.sync.central.CentralPacienteService;
import com.rasi.med.sync.dto.*;
import lombok.var;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/sync")
public class CentralSyncController {

    private final CentralPacienteService patientService;
    private final SyncTokenService tokenService;

    public CentralSyncController(CentralPacienteService patientService, SyncTokenService tokenService) {
        this.patientService = patientService;
        this.tokenService = tokenService;
    }

    // PULL: sede descarga cambios desde 'since'
    @GetMapping("/changes")
    public PullResponse changes(
            @RequestHeader("X-Api-Key") String apiKey,
            @RequestParam("clientId") String clientId,
            @RequestParam("since") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant since
    ){
        Instant until = Instant.now();
        // devolvemos lo que cambió desde 'since'
        var items = patientService.changesSince(since);
        // actualizamos watermark para esa sede
        tokenService.setSince(clientId, until);
        return new PullResponse(since, until, items);
    }

    // PUSH: sede sube sus cambios pendientes
    @PostMapping("/push")
    public Map<String, Object> push(@RequestHeader("X-Api-Key") String apiKey,
                                    @RequestBody PushRequest body){
        List<Map<String,Object>> conflicts = new ArrayList<>();

        if(body.patients()!=null){
            for (var ch : body.patients()){
                if("DELETE".equalsIgnoreCase(ch.op())){
                    // marcamos deleted=true (soft delete)
                    var dto = ch.data();
                    var del = new PatientSyncDTO(dto.id(), dto.documentNumber(), dto.name(), dto.updatedAt(), true);
                    var res = patientService.upsert(del);
                    if(!res.ok()) conflicts.add(Map.of(
                            "entity","patient","field",res.conflictField(),"conflictingId",res.conflictingId()));
                } else {
                    var res = patientService.upsert(ch.data());
                    if(!res.ok()) conflicts.add(Map.of(
                            "entity","patient","field",res.conflictField(),"conflictingId",res.conflictingId()));
                }
            }
        }
        return Map.of("accepted", conflicts.isEmpty(), "conflicts", conflicts);
    }
}
