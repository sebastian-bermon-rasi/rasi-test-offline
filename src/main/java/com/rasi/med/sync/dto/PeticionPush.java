package com.rasi.med.sync.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeticionPush {
    String idCliente;
    UUID idLote;
    List<CambioOp> pacientes;
}
