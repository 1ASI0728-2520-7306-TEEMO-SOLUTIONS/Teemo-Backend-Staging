# train_global.py
import os, json, time
import pandas as pd
import numpy as np

from sklearn.model_selection import train_test_split
from sklearn.preprocessing import LabelEncoder, StandardScaler
from sklearn.metrics import mean_absolute_error, r2_score

# Modelos
from xgboost import XGBRegressor
from sklearn.ensemble import RandomForestRegressor

# ONNX
import onnx
from skl2onnx import convert_sklearn
from skl2onnx.common.data_types import FloatTensorType
from onnxmltools.convert import convert_xgboost
from onnxmltools.utils import save_model

# Barra de progreso para el fallback
from tqdm import tqdm

# ---------- RUTAS ----------
BASE_DIR  = os.path.dirname(__file__)
DATA_PATH = os.path.join(BASE_DIR, "maritime_delays.csv")
ONNX_PATH = os.path.join(BASE_DIR, "weather-delay.onnx")
PREP_PATH = os.path.join(BASE_DIR, "preprocess.json")

print("📦 Cargando:", DATA_PATH)
df = pd.read_csv(DATA_PATH)
print(f"Filas={len(df)}  Columnas={len(df.columns)}")

# ---------- COLUMNAS ----------
target = "delay_hours"
features = [
    "gc_distance_km", "avg_wind_knots", "max_wave_m", "rain_mm",
    "visibility_km", "month", "origin_queue", "dest_queue", "port_congestion_idx",
    "lat_o", "lon_o", "lat_d", "lon_d"
]
cat_cols = ["port_origin", "port_dest", "season"]

# ---------- ENCODERS ----------
encoders = {}
for col in cat_cols:
    le = LabelEncoder()
    df[col] = le.fit_transform(df[col].astype(str))
    encoders[col] = {"classes_": le.classes_.tolist()}
features += cat_cols

# ---------- LIMPIEZA ----------
df = df.dropna(subset=[target] + features).reset_index(drop=True)

# ---------- ESCALADO ----------
X_raw = df[features].astype(float).values
y = df[target].astype(float).values

scaler = StandardScaler()
X_scaled = scaler.fit_transform(X_raw)

# ---------- SPLIT ----------
X_tr, X_va, y_tr, y_va = train_test_split(X_scaled, y, test_size=0.2, random_state=42)

# ---------- XGBOOST ----------
xgb_model = XGBRegressor(
    n_estimators=1200,
    max_depth=6,
    learning_rate=0.05,
    subsample=0.9,
    colsample_bytree=0.9,
    reg_lambda=1.0,
    random_state=42,
    n_jobs=-1,
    tree_method="hist",  # rápido en CPU
)

print("⚙️ Entrenando XGBoost…")
t0 = time.time()
xgb_model.fit(X_tr, y_tr)  # compatible con versiones antiguas
dur_xgb = time.time() - t0

# Evaluación
pred = xgb_model.predict(X_va)
mae = mean_absolute_error(y_va, pred)
r2  = r2_score(y_va, pred)
print(f"📊 XGB   MAE: {mae:.2f}  R²: {r2:.3f}")

# ---------- EXPORTACIÓN A ONNX ----------
n_features = X_tr.shape[1]
initial_type = [("input", FloatTensorType([None, n_features]))]

exported = False
try:
    # Nota: muchas veces skl2onnx no soporta XGBoost y aquí fallará.
    onx = convert_sklearn(xgb_model, initial_types=initial_type)
    with open(ONNX_PATH, "wb") as f:
        f.write(onx.SerializeToString())
    exported = True
    print("✅ ONNX exportado con skl2onnx:", ONNX_PATH)
except Exception as e1:
    print("ℹ️ skl2onnx no convirtió XGB. Probando onnxmltools…", repr(e1))
    try:
        onx = convert_xgboost(xgb_model, initial_types=initial_type)
        save_model(onx, ONNX_PATH)
        exported = True
        print("✅ ONNX exportado con onnxmltools:", ONNX_PATH)
    except Exception as e2:
        print("⚠️ onnxmltools también falló:", repr(e2))

# ---------- FALLBACK: RANDOM FOREST ----------
if not exported:
    print("⏮  Fallback: RandomForestRegressor…")
    total_trees = 400
    step = 40
    rf = RandomForestRegressor(n_estimators=0, warm_start=True, n_jobs=-1, random_state=42)
    with tqdm(total=total_trees, desc="RandomForest training", unit="tree") as pbar:
        while rf.n_estimators < total_trees:
            rf.n_estimators += step
            rf.fit(X_tr, y_tr)
            pbar.update(step)

    prf = rf.predict(X_va)
    print("📊 RF    MAE: %.2f  R²: %.3f" % (mean_absolute_error(y_va, prf), r2_score(y_va, prf)))

    onx = convert_sklearn(rf, initial_types=initial_type)
    with open(ONNX_PATH, "wb") as f:
        f.write(onx.SerializeToString())
    print("✅ ONNX exportado con RandomForest (fallback):", ONNX_PATH)

# ---------- PREPROCESAMIENTO ----------
prep = {
    "features_order": features,
    "scaler_mean_": scaler.mean_.tolist(),
    "scaler_scale_": scaler.scale_.tolist(),
    "encoders": encoders
}
with open(PREP_PATH, "w", encoding="utf-8") as f:
    json.dump(prep, f, ensure_ascii=False, indent=2)

print(f"📝 Preprocesamiento guardado en: {PREP_PATH}")
print(f"⏱️  Tiempo XGB (esta corrida): {dur_xgb:.1f}s")
