# train_baseline.py
import numpy as np
import pandas as pd
from sklearn.ensemble import RandomForestRegressor
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_absolute_error
from skl2onnx import convert_sklearn
from skl2onnx.common.data_types import FloatTensorType
import onnxruntime as ort

# -----------------------
# 1) Generar datos sintéticos
# Features: [distance_km, planned_hours, avg_wind_knots, max_wave_m]
# Target: delay_hours (>=0)
rng = np.random.RandomState(42)
N = 5000
distance = rng.uniform(200, 6000, size=N)                # km
cruise_speed_knots = rng.uniform(12, 22, size=N)         # nudos
planned_hours = distance / (cruise_speed_knots * 1.852)  # 1 nudo = 1.852 km/h
avg_wind = rng.uniform(2, 40, size=N)                    # nudos
max_wave = rng.uniform(0.2, 6.0, size=N)                 # metros

# Regla sintética de retraso
base = 0.05*avg_wind + 0.8*np.maximum(0, max_wave-2) + 0.0005*distance
noise = rng.normal(0, 0.4, size=N)
delay_hours = np.maximum(0, base + noise)

X = np.vstack([distance, planned_hours, avg_wind, max_wave]).T.astype(np.float32)
y = delay_hours.astype(np.float32)

# -----------------------
# 2) Entrenar modelo
Xtr, Xte, ytr, yte = train_test_split(X, y, test_size=0.2, random_state=42)
model = RandomForestRegressor(n_estimators=150, random_state=42, n_jobs=-1)
model.fit(Xtr, ytr)
pred = model.predict(Xte)
mae = mean_absolute_error(yte, pred)
print(f"MAE (horas) baseline: {mae:.2f}")

# -----------------------
# 3) Exportar a ONNX (input shape: [None, 4] → output: delay_hours)
initial_type = [('input', FloatTensorType([None, 4]))]
onnx_model = convert_sklearn(model, initial_types=initial_type, target_opset=13)
with open("weather-delay.onnx", "wb") as f:
    f.write(onnx_model.SerializeToString())

# 4) Smoke test onnxruntime
sess = ort.InferenceSession("weather-delay.onnx", providers=["CPUExecutionProvider"])
out = sess.run(None, {"input": Xte[:3].astype(np.float32)})[0]
print("Predicciones ejemplo (horas):", out[:3])
print(">> Archivo ONNX listo: weather-delay.onnx")

