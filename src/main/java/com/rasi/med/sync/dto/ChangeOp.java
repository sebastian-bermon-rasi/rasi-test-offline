package com.rasi.med.sync.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeOp {
    private String operation;         // "UPSERT" | "DELETE"
    private PacienteSyncDTO data;
}
