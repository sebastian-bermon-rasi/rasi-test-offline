package com.rasi.med.config;

import com.rasi.med.paciente.domain.Paciente;
import com.rasi.med.paciente.repo.PacienteRepository;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@Profile("central")
// Solo mapeamos las entidades necesarias en el central
@EntityScan(basePackageClasses = { Paciente.class })
@EnableJpaRepositories(basePackageClasses = { PacienteRepository.class })
public class CentralJpaConfig {
}
