package com.lottery.tickets.repository;

import com.lottery.tickets.entity.SeedImportError;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeedImportErrorRepository extends JpaRepository<SeedImportError, Long> {
}