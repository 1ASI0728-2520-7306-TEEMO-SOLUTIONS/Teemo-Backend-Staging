package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.application.internal.outboundservices.email;

public interface EmailService {
    void sendEmail(String to, String subject, String body);
}
