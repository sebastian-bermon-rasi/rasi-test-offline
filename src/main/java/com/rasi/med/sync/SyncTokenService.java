package com.rasi.med.sync;

import lombok.var;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class SyncTokenService {
    private final SyncTokenRepository repo;

    public SyncTokenService(SyncTokenRepository repo){ this.repo = repo; }

    @Transactional
    public Instant getSinceAndUpdateWindow(String clientId, Instant until){
        var token = repo.findByClientId(clientId).orElseGet(() -> new SyncToken(clientId));
        Instant since = token.getLastSyncAt();
        token.setLastSyncAt(until);
        repo.save(token);
        return since;
    }

    @Transactional(readOnly = true)
    public Instant getSince(String clientId){
        return repo.findByClientId(clientId)
                .map(SyncToken::getLastSyncAt)
                .orElse(Instant.parse("1970-01-01T00:00:00Z"));
    }

    @Transactional
    public void setSince(String clientId, Instant value){
        var token = repo.findByClientId(clientId).orElseGet(() -> new SyncToken(clientId));
        token.setLastSyncAt(value);
        repo.save(token);
    }
}
