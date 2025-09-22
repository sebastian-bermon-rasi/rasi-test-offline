package com.rasi.med.sync.dto;

import lombok.*;

import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PushRequest {
    private String clientId;          // id de la sede (ej: SEDE-01)
    private UUID batchId;             // UUID del lote para idempotencia (opcional)
    private List<ChangeOp> pacientes; // solo pacientes por ahora
}
