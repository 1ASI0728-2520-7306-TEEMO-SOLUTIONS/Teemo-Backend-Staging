package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.Coordinates;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.domain.RouteDocument;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.repositories.PortRepository;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.repositories.RouteRepository;

import java.util.List;

// Infrastructure Layer
@Component
public class DataInitializer {

    @Autowired
    private PortRepository portRepository;

    @Autowired
    private RouteRepository routeRepository;

    @PostConstruct
    public void init() {
        initPorts();
        initRoutes();
    }

    private void initPorts() {
        List<Port> ports = List.of(
                // Puertos para mapa3.png (Asia)
                new Port("Tokyo", new Coordinates(31.6895, -34.6917), "Asia"),
                new Port("Busan", new Coordinates(30.6895, -43.6917), "Asia"),
                new Port("Shanghai", new Coordinates(27.2304, -50.4737), "Asia"),
                new Port("Tianjing", new Coordinates(34.2304, -55.4737), "Asia"),
                new Port("Hon Kong", new Coordinates(19.2304, -58.4737), "Asia"),
                new Port("Quanzhou", new Coordinates(22.2304, -51.4737), "Asia"),
                new Port("Zhanjiang", new Coordinates(35.2304, -45.4737), "Asia"),
                new Port("Mascate", new Coordinates(17.0, -115.296249), "Asia"),
                new Port("Singapore", new Coordinates(5.3521, -65.8198), "Asia"),
                new Port("Yakarta", new Coordinates(0.0, -64.0), "Asia"),
                new Port("Mumbai", new Coordinates(18.0760, -98.8777), "Asia"),
                new Port("Chennai", new Coordinates(14.0, -89.8198), "Asia"),
                new Port("Tuticorin", new Coordinates(9.0, -92.8198), "Asia"),
                new Port("Dubai", new Coordinates(22.276987, -118.296249), "Asia"),
                new Port("Alexandria", new Coordinates(26.276987, -142.296249), "Asia"),
                new Port("Aden", new Coordinates(13.0, -128.0), "Asia"),
                new Port("San Petersburgo", new Coordinates(52.0, -147.0), "Asia"),
                new Port("Murmansk", new Coordinates(62.0, -147.0), "Asia"),
                new Port("Arkits", new Coordinates(75.0, -128.0), "Asia"),
                new Port("Uelen", new Coordinates(77.0, -16.0), "Asia"),
                new Port("Copenhague", new Coordinates(47.0, -163.0), "Asia"),
                new Port("Sidney", new Coordinates(-23.0, -21.0), "Asia"),
                new Port("Brisbane", new Coordinates(-19.0, -17.0), "Asia"),
                new Port("Fremantle", new Coordinates(-20.0, -57.0), "Asia"),
                new Port("Darwin", new Coordinates(-5.0, -35.0), "Asia"),
                new Port("Moresby", new Coordinates(-3.0, -21.0), "Asia"),

                // Puertos americanos en Asia
                new Port("callao", new Coordinates(-11.276987, 65.296249), "Asia"),
                new Port("buenos aires", new Coordinates(-34.276987, 89.0), "Asia"),
                new Port("Valparaiso", new Coordinates(-27.276987, 75.0), "Asia"),
                new Port("Rio de janeiro", new Coordinates(-20.9068, 110.1729), "Asia"),
                new Port("fort lauderdale", new Coordinates(18.276987, 64.0), "Asia"),
                new Port("guayaquil", new Coordinates(-4.276987, 62.5), "Asia"),
                new Port("balboa", new Coordinates(3.276987, 64.0), "Asia"),
                new Port("manzanilla", new Coordinates(14.0, 34.0), "Asia"),
                new Port("long beach", new Coordinates(25.0, 22.0), "Asia"),
                new Port("new york", new Coordinates(31.0, 74.0), "Asia"),
                new Port("houston", new Coordinates(21.0, 50.0), "Asia"),
                new Port("san francisco", new Coordinates(29.0, 19.0), "Asia"),
                new Port("vancouver", new Coordinates(42.0, 23.0), "Asia"),
                new Port("prince roupert", new Coordinates(49.0, 20.0), "Asia"),
                new Port("Guayana francesa", new Coordinates(0.0, 96.0), "Asia"),
                new Port("cartagena", new Coordinates(6.0, 70.0), "Asia"),
                new Port("cabello", new Coordinates(5.0, 80.0), "Asia"),

                // Puertos africanos en Asia
                new Port("abiyan", new Coordinates(8.0, -177.5), "Africa"),
                new Port("durban", new Coordinates(-20.0, -150.0), "Africa"),
                new Port("mombasa", new Coordinates(2.5, -133.0), "Africa"),
                new Port("toamasina", new Coordinates(-8.0, -124.0), "Africa"),
                new Port("port-Gentil", new Coordinates(4.0, -165.0), "Africa"),

                // Puertos europeos en Asia
                new Port("genova", new Coordinates(35.276987, -165.296249), "Asia"),
                new Port("atenas", new Coordinates(31.276987, -152.296249), "Asia"),
                new Port("valencia", new Coordinates(32.276987, -174.296249), "Asia"),
                new Port("le havre", new Coordinates(37.276987, -174.296249), "Asia"),
                new Port("hamburg", new Coordinates(43.276987, -167.296249), "Asia"),
                new Port("eupatoria", new Coordinates(37.276987, -140.296249), "Asia"),
                new Port("Stavanger", new Coordinates(52.276987, -168.296249), "Asia"),

                // Puertos para otros mapas
                new Port("New York", new Coordinates(55.7128, -106.0060), "América"),
                new Port("Long Beach", new Coordinates(54.0, -169.0060), "América"),
                new Port("San Francisco", new Coordinates(59.7128, -173.0060), "América"),
                new Port("Fort Lauderdale", new Coordinates(43.7128, -111.0060), "América"),
                new Port("Panama City", new Coordinates(20.9824, -109.5199), "América"),
                new Port("Manzanillo", new Coordinates(36.9824, -149.5199), "América"),
                new Port("Buenos Aires", new Coordinates(-40.6037, -85.3816), "América"),
                new Port("Rio de Janeiro", new Coordinates(-10.9068, -45.1729), "América"),
                new Port("Rio Grande", new Coordinates(-30.9068, -70.1729), "América"),
                new Port("Guayana Francesa", new Coordinates(16.9068, -65.1729), "América"),
                new Port("Callao", new Coordinates(0.9068, -106.1729), "América"),
                new Port("Guayaquil", new Coordinates(8.5068, -110.1729), "América"),
                new Port("Cartagena", new Coordinates(25.9068, -100.1729), "América"),
                new Port("Cabello", new Coordinates(23.9068, -85.1729), "América"),
                new Port("San Antonio", new Coordinates(-28.5068, -95.1729), "América"),
                new Port("Montreal", new Coordinates(70.0, -100.0), "América"),

                // Puertos europeos
                new Port("Portugal", new Coordinates(63.0, 2.5), "Europa"),
                new Port("Valencia", new Coordinates(61.0, 15.5), "Europa"),
                new Port("Hamburg", new Coordinates(83.6, 34.0), "Europa"),
                new Port("Roterdam", new Coordinates(81.6, 24.0), "Europa"),
                new Port("Le Havre", new Coordinates(70.5, 15.1), "Europa"),
                new Port("Genova", new Coordinates(69.0, 30.5), "Europa"),
                new Port("Atenas", new Coordinates(60.0, 49.5), "Europa"),
                new Port("Casablanca", new Coordinates(52.0, 2.5), "Europa"),

                // Puertos africanos
                new Port("Nuakchot", new Coordinates(35.0, -7.5), "Africa"),
                new Port("Dakar", new Coordinates(30.0, -10.5), "Africa"),
                new Port("Abiyan", new Coordinates(19.0, 6.5), "Africa"),
                new Port("Lagos", new Coordinates(19.0, 23.5), "Africa"),
                new Port("Walbys bay", new Coordinates(-15.0, 40.0), "Africa"),
                new Port("Durban", new Coordinates(-31.0, 48.0), "Africa"),
                new Port("Mombasa", new Coordinates(7.0, 80.0), "Africa"),
                new Port("Toamasina", new Coordinates(-10.0, 93.0), "Africa"),
                new Port("Port-Gentil", new Coordinates(10.0, 31.0), "Africa"),
                new Port("Luanda", new Coordinates(-1.0, 39.0), "Africa"),
                new Port("alexandria", new Coordinates(51.0, 66.0), "Africa"),
                new Port("aden", new Coordinates(28.0, 85.0), "Africa"),
                new Port("Mogadiscio", new Coordinates(20.0, 95.0), "Africa"),
                new Port("mascate", new Coordinates(35.0, 108.0), "Africa"),
                new Port("dubai", new Coordinates(43.0, 105.0), "Africa")
        );
        portRepository.saveAll(ports);
    }

    private void initRoutes() {
        List<RouteDocument> routes = List.of(
                // Rutas de Asia
                new RouteDocument("Tokyo", "Busan", 200.0),
                new RouteDocument("Shanghai", "Busan", 150.0),
                new RouteDocument("Shanghai", "Quanzhou", 200.0),
                new RouteDocument("Hon Kong", "Quanzhou", 100.0),
                new RouteDocument("Hon Kong", "Singapore", 100.0),
                new RouteDocument("Singapore", "Chennai", 300.0),
                new RouteDocument("Singapore", "Yakarta", 200.0),
                new RouteDocument("Singapore", "Tuticorin", 200.0),
                new RouteDocument("Chennai", "Tuticorin", 3000.0),
                new RouteDocument("Mumbai", "Tuticorin", 1200.0),
                new RouteDocument("Mumbai", "Mascate", 500.0),
                new RouteDocument("Mumbai", "Dubai", 400.0),
                new RouteDocument("Aden", "Mascate", 400.0),
                new RouteDocument("Yakarta", "Darwin", 2000.0),
                new RouteDocument("Darwin", "Brisbane", 500.0),
                new RouteDocument("Darwin", "Moresby", 300.0),
                new RouteDocument("Moresby", "Darwin", 2000.0),
                new RouteDocument("Moresby", "Brisbane", 2000.0),
                new RouteDocument("Brisbane", "Sidney", 200.0),
                new RouteDocument("Brisbane", "Sidney", 200.0),

                // Rutas América-Asia
                new RouteDocument("san francisco", "Tokyo", 5000.0),
                new RouteDocument("Rio de janeiro", "buenos aires", 200.0),
                new RouteDocument("Rio de janeiro", "Guayana francesa", 200.0),
                new RouteDocument("cabello", "Guayana francesa", 200.0),
                new RouteDocument("cabello", "cartagena", 200.0),
                new RouteDocument("cartagena", "balboa", 200.0),
                new RouteDocument("callao", "Valparaiso", 150.0),
                new RouteDocument("callao", "guayaquil", 200.0),
                new RouteDocument("guayaquil", "balboa", 250.0),
                new RouteDocument("balboa", "manzanilla", 500.0),
                new RouteDocument("manzanilla", "long beach", 200.0),
                new RouteDocument("long beach", "san francisco", 100.0),
                new RouteDocument("new york", "fort lauderdale", 200.0),
                new RouteDocument("balboa", "fort lauderdale", 200.0),
                new RouteDocument("houston", "balboa", 200.0),

                // Rutas África-Asia
                new RouteDocument("toamasina", "Tuticorin", 2000.0),
                new RouteDocument("toamasina", "mombasa", 200.0),
                new RouteDocument("toamasina", "durban", 2000.0),
                new RouteDocument("port-Gentil", "durban", 1000.0),

                // Rutas Europa-Asia
                new RouteDocument("San Petersburgo", "Copenhague", 200.0),
                new RouteDocument("Copenhague", "Murmansk", 800.0),
                new RouteDocument("Murmansk", "Arkits", 200.0),
                new RouteDocument("Uelen", "Arkits", 200.0),
                new RouteDocument("Uelen", "Tokyo", 200.0),
                new RouteDocument("eupatoria", "atenas", 200.0),
                new RouteDocument("genova", "atenas", 200.0),
                new RouteDocument("genova", "valencia", 200.0),
                new RouteDocument("le havre", "valencia", 200.0),
                new RouteDocument("le havre", "hamburg", 200.0),
                new RouteDocument("Copenhague", "hamburg", 200.0),
                new RouteDocument("Copenhague", "Stavanger", 200.0),
                new RouteDocument("Stavanger", "Murmansk", 200.0),

                // Rutas América
                new RouteDocument("New York", "Portugal", 10000.0),
                new RouteDocument("Fort Lauderdale", "Panama City", 700.0),
                new RouteDocument("Fort Lauderdale", "New York", 700.0),
                new RouteDocument("Cartagena", "Panama City", 100.0),
                new RouteDocument("Cartagena", "Fort Lauderdale", 200.0),
                new RouteDocument("Manzanillo", "Panama City", 500.0),
                new RouteDocument("Cabello", "Cartagena", 100.0),
                new RouteDocument("Callao", "Guayaquil", 180.0),
                new RouteDocument("Callao", "San Antonio", 100.0),
                new RouteDocument("Guayaquil", "Panama City", 180.0),
                new RouteDocument("Guayana Francesa", "Cabello", 100.0),
                new RouteDocument("Guayana Francesa", "Rio de Janeiro", 1000.0),
                new RouteDocument("Rio Grande", "Rio de Janeiro", 100.0),
                new RouteDocument("Buenos Aires", "Rio Grande", 100.0),
                new RouteDocument("Rio de Janeiro", "Guayana Francesa", 700.0),
                new RouteDocument("Montreal", "New York", 100.0),
                new RouteDocument("Long Beach", "San Francisco", 100.0),
                new RouteDocument("Long Beach", "Manzanillo", 100.0),

                // Rutas Europa
                new RouteDocument("Portugal", "Le Havre", 110.0),
                new RouteDocument("Portugal", "Casablanca", 1000.0),
                new RouteDocument("Roterdam", "Le Havre", 100.0),
                new RouteDocument("Roterdam", "Hamburg", 20.0),
                new RouteDocument("Genova", "Valencia", 500.0),
                new RouteDocument("Genova", "Atenas", 600.0),
                new RouteDocument("Estambul", "Atenas", 200.0),
                new RouteDocument("Estambul", "Constanza", 300.0),
                new RouteDocument("Estambul", "Eupatoria", 300.0),
                new RouteDocument("Mersin", "Atenas", 200.0),
                new RouteDocument("Mersin", "alexandria", 600.0),
                new RouteDocument("Casablanca", "Valencia", 100.0),

                // Rutas África
                new RouteDocument("Nuakchot", "Casablanca", 200.0),
                new RouteDocument("Nuakchot", "Dakar", 50.0),
                new RouteDocument("Dakar", "Abiyan", 100.0),
                new RouteDocument("Abiyan", "Lagos", 180.0),
                new RouteDocument("Lagos", "Port-Gentil", 70.0),
                new RouteDocument("Luanda", "Port-Gentil", 100.0),
                new RouteDocument("Luanda", "Walbys bay", 200.0),
                new RouteDocument("Walbys bay", "Durban", 200.0),
                new RouteDocument("Durban", "Toamasina", 700.0),
                new RouteDocument("Mombasa", "Toamasina", 250.0),
                new RouteDocument("Mombasa", "Mogadiscio", 250.0),
                new RouteDocument("Mogadiscio", "aden", 250.0),
                new RouteDocument("alexandria", "aden", 300.0),
                new RouteDocument("alexandria", "Atenas", 250.0),
                new RouteDocument("mascate", "aden", 250.0),
                new RouteDocument("mascate", "dubai", 250.0),

                // Ruta especial deshabilitada
                new RouteDocument("Guayaquil", "Lagos", 0.0) // Distancia 0 para indicar ruta deshabilitada
        );

        routeRepository.saveAll(routes);
    }
}