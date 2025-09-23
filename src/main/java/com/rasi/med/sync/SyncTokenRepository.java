package com.rasi.med.sync;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

@Profile("sede")
public interface SyncTokenRepository extends JpaRepository<SyncToken, UUID> {
    Optional<SyncToken> findByClientId(String clientId);
}
