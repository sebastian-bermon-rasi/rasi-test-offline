package com.rasi.med.sync;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SyncTokenRepository extends JpaRepository<SyncToken, UUID> {
    Optional<SyncToken> findByClientId(String clientId);
}
