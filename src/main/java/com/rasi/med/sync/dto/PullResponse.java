package com.rasi.med.sync.dto;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PullResponse {
    private OffsetDateTime since;
    private OffsetDateTime until;
    private List<PacienteSyncDTO> pacientes;
    private boolean hasMore; // por si luego paginas
}
