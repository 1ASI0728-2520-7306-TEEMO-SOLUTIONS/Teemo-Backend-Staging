package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.application.internal.outboundservices.hashing;

public interface HashingService {
    String encode(CharSequence rawPassword);
    boolean matches(CharSequence rawPassword, String encodePassword);
}
