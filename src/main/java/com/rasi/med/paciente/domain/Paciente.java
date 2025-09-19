// Paciente.java
package com.rasi.med.paciente.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "paciente")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Paciente {

    @Id
    @Column(name = "public_id", columnDefinition = "uuid")
    private UUID publicId;

    @Version
    @Column(name = "version")
    private Integer version;

    @NotBlank
    @Column(name = "tipo_doc", length = 10)
    private String tipoDoc;

    @NotBlank
    @Column(name = "num_doc", length = 50)
    private String numDoc;

    @NotBlank
    @Column(name = "nombre1", length = 100)
    private String nombre1;

    @NotBlank
    @Column(name = "apellido1", length = 100)
    private String apellido1;

    @Email
    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "updated_at", columnDefinition = "timestamptz")
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at", columnDefinition = "timestamptz")
    private OffsetDateTime deletedAt;

    @PrePersist
    public void prePersist() {
        if (publicId == null) publicId = UUID.randomUUID();
        if (updatedAt == null) updatedAt = OffsetDateTime.now();
        if (version == null) version = 0;
    }

    @PreUpdate
    public void preUpdate() { updatedAt = OffsetDateTime.now(); }


}
