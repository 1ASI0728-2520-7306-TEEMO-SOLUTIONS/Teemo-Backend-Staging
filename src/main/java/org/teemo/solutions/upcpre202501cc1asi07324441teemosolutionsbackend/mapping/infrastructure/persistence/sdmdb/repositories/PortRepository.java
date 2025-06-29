// PortRepository.java
package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.repositories;

import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;

import java.util.List;
import java.util.Optional;

public interface PortRepository {
    Port savePort(Port port);
    void saveAll(List<Port> ports);
    void eliminatePort(String id);
    List<Port> findByName(String name);
    Optional<Port> findById(String id);
    List<Port> getAll();
    Optional<Port> getPortByNameAndContinent(String name, String continent);
    boolean existsByNameAndContinent(String name, String continent);
}