package com.rasi.med.paciente.api;

import javax.validation.constraints.*;
import java.time.OffsetDateTime;
import java.util.UUID;

public class PacienteDto {
    public UUID publicId;
    public Integer version;
    @NotBlank public String tipoDoc;
    @NotBlank public String numDoc;
    @NotBlank public String nombre1;
    @NotBlank public String apellido1;
    @Email public String email;
    public OffsetDateTime updatedAt;
    public OffsetDateTime deletedAt;
}
