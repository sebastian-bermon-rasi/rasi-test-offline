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
    public UUID publicId;
    String tipoDoc;
    String numDoc;
    String nombre1;
    String apellido1;
    String email;
    OffsetDateTime updatedAt;
    OffsetDateTime deletedAt;
}
