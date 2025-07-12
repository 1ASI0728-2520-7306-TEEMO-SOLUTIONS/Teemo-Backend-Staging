package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.repositories.MongoEventRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final MongoEventRepository eventRepository;

    /**
     * Obtiene una lista de todos los puertos de origen únicos de los eventos.
     * La lista se devuelve ordenada alfabéticamente para una mejor experiencia de usuario.
     *
     * @return Lista ordenada de nombres de puertos de origen.
     */
    public List<String> getAllDistinctOriginPorts() {
        // Llama al nuevo método del repositorio
        List<String> ports = eventRepository.findDistinctOriginPorts();

        // Es una buena práctica ordenar la lista antes de devolverla
        return ports.stream().sorted().collect(Collectors.toList());
    }
}