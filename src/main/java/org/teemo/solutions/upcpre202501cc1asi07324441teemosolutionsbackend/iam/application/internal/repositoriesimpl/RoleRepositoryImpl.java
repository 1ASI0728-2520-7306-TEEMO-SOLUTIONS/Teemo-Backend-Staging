package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.application.internal.repositoriesimpl;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.domain.model.entities.Role;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.domain.model.valueobjects.Roles;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.infrastructure.persistence.jpa.repositories.RoleRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class RoleRepositoryImpl implements RoleRepository {
    MongoTemplate mongoTemplate;
    RoleRepositoryImpl(MongoTemplate _mongoTemplate) {
        this.mongoTemplate = _mongoTemplate;
    }
    @Override
    public Optional<Role> findByName(Roles name) {
        return Optional.ofNullable(mongoTemplate.findOne(Query.query(Criteria.where("name").is(name)), Role.class));
    }
    @Override
    public List<Role> findAll() {
        return mongoTemplate.findAll(Role.class);
    }
    @Override
    public void saveRole(Role role) {
        mongoTemplate.save(role);
    }
    @Override
    public boolean existsByName(Roles name) {
        return mongoTemplate.exists(Query.query(Criteria.where("name").is(name)), Role.class);
    }
}