# train_model.py
from pathlib import Path
import warnings

import numpy as np
import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_absolute_error, mean_squared_error, roc_auc_score
from sklearn.ensemble import GradientBoostingRegressor
from skl2onnx import convert_sklearn
from skl2onnx.common.data_types import FloatTensorType
import onnx

# ------------------------------------------------------------------
# Config
# ------------------------------------------------------------------
SEED = 42
np.random.seed(SEED)

# Ruta del CSV junto al script (independiente del working dir)
CSV_PATH = Path(__file__).with_name("dataset_weather_delay.csv")
MODEL_PATH = Path(__file__).with_name("weather-delay.onnx")

# ------------------------------------------------------------------
# Cargar dataset
# ------------------------------------------------------------------
print(f"[DEBUG] Leyendo CSV en: {CSV_PATH.resolve()}")
if not CSV_PATH.exists():
    raise FileNotFoundError(
        f"No se encontró {CSV_PATH.name}. "
        f"Genera el dataset ejecutando: python build_dataset.py"
    )

df = pd.read_csv(CSV_PATH)
if df.empty:
    raise RuntimeError(f"{CSV_PATH.name} está vacío. Revisa build_dataset.py")

required_cols = {
    "distance_km", "planned_hours", "avg_wind_knots", "max_wave_m", "delay_hours"
}
missing = required_cols - set(df.columns)
if missing:
    raise RuntimeError(f"Faltan columnas en el CSV: {sorted(missing)}")

# ------------------------------------------------------------------
# Features / Targets
# ------------------------------------------------------------------
features = ["distance_km", "planned_hours", "avg_wind_knots", "max_wave_m"]
X = df[features].values.astype(np.float32)
y = df["delay_hours"].values.astype(np.float32)

# Etiqueta binaria opcional para métricas de clasificación
is_delay = (df["delay_hours"] > 2.0).astype(int).values

# Split (intenta estratificar si hay 2 clases)
stratify_opt = is_delay if len(np.unique(is_delay)) == 2 else None
Xtr, Xte, ytr, yte, dtr, dte = train_test_split(
    X, y, is_delay, test_size=0.2, random_state=SEED, stratify=stratify_opt
)

# ------------------------------------------------------------------
# Entrenamiento (baseline CPU-friendly)
# ------------------------------------------------------------------
model = GradientBoostingRegressor(random_state=SEED)
model.fit(Xtr, ytr)

# ------------------------------------------------------------------
# Métricas
# ------------------------------------------------------------------
pred = model.predict(Xte)
mae = mean_absolute_error(yte, pred)

# mean_squared_error(squared=False) -> RMSE (en 1.4 muestra FutureWarning; lo silenciamos)
with warnings.catch_warnings():
    warnings.simplefilter("ignore")
    rmse = mean_squared_error(yte, pred, squared=False)

# ROC-AUC solo si el y_true de test tiene ambas clases
roc = float("nan")
if len(np.unique(dte)) == 2:
    roc = roc_auc_score(dte, (pred > 2.0).astype(int))
else:
    print("[WARN] ROC-AUC no definido: el conjunto de test sólo tiene una clase.")

print(f"MAE={mae:.3f}  RMSE={rmse:.3f}  ROC-AUC={roc:.3f}")
print("Filas en dataset:", len(df))

# ------------------------------------------------------------------
# Exportar a ONNX (input shape [None, 4])
# ------------------------------------------------------------------
onnx_model = convert_sklearn(model, initial_types=[("input", FloatTensorType([None, 4]))])
onnx.save_model(onnx_model, str(MODEL_PATH))
print(f"✅ Guardado ONNX: {MODEL_PATH.resolve()}")
