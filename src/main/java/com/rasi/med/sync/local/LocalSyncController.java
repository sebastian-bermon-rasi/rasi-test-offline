package com.rasi.med.sync.local;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/local/sync")
@RequiredArgsConstructor
@Profile("sede")
public class LocalSyncController {

    private final LocalSyncService service;

    @GetMapping("/ping")
    public String ping(){ return "pong"; }

    @PostMapping("/sync/push-once")
    public ResponseEntity<?> pushOnce() {
        try {
            return ResponseEntity.ok(service.pushOnce());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    Collections.singletonMap("error", e.getMessage())
            );
        }
    }

    @PostMapping("/sync/pull-once")
    public ResponseEntity<?> pullOnce() {
        try {
            return ResponseEntity.ok(service.pullOnce());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    Collections.singletonMap("error", e.getMessage())
            );
        }
    }
}
