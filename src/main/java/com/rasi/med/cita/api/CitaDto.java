package com.rasi.med.cita.api;

import javax.validation.constraints.*;
import java.time.OffsetDateTime;
import java.util.UUID;

public class CitaDto {
    public UUID publicId;
    public Integer version;
    @NotNull public UUID pacientePublicId;
    @NotNull public OffsetDateTime fechaHora;
    @NotBlank public String profesional;
    @NotBlank public String sede;
    public String motivo;
    public String estado;
    public OffsetDateTime updatedAt;
    public OffsetDateTime deletedAt;
}
