from datetime import datetime, timedelta, timezone
import math, time, requests, random
import pandas as pd
from tqdm import tqdm

MS_TO_KNOTS = 1.9438444924406

# --- util de fechas: limita a ventana corta y cercana ---
def make_valid_window(dep_iso: str, planned_h: float):
    """
    Open-Meteo (forecast/marine) solo permite ventanas cortas en el futuro cercano.
    Regresamos (start_date, end_date) clamped:
      - start = hoy+offset (1..6 días)
      - end   = start + min(5 días, planned_h/24 + 1)
    """
    now = datetime.now(timezone.utc)
    # fuerza salida entre 1 y 6 días en el futuro
    start = now + timedelta(days=min(6, max(1, (now.hour >= 18) and 2 or 1)))
    # duración en días: como máximo 5
    dur_days = max(1.0, min(5.0, planned_h / 24.0 + 1.0))
    end = start + timedelta(days=dur_days)
    return start.strftime("%Y-%m-%d"), end.strftime("%Y-%m-%d")

def safe_numeric_list(seq):
    if not seq:
        return []
    out = []
    for x in seq:
        try:
            if x is None:
                continue
            out.append(float(x))
        except (TypeError, ValueError):
            continue
    return out

def sph_to_cart(lat, lon):
    cl = math.cos(lat)
    return (cl*math.cos(lon), cl*math.sin(lon), math.sin(lat))

def dot(a,b): return a[0]*b[0]+a[1]*b[1]+a[2]*b[2]
def clamp(x,a,b): return max(a,min(b,x))

def great_circle_waypoints(lat1d, lon1d, lat2d, lon2d, n):
    lat1,lon1,lat2,lon2 = map(math.radians,[lat1d,lon1d,lat2d,lon2d])
    p1, p2 = sph_to_cart(lat1,lon1), sph_to_cart(lat2,lon2)
    omega = math.acos(clamp(dot(p1,p2),-1,1)); sinom = math.sin(omega)
    out=[]
    for i in range(1,n+1):
        f=i/n; a=math.sin((1-f)*omega)/sinom; b=math.sin(f*omega)/sinom
        x=a*p1[0]+b*p2[0]; y=a*p1[1]+b*p2[1]; z=a*p1[2]+b*p2[2]
        r=math.sqrt(x*x+y*y+z*z); x/=r; y/=r; z/=r
        lat=math.asin(z); lon=math.atan2(y,x)
        out.append((math.degrees(lat), ((math.degrees(lon)+180)%360)-180))
    return out

def fetch_hourly_safe(lat, lon, start_date, end_date, kind):
    """
    Llama a Open-Meteo; si 400 por ventana inválida, reduce a 3 días y reintenta.
    kind: "wind" -> forecast; "wave" -> marine.
    """
    host = "https://api.open-meteo.com/v1/forecast" if kind=="wind" \
        else "https://marine-api.open-meteo.com/v1/marine"
    hourly = "wind_speed_10m" if kind=="wind" else "wave_height"

    def call(s,e):
        url = f"{host}?latitude={lat}&longitude={lon}&hourly={hourly}&start_date={s}&end_date={e}&timezone=UTC"
        r = requests.get(url, timeout=20)
        r.raise_for_status()
        return r.json()

    try:
        return call(start_date, end_date)
    except requests.HTTPError as e:
        if e.response is not None and e.response.status_code == 400:
            # reduce ventana y reintenta
            try:
                s_dt = datetime.fromisoformat(start_date + "T00:00:00+00:00")
                e_dt = s_dt + timedelta(days=3)
                s2, e2 = s_dt.strftime("%Y-%m-%d"), e_dt.strftime("%Y-%m-%d")
                r2 = call(s2, e2)
                return r2
            except Exception:
                pass
        # si no se pudo, propaga para que lo capturemos arriba
        raise

def summarize_route(origin, dest, dep_iso, distance_km, cruise_knots, waypoints=6):
    planned_h = distance_km/(cruise_knots*1.852)

    # ventana válida para OM (ignoramos dep_iso aquí para asegurar pronóstico)
    start_date, end_date = make_valid_window(dep_iso, planned_h)

    pts = [(origin[0],origin[1])] + great_circle_waypoints(origin[0],origin[1],dest[0],dest[1],waypoints)

    winds, waves = [], []
    for (lat,lon) in pts:
        # VIENTO
        try:
            wj = fetch_hourly_safe(lat,lon,start_date,end_date,"wind").get("hourly",{})
            vj = safe_numeric_list(wj.get("wind_speed_10m", []))
            if vj: winds += vj
        except Exception as e:
            print(f"[WARN] wind API fail lat={lat:.3f} lon={lon:.3f}: {e}")

        # OLA
        try:
            mj = fetch_hourly_safe(lat,lon,start_date,end_date,"wave").get("hourly",{})
            vj2 = safe_numeric_list(mj.get("wave_height", []))
            if vj2: waves += vj2
        except Exception as e:
            print(f"[WARN] wave API fail lat={lat:.3f} lon={lon:.3f}: {e}")

        time.sleep(0.2)  # ser amable con la API

    avg_wind_knots = (sum(winds)/len(winds))*MS_TO_KNOTS if winds else 0.0
    max_wave_m = max(waves) if waves else 0.0
    return avg_wind_knots, max_wave_m, planned_h

def main():
    routes = [
        {"name":"Callao→Tokio", "origin":(-12.0464,-77.0428), "dest":(35.6762,139.6503), "distance_km":2500},
        {"name":"NYC→Londres",  "origin":(40.7128,-74.0060), "dest":(51.5074,-0.1278), "distance_km":5600},
        {"name":"Singapur→Shanghái","origin":(1.3521,103.8198),"dest":(31.2304,121.4737),"distance_km":3800},
    ]
    cruise = 18
    base_dep = datetime.utcnow().replace(tzinfo=timezone.utc)

    # Empieza con pocas muestras para validar (evita loops largos si algo falla)
    samples_per_route = 5

    rows = []
    for s in tqdm(routes):
        for k in range(samples_per_route):
            # depart a 1..6 días (se ignora en fetch, pero lo guardamos en CSV)
            dep_iso = (base_dep + timedelta(days=1 + (k % 6))).isoformat().replace("+00:00","Z")

            avg_wind, max_wave, planned_h = summarize_route(
                s["origin"], s["dest"], dep_iso, s["distance_km"], cruise, waypoints=6
            )

            # etiqueta sintética
            delay_h = max(0.0, 0.15 * max_wave * planned_h / 24.0 + 0.05 * avg_wind - 1.0 + random.uniform(-0.5, 0.5))
            is_delay = 1 if delay_h > 2.0 else 0

            rows.append({
                "route": s["name"],
                "distance_km": s["distance_km"],
                "cruise_knots": cruise,
                "planned_hours": planned_h,
                "avg_wind_knots": avg_wind,
                "max_wave_m": max_wave,
                "delay_hours": delay_h,
                "is_delay": is_delay,
                "departure_iso": dep_iso
            })

            time.sleep(0.1)

    df = pd.DataFrame(rows)
    df.to_csv("dataset_weather_delay.csv", index=False)
    print("✅ Dataset guardado en: dataset_weather_delay.csv")
    print(df.head())

if __name__ == "__main__":
    main()
