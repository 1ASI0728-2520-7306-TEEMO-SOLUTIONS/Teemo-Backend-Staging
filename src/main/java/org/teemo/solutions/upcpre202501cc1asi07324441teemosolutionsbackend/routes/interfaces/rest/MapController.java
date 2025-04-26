package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.interfaces.rest;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.model.commands.CreateMapCommand;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.model.queries.GetMapByIdQuery;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.services.MapCommandService;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.services.MapQueryService;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.interfaces.rest.resources.CreateMapResource;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.interfaces.rest.resources.MapResource;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.interfaces.rest.transform.CreateMapCommandFromResourceAssembler;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.interfaces.rest.transform.MapResourceFromEntityAssembler;

@RestController
@RequestMapping(value = "/api/v1/maps", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Maps", description = "Map Management Endpoints")
@RequiredArgsConstructor
public class MapController {

    private final MapCommandService mapCommandService;
    private final MapQueryService mapQueryService;

    // ============================
    // Crear un nuevo mapa
    // ============================
    @PostMapping
    public ResponseEntity<MapResource> createMap(@RequestBody CreateMapResource resource) {
        CreateMapCommand command = CreateMapCommandFromResourceAssembler.toCommandFromResource(resource);
        var map = mapCommandService.handle(command);

        if (map.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        MapResource mapResource = MapResourceFromEntityAssembler.toResourceFromEntity(map.get());
        return new ResponseEntity<>(mapResource, HttpStatus.CREATED);
    }

    // ============================
    // Obtener un mapa por ID
    // ============================
    @GetMapping("/{mapId}")
    public ResponseEntity<MapResource> getMapById(@PathVariable Long mapId) {
        var query = new GetMapByIdQuery(mapId);
        var map = mapQueryService.handle(query);

        if (map.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        MapResource mapResource = MapResourceFromEntityAssembler.toResourceFromEntity(map.get());
        return ResponseEntity.ok(mapResource);
    }
}
