package com.rasi.med.paciente.api;

import com.rasi.med.paciente.PacienteService;
import com.rasi.med.paciente.domain.Paciente;
import com.rasi.med.paciente.repo.PacienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Controller
@RequiredArgsConstructor
@RequestMapping("/api/pacientes")
@Profile("sede")
public class PacienteController {

    private final PacienteService service;     // <<-- usa el SERVICE
    private final PacienteRepository repo;     // solo para lecturas/listados

    @PostMapping
    @ResponseBody
    public ResponseEntity<Paciente> crear(@RequestBody Paciente p) {
        Paciente saved = service.save(p);      // <<-- NO usar repo.save aquí
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Void> borrar(@PathVariable("id") UUID publicId) {
        Paciente p = repo.findById(publicId).orElse(null);
        if (p == null) return ResponseEntity.notFound().build();
        service.softDelete(p);                 // <<-- pasa por el SERVICE
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @ResponseBody
    public ResponseEntity<List<Paciente>> listar() {
        return ResponseEntity.ok(repo.findAll());
    }
}
