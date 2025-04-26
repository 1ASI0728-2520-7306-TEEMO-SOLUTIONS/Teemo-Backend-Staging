package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.interfaces.rest.resources;

import java.util.List;

public record UserResource(Long id, String username, List<String> roles) {
}
