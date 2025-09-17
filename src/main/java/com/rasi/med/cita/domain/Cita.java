package com.rasi.med.cita.domain;

import com.rasi.med.paciente.domain.Paciente;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "cita")
@Data
public class Cita {

    @Id
    @Column(name = "public_id", columnDefinition = "uuid")
    private UUID publicId;

    @Version
    @Column(name = "version")
    private Integer version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", referencedColumnName = "public_id")
    private Paciente paciente;

    @Column(name = "fecha_hora", columnDefinition = "timestamptz", nullable = false)
    private OffsetDateTime fechaHora;

    @NotBlank
    @Column(name = "profesional", length = 120, nullable = false)
    private String profesional;

    @NotBlank
    @Column(name = "sede", length = 120, nullable = false)
    private String sede;

    @Column(name = "motivo", length = 250)
    private String motivo;

    @NotBlank
    @Column(name = "estado", length = 20, nullable = false)
    private String estado; // PROGRAMADA / CANCELADA

    @Column(name = "updated_at", columnDefinition = "timestamptz")
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at", columnDefinition = "timestamptz")
    private OffsetDateTime deletedAt;

    @PrePersist
    public void prePersist() {
        if (publicId == null) publicId = UUID.randomUUID();
        if (estado == null) estado = "PROGRAMADA";
        if (updatedAt == null) updatedAt = OffsetDateTime.now();
        if (version == null) version = 0;
    }
    @PreUpdate public void preUpdate() { updatedAt = OffsetDateTime.now(); }
}
