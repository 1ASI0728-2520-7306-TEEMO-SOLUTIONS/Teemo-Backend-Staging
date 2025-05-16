package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.services.PortService;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.resources.*;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.transform.CreatePortCommandFromResourceAssembler;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.transform.PortResourceFromEntityAssembler;

import java.util.List;

@RestController
@RequestMapping(value = "/api/ports", produces = MediaType.APPLICATION_JSON_VALUE) // 👈 Plural
@Tag(name = "Port", description = "Port Endpoints")
public class PortController {

    private final PortService portService;

    public PortController(PortService portService) {
        this.portService = portService;
    }

    // Endpoint para crear un nuevo puerto
    // Ruta: POST /api/ports
    // Descripción: Crea un nuevo puerto en el sistema a partir de los datos proporcionados en el cuerpo de la solicitud.
    @PostMapping
    public ResponseEntity<PortResource> createPort(@RequestBody CreatePortResource resource) {
        var command = CreatePortCommandFromResourceAssembler.toCommandFromResource(resource);
        var port = portService.createPort(command);
        var portResource = PortResourceFromEntityAssembler.toResourceFromEntity(port);
        return new ResponseEntity<>(portResource, HttpStatus.CREATED);
    }

    // Endpoint para obtener un puerto por su ID
    // Ruta: GET /api/ports/{portId}
    // Descripción: Devuelve los detalles de un puerto específico identificado por su ID.
    @GetMapping("/{portId}")
    public ResponseEntity<PortResource> getPortById(@PathVariable String portId) {
        return portService.getPortById(portId)
                .map(PortResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Endpoint para obtener un puerto por su nombre
    // Ruta: GET /api/ports/name/{name}
    // Descripción: Devuelve los detalles de un puerto específico identificado por su nombre.
    @GetMapping("/name/{name}")
    public ResponseEntity<PortResource> getPortByName(@PathVariable String name) {
        return portService.getPortByName(name)
                .map(PortResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Endpoint para eliminar un puerto
    // Ruta: DELETE /api/ports/{portId}
    // Descripción: Elimina un puerto específico identificado por su ID.
    @DeleteMapping("/{portId}")
    public ResponseEntity<Void> deletePort(@PathVariable String portId) {
        portService.deletePort(portId);
        return ResponseEntity.noContent().build();
    }

    // Endpoint para obtener todos los puertos
    @GetMapping("/all-ports")
    public ResponseEntity<List<PortResource>> getAllPorts() {
        return ResponseEntity.ok(
                portService.getAllPorts()
                        .stream()
                        .map(PortResourceFromEntityAssembler::toResourceFromEntity)
                        .toList()
        );
    }
}