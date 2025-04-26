package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.model.aggregates.Map;

@Repository
public interface MapRepository extends JpaRepository<Map, Long> {
}
