import React, { useState, useEffect } from 'react';
import MapComponent from './components/MapComponent';
import UploadComponent from './components/UploadComponent';
import FilterComponent from './components/FilterComponent';
import './App.css';

// --- Hook personalizado para Debouncing ---
// Esto hace que la interfaz sea más fluida al no actualizar el mapa
// en cada pequeño cambio del deslizador, sino solo cuando el usuario se detiene.
function useDebounce(value, delay) {
    const [debouncedValue, setDebouncedValue] = useState(value);
    useEffect(() => {
        const handler = setTimeout(() => {
            setDebouncedValue(value);
        }, delay);
        return () => {
            clearTimeout(handler);
        };
    }, [value, delay]);
    return debouncedValue;
}

function App() {
    // Estado para la comunicación entre componentes
    const [predictionData, setPredictionData] = useState(null);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState(null);

    // Estado para los filtros de incendios
    const [exploreYear, setExploreYear] = useState(2015);
    const [exploreMonth, setSelectedMonth] = useState(0); // 0 significa "Todos los meses"
    const yearRange = { min: 1992, max: 2015 }; // Rango del dataset de incendios

    // Aplicamos el "debounce" a los valores de los filtros para mejorar el rendimiento
    const debouncedYear = useDebounce(exploreYear, 250); // Retraso de 250ms
    const debouncedMonth = useDebounce(exploreMonth, 250);

    // Función que se llama cuando el UploadComponent tiene un resultado exitoso
    const handlePredictionSuccess = (data) => {
        setPredictionData(data);
    };

    return (
        <div className="App">
            <header className="App-header">
                <h1>Identificador de Hábitats de Animales con Big Data</h1>
                <p>Sube una imagen y descubre su hábitat o explora los incendios por año y mes.</p>
            </header>

            <main className="main-content">
                {/* --- Columna Izquierda: Controles --- */}
                <div className="controls-area">
                    <UploadComponent 
                        onPrediction={handlePredictionSuccess} 
                        onLoading={setIsLoading}
                        onError={setError}
                    />
                    
                    {isLoading && <div className="loader">Analizando...</div>}
                    {error && <div className="error-message">{error}</div>}

                    {predictionData && (
                        <div className="prediction-results">
                            <h3>Resultado de la Predicción</h3>
                            <p><strong>Animal:</strong> {predictionData.prediccion_principal.animal}</p>
                            <p><strong>Confianza:</strong> {predictionData.prediccion_principal.confianza}</p>
                        </div>
                    )}

                    <FilterComponent 
                        year={exploreYear} 
                        setYear={setExploreYear} 
                        month={exploreMonth}
                        setMonth={setSelectedMonth}
                        minYear={yearRange.min} 
                        maxYear={yearRange.max} 
                    />
                </div>

                {/* --- Columna Derecha: Mapa --- */}
                <div className="map-area">
                    <MapComponent 
                        prediction={predictionData} 
                        selectedYear={debouncedYear}
                        selectedMonth={debouncedMonth}
                    />
                </div>
            </main>
        </div>
    );
}

export default App;