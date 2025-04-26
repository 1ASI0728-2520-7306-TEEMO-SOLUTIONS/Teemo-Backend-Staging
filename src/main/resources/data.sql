-- ================================
-- LIMPIEZA DE BASE DE DATOS
-- ================================
SET FOREIGN_KEY_CHECKS=0;
TRUNCATE TABLE edges;
TRUNCATE TABLE ports;
SET FOREIGN_KEY_CHECKS=1;

-- ================================
-- PUERTOS
-- ================================

INSERT INTO ports (id, name, latitude, longitude, continent) VALUES
                                                                 (1, 'New York', 55.7128, -106.0060, 'América'),
                                                                 (2, 'Los Angeles', 53.0522, -169.2437, 'América'),
                                                                 (3, 'Panama City', 20.9824, -109.5199, 'América'),
                                                                 (4, 'Manzanillo', 36.9824, -149.5199, 'América'),
                                                                 (5, 'Buenos Aires', -40.6037, -85.3816, 'América'),
                                                                 (6, 'Rio de Janeiro', -10.9068, -45.1729, 'América'),
                                                                 (7, 'Guayana Francesa', 16.9068, -65.1729, 'América'),
                                                                 (8, 'Callao', 0.9068, -106.1729, 'América'),
                                                                 (9, 'Guayaquil', 8.5068, -110.1729, 'América'),
                                                                 (10, 'Cartagena', 25.9068, -100.1729, 'América'),
                                                                 (11, 'San Antonio', -28.5068, -95.1729, 'América'),
                                                                 (12, 'Montreal', 70.0, -100.0, 'América'),
                                                                 (34, 'Callao 2', -12.0464, -77.0428, 'América'),
                                                                 (35, 'Valparaiso', -33.0472, -71.6127, 'América'),
                                                                 (13, 'Portugal', 63.0, 2.5, 'Europa'),
                                                                 (14, 'Valencia', 61.0, 15.5, 'Europa'),
                                                                 (15, 'Hamburg', 83.6, 34.0, 'Europa'),
                                                                 (16, 'Roterdam', 81.6, 24.0, 'Europa'),
                                                                 (17, 'Le Havre', 70.5, 15.1, 'Europa'),
                                                                 (18, 'Genova', 69.0, 30.5, 'Europa'),
                                                                 (19, 'Atenas', 60.0, 49.5, 'Europa'),
                                                                 (20, 'Casablanca', 52.0, 2.5, 'Europa'),
                                                                 (21, 'Nuakchot', 35.0, -7.5, 'Africa'),
                                                                 (22, 'Dakar', 30.0, -10.5, 'Africa'),
                                                                 (23, 'Abiyan', 19.0, 6.5, 'Africa'),
                                                                 (24, 'Lagos', 19.0, 23.5, 'Africa'),
                                                                 (25, 'Duban', -33.0, 45.0, 'Africa'),
                                                                 (26, 'Mombasa', 7.0, 80.0, 'Africa'),
                                                                 (27, 'Toamasina', -10.0, 93.0, 'Africa'),
                                                                 (28, 'Port-Gentil', 10.0, 31.0, 'Africa'),
                                                                 (36, 'Alexandria', 31.2001, 29.9187, 'Africa'),
                                                                 (29, 'Tokyo', 35.6895, 139.6917, 'Asia'),
                                                                 (30, 'Shanghai', 31.2304, 121.4737, 'Asia'),
                                                                 (31, 'Singapore', 1.3521, 103.8198, 'Asia'),
                                                                 (32, 'Mumbai', 19.0760, 72.8777, 'Asia'),
                                                                 (33, 'Dubai', 25.276987, 55.296249, 'Asia');

-- ================================
-- EDGES (RUTAS)
-- ================================

INSERT INTO edges (id, origin_port_id, destination_port_id, distance, curvature, direction) VALUES
                                                                                                (1, 1, 13, 10000, 0.3, 'in'),
                                                                                                (2, 1, 3, 1000, 0.4, 'in'),
                                                                                                (3, 4, 3, 500, 0.3, 'in'),
                                                                                                (4, 7, 10, 200, 0.5, 'in'),
                                                                                                (5, 10, 3, 150, 0.5, 'in'),
                                                                                                (6, 2, 3, 1200, 0.3, 'out'),
                                                                                                (7, 8, 9, 180, 0.3, 'out'),
                                                                                                (8, 8, 11, 100, 0.3, 'out'),
                                                                                                (9, 9, 3, 180, 0.3, 'out'),
                                                                                                (10, 5, 6, 1000, 0.3, 'in'),
                                                                                                (11, 6, 7, 700, 0.9, 'in'),
                                                                                                (12, 12, 1, 100, 2, 'in'),
                                                                                                (13, 13, 17, 110, 0.6, 'in'),
                                                                                                (14, 13, 20, 1000, 0.2, 'in'),
                                                                                                (15, 16, 17, 100, 0.5, 'in'),
                                                                                                (16, 16, 15, 20, 0.5, 'in'),
                                                                                                (17, 18, 14, 500, 0.2, 'out'),
                                                                                                (18, 18, 19, 600, 0.5, 'out'),
                                                                                                (19, 20, 14, 200, 0.2, 'out'),
                                                                                                (20, 21, 20, 200, 0.5, 'in'),
                                                                                                (21, 21, 22, 50, 0.5, 'in'),
                                                                                                (22, 22, 23, 100, 0.5, 'in'),
                                                                                                (23, 23, 24, 180, 0.6, 'in'),
                                                                                                (24, 24, 28, 70, 0.5, 'in'),
                                                                                                (25, 25, 28, 800, 0.7, 'in'),
                                                                                                (26, 25, 27, 700, 0.7, 'out'),
                                                                                                (27, 26, 27, 250, 1.2, 'out'),
                                                                                                (28, 29, 30, 1000, 0.3, 'in'),
                                                                                                (29, 30, 31, 1800, 0.4, 'out'),
                                                                                                (30, 31, 32, 3000, 0.3, 'out'),
                                                                                                (31, 32, 33, 1200, 0.2, 'in'),
                                                                                                (32, 36, 33, 500, 0.2, 'out'),
                                                                                                (33, 34, 29, 5000, 0.2, 'in');
