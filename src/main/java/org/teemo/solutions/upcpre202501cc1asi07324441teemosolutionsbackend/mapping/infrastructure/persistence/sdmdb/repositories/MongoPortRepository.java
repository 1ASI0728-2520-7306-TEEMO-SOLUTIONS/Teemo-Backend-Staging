package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.repositories;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.documents.PortDocument;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.mappers.PortMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Repository
public class MongoPortRepository implements PortRepository {
    private final MongoTemplate mongoTemplate;
    private final PortMapper portMapper;

    public MongoPortRepository(MongoTemplate mongoTemplate, PortMapper portMapper) {
        this.mongoTemplate = mongoTemplate;
        this.portMapper = portMapper;
    }

    @Override
    public Port savePort(Port port) {
        PortDocument doc = portMapper.toDocument(port);
        mongoTemplate.save(doc);
        return portMapper.toDomain(doc);
    }

    // Utilizaremos las operaciones de inserción en lote (bulk insert) que MongoTemplate proporciona.
    // Esto nos permite enviar toda la lista de documentos a la base de datos en una sola operación de red, lo que es órdenes de magnitud más rápido.
    @Override
    public void saveAll(List<Port> ports) {
        // 1. Convierte la lista de entidades de dominio a una lista de documentos de persistencia
        List<PortDocument> portDocuments = ports.stream()
                .map(portMapper::toDocument)
                .toList();

        // 2. Inserta todos los documentos en una sola operación de lote
        if (!portDocuments.isEmpty()) {
            mongoTemplate.insertAll(portDocuments);
        }
    }

    @Override
    public void eliminatePort(String id) {
        // 1. Crea una consulta para encontrar el documento por su ID.
        // El campo ID en MongoDB se llama "_id" por defecto.
        Query query = new Query(Criteria.where("_id").is(id));

        // 2. Ejecuta la operación de eliminación directamente usando la consulta.
        // Esto elimina el documento que coincide con la consulta, si existe.
        // Si no existe, simplemente no hace nada y no lanza un error.

        // Asumiendo que has implementado el Punto 6 y usas PortDocument:
        mongoTemplate.remove(query, PortDocument.class);

        /*
        // Si aún estuvieras usando la entidad Port directamente:
        mongoTemplate.remove(query, Port.class);
        */
    }

    @Override
    public List<Port> findByName(String name) {
        return mongoTemplate.find(Query.query(where("name").is(name)), PortDocument.class).stream().map(portMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<Port> findById(String id) {
        PortDocument doc = mongoTemplate.findById(id, PortDocument.class);
        return Optional.ofNullable(doc).map(portMapper::toDomain);
    }

    @Override
    public List<Port> getAll() {
        return mongoTemplate.findAll(PortDocument.class)
                .stream()
                .map(portMapper::toDomain)
                .toList();
    }

    // ... y así sucesivamente para todos los métodos.
    @Override
    public Optional<Port> getPortByNameAndContinent(String name, String continent) {
        Query query = new Query(Criteria.where("name").is(name).and("continent").is(continent));
        PortDocument doc = mongoTemplate.findOne(query, PortDocument.class);
        return Optional.ofNullable(doc).map(portMapper::toDomain);
    }

    @Override
    public boolean existsByNameAndContinent(String name, String continent) {
        Query query = new Query(Criteria.where("name").is(name).and("continent").is(continent));
        return mongoTemplate.exists(query, PortDocument.class);
    }

    // ...etc
}