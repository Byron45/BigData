import React, { useState } from 'react';
import axios from 'axios';
import './UploadComponent.css'; // Crearemos este archivo para los estilos

function UploadComponent({ onPrediction, onLoading, onError }) {
    const [selectedFile, setSelectedFile] = useState(null);
    const [preview, setPreview] = useState(null);

    const handleFileChange = (event) => {
        const file = event.target.files[0];
        if (file) {
            setSelectedFile(file);
            setPreview(URL.createObjectURL(file));
        }
    };

    const handleUpload = async () => {
        if (!selectedFile) {
            alert('Por favor, selecciona un archivo de imagen.');
            return;
        }
        onLoading(true); // Activa el estado de carga
        onError(null); // Limpia errores previos
        onPrediction(null); // Limpia predicciones previas

        const formData = new FormData();
        formData.append('image', selectedFile);

        try {
            // Gracias al proxy, solo necesitamos la ruta relativa
            const response = await axios.post('/api/predict', formData, {
                headers: { 'Content-Type': 'multipart/form-data' },
            });
            onPrediction(response.data);
        } catch (error) {
            console.error('Error al subir la imagen:', error);
            onError('Hubo un error en la predicción. Revisa la consola para más detalles.');
        } finally {
            onLoading(false); // Desactiva el estado de carga
        }
    };

    return (
        <div className="upload-container">
            <h2>1. Carga una imagen de un animal</h2>
            <input type="file" accept="image/*" onChange={handleFileChange} />
            {preview && <img src={preview} alt="Vista previa" className="image-preview" />}
            <button onClick={handleUpload} disabled={!selectedFile}>
                Analizar Hábitat
            </button>
        </div>
    );
}

export default UploadComponent;