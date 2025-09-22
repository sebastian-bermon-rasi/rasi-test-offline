package com.rasi.med.sync.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PacienteSyncDTO {
    private UUID publicId;
    private String tipoDoc;
    private String numDoc;
    private String nombre1;
    private String apellido1;
    private String email;
    private OffsetDateTime updatedAt;
    private OffsetDateTime deletedAt;
}
