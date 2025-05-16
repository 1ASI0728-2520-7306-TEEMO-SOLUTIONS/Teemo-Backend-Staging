package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.repositories;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;

import java.util.List;
import java.util.Optional;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
public class MongoPortRepository implements PortRepository {
    MongoTemplate mongoTemplate;

    public MongoPortRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void eliminatePort(String id) {
        Port port = mongoTemplate.findById(id, Port.class);
        assert port != null;
        mongoTemplate.remove(port);
    }

    @Override
    public Port savePort(Port port) {
        return mongoTemplate.save(port);
    }

    @Override
    public Optional<Port> findById(String id) {
        return Optional.ofNullable(mongoTemplate.findById(id, Port.class));
    }

    @Override
    public List<Port> getAll() {
        return mongoTemplate.findAll(Port.class);
    }

    @Override
    public List<Port> findByName(String name) {
        return mongoTemplate.find(query(where("name").is(name)), Port.class);
    }

    @Override
    public void saveAll(List<Port> ports) {
        for (Port port : ports) {
            mongoTemplate.save(port);
        }
    }

    @Override
    public Optional<Port> getPortByNameAndContinent(String name, String continent) {
        return Optional.ofNullable(mongoTemplate.findOne(query(where("name").is(name).and("continent").is(continent)), Port.class));
    }
}