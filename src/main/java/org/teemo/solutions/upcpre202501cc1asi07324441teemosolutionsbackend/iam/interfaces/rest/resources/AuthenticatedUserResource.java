package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.interfaces.rest.resources;

import java.util.List;

public record AuthenticatedUserResource(String id,
                                        String username,
                                        String token,
                                        List<String> roles) {
}
