package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.Coordinates;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.domain.RouteDocument;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.repositories.PortRepository;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.repositories.RouteRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// Infrastructure Layer
@Component
public class DataInitializer {

    private final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private PortRepository portRepository;

    @Autowired
    private RouteRepository routeRepository;

    @PostConstruct
    public void init() {
        initPorts();
        initRoutes();
        validateRoutes();
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
                    new Port("Muscat", new Coordinates(23.6142, 58.5458), "Asia"), // Mascate corregido
                    new Port("Singapore", new Coordinates(1.3521, 103.8198), "Asia"),
                    new Port("Jakarta", new Coordinates(-6.2088, 106.8456), "Asia"), // Yakarta corregido
                    new Port("Mumbai", new Coordinates(19.0760, 72.8777), "Asia"),
                    new Port("Chennai", new Coordinates(13.0827, 80.2707), "Asia"),
                    new Port("Tuticorin", new Coordinates(8.7642, 78.1348), "Asia"),
                    new Port("Dubai", new Coordinates(25.2769, 55.2962), "Asia"),
                    new Port("Aden", new Coordinates(12.8000, 45.0333), "Asia"),
                    new Port("Vladivostok", new Coordinates(43.1056, 131.8735), "Asia"), // San Petersburgo asiático
                    new Port("Uelen", new Coordinates(66.1667, -169.8000), "Asia"), // Rusia (Asia)

// ========== AMÉRICA ==========
                    new Port("Arkits", new Coordinates(73.0, -128.0), "América"), // Canadá ártico
                    new Port("Callao", new Coordinates(-12.0564, -77.1319), "América"), // Perú
                    new Port("Buenos Aires", new Coordinates(-34.6037, -58.3816), "América"),
                    new Port("Valparaíso", new Coordinates(-33.0472, -71.6128), "América"),
                    new Port("Rio de Janeiro", new Coordinates(-22.9068, -43.1729), "América"),
                    new Port("Fort Lauderdale", new Coordinates(26.1224, -80.1373), "América"),
                    new Port("Guayaquil", new Coordinates(-2.1962, -79.8862), "América"),
                    new Port("Balboa", new Coordinates(8.9833, -79.5167), "América"), // Panamá
                    new Port("Manzanillo", new Coordinates(19.0514, -104.3158), "América"), // México
                    new Port("Long Beach", new Coordinates(33.7709, -118.1937), "América"),
                    new Port("New York", new Coordinates(40.7128, -74.0060), "América"),
                    new Port("Houston", new Coordinates(29.7604, -95.3698), "América"),
                    new Port("San Francisco", new Coordinates(37.7749, -122.4194), "América"),
                    new Port("Vancouver", new Coordinates(49.2827, -123.1207), "América"),
                    new Port("Prince Rupert", new Coordinates(54.3150, -130.3208), "América"), // Canadá (corregido nombre)
                    new Port("Cayena", new Coordinates(4.9372, -52.3260), "América"), // Guayana Francesa
                    new Port("Cartagena", new Coordinates(10.3932, -75.4832), "América"),
                    new Port("Puerto Cabello", new Coordinates(10.4731, -68.0125), "América"), // Cabello corregido
                    new Port("San Antonio", new Coordinates(-33.5983, -71.6123), "América"), // Chile
                    new Port("Montreal", new Coordinates(45.5017, -73.5673), "América"),
                    new Port("Rio Grande", new Coordinates(-32.0351, -52.0986), "América"), // Brasil

// ========== ÁFRICA ==========
                    new Port("Ciudad del Cabo", new Coordinates(-33.9249, 18.4241), "África"), // Sudáfrica
                    new Port("Abiyán", new Coordinates(5.3599, -4.0084), "África"), // Costa de Marfil
                    new Port("Durban", new Coordinates(-29.8833, 31.0500), "África"),
                    new Port("Mombasa", new Coordinates(-4.0435, 39.6682), "África"),
                    new Port("Toamasina", new Coordinates(-18.1443, 49.3958), "África"),
                    new Port("Port-Gentil", new Coordinates(-0.7167, 8.7833), "África"),
                    new Port("Nuakchot", new Coordinates(18.0731, -15.9582), "África"), // Mauritania
                    new Port("Dakar", new Coordinates(14.7167, -17.4677), "África"),
                    new Port("Lagos", new Coordinates(6.4541, 3.3947), "África"),
                    new Port("Walvis Bay", new Coordinates(-22.9587, 14.5058), "África"), // Namibia (corregido)
                    new Port("Luanda", new Coordinates(-8.8383, 13.2344), "África"),
                    new Port("Alexandría", new Coordinates(31.2001, 29.9187), "África"),
                    new Port("Casablanca", new Coordinates(33.5731, -7.5898), "África"), // Pertenece a África
                    new Port("Mogadishu", new Coordinates(2.0469, 45.3182), "África"), // Somalia

// ========== EUROPA ==========
                    new Port("Murmansk", new Coordinates(68.9585, 33.0827), "Europa"), // Pertenece a Rusia europea
                    new Port("Estambul", new Coordinates(41.0082, 28.9784), "Europa"),
                    new Port("Constanza", new Coordinates(44.1598, 28.6348), "Europa"), // Rumania
                    new Port("Mersin", new Coordinates(36.7950, 34.6179), "Europa"), // Turquía (Europa/Asia, pero asignado a Europa)
                    new Port("Lisboa", new Coordinates(38.7223, -9.1393), "Europa"), // Portugal
                    new Port("Valencia", new Coordinates(39.4699, -0.3763), "Europa"),
                    new Port("Hamburgo", new Coordinates(53.5488, 9.9872), "Europa"),
                    new Port("Rotterdam", new Coordinates(51.9244, 4.4777), "Europa"),
                    new Port("Le Havre", new Coordinates(49.4944, 0.1089), "Europa"),
                    new Port("Genova", new Coordinates(44.4056, 8.9463), "Europa"),
                    new Port("Atenas", new Coordinates(37.9838, 23.7275), "Europa"),
                    new Port("San Petersburgo", new Coordinates(59.9343, 30.3351), "Europa"),
                    new Port("Copenhague", new Coordinates(55.6761, 12.5683), "Europa"),
                    new Port("Stavanger", new Coordinates(58.9699, 5.7331), "Europa"), // Noruega
                    new Port("Eupatoria", new Coordinates(45.2000, 33.3583), "Europa"), // Crimea

// ========== OCEANÍA ==========
                    new Port("Sídney", new Coordinates(-33.8688, 151.2093), "Oceanía"),
                    new Port("Brisbane", new Coordinates(-27.4698, 153.0251), "Oceanía"),
                    new Port("Fremantle", new Coordinates(-32.0564, 115.7417), "Oceanía"),
                    new Port("Darwin", new Coordinates(-12.4634, 130.8456), "Oceanía"),
                    new Port("Port Moresby", new Coordinates(-9.4438, 147.1803), "Oceanía")
            );
            List<Port> newPorts = new ArrayList<>();

            for (Port port : ports) {
                if (!portRepository.existsByNameAndContinent(port.getName(), port.getContinent())) {
                    newPorts.add(port);
                }
            }

            if (!newPorts.isEmpty()) {
                portRepository.saveAll(newPorts);
                logger.info("Insertados {} puertos: {}",
                        newPorts.size(),
                        newPorts.stream().map(Port::getName).collect(Collectors.joining(", "))
                );
            }
        } catch (Exception e) {
            logger.error("Error inicializando puertos: {}", e.getMessage());
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

                    new RouteDocument("Houston", "América", "New York", "América", 2500.0),
                    new RouteDocument("Prince Rupert", "América", "Vancouver", "América", 800.0),

// ========== Rutas Transatlánticas ==========
                    new RouteDocument("Rotterdam", "Europa", "New York", "América", 5800.0),
                    new RouteDocument("Lisboa", "Europa", "Rio de Janeiro", "América", 7200.0),

// ========== Rutas Europeas ==========
                    new RouteDocument("Stavanger", "Europa", "Murmansk", "Europa", 2800.0),
                    new RouteDocument("Eupatoria", "Europa", "Estambul", "Europa", 800.0),
                    new RouteDocument("Lisboa", "Europa", "Casablanca", "África", 900.0),
                    new RouteDocument("Genova", "Europa", "Atenas", "Europa", 1800.0),
                    new RouteDocument("Hamburgo", "Europa", "Rotterdam", "Europa", 600.0),
                    new RouteDocument("Le Havre", "Europa", "Valencia", "Europa", 1500.0),
                    new RouteDocument("San Petersburgo", "Europa", "Murmansk", "Europa", 1700.0),

// ========== Rutas África-Asia ==========
                    new RouteDocument("Mogadishu", "África", "Aden", "Asia", 1500.0),
                    new RouteDocument("Mombasa", "África", "Mumbai", "Asia", 4300.0),
                    new RouteDocument("Durban", "África", "Chennai", "Asia", 6800.0),

// ========== Rutas Áfricanas ==========
                    new RouteDocument("Walvis Bay", "África", "Luanda", "África", 1800.0), // Corregido nombre
                    new RouteDocument("Mogadishu", "África", "Aden", "Asia", 1200.0), // Nombre corregido
                    new RouteDocument("Ciudad del Cabo", "África", "Walvis Bay", "África", 1300.0), // Nombre corregido
                    new RouteDocument("Dakar", "África", "Casablanca", "África", 2600.0),
                    new RouteDocument("Lagos", "África", "Luanda", "África", 2400.0),

// ========== Rutas Mediterráneas ==========
                    new RouteDocument("Atenas", "Europa", "Alexandría", "África", 1100.0),
                    new RouteDocument("Estambul", "Europa", "Valencia", "Europa", 2900.0),

// Rutas Árticas (Uelen/Arkits)
                    new RouteDocument("Uelen", "Asia", "Arkits", "América", 2500.0),
                    new RouteDocument("Murmansk", "Europa", "Arkits", "América", 2200.0)
            );
            List<RouteDocument> newRoutes = new ArrayList<>();

            for (RouteDocument route : routes) {
                if (!routeRepository.existsByHomePortAndDestinationPort(
                        route.getHomePort(),
                        route.getDestinationPort()
                )) {
                    newRoutes.add(route);
                }
            }

            if (!newRoutes.isEmpty()) {
                routeRepository.saveAll(newRoutes);
                logger.info("Insertadas {} rutas: {}",
                        newRoutes.size(),
                        newRoutes.stream()
                                .map(route -> route.getHomePort() + " -> " + route.getDestinationPort())
                                .collect(Collectors.joining(", "))
                );
            }
        } catch (Exception e) {
            logger.error("Error inicializando rutas: {}", e.getMessage());
        }

    }

    private void validateRoutes() {
        List<RouteDocument> routes = routeRepository.getAll();
        for (RouteDocument route : routes) {
            Optional<Port> homePortExists = portRepository.getPortByNameAndContinent(
                    route.getHomePort(),
                    route.getHomeContinent()
            );

            Optional<Port> destinationPortExists = portRepository.getPortByNameAndContinent(
                    route.getDestinationPort(),
                    route.getDestinationContinent()
            );

            if (homePortExists.isEmpty()) {
                throw new IllegalStateException("Ruta inválida (Home Port doesnt Exists): " + route.getHomePort()
                        +   "("
                        +   route.getHomeContinent()
                        +   ") -> "
                        +   route.getDestinationPort()
                        +   "("
                        +   route.getDestinationContinent()
                        +   ")");
            }

            if (destinationPortExists.isEmpty()) {
                throw new IllegalStateException("Ruta inválida (Destination Port doesnt Exists): " + route.getHomePort()
                        +   "("
                        +   route.getHomeContinent()
                        +   ") -> "
                        +   route.getDestinationPort()
                        +   "("
                        +   route.getDestinationContinent()
                        +   ")");
            }
        }
    }
}