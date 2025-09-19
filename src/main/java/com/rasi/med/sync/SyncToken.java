package com.rasi.med.sync;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="sync_tokens")
@Data
public class SyncToken {
    @Id private UUID id;
    @Column(name="client_id", unique = true ,nullable=false) private String clientId;
    @Column(name="last_sync_at", nullable=false) private Instant lastSyncAt;

    protected SyncToken() {}

    public SyncToken(String clientId) {
        this.id = UUID.randomUUID();
        this.clientId = clientId;
        this.lastSyncAt = Instant.parse("1970-01-01T00:00:00Z");
    }

}
