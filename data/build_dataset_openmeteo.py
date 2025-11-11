# build_dataset_openmeteo.py
# Genera un dataset sintético/realista de demoras marítimas usando Open-Meteo (weather + marine)
# Mejoras: 2 puntos de ruta, reintentos ±2 días en marine, fechas 2020–2024, caché agresiva y barra de progreso.

import pandas as pd
import numpy as np
import requests, time, math, random
from datetime import datetime, timedelta
from math import radians, sin, cos, sqrt, atan2
from tqdm import tqdm

# ================== CONFIG ==================
# Para ~10k filas finales, con las mejoras, 25k intentos es un buen objetivo.
N_SAMPLES = 25_000
START_DATE = datetime(2020, 1, 1)
END_DATE   = datetime(2024, 12, 31)

OUTPUT_CSV = "maritime_delays.csv"
REQ_TIMEOUT = 12
SLEEP_BETWEEN_CALLS = 0.025      # 50 ms entre llamadas para no saturar
MAX_RETRIES = 3                 # reintentos de red
DATE_JITTER_RETRIES = 2         # reintentos ±días para marine
SAVE_EVERY_ROWS = 2_000         # guardado incremental

# Puertos (nombre, lat, lon) — añade más para mejor cobertura global
PORTS = [
    # --- ASIA ---
    ("Tokyo", 35.6895, 139.6917),
    ("Busan", 35.1796, 129.0756),
    ("Shanghai", 31.2304, 121.4737),
    ("Tianjin", 39.0842, 117.2010),
    ("Hong Kong", 22.3193, 114.1694),
    ("Quanzhou", 24.9139, 118.5858),
    ("Zhanjiang", 21.1967, 110.4031),
    ("Muscat", 23.6142, 58.5458),
    ("Singapore", 1.3521, 103.8198),
    ("Jakarta", -6.2088, 106.8456),
    ("Mumbai", 19.0760, 72.8777),
    ("Chennai", 13.0827, 80.2707),
    ("Tuticorin", 8.7642, 78.1348),
    ("Dubai", 25.2769, 55.2962),
    ("Aden", 12.8000, 45.0333),
    ("Vladivostok", 43.1056, 131.8735),
    ("Uelen", 66.1667, -169.8000),
    ("Macasar", -5.112648, 119.409072),
    ("Muara Port", 5.024628, 115.072012),
    ("Semayang", -1.273471, 116.805472),
    ("Pyongyang", 39.0392, 125.7625),             # ojo: interior
    ("Taiwán", 25.143604, 121.756390),            # costa NE de Taiwan
    ("Puerto de Beirut", 33.8938, 35.5018),
    ("Latakia", 35.5236, 35.7877),
    ("Puerto de Haifa", 32.8184, 34.9895),
    ("Puerto Said", 31.2653, 32.3019),

    # --- AMÉRICA ---
    ("Santos", -23.9500, -46.3333),
    ("Montevideo", -34.9033, -56.2000),
    ("Veracruz", 19.1800, -96.1333),
    ("Lázaro Cárdenas", 17.9583, -102.2000),
    ("Seattle", 47.6062, -122.3321),
    ("Tacoma", 47.2529, -122.4443),
    ("Savannah", 32.0835, -81.0998),
    ("Norfolk", 36.8508, -76.2859),
    ("Kingston", 17.9714, -76.7920),
    ("Freeport", 26.5333, -78.7000),
    ("Caucedo", 18.4210, -69.6120),
    ("Arkits", 73.0000, -128.0000),               # ojo: ártico
    ("Callao", -12.0564, -77.1319),
    ("Buenos Aires", -34.6037, -58.3816),
    ("Valparaíso", -33.0472, -71.6128),
    ("Rio de Janeiro", -22.9068, -43.1729),
    ("Fort Lauderdale", 26.1224, -80.1373),
    ("Guayaquil", -2.1962, -79.8862),
    ("Balboa", 8.9833, -79.5167),
    ("Manzanillo", 19.0514, -104.3158),
    ("Long Beach", 33.7709, -118.1937),
    ("New York", 40.7128, -74.0060),
    ("Houston", 29.7604, -95.3698),
    ("San Francisco", 37.7749, -122.4194),
    ("Vancouver", 49.2827, -123.1207),
    ("Prince Rupert", 54.3150, -130.3208),
    ("Cayena", 4.9372, -52.3260),
    ("Cartagena", 10.3932, -75.4832),
    ("Puerto Cabello", 10.4731, -68.0125),
    ("San Antonio", -33.5983, -71.6123),
    ("Montreal", 45.5017, -73.5673),
    ("Rio Grande", -32.0351, -52.0986),
    ("Puerto Montt", -41.4718, -72.9396),
    ("Chancay", -11.5903, -77.2761),
    ("Necochea y Quequén", -38.5780, -58.7000),
    ("Ushuaia", -54.810668, -68.296487),
    ("Stanley", -51.692358, -57.860840),
    ("Santo Domingo", 18.453811, -69.948417),
    ("La Habana", 23.149916, -82.372406),
    ("Colón", 9.3582, -79.9015),
    ("Acajutla", 13.574724, -89.834303),

    # --- ÁFRICA ---
    ("Ciudad del Cabo", -33.9249, 18.4241),
    ("Abiyán", 5.3599, -4.0084),
    ("Durban", -29.8833, 31.0500),
    ("Mombasa", -4.0435, 39.6682),
    ("Toamasina", -18.1443, 49.3958),
    ("Port-Gentil", -0.7167, 8.7833),
    ("Nuakchot", 18.0731, -15.9582),
    ("Dakar", 14.7167, -17.4677),
    ("Lagos", 6.4541, 3.3947),
    ("Walvis Bay", -22.9587, 14.5058),
    ("Luanda", -8.8383, 13.2344),
    ("Alexandría", 31.2001, 29.9187),
    ("Casablanca", 33.5731, -7.5898),
    ("Mogadishu", 2.0469, 45.3182),
    ("Tema", 5.6333, 0.0167),
    ("Lomé", 6.1375, 1.2228),
    ("Cotonou", 6.3654, 2.4183),
    ("Beira", -19.8436, 34.8389),
    ("Maputo", -25.9653, 32.5892),
    ("Tanger Med", 35.8844, -5.5036),
    ("Trípoli", 32.8872, 13.1913),
    ("Túnez", 36.8065, 10.1815),

    # --- EUROPA ---
    ("Odessa", 46.4825, 30.7233),
    ("Murmansk", 68.9585, 33.0827),
    ("Estambul", 41.0082, 28.9784),
    ("Constanza", 44.1598, 28.6348),
    ("Mersin", 36.7950, 34.6179),
    ("Lisboa", 38.7223, -9.1393),
    ("Valencia", 39.4699, -0.3763),
    ("Hamburgo", 53.5488, 9.9872),
    ("Rotterdam", 51.9244, 4.4777),
    ("Le Havre", 49.4944, 0.1089),
    ("Genova", 44.4056, 8.9463),
    ("Venecia", 45.442374, 12.301968),
    ("Bari", 41.126746, 16.888232),
    ("Napoli", 40.833109, 14.268100),
    ("Palermo", 38.128761, 13.367153),
    ("Atenas", 37.9838, 23.7275),
    ("San Petersburgo", 59.9343, 30.3351),
    ("Copenhague", 55.6761, 12.5683),
    ("Stavanger", 58.9699, 5.7331),
    ("Eupatoria", 45.2000, 33.3583),
    ("Puerto de Crimea", 45.3481, 34.4993),

    # --- OCEANÍA ---
    ("Sídney", -33.8688, 151.2093),
    ("Brisbane", -27.4698, 153.0251),
    ("Fremantle", -32.0564, 115.7417),
    ("Darwin", -12.4634, 130.8456),
    ("Port Moresby", -9.4438, 147.1803),
    ("Melbourne", -37.8136, 144.9631),
    ("Auckland", -36.8509, 174.7645),
]

# Fracciones de ruta a muestrear (si falla una, probamos la otra)
ROUTE_FRACTIONS = (0.4, 0.7)

# =============== HELPERS ====================
def haversine(lat1, lon1, lat2, lon2):
    R = 6371.0
    dlat = radians(lat2 - lat1); dlon = radians(lon2 - lon1)
    a = sin(dlat/2)**2 + cos(radians(lat1))*cos(radians(lat2))*sin(dlon/2)**2
    return 2*R*atan2(sqrt(a), sqrt(1-a))

def rand_date(rng):
    # Viés leve hacia años recientes para mayor cobertura
    span = (END_DATE - START_DATE).days
    d = int(rng.random() * span)
    return START_DATE + timedelta(days=d)

def season_from_month(m):
    # DJF,DJF, MAM,MAM,MAM, JJA,JJA,JJA, SON,SON,SON, DJF
    return ["DJF","DJF","MAM","MAM","MAM","JJA","JJA","JJA","SON","SON","SON","DJF"][m-1]

def round_coord(x, ndigits=2):
    # Redondeo para aumentar hit-rate de caché (0.01 ~ 1.1 km latitud)
    return round(float(x), ndigits)

# --------- Sesión HTTP + caché ----------
_session = requests.Session()
def req_json(url):
    for _ in range(MAX_RETRIES):
        try:
            r = _session.get(url, timeout=REQ_TIMEOUT)
            if r.status_code == 200:
                return r.json()
        except Exception:
            pass
        time.sleep(0.3)
    return None

_weather_cache = {}
_marine_cache  = {}

def fetch_marine(lat, lon, date):
    # Marine es el que más falla; lo pedimos primero y reintentamos ± días
    latr = round_coord(lat); lonr = round_coord(lon)
    base_key = (latr, lonr, date.strftime("%Y-%m-%d"))

    # chequeo directo
    res = _marine_cache.get(base_key)
    if res is not None:
        return res

    def _try_day(d):
        key = (latr, lonr, d.strftime("%Y-%m-%d"))
        if key in _marine_cache:
            return _marine_cache[key]
        url = (
            "https://marine-api.open-meteo.com/v1/marine"
            f"?latitude={latr}&longitude={lonr}"
            f"&start_date={d.strftime('%Y-%m-%d')}"
            f"&end_date={d.strftime('%Y-%m-%d')}"
            "&daily=wave_height_max&timezone=UTC"
        )
        j = req_json(url)
        time.sleep(SLEEP_BETWEEN_CALLS)
        if not j or "daily" not in j:
            _marine_cache[key] = None
            return None
        vals = j["daily"].get("wave_height_max")
        if not vals or vals[0] is None:
            _marine_cache[key] = None
            return None
        out = {"max_wave_m": float(vals[0])}
        _marine_cache[key] = out
        return out

    # día base
    out = _try_day(date)
    if out:
        return out

    # reintentos ±1..DATE_JITTER_RETRIES días
    for delta in range(1, DATE_JITTER_RETRIES+1):
        for sign in (-1, +1):
            d2 = date + timedelta(days=sign*delta)
            out = _try_day(d2)
            if out:
                return out
    return None

def fetch_weather(lat, lon, date):
    latr = round_coord(lat); lonr = round_coord(lon)
    key = (latr, lonr, date.strftime("%Y-%m-%d"))
    if key in _weather_cache:
        return _weather_cache[key]
    url = (
        "https://archive-api.open-meteo.com/v1/archive"
        f"?latitude={latr}&longitude={lonr}"
        f"&start_date={date.strftime('%Y-%m-%d')}"
        f"&end_date={date.strftime('%Y-%m-%d')}"
        "&daily=wind_speed_10m_max,precipitation_sum,temperature_2m_max"
        "&timezone=UTC"
    )
    j = req_json(url)
    time.sleep(SLEEP_BETWEEN_CALLS)
    if not j or "daily" not in j:
        _weather_cache[key] = None
        return None
    d = j["daily"]
    try:
        out = {
            "max_wind_knots": float(d["wind_speed_10m_max"][0]) * 0.54,  # m/s -> kn
            "rain_mm": float(d["precipitation_sum"][0]),
            "temp_c": float(d["temperature_2m_max"][0]),
        }
    except Exception:
        _weather_cache[key] = None
        return None
    _weather_cache[key] = out
    return out

def interp_point(lat1, lon1, lat2, lon2, frac):
    # interpolación lineal simple (suficiente para muestreo de ruta)
    return (lat1 + (lat2 - lat1) * frac, lon1 + (lon2 - lon1) * frac)

# ======== GENERACIÓN DEL DATASET ============
rows = []
rng = np.random.default_rng(42)

pbar = tqdm(total=N_SAMPLES, desc="Generando dataset", unit="muestra")
last_saved_len = 0

for k in range(N_SAMPLES):
    # elige dos puertos distintos
    i, j = rng.choice(len(PORTS), 2, replace=False)
    name_o, lat_o, lon_o = PORTS[i]
    name_d, lat_d, lon_d = PORTS[j]

    dep = rand_date(rng)
    dist_km = haversine(lat_o, lon_o, lat_d, lon_d)

    # Tomamos primero MARINE en 2 puntos de la ruta (40% y 70%). Si alguno da válido, usamos ese.
    m = None
    chosen_lat = chosen_lon = None
    for frac in ROUTE_FRACTIONS:
        lat_m, lon_m = interp_point(lat_o, lon_o, lat_d, lon_d, frac)
        m = fetch_marine(lat_m, lon_m, dep)
        if m:
            chosen_lat, chosen_lon = lat_m, lon_m
            break
    if not m:
        pbar.set_postfix(
            marine_cache=len([1 for v in _marine_cache.values() if v is not None]),
            rows=len(rows),
            weather_cache=len([1 for v in _weather_cache.values() if v is not None])
        )
        pbar.update(1)
        continue

    # Solo si marine es válido, pedimos weather en el mismo punto
    w = fetch_weather(chosen_lat, chosen_lon, dep)
    if not w:
        pbar.set_postfix(
            marine_cache=len([1 for v in _marine_cache.values() if v is not None]),
            rows=len(rows),
            weather_cache=len([1 for v in _weather_cache.values() if v is not None])
        )
        pbar.update(1)
        continue

    # Variables “operativas” sintéticas
    origin_queue = rng.integers(0, 8)
    dest_queue   = rng.integers(0, 8)
    port_congestion_idx = rng.uniform(0.2, 1.2)

    # Visibilidad aproximada
    visibility_km = max(2.0, 18.0 - 0.4*w["rain_mm"] + rng.normal(0, 1.2))

    month = dep.month
    season = season_from_month(month)

    # Tiempo base por velocidad crucero 30 kn (1 kn = 1.852 km/h)
    travel_hours = dist_km / (30 * 1.852)

    # Delay “realista” (coeficientes suaves)
    delay = (
            0.22 * w["max_wind_knots"] +
            0.12 * w["rain_mm"] +
            1.05 * m["max_wave_m"] +
            0.55 * port_congestion_idx +
            0.30 * origin_queue + 0.30 * dest_queue +
            rng.normal(1.2, 1.0)
    )
    delay = max(0.0, float(delay))

    rows.append({
        "port_origin": name_o, "port_dest": name_d,
        "lat_o": lat_o, "lon_o": lon_o, "lat_d": lat_d, "lon_d": lon_d,
        "gc_distance_km": dist_km,
        "departure_date": dep.strftime("%Y-%m-%d"),
        "avg_wind_knots": w["max_wind_knots"],
        "rain_mm": w["rain_mm"],
        "temp_c": w["temp_c"],
        "max_wave_m": m["max_wave_m"],
        "visibility_km": visibility_km,
        "month": month,
        "season": season,
        "origin_queue": origin_queue,
        "dest_queue": dest_queue,
        "port_congestion_idx": port_congestion_idx,
        "delay_hours": delay
    })

    # Guardado incremental para no perder progreso y para poder parar/continuar
    if len(rows) - last_saved_len >= SAVE_EVERY_ROWS:
        pd.DataFrame(rows).to_csv(OUTPUT_CSV, index=False)
        last_saved_len = len(rows)

    if k % 50 == 0:
        pbar.set_postfix(
            marine_cache=len([1 for v in _marine_cache.values() if v is not None]),
            rows=len(rows),
            weather_cache=len([1 for v in _weather_cache.values() if v is not None])
        )
    pbar.update(1)

pbar.close()
df = pd.DataFrame(rows)
df.to_csv(OUTPUT_CSV, index=False)
print(f"✅ Dataset generado: {len(df)} filas → {OUTPUT_CSV}")
