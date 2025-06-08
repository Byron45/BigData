from fastapi import FastAPI, File, UploadFile, HTTPException
import traceback

# Importamos la función de predicción que crearemos en el siguiente archivo
from .prediction import predict_animal_and_find_parks

app = FastAPI(
    title="Servicio de Predicción de Animales",
    description="Recibe una imagen y devuelve la predicción del animal y los parques donde se encuentra.",
    version="1.0.0"
)

@app.post("/predict", summary="Predice el animal en una imagen")
async def predict(file: UploadFile = File(...)):
    """
    Endpoint para predecir el animal a partir de una imagen.

    - **file**: Archivo de imagen (JPG, PNG, etc.).
    """
    if not file.content_type.startswith('image/'):
        raise HTTPException(status_code=400, detail="El archivo no es una imagen válida.")
    
    try:
        image_bytes = await file.read()
        # Llama a la lógica de predicción separada
        prediction_result = predict_animal_and_find_parks(image_bytes)
        return prediction_result
    except Exception as e:
        print(traceback.format_exc()) # Imprime el error completo en la consola del servicio
        raise HTTPException(status_code=500, detail=f"Error interno al procesar la imagen: {str(e)}")

@app.get("/", summary="Endpoint de estado")
def root():
    """Confirma que el servicio está en funcionamiento."""
    return {"message": "Servicio de ML para predicción de hábitats está activo."}