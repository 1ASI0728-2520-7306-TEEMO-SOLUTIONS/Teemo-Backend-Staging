package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.application.internal.repositoriesimpl;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.domain.model.aggregates.User;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {

    MongoTemplate mongoTemplate;

    UserRepositoryImpl(MongoTemplate _mongoTemplate) {
        this.mongoTemplate = _mongoTemplate;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(mongoTemplate.findOne(Query.query(Criteria.where("username").is(username)), User.class));
    }

    @Override
    public Optional<User> findById(String id) {
        return Optional.ofNullable(mongoTemplate.findOne(Query.query(Criteria.where("id").is(id)), User.class));
    }

    @Override
    public List<User> findAll() {
        return mongoTemplate.findAll(User.class);
    }

    @Override
    public void saveUser(User user) {
        mongoTemplate.save(user);
    }

    @Override
    public boolean existsByUsername(String username) {
        return mongoTemplate.exists(Query.query(Criteria.where("username").is(username)), User.class);
    }
}