# data/train_model.py
import os, json
import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import LabelEncoder, StandardScaler
from sklearn.metrics import mean_absolute_error, r2_score
from xgboost import XGBRegressor
from sklearn.ensemble import RandomForestRegressor

BASE_DIR = os.path.dirname(__file__)
DATA_PATH = os.path.join(BASE_DIR, "maritime_delays.csv")
ONNX_PATH = os.path.join(BASE_DIR, "weather-delay.onnx")
PREP_PATH = os.path.join(BASE_DIR, "preprocess.json")

print("📦 Cargando dataset:", DATA_PATH)
df = pd.read_csv(DATA_PATH)
print("Filas:", len(df), " Columnas:", len(df.columns))

target = "delay_hours"
base_features = [
    "gc_distance_km","avg_wind_knots","max_wave_m","rain_mm",
    "visibility_km","month","origin_queue","dest_queue","port_congestion_idx",
    "lat_o","lon_o","lat_d","lon_d"
]
cat_cols = ["port_origin","port_dest","season"]

# ---- LabelEncoders (guardar mapeos) ----
label_maps = {}
for col in cat_cols:
    le = LabelEncoder()
    df[col] = le.fit_transform(df[col].astype(str))
    label_maps[col] = {cls: int(code) for cls, code in zip(le.classes_, le.transform(le.classes_))}

features = base_features + cat_cols  # total 16

# limpiar
df = df.dropna(subset=[target] + features)
X = df[features].astype(float)
y = df[target].astype(float)

# ---- Escalado (guardar mean/scale) ----
scaler = StandardScaler()
X_scaled = scaler.fit_transform(X)

X_train, X_test, y_train, y_test = train_test_split(X_scaled, y, test_size=0.2, random_state=42)

print("⚙️ Entrenando modelo XGBoost...")
xgb = XGBRegressor(
    n_estimators=250, max_depth=6, learning_rate=0.08,
    subsample=0.9, colsample_bytree=0.9, random_state=42
)
xgb.fit(X_train, y_train)

y_pred = xgb.predict(X_test)
mae = mean_absolute_error(y_test, y_pred)
r2  = r2_score(y_test, y_pred)
print("📊 Evaluación del modelo:")
print(f"  MAE: {mae:.2f} horas")
print(f"  R²:  {r2:.3f}")

# ---- Exportar a ONNX ----
import skl2onnx
from skl2onnx.common.data_types import FloatTensorType
from skl2onnx import convert_sklearn

def try_export_skl2onnx(model, n_features):
    initial_type = [("input", FloatTensorType([None, n_features]))]
    return convert_sklearn(model, initial_types=initial_type)

onnx_model = None
n_features = len(features)
try:
    # skl2onnx no soporta XGBRegressor ⇒ esto lanzará
    onnx_model = try_export_skl2onnx(xgb, n_features)
    print("✅ skl2onnx exportó XGBoost (sorprendente)")
except Exception:
    print("ℹ️ skl2onnx no pudo convertir XGBoost. Probando conversor de XGBoost…")
    try:
        from onnxmltools.convert import convert_xgboost
        from onnxmltools.utils import save_model
        onnx_model = convert_xgboost(xgb.get_booster(), initial_types=[("input", FloatTensorType([None, n_features]))])
        save_model(onnx_model, ONNX_PATH)
        print(f"✅ ONNX exportado con onnxmltools (XGBoost): {ONNX_PATH}")
    except Exception:
        print("⚠️ onnxmltools también falló. Generando un modelo RandomForest solo para ONNX…")
        rf = RandomForestRegressor(n_estimators=300, random_state=42)
        rf.fit(X_train, y_train)
        onnx_model = try_export_skl2onnx(rf, n_features)
        with open(ONNX_PATH, "wb") as f:
            f.write(onnx_model.SerializeToString())
        print(f"✅ ONNX exportado con RandomForest (fallback): {ONNX_PATH}")

# Guardar preprocessing
preproc = {
    "feature_order": features,
    "scaler_mean": scaler.mean_.tolist(),
    "scaler_scale": scaler.scale_.tolist(),
    "label_maps": label_maps
}
with open(PREP_PATH, "w", encoding="utf-8") as f:
    json.dump(preproc, f, ensure_ascii=False, indent=2)

print(f"📝 Preprocesamiento guardado en: {PREP_PATH}")
