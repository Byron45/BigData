# ---- Dockerfile para el servicio de Python (ml-service/Dockerfile) ----

# 1. Usar una imagen base oficial de Python
FROM python:3.9-slim

# 2. Establecer el directorio de trabajo dentro del contenedor
WORKDIR /app

# 3. Copiar el archivo de requerimientos e instalarlos
# Esto se hace primero para aprovechar el caché de Docker si los requerimientos no cambian.
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 4. Copiar los directorios de modelos y datos
COPY ./models/ ./models/
COPY ./data/ ./data/

# 5. Copiar el código de la aplicación (el directorio app/)
COPY ./app/ ./app/

# 6. Exponer el puerto en el que correrá la API
EXPOSE 8000

# 7. Comando para iniciar el servidor uvicorn cuando el contenedor arranque
# --host 0.0.0.0 es crucial para que sea accesible desde otros contenedores de la red Docker.
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]