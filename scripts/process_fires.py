# scripts/process_fires.py

from pyspark.sql import SparkSession
from pyspark.sql.functions import col, month, to_date

def process_us_fire_data():
    """
    Lee el dataset masivo de incendios desde HDFS, lo optimiza y guarda
    un único archivo JSON directamente en la carpeta de salida mapeada.
    """
    spark = SparkSession.builder \
        .appName("ProcesamientoIncendiosOptimizados") \
        .getOrCreate()

    print("Sesión de Spark iniciada.")

    # Ruta de entrada en HDFS
    input_path = "hdfs://namenode:8020/user/hadoop/datasets/forest-fires/incendiosforestales.csv"
    
    # Ruta de salida DENTRO del contenedor, mapeada a tu PC.
    # Spark escribirá un directorio aquí, por ej: /output/fires_data.json/
    output_path = "/output/fires_data_temp"

    print(f"Leyendo datos desde: {input_path}")
    try:
        df = spark.read.csv(input_path, header=True, inferSchema=True)
    except Exception as e:
        print(f"ERROR al leer el archivo de entrada: {e}")
        spark.stop()
        return

    # Filtramos por causa natural Y por los incendios más grandes para optimizar.
    print("Filtrando por incendios grandes (Clase F y G) de causa natural...")
    optimized_df = df.filter(
        (col("NWCG_CAUSE_CLASSIFICATION") == "Natural") & 
        (col("FIRE_SIZE_CLASS").isin(["F", "G"]))
    )
    
    record_count = optimized_df.count()
    print(f"Se encontraron {record_count} incendios que cumplen los criterios.")
    
    # Seleccionamos y limpiamos solo el subconjunto de datos optimizado
    cleaned_df = optimized_df.select(
        col("FIRE_NAME").alias("name"),
        col("FIRE_YEAR").alias("year"),
        month(to_date(col("DISCOVERY_DATE"))).alias("month"),
        col("LATITUDE").alias("lat"),
        col("LONGITUDE").alias("lon"),
        col("STATE").alias("state")
    ).na.drop(subset=["lat", "lon", "year", "state"])

    print(f"Schema del DataFrame limpio:")
    cleaned_df.printSchema()
    
    # --- LÓGICA DE GUARDADO SIMPLIFICADA ---
    # Guardamos como un único archivo part-* dentro de la carpeta /output/fires_data_temp
    print(f"Guardando datos como JSON en: {output_path}")
    cleaned_df.coalesce(1).write.mode("overwrite").json(output_path)
    
    # Ahora usamos comandos del sistema operativo para mover y renombrar el archivo
    # Esto es mucho más fiable que usar la API de Java desde PySpark
    import os
    import shutil

    # El archivo generado por Spark está en /output/fires_data_temp/part-*.json
    source_dir = output_path
    dest_file = "/output/fires_data.json" # El nombre final que queremos

    try:
        # Encontrar el archivo part-*
        part_file_name = [f for f in os.listdir(source_dir) if f.endswith('.json')][0]
        source_file_path = os.path.join(source_dir, part_file_name)
        
        # Mover y renombrar
        shutil.move(source_file_path, dest_file)
        print(f"Archivo renombrado exitosamente a: {dest_file}")
        
        # Eliminar el directorio temporal vacío
        shutil.rmtree(source_dir)

    except Exception as e:
        print(f"ERROR al intentar renombrar el archivo de salida: {e}")

    finally:
        spark.stop()
        print("Proceso completado.")

if __name__ == "__main__":
    process_us_fire_data()