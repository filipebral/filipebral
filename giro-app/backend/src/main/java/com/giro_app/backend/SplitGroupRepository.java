package com.giro_app.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface SplitGroupRepository extends JpaRepository<SplitGroup, UUID> {
    @Query("select distinct g from SplitGroup g where g.ownerUserId = :userId or exists (select m.id from GroupMember m where m.splitGroup = g and m.userId = :userId) order by g.name")
    List<SplitGroup> findVisibleTo(@Param("userId") UUID userId);
}