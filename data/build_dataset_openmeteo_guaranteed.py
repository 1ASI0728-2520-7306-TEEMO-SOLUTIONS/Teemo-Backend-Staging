# build_dataset_openmeteo_guaranteed.py
# Objetivo: asegurar >= TARGET_ROWS filas en el CSV, sin caché en disco.
# Estrategia:
#  - Genera muestras por lotes (batches) y hace prefetch paralelo (weather+marine).
#  - Si faltan filas, aumenta la ventana de días (marine_day_window: 2 -> 3 -> 4)
#    y/o prueba más puntos intermedios en la ruta.
#  - Escribe streaming al CSV (append) con flush periódico.
#  - Progreso mostrando filas confirmadas.

import os, csv, math, time, threading, sys
from math import radians, sin, cos, sqrt, atan2
from datetime import datetime, timedelta
from concurrent.futures import ThreadPoolExecutor, as_completed

import numpy as np
import requests
from requests.adapters import HTTPAdapter
from urllib3.util.retry import Retry

try:
    from tqdm import tqdm
    HAS_TQDM = True
except Exception:
    HAS_TQDM = False

# =================== CONFIGURACIÓN ===================
TARGET_ROWS = 10_000      # <- Meta garantizada
BATCH_SAMPLES = 5_000     # muestras por lote (sube/baja para memoria/velocidad)
MAX_BATCHES   = 50        # tope de lotes por seguridad (50 * 5k = 250k muestras)
MAX_WORKERS   = 32        # hilos de prefetch
REQ_TIMEOUT   = 8
START_DATE    = datetime(2020, 1, 1)
END_DATE      = datetime(2024, 12, 31)
CSV_PATH      = "maritime_delays.csv"

# ventanas y puntos a intentar si faltan filas
WINDOW_SCHEDULE = [2, 3, 4]            # se amplía si sigue faltando
ROUTE_FRACTIONS_LIST = [(0.5,), (0.4,0.7), (0.3,0.5,0.7)]  # más puntos si es necesario

PUSH_SEA_DEG   = 0.2    # empuje a mar si Marine da None
RNG_SEED       = 42
WRITE_HEADER   = True

# =================== PUERTOS (tu lista) ===================
PORTS = [
    # --- ASIA ---
    ("Tokyo", 35.6895, 139.6917), ("Busan", 35.1796, 129.0756),
    ("Shanghai", 31.2304, 121.4737), ("Tianjin", 39.0842, 117.2010),
    ("Hong Kong", 22.3193, 114.1694), ("Quanzhou", 24.9139, 118.5858),
    ("Zhanjiang", 21.1967, 110.4031), ("Muscat", 23.6142, 58.5458),
    ("Singapore", 1.3521, 103.8198), ("Jakarta", -6.2088, 106.8456),
    ("Mumbai", 19.0760, 72.8777), ("Chennai", 13.0827, 80.2707),
    ("Tuticorin", 8.7642, 78.1348), ("Dubai", 25.2769, 55.2962),
    ("Aden", 12.8000, 45.0333), ("Vladivostok", 43.1056, 131.8735),
    ("Uelen", 66.1667, -169.8000), ("Macasar", -5.112648, 119.409072),
    ("Muara Port", 5.024628, 115.072012), ("Semayang", -1.273471, 116.805472),
    ("Pyongyang", 39.0392, 125.7625), ("Taiwán", 25.143604, 121.756390),
    ("Puerto de Beirut", 33.8938, 35.5018), ("Latakia", 35.5236, 35.7877),
    ("Puerto de Haifa", 32.8184, 34.9895), ("Puerto Said", 31.2653, 32.3019),

    # --- AMÉRICA ---
    ("Santos", -23.9500, -46.3333), ("Montevideo", -34.9033, -56.2000),
    ("Veracruz", 19.1800, -96.1333), ("Lázaro Cárdenas", 17.9583, -102.2000),
    ("Seattle", 47.6062, -122.3321), ("Tacoma", 47.2529, -122.4443),
    ("Savannah", 32.0835, -81.0998), ("Norfolk", 36.8508, -76.2859),
    ("Kingston", 17.9714, -76.7920), ("Freeport", 26.5333, -78.7000),
    ("Caucedo", 18.4210, -69.6120), ("Arkits", 73.0000, -128.0000),
    ("Callao", -12.0564, -77.1319), ("Buenos Aires", -34.6037, -58.3816),
    ("Valparaíso", -33.0472, -71.6128), ("Rio de Janeiro", -22.9068, -43.1729),
    ("Fort Lauderdale", 26.1224, -80.1373), ("Guayaquil", -2.1962, -79.8862),
    ("Balboa", 8.9833, -79.5167), ("Manzanillo", 19.0514, -104.3158),
    ("Long Beach", 33.7709, -118.1937), ("New York", 40.7128, -74.0060),
    ("Houston", 29.7604, -95.3698), ("San Francisco", 37.7749, -122.4194),
    ("Vancouver", 49.2827, -123.1207), ("Prince Rupert", 54.3150, -130.3208),
    ("Cayena", 4.9372, -52.3260), ("Cartagena", 10.3932, -75.4832),
    ("Puerto Cabello", 10.4731, -68.0125), ("San Antonio", -33.5983, -71.6123),
    ("Montreal", 45.5017, -73.5673), ("Rio Grande", -32.0351, -52.0986),
    ("Puerto Montt", -41.4718, -72.9396), ("Chancay", -11.5903, -77.2761),
    ("Necochea y Quequén", -38.5780, -58.7000), ("Ushuaia", -54.810668, -68.296487),
    ("Stanley", -51.692358, -57.860840), ("Santo Domingo", 18.453811, -69.948417),
    ("La Habana", 23.149916, -82.372406), ("Colón", 9.3582, -79.9015),
    ("Acajutla", 13.574724, -89.834303),

    # --- ÁFRICA ---
    ("Ciudad del Cabo", -33.9249, 18.4241), ("Abiyán", 5.3599, -4.0084),
    ("Durban", -29.8833, 31.0500), ("Mombasa", -4.0435, 39.6682),
    ("Toamasina", -18.1443, 49.3958), ("Port-Gentil", -0.7167, 8.7833),
    ("Nuakchot", 18.0731, -15.9582), ("Dakar", 14.7167, -17.4677),
    ("Lagos", 6.4541, 3.3947), ("Walvis Bay", -22.9587, 14.5058),
    ("Luanda", -8.8383, 13.2344), ("Alexandría", 31.2001, 29.9187),
    ("Casablanca", 33.5731, -7.5898), ("Mogadishu", 2.0469, 45.3182),
    ("Tema", 5.6333, 0.0167), ("Lomé", 6.1375, 1.2228),
    ("Cotonou", 6.3654, 2.4183), ("Beira", -19.8436, 34.8389),
    ("Maputo", -25.9653, 32.5892), ("Tanger Med", 35.8844, -5.5036),
    ("Trípoli", 32.8872, 13.1913), ("Túnez", 36.8065, 10.1815),

    # --- EUROPA ---
    ("Odessa", 46.4825, 30.7233), ("Murmansk", 68.9585, 33.0827),
    ("Estambul", 41.0082, 28.9784), ("Constanza", 44.1598, 28.6348),
    ("Mersin", 36.7950, 34.6179), ("Lisboa", 38.7223, -9.1393),
    ("Valencia", 39.4699, -0.3763), ("Hamburgo", 53.5488, 9.9872),
    ("Rotterdam", 51.9244, 4.4777), ("Le Havre", 49.4944, 0.1089),
    ("Genova", 44.4056, 8.9463), ("Venecia", 45.442374, 12.301968),
    ("Bari", 41.126746, 16.888232), ("Napoli", 40.833109, 14.268100),
    ("Palermo", 38.128761, 13.367153), ("Atenas", 37.9838, 23.7275),
    ("San Petersburgo", 59.9343, 30.3351), ("Copenhague", 55.6761, 12.5683),
    ("Stavanger", 58.9699, 5.7331), ("Eupatoria", 45.2000, 33.3583),
    ("Puerto de Crimea", 45.3481, 34.4993),

    # --- OCEANÍA ---
    ("Sídney", -33.8688, 151.2093), ("Brisbane", -27.4698, 153.0251),
    ("Fremantle", -32.0564, 115.7417), ("Darwin", -12.4634, 130.8456),
    ("Port Moresby", -9.4438, 147.1803), ("Melbourne", -37.8136, 144.9631),
    ("Auckland", -36.8509, 174.7645),
]

# =================== UTILS ===================
def rc(x): return round(float(x), 2)

def haversine(lat1, lon1, lat2, lon2):
    R = 6371.0
    dlat = radians(lat2 - lat1); dlon = radians(lon2 - lon1)
    a = sin(dlat/2)**2 + cos(radians(lat1))*cos(radians(lat2))*sin(dlon/2)**2
    return 2*R*atan2(sqrt(1-a), sqrt(a))

def season_from_month(m):
    return ["DJF","DJF","MAM","MAM","MAM","JJA","JJA","JJA","SON","SON","SON","DJF"][m-1]

def interp(lat1, lon1, lat2, lon2, f):
    return (lat1 + (lat2-lat1)*f, lon1 + (lon2-lon1)*f)

# =================== CLIENT + CACHE RAM ===================
session = requests.Session()
retry = Retry(total=3, backoff_factor=0.2,
              status_forcelist=[429,500,502,503,504],
              allowed_methods=frozenset(["GET"]))
adapter = HTTPAdapter(pool_connections=100, pool_maxsize=100, max_retries=retry)
session.mount("http://", adapter)
session.mount("https://", adapter)

_cache = {}
_lock  = threading.Lock()
rng = np.random.default_rng(RNG_SEED)

def cache_get(k):
    with _lock:
        return _cache.get(k)
def cache_set(k, v):
    with _lock:
        _cache[k] = v

def req_json(url):
    try:
        r = session.get(url, timeout=REQ_TIMEOUT)
        if r.status_code == 200:
            return r.json()
    except Exception:
        pass
    return None

def fetch_marine_window(lat, lon, center_date, window):
    latr, lonr = rc(lat), rc(lon)
    start = (center_date - timedelta(days=window)).strftime("%Y-%m-%d")
    end   = (center_date + timedelta(days=window)).strftime("%Y-%m-%d")
    k = ("marine", latr, lonr, start, end)
    hit = cache_get(k)
    if hit is not None: return hit

    url = ("https://marine-api.open-meteo.com/v1/marine"
           f"?latitude={latr}&longitude={lonr}"
           f"&start_date={start}&end_date={end}"
           "&daily=wave_height_max&timezone=UTC")
    j = req_json(url)
    if not j or "daily" not in j:
        cache_set(k, None); return None
    vals = j["daily"].get("wave_height_max")
    if not vals:
        cache_set(k, None); return None
    for v in vals:
        if v is not None:
            out = {"max_wave_m": float(v)}
            cache_set(k, out); return out
    cache_set(k, None); return None

def fetch_weather_exact(lat, lon, date):
    latr, lonr = rc(lat), rc(lon)
    dstr = date.strftime("%Y-%m-%d")
    k = ("weather", latr, lonr, dstr)
    hit = cache_get(k)
    if hit is not None: return hit

    url = ("https://archive-api.open-meteo.com/v1/archive"
           f"?latitude={latr}&longitude={lonr}"
           f"&start_date={dstr}&end_date={dstr}"
           "&daily=wind_speed_10m_max,precipitation_sum,temperature_2m_max"
           "&timezone=UTC")
    j = req_json(url)
    if not j or "daily" not in j:
        cache_set(k, None); return None
    d = j["daily"]
    try:
        out = {
            "max_wind_knots": float(d["wind_speed_10m_max"][0])*0.54,
            "rain_mm": float(d["precipitation_sum"][0]),
            "temp_c": float(d["temperature_2m_max"][0]),
        }
    except Exception:
        cache_set(k, None); return None
    cache_set(k, out); return out

# =================== PREFETCH ===================
def rand_date():
    span = (END_DATE - START_DATE).days
    return START_DATE + timedelta(days=int(rng.random()*span))

def build_samples(n, fracs):
    S = []
    for _ in range(n):
        i, j = rng.choice(len(PORTS), 2, replace=False)
        no, la, lo = PORTS[i]
        nd, ld, lod = PORTS[j]
        dep = rand_date()
        pts = [interp(la, lo, ld, lod, f) for f in fracs]
        S.append((no, la, lo, nd, ld, lod, dep, pts))
    return S

def keysets(samples, window):
    mk, wk = set(), set()
    for (_, _, _, _, _, _, dep, pts) in samples:
        start = (dep - timedelta(days=window)).strftime("%Y-%m-%d")
        end   = (dep + timedelta(days=window)).strftime("%Y-%m-%d")
        dstr  = dep.strftime("%Y-%m-%d")
        for (la, lo) in pts:
            latr, lonr = rc(la), rc(lo)
            mk.add(("marine", latr, lonr, start, end))
            wk.add(("weather", latr, lonr, dstr))
    return mk, wk

def pf_marine(k):
    _, latr, lonr, start, end = k
    cdate = datetime.fromisoformat(start) + (datetime.fromisoformat(end) - datetime.fromisoformat(start))/2
    d = fetch_marine_window(latr, lonr, cdate, window=(datetime.fromisoformat(end)-datetime.fromisoformat(start)).days//2)
    if not d:
        d = fetch_marine_window(latr, lonr + PUSH_SEA_DEG, cdate, window=(datetime.fromisoformat(end)-datetime.fromisoformat(start)).days//2)
    return k, d

def pf_weather(k):
    _, latr, lonr, dstr = k
    return k, fetch_weather_exact(latr, lonr, datetime.fromisoformat(dstr))

def prefetch_all(mk, wk):
    futures = []
    with ThreadPoolExecutor(max_workers=MAX_WORKERS) as ex:
        for k in mk: futures.append(ex.submit(pf_marine, k))
        for k in wk: futures.append(ex.submit(pf_weather, k))
        it = tqdm(as_completed(futures), total=len(futures), desc="Prefetch", unit="req") if HAS_TQDM else as_completed(futures)
        for f in it: _ = f.result()

# =================== ENSAMBLA UNA FILA ===================
def row_from_sample(s, window, fracs):
    (name_o, lat_o, lon_o, name_d, lat_d, lon_d, dep, pts) = s
    dist_km = haversine(lat_o, lon_o, lat_d, lon_d)
    start = (dep - timedelta(days=window)).strftime("%Y-%m-%d")
    end   = (dep + timedelta(days=window)).strftime("%Y-%m-%d")
    dstr  = dep.strftime("%Y-%m-%d")

    chosen = None
    for (la, lo) in pts:
        latr, lonr = rc(la), rc(lo)
        m = cache_get(("marine", latr, lonr, start, end))
        w = cache_get(("weather", latr, lonr, dstr))
        if m and w:
            chosen = (m, w); break
    if not chosen: return None

    m, w = chosen
    origin_queue = rng.integers(0, 8)
    dest_queue   = rng.integers(0, 8)
    port_congestion_idx = rng.uniform(0.2, 1.2)
    visibility_km = max(2.0, 18.0 - 0.4*w["rain_mm"] + rng.normal(0,1.2))
    month = dep.month
    season = season_from_month(month)

    # referencia
    travel_hours = dist_km / (30*1.852)

    delay = (
            0.22*w["max_wind_knots"] +
            0.12*w["rain_mm"] +
            1.05*m["max_wave_m"] +
            0.55*port_congestion_idx +
            0.30*int(origin_queue) + 0.30*int(dest_queue) +
            rng.normal(1.2, 1.0)
    )
    delay = max(0.0, float(delay))

    return [
        name_o, name_d, lat_o, lon_o, lat_d, lon_d, dist_km,
        dep.strftime("%Y-%m-%d"),
        w["max_wind_knots"], w["rain_mm"], w["temp_c"],
        m["max_wave_m"], visibility_km, month, season,
        int(origin_queue), int(dest_queue), float(port_congestion_idx), delay
    ]

# =================== LOOP GARANTIZADO ===================
header = ["port_origin","port_dest","lat_o","lon_o","lat_d","lon_d","gc_distance_km",
          "departure_date","avg_wind_knots","rain_mm","temp_c","max_wave_m",
          "visibility_km","month","season","origin_queue","dest_queue",
          "port_congestion_idx","delay_hours"]

def ensure_csv():
    exists = os.path.exists(CSV_PATH)
    f = open(CSV_PATH, "w" if not exists else "w", newline="", encoding="utf-8")
    w = csv.writer(f)
    if WRITE_HEADER: w.writerow(header)
    return f, w

def main():
    total_rows = 0
    f, writer = ensure_csv()

    try:
        for batch_id in range(1, MAX_BATCHES+1):
            for fracs in ROUTE_FRACTIONS_LIST:
                for window in WINDOW_SCHEDULE:
                    # genera lote
                    samples = build_samples(BATCH_SAMPLES, fracs)
                    mk, wk = keysets(samples, window)
                    prefetch_all(mk, wk)

                    # ensambla
                    written_this = 0
                    it = tqdm(samples, desc=f"Batch {batch_id} (win={window}, pts={len(fracs)})",
                              unit="fila") if HAS_TQDM else samples
                    for s in it:
                        row = row_from_sample(s, window, fracs)
                        if row is None: continue
                        writer.writerow(row)
                        written_this += 1
                        total_rows += 1
                        if HAS_TQDM: it.set_postfix(rows_total=total_rows)
                        if total_rows % 500 == 0:
                            f.flush()

                        if total_rows >= TARGET_ROWS:
                            f.flush()
                            print(f"\n✅ Objetivo alcanzado: {total_rows} filas → {CSV_PATH}")
                            return

                    print(f"· Lote {batch_id} con ventana {window} y {len(fracs)} pts → filas escritas: {written_this}")

            # si agotamos schedule sin llegar, volvemos a empezar (habrá nuevas fechas/rutas)
        print(f"⚠️ Finalizó el tope de lotes. Filas obtenidas: {total_rows} (objetivo: {TARGET_ROWS}).")
    finally:
        f.close()

if __name__ == "__main__":
    main()
