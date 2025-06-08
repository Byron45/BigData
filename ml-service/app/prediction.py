import os
import json
import pandas as pd
import numpy as np
import tensorflow as tf
from tensorflow.keras.applications.mobilenet_v2 import preprocess_input
from tensorflow.keras.preprocessing import image as keras_image_utils
from PIL import Image
import io
import re

print("MÓDULO DE PREDICCIÓN (V3) CARGADO") # <-- Línea de verificación

# --- RUTAS ---
MODEL_PATH = 'models/animal_classifier_final_mobilenetv2_savedmodel_dir'
CLASSES_PATH = 'models/animal_classes.json'
SPECIES_CSV_PATH = 'data/species.csv'

# --- CARGA INICIAL DE MODELOS Y DATOS ---
print("Iniciando carga de modelo y datos...")
model = tf.saved_model.load(MODEL_PATH)
infer = model.signatures['serving_default']
print("-> Modelo de TensorFlow cargado.")

with open(CLASSES_PATH, 'r') as f:
    animal_classes = json.load(f)
print(f"-> {len(animal_classes)} clases de animales cargadas.")

# --- Lógica de Pandas MÁS ROBUSTA (adaptada directamente de tu notebook) ---
species_df = pd.read_csv(SPECIES_CSV_PATH, low_memory=False)
species_df.columns = [col.strip() for col in species_df.columns] # Limpiar nombres de columnas

animal_to_species_map = {animal: [] for animal in animal_classes}
species_df['Common_Names_Cleaned'] = species_df['Common Names'].astype(str).str.lower().str.strip()

for animal_class in animal_classes:
    pattern = r'\b' + re.escape(animal_class) + r'\b'
    mask = species_df['Common_Names_Cleaned'].str.contains(pattern, na=False)
    matched_sci_names = species_df.loc[mask, 'Scientific Name'].dropna().unique().tolist()
    if matched_sci_names:
        animal_to_species_map[animal_class].extend(matched_sci_names)

species_to_animal_class_map = {}
for animal_cls, sci_names_list in animal_to_species_map.items():
    for sci_name in sci_names_list:
        species_to_animal_class_map[sci_name] = animal_cls

species_df['animal_class_from_map'] = species_df['Scientific Name'].map(species_to_animal_class_map)

biodiversity_data_filtered = species_df[
    species_df['animal_class_from_map'].notna() & (species_df['Occurrence'] == 'Present')
].copy()

park_animal_distribution = biodiversity_data_filtered.groupby(
    ['Park Name', 'animal_class_from_map']
).size().unstack(fill_value=0)

print("-> Datos de biodiversidad procesados y listos.")
print("Carga inicial completa. Servicio listo para recibir peticiones.")
# --- FIN DE LA CARGA INICIAL ---

def predict_animal_and_find_parks(image_bytes: bytes):
    print("\n--- INICIANDO PREDICCIÓN PARA UNA NUEVA IMAGEN ---") # <-- Línea de verificación
    
    img = Image.open(io.BytesIO(image_bytes)).convert('RGB')
    img_resized = img.resize((224, 224))
    
    img_array = keras_image_utils.img_to_array(img_resized)
    img_array_expanded = np.expand_dims(img_array, axis=0)
    img_preprocessed = preprocess_input(img_array_expanded)
    
    tensor_input = tf.constant(img_preprocessed, dtype=tf.float32)

    # --- Llamada simplificada y robusta al modelo ---
    prediction_dict = infer(tensor_input)
    output_tensor_name = list(prediction_dict.keys())[0]
    prediction_vector = prediction_dict[output_tensor_name].numpy()
    # --- Fin de la llamada ---
    
    print(f"Predicción cruda (shape): {prediction_vector.shape}")

    predicted_class_index = np.argmax(prediction_vector[0])
    
    if predicted_class_index >= len(animal_classes):
        raise ValueError(f"Índice de predicción ({predicted_class_index}) fuera de rango.")

    predicted_animal_class = animal_classes[predicted_class_index]
    confidence = float(prediction_vector[0][predicted_class_index])
    
    print(f"Animal predicho: {predicted_animal_class} con confianza {confidence:.4f}")

    # --- Lógica de búsqueda de parques (sin cambios) ---
    location_info = f"La clase '{predicted_animal_class}' no se encontró en los datos de distribución de parques."
    parks_present = []
    if predicted_animal_class in park_animal_distribution.columns:
        park_series = park_animal_distribution[predicted_animal_class]
        parks_present_series = park_series[park_series > 0]
        if not parks_present_series.empty:
            parks_present = parks_present_series.index.tolist()
            location_info = f"Hábitats encontrados en los siguientes parques: {', '.join(parks_present)}"

    top_n = 5
    top_indices = np.argsort(prediction_vector[0])[-top_n:][::-1]
    top_predictions = [
        {"clase": animal_classes[i], "probabilidad": f"{float(prediction_vector[0][i]):.4f}"}
        for i in top_indices
    ]

    result = {
        "prediccion_principal": { "animal": predicted_animal_class, "confianza": f"{confidence:.4f}" },
        "top_5_predicciones": top_predictions,
        "ubicacion": { "parques_encontrados": parks_present, "mensaje": location_info }
    }
    
    print("--- PREDICCIÓN COMPLETADA ---")
    return result