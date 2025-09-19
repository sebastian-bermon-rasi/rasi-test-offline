package com.rasi.med.sync.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RespuestaPull {
    OffsetDateTime desde;
    OffsetDateTime hasta;
    List<PacienteSyncDTO> pacientes;
}
