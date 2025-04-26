package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.interfaces.rest;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.model.commands.CreatePortCommand;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.model.queries.GetAllPortsQuery;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.model.queries.GetPortByIdQuery;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.services.PortCommandService;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.services.PortQueryService;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.interfaces.rest.resources.CreatePortResource;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.interfaces.rest.resources.PortResource;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.interfaces.rest.transform.CreatePortCommandFromResourceAssembler;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.interfaces.rest.transform.PortResourceFromEntityAssembler;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST para la gestión de puertos.
 * Proporciona endpoints para crear, obtener y listar puertos.
 */
@RestController
@RequestMapping("/api/v1/ports")
@Tag(name = "Ports", description = "Port Management Endpoints")
@RequiredArgsConstructor
public class PortController {

    private final PortCommandService portCommandService;
    private final PortQueryService portQueryService;

    /**
     * Crea un nuevo puerto.
     *
     * @param resource Objeto que contiene los datos necesarios para crear un puerto.
     * @return Un recurso que representa el puerto creado o un error si no se pudo crear.
     */
    @PostMapping
    public ResponseEntity<PortResource> createPort(@RequestBody CreatePortResource resource) {
        // Convierte el recurso recibido en un comando para la capa de dominio.
        CreatePortCommand command = CreatePortCommandFromResourceAssembler.toCommandFromResource(resource);
        var port = portCommandService.handle(command);

        // Si no se pudo crear el puerto, retorna un error 400.
        if (port.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // Convierte la entidad del puerto creado en un recurso para la respuesta.
        PortResource portResource = PortResourceFromEntityAssembler.toResourceFromEntity(port.get());
        return new ResponseEntity<>(portResource, HttpStatus.CREATED);
    }

    /**
     * Obtiene una lista de todos los puertos disponibles.
     *
     * @return Una lista de recursos que representan los puertos.
     */
    @GetMapping
    public ResponseEntity<List<PortResource>> getAllPorts() {
        // Crea una consulta para obtener todos los puertos.
        var query = new GetAllPortsQuery();
        var ports = portQueryService.handle(query);

        // Convierte las entidades de puertos en recursos para la respuesta.
        var resources = ports.stream()
                .map(PortResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(resources);
    }

    /**
     * Obtiene un puerto específico por su ID.
     *
     * @param portId El ID del puerto a buscar.
     * @return Un recurso que representa el puerto encontrado o un error 404 si no existe.
     */
    @GetMapping("/{portId}")
    public ResponseEntity<PortResource> getPortById(@PathVariable Long portId) {
        // Crea una consulta para obtener un puerto por su ID.
        var query = new GetPortByIdQuery(portId);
        var port = portQueryService.handle(query);

        // Si no se encuentra el puerto, retorna un error 404.
        if (port.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Convierte la entidad del puerto encontrado en un recurso para la respuesta.
        PortResource portResource = PortResourceFromEntityAssembler.toResourceFromEntity(port.get());
        return ResponseEntity.ok(portResource);
    }
}