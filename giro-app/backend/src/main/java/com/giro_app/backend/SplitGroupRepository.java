package com.giro_app.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface SplitGroupRepository extends JpaRepository<SplitGroup, UUID> {
}