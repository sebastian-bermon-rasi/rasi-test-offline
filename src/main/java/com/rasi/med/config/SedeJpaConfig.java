package com.rasi.med.config;

import com.rasi.med.paciente.domain.Paciente;
import com.rasi.med.sync.outbox.Outbox; // ajusta el paquete real de tu entidad Outbox
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@Profile("sede")
// En sede sí mapeamos Outbox
@EntityScan(basePackageClasses = { Paciente.class, Outbox.class })
@EnableJpaRepositories(basePackages = {
        "com.rasi.med.paciente.repo",
        "com.rasi.med.sync.local"      // si aquí tienes el repo de outbox
})
public class SedeJpaConfig {
}
