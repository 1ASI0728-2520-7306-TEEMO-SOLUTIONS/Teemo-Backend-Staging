package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.services.PortService;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.services.RouteService;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.Coordinates;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.documents.PortDocument;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.documents.RouteDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class DataInitializer {

    private final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final PortService portService;
    private final RouteService routeService;

    @Autowired
    public DataInitializer(PortService portService, RouteService routeService) {
        this.portService = portService;
        this.routeService = routeService;
    }

    @PostConstruct
    public void init() {
        try {
            logger.info("Limpiando colecciones de rutas y puertos existentes...");
            routeService.deleteAllRoutes();
            portService.deleteAllPorts();
            logger.info("Colecciones limpiadas exitosamente.");
        } catch (Exception e) {
            logger.error("Error limpiando las colecciones: {}", e.getMessage(), e);
        }

        // =================================================================================
        // PASO 2: INICIALIZAR LOS DATOS DESDE CERO
        // =================================================================================
        logger.info("Iniciando la inserción de datos...");
        initPorts();
        initRoutes();

        // =================================================================================
        // PASO 3: VALIDAR LOS DATOS RECIÉN INSERTADOS
        // =================================================================================
        logger.info("Validando los datos insertados...");
        validateRoutes();
        logger.info("¡Datos inicializados y validados correctamente!");
    }

    private void initPorts() {
        try {
            List<Port> ports = List.of(
                    // ========== ASIA ==========
                    new Port("Tokyo", new Coordinates(35.6895, 139.6917), "Asia"),
                    new Port("Busan", new Coordinates(35.1796, 129.0756), "Asia"),
                    new Port("Shanghai", new Coordinates(31.2304, 121.4737), "Asia"),
                    new Port("Tianjin", new Coordinates(39.0842, 117.2010), "Asia"),
                    new Port("Hong Kong", new Coordinates(22.3193, 114.1694), "Asia"),
                    new Port("Quanzhou", new Coordinates(24.9139, 118.5858), "Asia"),
                    new Port("Zhanjiang", new Coordinates(21.1967, 110.4031), "Asia"),
                    new Port("Muscat", new Coordinates(23.6142, 58.5458), "Asia"),
                    new Port("Singapore", new Coordinates(1.3521, 103.8198), "Asia"),
                    new Port("Jakarta", new Coordinates(-6.2088, 106.8456), "Asia"),
                    new Port("Mumbai", new Coordinates(19.0760, 72.8777), "Asia"),
                    new Port("Chennai", new Coordinates(13.0827, 80.2707), "Asia"),
                    new Port("Tuticorin", new Coordinates(8.7642, 78.1348), "Asia"),
                    new Port("Dubai", new Coordinates(25.2769, 55.2962), "Asia"),
                    new Port("Aden", new Coordinates(12.8000, 45.0333), "Asia"),
                    new Port("Vladivostok", new Coordinates(43.1056, 131.8735), "Asia"),
                    new Port("Uelen", new Coordinates(66.1667, -169.8000), "Asia"),

                    // ========== AMÉRICA ==========
                    new Port("Arkits", new Coordinates(73.0, -128.0), "América"),
                    new Port("Callao", new Coordinates(-12.0564, -77.1319), "América"),
                    new Port("Buenos Aires", new Coordinates(-34.6037, -58.3816), "América"),
                    new Port("Valparaíso", new Coordinates(-33.0472, -71.6128), "América"),
                    new Port("Rio de Janeiro", new Coordinates(-22.9068, -43.1729), "América"),
                    new Port("Fort Lauderdale", new Coordinates(26.1224, -80.1373), "América"),
                    new Port("Guayaquil", new Coordinates(-2.1962, -79.8862), "América"),
                    new Port("Balboa", new Coordinates(8.9833, -79.5167), "América"),
                    new Port("Manzanillo", new Coordinates(19.0514, -104.3158), "América"),
                    new Port("Long Beach", new Coordinates(33.7709, -118.1937), "América"),
                    new Port("New York", new Coordinates(40.7128, -74.0060), "América"),
                    new Port("Houston", new Coordinates(29.7604, -95.3698), "América"),
                    new Port("San Francisco", new Coordinates(37.7749, -122.4194), "América"),
                    new Port("Vancouver", new Coordinates(49.2827, -123.1207), "América"),
                    new Port("Prince Rupert", new Coordinates(54.3150, -130.3208), "América"),
                    new Port("Cayena", new Coordinates(4.9372, -52.3260), "América"),
                    new Port("Cartagena", new Coordinates(10.3932, -75.4832), "América"),
                    new Port("Puerto Cabello", new Coordinates(10.4731, -68.0125), "América"),
                    new Port("San Antonio", new Coordinates(-33.5983, -71.6123), "América"),
                    new Port("Montreal", new Coordinates(45.5017, -73.5673), "América"),
                    new Port("Rio Grande", new Coordinates(-32.0351, -52.0986), "América"),

                    // ========== ÁFRICA ==========
                    new Port("Ciudad del Cabo", new Coordinates(-33.9249, 18.4241), "África"),
                    new Port("Abiyán", new Coordinates(5.3599, -4.0084), "África"),
                    new Port("Durban", new Coordinates(-29.8833, 31.0500), "África"),
                    new Port("Mombasa", new Coordinates(-4.0435, 39.6682), "África"),
                    new Port("Toamasina", new Coordinates(-18.1443, 49.3958), "África"),
                    new Port("Port-Gentil", new Coordinates(-0.7167, 8.7833), "África"),
                    new Port("Nuakchot", new Coordinates(18.0731, -15.9582), "África"),
                    new Port("Dakar", new Coordinates(14.7167, -17.4677), "África"),
                    new Port("Lagos", new Coordinates(6.4541, 3.3947), "África"),
                    new Port("Walvis Bay", new Coordinates(-22.9587, 14.5058), "África"),
                    new Port("Luanda", new Coordinates(-8.8383, 13.2344), "África"),
                    new Port("Alexandría", new Coordinates(31.2001, 29.9187), "África"),
                    new Port("Casablanca", new Coordinates(33.5731, -7.5898), "África"),
                    new Port("Mogadishu", new Coordinates(2.0469, 45.3182), "África"),

                    // ========== EUROPA ==========
                    new Port("Odessa", new Coordinates(46.4825, 30.7233), "Europa"),
                    new Port("Murmansk", new Coordinates(68.9585, 33.0827), "Europa"),
                    new Port("Estambul", new Coordinates(41.0082, 28.9784), "Europa"),
                    new Port("Constanza", new Coordinates(44.1598, 28.6348), "Europa"),
                    new Port("Mersin", new Coordinates(36.7950, 34.6179), "Europa"),
                    new Port("Lisboa", new Coordinates(38.7223, -9.1393), "Europa"),
                    new Port("Valencia", new Coordinates(39.4699, -0.3763), "Europa"),
                    new Port("Hamburgo", new Coordinates(53.5488, 9.9872), "Europa"),
                    new Port("Rotterdam", new Coordinates(51.9244, 4.4777), "Europa"),
                    new Port("Le Havre", new Coordinates(49.4944, 0.1089), "Europa"),
                    new Port("Genova", new Coordinates(44.4056, 8.9463), "Europa"),
                    new Port("Atenas", new Coordinates(37.9838, 23.7275), "Europa"),
                    new Port("San Petersburgo", new Coordinates(59.9343, 30.3351), "Europa"),
                    new Port("Copenhague", new Coordinates(55.6761, 12.5683), "Europa"),
                    new Port("Stavanger", new Coordinates(58.9699, 5.7331), "Europa"),
                    new Port("Eupatoria", new Coordinates(45.2000, 33.3583), "Europa"),

                    // ========== OCEANÍA ==========
                    new Port("Sídney", new Coordinates(-33.8688, 151.2093), "Oceanía"),
                    new Port("Brisbane", new Coordinates(-27.4698, 153.0251), "Oceanía"),
                    new Port("Fremantle", new Coordinates(-32.0564, 115.7417), "Oceanía"),
                    new Port("Darwin", new Coordinates(-12.4634, 130.8456), "Oceanía"),
                    new Port("Port Moresby", new Coordinates(-9.4438, 147.1803), "Oceanía"),
                    // ... (después de los puertos existentes de América)
                    new Port("Ciudad de México", new Coordinates(19.4326, -99.1332), "América"), // Asumiendo puerto cercano
                    new Port("Colón", new Coordinates(9.3582, -79.9015), "América"),
                    new Port("San Salvador", new Coordinates(13.7942, -88.8965), "América"), // Asumiendo puerto cercano

                    // ... (después de los puertos existentes de África)
                    new Port("Puerto de Trípoli", new Coordinates(32.8872, 13.1913), "África"),
                    new Port("Puerto de Túnez", new Coordinates(36.8065, 10.1815), "África"), // Asumiendo puerto cercano

                    // ... (después de los puertos existentes de Asia)
                    new Port("Pyongyang", new Coordinates(39.0392, 125.7625), "Asia"), // Asumiendo puerto cercano
                    new Port("Taiwán", new Coordinates(23.6978, 120.9605), "Asia"), // Coordenadas generales de la isla
                    new Port("Puerto de Beirut", new Coordinates(33.8938, 35.5018), "Asia"),
                    new Port("Latakia", new Coordinates(35.5236, 35.7877), "Asia"),
                    new Port("Puerto de Haifa", new Coordinates(32.8184, 34.9895), "Asia"),
                    new Port("Puerto Said", new Coordinates(31.2653, 32.3019), "Asia"),

                    // ... (después de los puertos existentes de Europa)
                    new Port("Puerto de Crimea", new Coordinates(45.3481, 34.4993), "Europa") // Coordenadas generales
            );

            // Se insertan solo los que no existen para optimizar, aunque ya limpiamos antes.
            List<Port> newPorts = new ArrayList<>();
            for (Port port : ports) {
                if (!portService.existsByNameAndContinent(port.getName(), port.getContinent())) {
                    newPorts.add(port);
                }
            }

            if (!newPorts.isEmpty()) {
                portService.saveAllPorts(newPorts);
                logger.info("Insertados {} nuevos puertos.", newPorts.size());
            } else {
                logger.info("No se insertaron puertos nuevos, ya existían (o la lista estaba vacía).");
            }
        } catch (Exception e) {
            logger.error("Error inicializando puertos: {}", e.getMessage(), e);
        }
    }

    private void initRoutes() {
        try {
            List<RouteDocument> routes = List.of(
                    // ========== Rutas Asiáticas ==========
                    new RouteDocument("Tokyo", "Asia", "Busan", "Asia", 1000.0),
                    new RouteDocument("Shanghai", "Asia", "Busan", "Asia", 850.0),
                    new RouteDocument("Shanghai", "Asia", "Quanzhou", "Asia", 700.0),
                    new RouteDocument("Hong Kong", "Asia", "Quanzhou", "Asia", 800.0),
                    new RouteDocument("Hong Kong", "Asia", "Singapore", "Asia", 2600.0),
                    new RouteDocument("Singapore", "Asia", "Chennai", "Asia", 2700.0),
                    new RouteDocument("Singapore", "Asia", "Jakarta", "Asia", 900.0),
                    new RouteDocument("Mumbai", "Asia", "Muscat", "Asia", 1800.0),
                    new RouteDocument("Mumbai", "Asia", "Dubai", "Asia", 1900.0),
                    new RouteDocument("Aden", "Asia", "Muscat", "Asia", 1500.0),

                    // ========== Rutas Oceánicas ==========
                    new RouteDocument("Jakarta", "Asia", "Darwin", "Oceanía", 2700.0),
                    new RouteDocument("Darwin", "Oceanía", "Brisbane", "Oceanía", 3300.0),
                    new RouteDocument("Darwin", "Oceanía", "Port Moresby", "Oceanía", 1800.0),
                    new RouteDocument("Port Moresby", "Oceanía", "Brisbane", "Oceanía", 2500.0),
                    new RouteDocument("Brisbane", "Oceanía", "Sídney", "Oceanía", 900.0),

                    // ========== Rutas Transpacíficas ==========
                    new RouteDocument("San Francisco", "América", "Tokyo", "Asia", 8500.0),
                    new RouteDocument("Tokyo", "Asia", "Balboa", "América", 12500.0),
                    new RouteDocument("Singapore", "Asia", "Valparaíso", "América", 17500.0),

                    // ========== Rutas Americanas ==========
                    new RouteDocument("San Francisco", "América", "Balboa", "América", 3200.0),
                    new RouteDocument("San Francisco", "América", "Manzanillo", "América", 2100.0),
                    new RouteDocument("Long Beach", "América", "San Francisco", "América", 400.0),
                    new RouteDocument("Balboa", "América", "Guayaquil", "América", 2100.0),
                    new RouteDocument("Guayaquil", "América", "Callao", "América", 1150.0),
                    new RouteDocument("Puerto Cabello", "América", "Cartagena", "América", 200.0),
                    new RouteDocument("San Antonio", "América", "Valparaíso", "América", 1100.0),
                    new RouteDocument("Rio Grande", "América", "Buenos Aires", "América", 800.0),
                    new RouteDocument("Rio de Janeiro", "América", "Buenos Aires", "América", 2000.0),
                    new RouteDocument("Cartagena", "América", "Balboa", "América", 800.0),
                    new RouteDocument("Callao", "América", "Valparaíso", "América", 1500.0),
                    new RouteDocument("Valparaíso", "América", "Buenos Aires", "América", 2200.0),

                    new RouteDocument("Balboa", "América", "Manzanillo", "América", 2700.0),
                    new RouteDocument("Manzanillo", "América", "Long Beach", "América", 2800.0),
                    new RouteDocument("New York", "América", "Fort Lauderdale", "América", 1600.0),
                    new RouteDocument("Vancouver", "América", "San Francisco", "América", 1900.0),
                    new RouteDocument("Buenos Aires", "América", "Rio de Janeiro", "América", 2000.0),
                    new RouteDocument("Rio de Janeiro", "América", "Rio Grande", "América", 800.0),

                    // ========== Rutas Áfricanas ==========
                    new RouteDocument("Walvis Bay", "África", "Luanda", "África", 1800.0),
                    new RouteDocument("Mogadishu", "África", "Aden", "Asia", 1200.0),
                    new RouteDocument("Ciudad del Cabo", "África", "Walvis Bay", "África", 1300.0),
                    new RouteDocument("Dakar", "África", "Casablanca", "África", 2600.0),
                    new RouteDocument("Lagos", "África", "Luanda", "África", 2400.0),

                    new RouteDocument("Houston", "América", "New York", "América", 2500.0),
                    new RouteDocument("Prince Rupert", "América", "Vancouver", "América", 800.0),

                    // ========== Rutas Transatlánticas ==========
                    new RouteDocument("Rotterdam", "Europa", "New York", "América", 5800.0),
                    new RouteDocument("Lisboa", "Europa", "Rio de Janeiro", "América", 7200.0),

                    // --- AÑADE ESTA RUTA PUENTE ---
                    new RouteDocument("Luanda", "África", "Ciudad del Cabo", "África", 2500.0), // Conecta el centro-oeste con el sur
                    new RouteDocument("Ciudad del Cabo", "África", "Durban", "África", 1300.0), // Conecta el sur con el este
                    new RouteDocument("Durban", "África", "Mombasa", "África", 3200.0),     // Conecta el sur-este con el este
                    new RouteDocument("Mombasa", "África", "Mogadishu", "África", 1000.0),   // Conecta con el destino final

                    // ========== Rutas Europeas ==========
                    new RouteDocument("Stavanger", "Europa", "Murmansk", "Europa", 2800.0),
                    new RouteDocument("Eupatoria", "Europa", "Estambul", "Europa", 800.0),
                    new RouteDocument("Lisboa", "Europa", "Casablanca", "África", 900.0),
                    new RouteDocument("Genova", "Europa", "Atenas", "Europa", 1800.0),
                    new RouteDocument("Hamburgo", "Europa", "Rotterdam", "Europa", 600.0),
                    new RouteDocument("Le Havre", "Europa", "Valencia", "Europa", 1500.0),
                    new RouteDocument("San Petersburgo", "Europa", "Murmansk", "Europa", 1700.0),

                    // ========== Rutas África-Asia ==========
                    new RouteDocument("Mombasa", "África", "Mumbai", "Asia", 4300.0),
                    new RouteDocument("Durban", "África", "Chennai", "Asia", 6800.0),

                    // ========== Rutas Áfricanas ==========
                    new RouteDocument("Walvis Bay", "África", "Luanda", "África", 1800.0), // Corregido nombre
                    new RouteDocument("Mogadishu", "África", "Aden", "Asia", 1200.0), // Nombre corregido
                    new RouteDocument("Ciudad del Cabo", "África", "Walvis Bay", "África", 1300.0), // Nombre corregido
                    new RouteDocument("Dakar", "África", "Casablanca", "África", 2600.0),
                    new RouteDocument("Lagos", "África", "Luanda", "África", 2400.0),
                    // --- CONEXIÓN QUE FALTA ---
                    new RouteDocument("Arkits", "América", "Vancouver", "América", 1500.0),

                    // --- CONEXIONES QUE FALTAN EN ÁFRICA ---
                    new RouteDocument("Dakar", "África", "Casablanca", "África", 2600.0),
                    new RouteDocument("Lagos", "África", "Luanda", "África", 2400.0),
                    new RouteDocument("Abiyán", "África", "Lagos", "África", 1000.0),
                    new RouteDocument("Luanda", "África", "Ciudad del Cabo", "África", 2500.0),
                    new RouteDocument("Ciudad del Cabo", "África", "Durban", "África", 1300.0),
                    new RouteDocument("Durban", "África", "Mombasa", "África", 3200.0),
                    new RouteDocument("Mombasa", "África", "Mogadishu", "África", 1000.0),

                    // ========== Rutas Mediterráneas ==========
                    new RouteDocument("Atenas", "Europa", "Alexandría", "África", 1100.0),
                    new RouteDocument("Estambul", "Europa", "Valencia", "Europa", 2900.0),

                    // Rutas Árticas (Uelen/Arkits)
                    new RouteDocument("Uelen", "Asia", "Arkits", "América", 2500.0),
                    new RouteDocument("Murmansk", "Europa", "Arkits", "América", 2200.0),
                    new RouteDocument("Arkits", "América", "Vancouver", "América", 1500.0), // Conexión crucial que falta
                    // --- CONEXIONES PARA LOS PUERTOS AISLADOS ---
                    new RouteDocument("Nuakchot", "África", "Dakar", "África", 500.0),
                    new RouteDocument("Abiyán", "África", "Lagos", "África", 1000.0),
                    new RouteDocument("Arkits", "América", "Vancouver", "América", 1500.0),

                    // --- PUENTES DENTRO DE ÁFRICA ---
                    new RouteDocument("Casablanca", "África", "Lisboa", "Europa", 900.0), // Ya lo tienes, pero es bueno tenerlo cerca
                    new RouteDocument("Dakar", "África", "Cayena", "América", 4000.0), // Un puente transatlántico alternativo
                    new RouteDocument("Lagos", "África", "Port-Gentil", "África", 800.0),
                    new RouteDocument("Port-Gentil", "África", "Luanda", "África", 900.0),
                    new RouteDocument("Luanda", "África", "Ciudad del Cabo", "África", 2500.0),
                    new RouteDocument("Ciudad del Cabo", "África", "Durban", "África", 1300.0),
                    new RouteDocument("Durban", "África", "Toamasina", "África", 2600.0),
                    new RouteDocument("Durban", "África", "Mombasa", "África", 3200.0),
                    new RouteDocument("Mombasa", "África", "Mogadishu", "África", 1000.0),
                    // ========== Rutas Troncales Transoceánicas (Puentes Globales) ==========
            new RouteDocument("San Francisco", "América", "Tokyo", "Asia", 8500.0),
                    new RouteDocument("Rotterdam", "Europa", "New York", "América", 5800.0),
                    new RouteDocument("Lisboa", "Europa", "Rio de Janeiro", "América", 7200.0),
                    new RouteDocument("Dakar", "África", "Cayena", "América", 4000.0),
                    new RouteDocument("Ciudad del Cabo", "África", "Buenos Aires", "América", 6900.0),
                    new RouteDocument("Sídney", "Oceanía", "Valparaíso", "América", 11000.0),
                    new RouteDocument("Mombasa", "África", "Mumbai", "Asia", 4300.0),

                    // ========== Rutas Locales (Conectando los nuevos puertos) ==========
                    new RouteDocument("Puerto Said", "Asia", "Alexandría", "África", 200.0),
                    new RouteDocument("Puerto de Haifa", "Asia", "Atenas", "Europa", 1200.0),
                    new RouteDocument("Latakia", "Asia", "Mersin", "Europa", 200.0),
                    new RouteDocument("Puerto de Beirut", "Asia", "Puerto de Haifa", "Asia", 150.0),
                    new RouteDocument("Puerto de Crimea", "Europa", "Estambul", "Europa", 600.0),
                    new RouteDocument("Odessa", "Europa", "Constanza", "Europa", 350.0),
                    new RouteDocument("Puerto de Túnez", "África", "Valencia", "Europa", 900.0),
                    new RouteDocument("Puerto de Trípoli", "África", "Genova", "Europa", 1000.0),
                    new RouteDocument("Taiwán", "Asia", "Shanghai", "Asia", 700.0),
                    new RouteDocument("Pyongyang", "Asia", "Tianjin", "Asia", 600.0),

                    // ========== Rutas para conectar la red existente ==========
                    // --- Conectando América ---
                    new RouteDocument("Long Beach", "América", "San Francisco", "América", 400.0),
                    new RouteDocument("San Francisco", "América", "Vancouver", "América", 1300.0),
                    new RouteDocument("Vancouver", "América", "Prince Rupert", "América", 800.0),
                    new RouteDocument("Prince Rupert", "América", "Arkits", "América", 1800.0),
                    new RouteDocument("Long Beach", "América", "Manzanillo", "América", 2000.0),
                    new RouteDocument("Manzanillo", "América", "Balboa", "América", 2700.0),
                    new RouteDocument("Balboa", "América", "Guayaquil", "América", 800.0),
                    new RouteDocument("Guayaquil", "América", "Callao", "América", 1150.0),
                    new RouteDocument("Callao", "América", "Valparaíso", "América", 2500.0),
                    new RouteDocument("Valparaíso", "América", "San Antonio", "América", 100.0),
                    new RouteDocument("San Antonio", "América", "Buenos Aires", "América", 1200.0), // Vía estrecho de Magallanes (estimado)
                    new RouteDocument("Buenos Aires", "América", "Rio Grande", "América", 800.0),
                    new RouteDocument("Rio Grande", "América", "Rio de Janeiro", "América", 1200.0),
                    new RouteDocument("New York", "América", "Montreal", "América", 600.0),
                    new RouteDocument("New York", "América", "Houston", "América", 2300.0),
                    new RouteDocument("Houston", "América", "Colón", "América", 2500.0),
                    new RouteDocument("Colón", "América", "Cartagena", "América", 500.0),
                    new RouteDocument("Cartagena", "América", "Puerto Cabello", "América", 600.0),
                    new RouteDocument("Puerto Cabello", "América", "Cayena", "América", 1500.0),

                    // --- Conectando Europa ---
                    new RouteDocument("Hamburgo", "Europa", "Rotterdam", "Europa", 450.0),
                    new RouteDocument("Rotterdam", "Europa", "Le Havre", "Europa", 500.0),
                    new RouteDocument("Le Havre", "Europa", "Lisboa", "Europa", 1300.0),
                    new RouteDocument("Lisboa", "Europa", "Valencia", "Europa", 900.0),
                    new RouteDocument("Valencia", "Europa", "Genova", "Europa", 900.0),
                    new RouteDocument("Genova", "Europa", "Atenas", "Europa", 1500.0),
                    new RouteDocument("Atenas", "Europa", "Estambul", "Europa", 700.0),
                    new RouteDocument("Hamburgo", "Europa", "Copenhague", "Europa", 500.0),
                    new RouteDocument("Copenhague", "Europa", "San Petersburgo", "Europa", 1200.0),
                    new RouteDocument("San Petersburgo", "Europa", "Murmansk", "Europa", 1400.0),
                    new RouteDocument("Copenhague", "Europa", "Stavanger", "Europa", 500.0),

                    // --- Conectando África ---
                    new RouteDocument("Casablanca", "África", "Dakar", "África", 2600.0),
                    new RouteDocument("Dakar", "África", "Abiyán", "África", 1800.0),
                    new RouteDocument("Abiyán", "África", "Lagos", "África", 1000.0),
                    new RouteDocument("Lagos", "África", "Luanda", "África", 2400.0),
                    new RouteDocument("Luanda", "África", "Walvis Bay", "África", 1300.0),
                    new RouteDocument("Walvis Bay", "África", "Ciudad del Cabo", "África", 1300.0),
                    new RouteDocument("Atenas", "Europa", "Alexandría", "África", 1100.0),
                    new RouteDocument("Aden", "Asia", "Mogadishu", "África", 1200.0),
            new RouteDocument("Atenas", "Europa", "Estambul", "Europa", 700.0),

// --- ¡¡¡AÑADE ESTA LÍNEA PUENTE!!! ---
                    new RouteDocument("Estambul", "Europa", "Constanza", "Europa", 400.0), // Conecta el Mediterráneo con el Mar Negro

                    new RouteDocument("Hamburgo", "Europa", "Copenhague", "Europa", 500.0)
            );

            List<RouteDocument> distinctRoutes = routes.stream().distinct().collect(Collectors.toList());

            routeService.saveAllRoutes(distinctRoutes);
            logger.info("Insertadas {} nuevas rutas.", distinctRoutes.size());

        } catch (Exception e) {
            logger.error("Error fatal inicializando rutas: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
    private void validateRoutes() {
        List<RouteDocument> routes = routeService.findAllRoutes();
        for (RouteDocument route : routes) {
            Optional<PortDocument> homePortExists = portService.findByNameAndContinent(route.getHomePort(), route.getHomePortContinent());
            Optional<PortDocument> destinationPortExists = portService.findByNameAndContinent(route.getDestinationPort(), route.getDestinationPortContinent());

            if (homePortExists.isEmpty()) {
                throw new IllegalStateException("Ruta inválida (Puerto de Origen no existe): " + route.getHomePort()
                        + " (" + route.getHomePortContinent() + ")");
            }

            if (destinationPortExists.isEmpty()) {
                throw new IllegalStateException("Ruta inválida (Puerto de Destino no existe): " + route.getDestinationPort()
                        + " (" + route.getDestinationPortContinent() + ")");
            }
        }
    }
}