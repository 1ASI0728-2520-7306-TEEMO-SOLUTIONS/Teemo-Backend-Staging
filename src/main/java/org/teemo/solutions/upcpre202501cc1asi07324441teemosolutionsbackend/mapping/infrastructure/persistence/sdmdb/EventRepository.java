package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb;

import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.domain.EventDocument;

import java.util.List;

public interface EventRepository {
    List<EventDocument> findValidEvents();
}
