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

@RestController
@RequestMapping("/api/v1/ports")
@Tag(name = "Ports", description = "Port Management Endpoints")
@RequiredArgsConstructor
public class PortController {

    private final PortCommandService portCommandService;
    private final PortQueryService portQueryService;

    @PostMapping
    public ResponseEntity<PortResource> createPort(@RequestBody CreatePortResource resource) {
        CreatePortCommand command = CreatePortCommandFromResourceAssembler.toCommandFromResource(resource);
        var port = portCommandService.handle(command);

        if (port.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        PortResource portResource = PortResourceFromEntityAssembler.toResourceFromEntity(port.get());
        return new ResponseEntity<>(portResource, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<PortResource>> getAllPorts() {
        var query = new GetAllPortsQuery();
        var ports = portQueryService.handle(query);

        var resources = ports.stream()
                .map(PortResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{portId}")
    public ResponseEntity<PortResource> getPortById(@PathVariable Long portId) {
        var query = new GetPortByIdQuery(portId);
        var port = portQueryService.handle(query);

        if (port.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        PortResource portResource = PortResourceFromEntityAssembler.toResourceFromEntity(port.get());
        return ResponseEntity.ok(portResource);
    }
}
