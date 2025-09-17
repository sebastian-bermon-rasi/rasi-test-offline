package com.rasi.med.sync;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface IdempotencyOpRepository extends JpaRepository<IdempotencyOp, UUID> {}
