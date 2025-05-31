// PortRepository.java
package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.repositories;

import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;

import java.util.List;
import java.util.Optional;

public interface PortRepository {
    void eliminatePort(String id);
    Port savePort(Port port);
    Optional<Port> findById(String id);
    List<Port> getAll();
    List<Port> findByName(String name);
    void saveAll(List<Port> ports);
    Optional<Port> getPortByNameAndContinent(String name, String continent);

    boolean existsByNameAndContinent(String name, String continent);
}