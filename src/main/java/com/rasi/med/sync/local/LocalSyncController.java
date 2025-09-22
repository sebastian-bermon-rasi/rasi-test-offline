package com.rasi.med.sync.local;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/local/sync")
@RequiredArgsConstructor
@Profile("sede")
public class LocalSyncController {

    private final LocalSyncService svc;

    @PostMapping("/push")
    public Map<String,Object> push(){ return svc.pushOnce(); }

    @PostMapping("/pull")
    public Map<String,Object> pull(){ return svc.pullOnce(); }
}
