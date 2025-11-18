# NOAA hurricane alerts integration

La detección de huracanes del endpoint `POST /api/ai/hazard-assessment` ahora consulta el API público de alertas NOAA (`https://api.weather.gov/alerts/active`). Datos clave:

- Se consulta un `bbox` que cubre la ruta origen-destino con un margen configurable; únicamente se solicitan eventos de tipo *Hurricane/Tropical Storm/Typhoon*.
- Cada alerta se convierte en un `WeatherHazardProbability` con tipo `HURRICANE`, centro aproximado (centroide del polígono NOAA o midpoint de la ruta) y una probabilidad que depende de `severity` y `certainty`.
- Si existe al menos una alerta NOAA, el servicio omite la heurística interna de huracanes y, si la alerta más intensa ≥ 0.7, marca `routeViable=false` con la razón `Alerta NOAA: ciclon activo en la ruta`.
- Si la llamada falla o no hay alertas, se conserva la heurística actual (ICE/MAREAJE siempre siguen siendo heurísticos).

## Configuración

Configurable via `application.properties` (prefijo `ai.weather.hazard.noaa`):

```
enabled=true                         # Desactiva la llamada externa si es necesario
base-url=https://api.weather.gov     # Override solo para pruebas
timeout=6s                           # Formato Duration (5s, 500ms, etc.)
lat-padding-deg=5.0                  # Margen en grados sobre la latitud de la ruta
lon-padding-deg=5.0                  # Margen en grados sobre la longitud de la ruta
default-radius-km=600.0              # Radio aproximado si NOAA no entrega geometría
max-results=25                       # Límite de alertas a recuperar
user-agent=TeemoHazardService/1.0 (+support@teemo.solutions)
```

El listado de eventos soportados se puede sobreescribir con `ai.weather.hazard.noaa.cyclone-events=Hurricane Warning,...` si se desea ampliar o reducir la consulta.

## Verificación manual

1. Ajusta temporalmente los márgenes de `lat-padding-deg` y `lon-padding-deg` si deseas cubrir un área mayor.
2. Lanza la app (`mvn spring-boot:run`) y ejecuta una llamada al endpoint apuntando hacia una zona con alertas activas (por ejemplo Caribe en temporada).
3. Observa los logs: cuando NOAA responde, se registran los hazards con `source=heuristic-internal`/`hazards` y la respuesta JSON mostrará la probabilidad correspondiente (ya no será fija en 25 %).
4. Si necesitas forzar el fallback heurístico, basta con poner `ai.weather.hazard.noaa.enabled=false` y reiniciar la app.
