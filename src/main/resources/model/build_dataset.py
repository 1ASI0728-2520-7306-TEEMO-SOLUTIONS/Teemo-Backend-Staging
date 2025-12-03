import pandas as pd
import numpy as np
from datetime import datetime, timedelta
import random
import os

# === CONFIGURACIÓN ===
output_path = os.path.join(os.path.dirname(__file__), "maritime_delays.csv")
n = 300  # cantidad de filas simuladas

ports = [
    ("Callao", -12.0564, -77.1319),
    ("Guayaquil", -2.1962, -79.8862),
    ("Cartagena", 10.3932, -75.4832),
    ("Lisboa", 38.7223, -9.1393),
    ("Dakar", 14.7167, -17.4677),
    ("Shanghai", 31.2304, 121.4737),
    ("Los Angeles", 34.0522, -118.2437),
    ("Rotterdam", 51.9244, 4.4777),
    ("Singapore", 1.3521, 103.8198)
]

def season_from_month(m):
    return ["DJF", "DJF", "MAM", "MAM", "MAM", "JJA", "JJA", "JJA", "SON", "SON", "SON", "DJF"][m-1]

def haversine(lat1, lon1, lat2, lon2):
    R = 6371
    dLat = np.radians(lat2 - lat1)
    dLon = np.radians(lon2 - lon1)
    a = np.sin(dLat/2)**2 + np.cos(np.radians(lat1)) * np.cos(np.radians(lat2)) * np.sin(dLon/2)**2
    return 2 * R * np.arcsin(np.sqrt(a))

rows = []
for i in range(n):
    o, d = random.sample(ports, 2)
    dist = haversine(o[1], o[2], d[1], d[2])
    etd = datetime(2025, random.randint(1, 12), random.randint(1, 28))
    eta_plan = etd + timedelta(hours=dist / random.uniform(30, 40))
    delay = max(0, np.random.normal(8 if random.random() < 0.25 else 2, 3))  # 25% prob. retraso alto
    avg_wind = random.uniform(5, 30)
    max_wave = random.uniform(0.5, 4)
    rain = random.uniform(0, 10)
    vis = random.uniform(5, 20)
    q_o = random.uniform(0, 1)
    q_d = random.uniform(0, 1)

    rows.append({
        "voyage_id": f"V{i+1:03d}",
        "port_origin": o[0],
        "port_dest": d[0],
        "lat_o": o[1], "lon_o": o[2],
        "lat_d": d[1], "lon_d": d[2],
        "gc_distance_km": round(dist, 1),
        "etd_real": etd,
        "eta_plan": eta_plan,
        "delay_hours": round(delay, 2),
        "avg_wind_knots": avg_wind,
        "max_wave_m": max_wave,
        "rain_mm": rain,
        "visibility_km": vis,
        "month": etd.month,
        "season": season_from_month(etd.month),
        "origin_queue": q_o,
        "dest_queue": q_d,
        "port_congestion_idx": (q_o + q_d)/2,
    })

df = pd.DataFrame(rows)
df.to_csv(output_path, index=False)
print(f"✅ Dataset generado: {output_path}")
