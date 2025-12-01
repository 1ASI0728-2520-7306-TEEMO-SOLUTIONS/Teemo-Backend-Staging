package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.application.internal.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.application.internal.querymodels.NotificationPageView;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.application.internal.querymodels.NotificationView;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.domain.model.entities.Notification;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.domain.model.exceptions.NotificationNotFoundException;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.domain.model.valueobjects.NotificationAction;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.domain.model.valueobjects.NotificationAudience;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.domain.model.valueobjects.NotificationType;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.infrastructure.persistence.sdmdb.documents.NotificationDocument;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.infrastructure.persistence.sdmdb.documents.NotificationRecipientDocument;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.infrastructure.persistence.sdmdb.repositories.NotificationRecipientRepository;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.infrastructure.persistence.sdmdb.repositories.NotificationRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationRecipientRepository recipientRepository;
    private final Clock clock;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   NotificationRecipientRepository recipientRepository,
                                   Clock clock) {
        this.notificationRepository = notificationRepository;
        this.recipientRepository = recipientRepository;
        this.clock = clock;
    }

    @Override
    public Notification registerPortStatusChange(Port port, NotificationAction action, String reason, String performedBy) {
        Objects.requireNonNull(port, "port");
        Objects.requireNonNull(action, "action");
        Instant now = clock.instant();
        NotificationDocument document = new NotificationDocument();
        document.setId(UUID.randomUUID().toString());
        document.setType(NotificationType.PORT_STATUS_CHANGE);
        document.setTitle(buildPortStatusTitle(port.getName(), action));
        document.setMessage(buildPortStatusMessage(port.getName(), action, reason, now));
        document.setPortId(port.getId());
        document.setPortName(port.getName());
        document.setAction(action);
        document.setReason(StringUtils.hasText(reason) ? reason.trim() : null);
        document.setPerformedBy(performedBy);
        document.setAudience(NotificationAudience.ALL);
        document.setCreatedAt(now);
        document.setTargetUserId(null);
        notificationRepository.save(document);
        return document.toDomain();
    }

    @Override
    public NotificationPageView getNotificationsForUser(String userId, Pageable pageable) {
        Objects.requireNonNull(userId, "userId");
        Page<NotificationDocument> page = notificationRepository.findByAudience(NotificationAudience.ALL, pageable);
        List<NotificationDocument> documents = page.getContent();
        Map<String, Instant> readIndex = documents.isEmpty()
                ? Map.of()
                : recipientRepository.findByNotificationIdInAndUserId(
                                documents.stream().map(NotificationDocument::getId).toList(),
                                userId
                        )
                        .stream()
                        .collect(Collectors.toMap(NotificationRecipientDocument::getNotificationId,
                                NotificationRecipientDocument::getReadAt));

        List<NotificationView> views = documents.stream()
                .map(doc -> new NotificationView(doc.toDomain(), readIndex.get(doc.getId())))
                .toList();

        return new NotificationPageView(
                views,
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize()
        );
    }

    @Override
    public void markNotificationAsRead(String notificationId, String userId) {
        Objects.requireNonNull(userId, "userId");
        NotificationDocument document = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException("Notificacion no encontrada: " + notificationId));
        ensureUserCanAccess(document, userId);
        upsertRecipient(notificationId, userId, clock.instant());
    }

    @Override
    public void markNotificationsAsRead(Collection<String> notificationIds, String userId) {
        Objects.requireNonNull(userId, "userId");
        if (notificationIds == null || notificationIds.isEmpty()) {
            return;
        }
        Set<String> uniqueIds = notificationIds.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (uniqueIds.isEmpty()) {
            return;
        }
        List<NotificationDocument> documents = notificationRepository.findAllById(uniqueIds);
        Instant now = clock.instant();
        documents.stream()
                .filter(doc -> userCanAccess(doc, userId))
                .forEach(doc -> upsertRecipient(doc.getId(), userId, now));
    }

    private String buildPortStatusTitle(String portName, NotificationAction action) {
        String subject = portName != null ? portName : "desconocido";
        return action.isDisabled()
                ? "Puerto " + subject + " deshabilitado"
                : "Puerto " + subject + " habilitado";
    }

    private String buildPortStatusMessage(String portName, NotificationAction action, String reason, Instant timestamp) {
        String subject = portName != null ? portName : "desconocido";
        String formattedDate = DateTimeFormatter.ISO_INSTANT.format(timestamp);
        if (action.isDisabled()) {
            StringBuilder builder = new StringBuilder("El puerto ")
                    .append(subject)
                    .append(" quedo inoperativo el ")
                    .append(formattedDate)
                    .append(".");
            if (StringUtils.hasText(reason)) {
                builder.append(" Motivo: ").append(reason.trim());
            }
            return builder.toString();
        }
        return "El puerto " + subject + " fue habilitado nuevamente el " + formattedDate + ".";
    }

    private void upsertRecipient(String notificationId, String userId, Instant readAt) {
        recipientRepository.findByNotificationIdAndUserId(notificationId, userId)
                .ifPresentOrElse(recipient -> {
                    if (recipient.getReadAt() == null) {
                        recipient.setReadAt(readAt);
                        recipientRepository.save(recipient);
                    }
                }, () -> {
                    NotificationRecipientDocument recipient = new NotificationRecipientDocument();
                    recipient.setId(UUID.randomUUID().toString());
                    recipient.setNotificationId(notificationId);
                    recipient.setUserId(userId);
                    recipient.setReadAt(readAt);
                    recipient.setCreatedAt(clock.instant());
                    recipientRepository.save(recipient);
                });
    }

    private void ensureUserCanAccess(NotificationDocument document, String userId) {
        if (!userCanAccess(document, userId)) {
            throw new NotificationNotFoundException("Notificacion no disponible para este usuario");
        }
    }

    private boolean userCanAccess(NotificationDocument document, String userId) {
        if (document.getAudience() == NotificationAudience.ALL) {
            return true;
        }
        return document.getAudience() == NotificationAudience.USER
                && userId != null
                && Objects.equals(document.getTargetUserId(), userId);
    }
}
