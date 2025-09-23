// CentralSyncController.java
package com.rasi.med.sync.central;

import com.rasi.med.sync.dto.PullResponse;
import com.rasi.med.sync.dto.PushRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sync")
@RequiredArgsConstructor
@Profile("central")
public class CentralSyncController {

    private final CentralPacienteService pacientes;

    @PostMapping("/push")
    public Map<String,Object> push(@RequestBody PushRequest body,
                                   @RequestHeader("X-Api-Key") String apiKey){
        return pacientes.processPush(apiKey, body);
    }

    @GetMapping("/changes")
    public PullResponse changes(@RequestParam("clientId") String clientId,
                                @RequestParam("since") String sinceIso,
                                @RequestHeader("X-Api-Key") String apiKey){
        return pacientes.listChanges(apiKey, clientId, sinceIso);
    }
}
