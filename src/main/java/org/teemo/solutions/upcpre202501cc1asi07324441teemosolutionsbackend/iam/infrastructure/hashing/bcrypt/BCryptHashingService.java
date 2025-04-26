package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.infrastructure.hashing.bcrypt;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.application.internal.outboundservices.hashing.HashingService;

public interface BCryptHashingService extends HashingService, PasswordEncoder {
}
