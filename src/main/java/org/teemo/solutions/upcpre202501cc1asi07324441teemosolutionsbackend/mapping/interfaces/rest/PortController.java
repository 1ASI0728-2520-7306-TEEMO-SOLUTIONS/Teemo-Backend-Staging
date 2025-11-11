package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.services.PortService;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.resources.*;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.transform.CreatePortCommandFromResourceAssembler;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.transform.PortResourceFromEntityAssembler;

import java.util.List;
import java.util.Optional;

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
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_OPERATOR')")
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
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_OPERATOR','ROLE_VIEWER')")
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
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_OPERATOR','ROLE_VIEWER')")
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
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deletePort(@PathVariable String portId) {
        portService.deletePort(portId);
        return ResponseEntity.noContent().build();
    }

    // Endpoint para obtener todos los puertos
    @GetMapping("/all-ports")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_OPERATOR','ROLE_VIEWER')")
    public ResponseEntity<List<PortResource>> getAllPorts() {
        return ResponseEntity.ok(
                portService.getAllPorts()
                        .stream()
                        .map(PortResourceFromEntityAssembler::toResourceFromEntity)
                        .toList()
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_OPERATOR','ROLE_VIEWER')")
    public ResponseEntity<List<PortResource>> getPortsByDisabled(@RequestParam(value = "disabled", required = false) Boolean disabled) {
        List<PortResource> resources = Optional.ofNullable(disabled)
                .map(portService::getPortsByDisabled)
                .orElseGet(portService::getAllPorts)
                .stream()
                .map(PortResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @PatchMapping("/{portId}/disable")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_OPERATOR')")
    public ResponseEntity<PortResource> disablePort(@PathVariable String portId,
                                                    @Valid @RequestBody(required = false) DisablePortRequest request,
                                                    Authentication authentication) {
        String actor = authentication != null ? authentication.getName() : "unknown";
        String reason = request != null ? request.reason() : null;
        var updated = portService.disablePort(portId, reason, actor);
        return ResponseEntity.ok(PortResourceFromEntityAssembler.toResourceFromEntity(updated));
    }

    @PatchMapping("/{portId}/enable")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_OPERATOR')")
    public ResponseEntity<PortResource> enablePort(@PathVariable String portId, Authentication authentication) {
        String actor = authentication != null ? authentication.getName() : "unknown";
        var updated = portService.enablePort(portId, actor);
        return ResponseEntity.ok(PortResourceFromEntityAssembler.toResourceFromEntity(updated));
    }
}
