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


/**
 * Controlador REST para la gestión de mapas.
 * Proporciona endpoints para crear y obtener mapas.
 */
@RestController
@RequestMapping(value = "/api/v1/maps", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Maps", description = "Map Management Endpoints")
@RequiredArgsConstructor
public class MapController {

    /**
     * Servicio para manejar comandos relacionados con mapas.
     */
    private final MapCommandService mapCommandService;

    /**
     * Servicio para manejar consultas relacionadas con mapas.
     */
    private final MapQueryService mapQueryService;

    /**
     * Crea un nuevo mapa en el sistema.
     *
     * @param resource Objeto que contiene los datos necesarios para crear un mapa.
     * @return Un recurso que representa el mapa creado o un error si no se pudo crear.
     */
    @PostMapping
    public ResponseEntity<MapResource> createMap(@RequestBody CreateMapResource resource) {
        // Convierte el recurso recibido en un comando para la capa de dominio.
        CreateMapCommand command = CreateMapCommandFromResourceAssembler.toCommandFromResource(resource);
        var map = mapCommandService.handle(command);

        // Si no se pudo crear el mapa, retorna un error 400.
        if (map.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // Convierte la entidad del mapa creado en un recurso para la respuesta.
        MapResource mapResource = MapResourceFromEntityAssembler.toResourceFromEntity(map.get());
        return new ResponseEntity<>(mapResource, HttpStatus.CREATED);
    }

    /**
     * Obtiene un mapa específico por su ID.
     *
     * @param mapId El ID del mapa a buscar.
     * @return Un recurso que representa el mapa encontrado o un error 400 si no existe.
     */
    @GetMapping("/{mapId}")
    public ResponseEntity<MapResource> getMapById(@PathVariable Long mapId) {
        // Crea una consulta para obtener un mapa por su ID.
        var query = new GetMapByIdQuery(mapId);
        var map = mapQueryService.handle(query);

        // Si no se encuentra el mapa, retorna un error 400.
        if (map.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // Convierte la entidad del mapa encontrado en un recurso para la respuesta.
        MapResource mapResource = MapResourceFromEntityAssembler.toResourceFromEntity(map.get());
        return ResponseEntity.ok(mapResource);
    }
}
