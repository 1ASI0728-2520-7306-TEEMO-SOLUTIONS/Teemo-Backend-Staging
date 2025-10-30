import onnx

# ruta relativa al modelo
path = "../src/main/resources/model/weather-delay.onnx"

model = onnx.load(path)
print("Inputs :", [i.name for i in model.graph.input])
print("Outputs:", [o.name for o in model.graph.output])

for i in model.graph.input:
    dims = [d.dim_value if d.dim_value > 0 else -1 for d in i.type.tensor_type.shape.dim]
    print(" -", i.name, "shape:", dims)
