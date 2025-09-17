package com.rasi.med.sync;

import lombok.Data;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name="idempotency_ops")
@Data
public class IdempotencyOp {
    @Id
    @Column(name="op_id", columnDefinition="uuid")
    private UUID opId;

    @Column(name="endpoint", nullable=false)
    private String endpoint;

    @Column(name="processed_at", columnDefinition="timestamptz", nullable=false)
    private OffsetDateTime processedAt = OffsetDateTime.now();

}
