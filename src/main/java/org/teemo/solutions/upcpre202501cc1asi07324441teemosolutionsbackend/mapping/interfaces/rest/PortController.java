package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.services.PortService;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.resources.CreatePortResource;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.resources.PortResource;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.transform.CreatePortCommandFromResourceAssembler;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.transform.PortResourceFromEntityAssembler;

@RestController
@RequestMapping("/api/ports")
public class PortController {

    private final PortService portService;

    public PortController(PortService portService) {
        this.portService = portService;
    }

    @PostMapping
    public ResponseEntity<PortResource> createPort(@RequestBody CreatePortResource resource) {
        var command = CreatePortCommandFromResourceAssembler.toCommandFromResource(resource);
        var port = portService.handle(command);
        var portResource = PortResourceFromEntityAssembler.toResourceFromEntity(port);
        return new ResponseEntity<>(portResource, HttpStatus.CREATED);
    }

    @GetMapping("/{portId}")
    public ResponseEntity<PortResource> getPortById(@PathVariable String portId) {
        return portService.getPortById(portId)
                .map(PortResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<PortResource> getPortByName(@PathVariable String name) {
        return portService.getPortByName(name)
                .map(PortResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{portId}")
    public ResponseEntity<Void> deletePort(@PathVariable String portId) {
        portService.deletePort(portId);
        return ResponseEntity.noContent().build();
    }
}