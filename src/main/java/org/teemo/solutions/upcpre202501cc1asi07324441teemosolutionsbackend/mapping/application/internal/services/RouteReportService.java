package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.services;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.services.exceptions.RouteReportGenerationException;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.RouteHistory;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.exceptions.RouteHistoryNotFoundException;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.RouteHistorySource;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.RouteHistoryStatus;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.repositories.PortRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class RouteReportService {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneOffset.UTC);

    private final RouteHistoryService routeHistoryService;
    private final PortRepository portRepository;

    public RouteReportService(RouteHistoryService routeHistoryService,
                              PortRepository portRepository) {
        this.routeHistoryService = routeHistoryService;
        this.portRepository = portRepository;
    }

    public byte[] generatePdfReport(String historyId) {
        RouteReportData data = buildData(historyId);
        return renderPdf(data);
    }

    public byte[] generateExcelReport(String historyId) {
        RouteReportData data = buildData(historyId);
        return renderExcel(data);
    }

    private RouteReportData buildData(String historyId) {
        RouteHistory history = routeHistoryService.findById(historyId)
                .orElseThrow(() -> new RouteHistoryNotFoundException(historyId));

        PortDescriptor origin = describePort(history.originPortId(), history.originPortName());
        PortDescriptor destination = describePort(history.destinationPortId(), history.destinationPortName());
        List<PortDescriptor> waypoints = history.waypointPortIds().stream()
                .map(id -> describePort(id, null))
                .toList();

        Duration plannedDuration = history.durationEstimate() != null
                ? Duration.ofMinutes(Math.round(history.durationEstimate() * 60))
                : null;

        Instant departure = history.computedAt();
        Instant arrival = (plannedDuration != null && departure != null)
                ? departure.plus(plannedDuration)
                : null;

        List<RouteReportEvent> events = buildEvents(departure, arrival, origin, waypoints, destination);

        String shipmentId = history.routeId() != null ? history.routeId() : history.id();
        String routeLabel = "%s -> %s".formatted(
                formatLocation(origin),
                formatLocation(destination)
        );

        return new RouteReportData(
                history.id(),
                shipmentId,
                routeLabel,
                origin,
                destination,
                departure,
                arrival,
                plannedDuration,
                history.totalDistance(),
                history.costEstimate(),
                history.status(),
                history.source(),
                events
        );
    }

    private PortDescriptor describePort(String portId, String fallbackName) {
        if (portId == null && fallbackName == null) {
            return null;
        }
        if (portId == null) {
            return new PortDescriptor(null, fallbackName, null);
        }
        return portRepository.findById(portId)
                .map(doc -> new PortDescriptor(doc.getId(), doc.getName(), doc.getContinent()))
                .orElse(new PortDescriptor(portId, fallbackName != null ? fallbackName : portId, null));
    }

    private List<RouteReportEvent> buildEvents(Instant departure,
                                               Instant arrival,
                                               PortDescriptor origin,
                                               List<PortDescriptor> waypoints,
                                               PortDescriptor destination) {
        List<RouteReportEvent> events = new ArrayList<>();
        if (departure != null) {
            events.add(new RouteReportEvent(
                    departure,
                    "DEPARTURE",
                    "Zarpe desde %s".formatted(formatLocation(origin)),
                    formatLocation(origin)
            ));
        }
        int segments = waypoints.size() + 1;
        for (int i = 0; i < waypoints.size(); i++) {
            double ratio = segments > 0 ? (double) (i + 1) / segments : 0.0;
            Instant waypointTime = interpolate(departure, arrival, ratio);
            PortDescriptor waypoint = waypoints.get(i);
            events.add(new RouteReportEvent(
                    waypointTime,
                    "WAYPOINT",
                    "Paso por %s".formatted(formatLocation(waypoint)),
                    formatLocation(waypoint)
            ));
        }
        if (arrival != null) {
            events.add(new RouteReportEvent(
                    arrival,
                    "ARRIVAL",
                    "Arribo a %s".formatted(formatLocation(destination)),
                    formatLocation(destination)
            ));
        }
        return events;
    }

    private Instant interpolate(Instant start, Instant end, double ratio) {
        if (start == null || end == null) {
            return null;
        }
        long seconds = Duration.between(start, end).toSeconds();
        long offset = Math.round(seconds * ratio);
        return start.plusSeconds(offset);
    }

    private byte[] renderPdf(RouteReportData data) {
        List<String> lines = new ArrayList<>();
        lines.add("Reporte de Ruta");
        lines.add("Ruta: " + data.routeLabel());
        lines.add("Generado: " + DATE_FORMATTER.format(Instant.now()));
        lines.add("");
        lines.add("ID de envio: " + data.shipmentId());
        lines.add("Origen: " + formatLocation(data.origin()));
        lines.add("Destino: " + formatLocation(data.destination()));
        lines.add("Fecha de salida: " + formatInstant(data.departure()));
        lines.add("Fecha estimada llegada: " + formatInstant(data.arrival()));
        lines.add("Duracion estimada: " + formatDuration(data.plannedDuration()));
        lines.add("Distancia total (km): " + formatDouble(data.totalDistanceKm()));
        lines.add("Costo estimado: " + formatDouble(data.costEstimate()));
        lines.add("Estado: " + (data.status() != null ? data.status().name() : "-"));
        lines.add("Fuente: " + (data.source() != null ? data.source().name() : "-"));
        lines.add("");
        lines.add("Eventos del viaje:");
        if (data.events().isEmpty()) {
            lines.add(" - No se registraron eventos");
        } else {
            for (RouteReportEvent event : data.events()) {
                String line = " - %s | %s | %s | %s".formatted(
                        formatInstant(event.timestamp()),
                        event.type(),
                        event.description(),
                        event.location()
                );
                lines.add(line);
            }
        }
        return buildSimplePdf(lines);
    }

    private byte[] renderExcel(RouteReportData data) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Route Report");

            CellStyle boldStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldStyle.setFont(boldFont);

            int rowIdx = 0;
            Row titleRow = sheet.createRow(rowIdx++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Reporte de Ruta");
            titleCell.setCellStyle(boldStyle);

            rowIdx = writeSummaryRow(sheet, rowIdx, "ID de envío", data.shipmentId(), boldStyle);
            rowIdx = writeSummaryRow(sheet, rowIdx, "Ruta", data.routeLabel(), boldStyle);
            rowIdx = writeSummaryRow(sheet, rowIdx, "Origen", formatLocation(data.origin()), boldStyle);
            rowIdx = writeSummaryRow(sheet, rowIdx, "Destino", formatLocation(data.destination()), boldStyle);
            rowIdx = writeSummaryRow(sheet, rowIdx, "Fecha de salida", formatInstant(data.departure()), boldStyle);
            rowIdx = writeSummaryRow(sheet, rowIdx, "Fecha estimada llegada", formatInstant(data.arrival()), boldStyle);
            rowIdx = writeSummaryRow(sheet, rowIdx, "Duración estimada", formatDuration(data.plannedDuration()), boldStyle);
            rowIdx = writeSummaryRow(sheet, rowIdx, "Distancia total (km)", formatDouble(data.totalDistanceKm()), boldStyle);
            rowIdx = writeSummaryRow(sheet, rowIdx, "Costo estimado", formatDouble(data.costEstimate()), boldStyle);
            rowIdx = writeSummaryRow(sheet, rowIdx, "Estado", data.status() != null ? data.status().name() : "-", boldStyle);
            rowIdx = writeSummaryRow(sheet, rowIdx, "Fuente", data.source() != null ? data.source().name() : "-", boldStyle);

            rowIdx += 1;
            Row eventsTitleRow = sheet.createRow(rowIdx++);
            Cell eventsTitleCell = eventsTitleRow.createCell(0);
            eventsTitleCell.setCellValue("Eventos del viaje");
            eventsTitleCell.setCellStyle(boldStyle);

            Row headerRow = sheet.createRow(rowIdx++);
            String[] headers = {"Fecha", "Tipo", "Descripción", "Ubicación"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(boldStyle);
            }

            if (data.events().isEmpty()) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue("No se registraron eventos");
            } else {
                for (RouteReportEvent event : data.events()) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(formatInstant(event.timestamp()));
                    row.createCell(1).setCellValue(event.type());
                    row.createCell(2).setCellValue(event.description());
                    row.createCell(3).setCellValue(event.location());
                }
            }

            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RouteReportGenerationException("Error al generar Excel", e);
        }
    }

    private int writeSummaryRow(Sheet sheet, int rowIdx, String label, String value, CellStyle boldStyle) {
        Row row = sheet.createRow(rowIdx++);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(boldStyle);
        row.createCell(1).setCellValue(value);
        return rowIdx;
    }

    private String formatInstant(Instant instant) {
        return instant == null ? "-" : DATE_FORMATTER.format(instant);
    }

    private String formatLocation(PortDescriptor descriptor) {
        if (descriptor == null) {
            return "-";
        }
        if (descriptor.continent() == null || descriptor.continent().isBlank()) {
            return descriptor.name();
        }
        return "%s (%s)".formatted(descriptor.name(), descriptor.continent());
    }

    private String formatDuration(Duration duration) {
        if (duration == null) {
            return "-";
        }
        long days = duration.toDays();
        long hours = duration.minusDays(days).toHours();
        long minutes = duration.minusDays(days).minusHours(hours).toMinutes();
        return "%dd %dh %dm".formatted(days, hours, minutes);
    }

    private String formatDouble(Double value) {
        return value == null ? "-" : String.format("%.2f", value);
    }

    private byte[] buildSimplePdf(List<String> lines) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write("%PDF-1.4\n".getBytes(StandardCharsets.US_ASCII));
            List<Integer> offsets = new ArrayList<>();
            offsets.add(0);

            offsets.add(out.size());
            writeObject(out, "1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj\n");

            offsets.add(out.size());
            writeObject(out, "2 0 obj << /Type /Pages /Kids [3 0 R] /Count 1 >> endobj\n");

            offsets.add(out.size());
            writeObject(out, "3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >> endobj\n");

            String content = buildContentStream(lines);
            int contentLength = content.getBytes(StandardCharsets.UTF_8).length;

            offsets.add(out.size());
            writeObject(out, "4 0 obj << /Length " + contentLength + " >> stream\n");
            writeObject(out, content);
            writeObject(out, "\nendstream\nendobj\n");

            offsets.add(out.size());
            writeObject(out, "5 0 obj << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> endobj\n");

            int xrefStart = out.size();
            writeObject(out, "xref\n0 6\n");
            writeObject(out, "0000000000 65535 f \n");
            for (int i = 1; i <= 5; i++) {
                writeObject(out, String.format("%010d 00000 n \n", offsets.get(i)));
            }
            writeObject(out, "trailer << /Size 6 /Root 1 0 R >>\n");
            writeObject(out, "startxref\n" + xrefStart + "\n%%EOF");
            return out.toByteArray();
        } catch (IOException e) {
            throw new RouteReportGenerationException("Error al generar PDF", e);
        }
    }

    private void writeObject(ByteArrayOutputStream out, String content) throws IOException {
        out.write(content.getBytes(StandardCharsets.UTF_8));
    }

    private String buildContentStream(List<String> lines) {
        StringBuilder sb = new StringBuilder();
        sb.append("BT\n/F1 12 Tf\n16 TL\n50 780 Td\n");
        for (String line : lines) {
            sb.append("(").append(escapePdfText(line)).append(") Tj\n");
            sb.append("T*\n");
        }
        sb.append("ET");
        return sb.toString();
    }

    private String escapePdfText(String input) {
        return input.replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)");
    }

    private record RouteReportData(
            String historyId,
            String shipmentId,
            String routeLabel,
            PortDescriptor origin,
            PortDescriptor destination,
            Instant departure,
            Instant arrival,
            Duration plannedDuration,
            Double totalDistanceKm,
            Double costEstimate,
            RouteHistoryStatus status,
            RouteHistorySource source,
            List<RouteReportEvent> events
    ) {}

    private record RouteReportEvent(Instant timestamp, String type, String description, String location) {}

    private record PortDescriptor(String id, String name, String continent) {}
}
