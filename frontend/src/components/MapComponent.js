import React, { useEffect, useState } from 'react';
import { MapContainer, TileLayer, Marker, Popup, LayersControl, FeatureGroup } from 'react-leaflet';
import MarkerClusterGroup from 'react-leaflet-markercluster';
import 'leaflet/dist/leaflet.css';
import 'leaflet.markercluster/dist/MarkerCluster.css';
import 'leaflet.markercluster/dist/MarkerCluster.Default.css';
import L from 'leaflet';
import axios from 'axios';

// --- Íconos personalizados ---
const parkIcon = new L.Icon({
    iconUrl: 'https://cdn-icons-png.flaticon.com/512/3201/3201862.png',
    iconSize: [35, 35], iconAnchor: [17, 35], popupAnchor: [0, -35]
});

const fireIcon = new L.Icon({
    iconUrl: 'https://cdn-icons-png.flaticon.com/512/785/785116.png',
    iconSize: [25, 25], iconAnchor: [12, 25], popupAnchor: [0, -25]
});

function MapComponent({ prediction, selectedYear, selectedMonth }) {
    const [allParks, setAllParks] = useState([]);
    const [allFires, setAllFires] = useState([]);
    const [parksToShow, setParksToShow] = useState([]);
    const [firesToShow, setFiresToShow] = useState([]);
    const [mapInstance, setMapInstance] = useState(null);

    // Carga de datos inicial (solo se ejecuta una vez)
    useEffect(() => {
        axios.get('/api/parques').then(res => {
            const parks = res.data.map(p => ({ ...p, Latitude: parseFloat(p.Latitude), Longitude: parseFloat(p.Longitude) })).filter(p => !isNaN(p.Latitude));
            setAllParks(parks);
        }).catch(err => console.error("Error cargando parques:", err));

        fetch('/data/fires_data.json')
            .then(response => {
                if (!response.ok) throw new Error("Archivo fires_data.json no encontrado");
                return response.text();
            })
            .then(text => {
                const jsonData = text.split('\n').filter(line => line.trim() !== '').map(line => JSON.parse(line));
                const fires = jsonData.map(f => ({ ...f, year: parseInt(f.year), month: parseInt(f.month), lat: parseFloat(f.lat), lon: parseFloat(f.lon) })).filter(f => !isNaN(f.lat) && !isNaN(f.lon) && f.year);
                setAllFires(fires);
                console.log(`Éxito: ${fires.length} incendios optimizados cargados.`);
            })
            .catch(error => console.error("Error cargando datos de incendios:", error));
    }, []);

    // Lógica de filtrado y actualización
    useEffect(() => {
        let finalParks = [];
        let finalFires = [];

        if (prediction && prediction.ubicacion.parques_encontrados.length > 0) {
            // --- Modo Análisis de Hábitat ---
            finalParks = allParks.filter(p => prediction.ubicacion.parques_encontrados.includes(p['Park Name']?.trim()));
            const parkStates = [...new Set(finalParks.map(p => p.State))];
            finalFires = allFires.filter(fire => 
                fire.year === selectedYear &&
                (selectedMonth === 0 || fire.month === selectedMonth) &&
                parkStates.includes(fire.state)
            );
        } else {
            // --- Modo Exploración ---
            finalFires = allFires.filter(f => 
                f.year === selectedYear && 
                (selectedMonth === 0 || f.month === selectedMonth)
            );
        }
        setParksToShow(finalParks);
        setFiresToShow(finalFires);
    }, [prediction, selectedYear, selectedMonth, allParks, allFires]);

    // Efecto para ajustar la vista del mapa
    useEffect(() => {
        if (!mapInstance) return;
        const bounds = [];
        parksToShow.forEach(p => bounds.push([p.Latitude, p.Longitude]));
        firesToShow.forEach(f => bounds.push([f.lat, f.lon]));

        if (bounds.length > 0) {
            mapInstance.fitBounds(bounds, { padding: [50, 50], maxZoom: 12 });
        } else {
            mapInstance.setView([39.82, -98.57], 4);
        }
    }, [parksToShow, firesToShow, mapInstance]);

    return (
        <div className="map-container">
            <h2>2. Resultados en el Mapa</h2>
            <p>Usa el control de capas para mostrar/ocultar los datos.</p>
            <MapContainer 
                center={[39.82, -98.57]} 
                zoom={4} 
                style={{ height: '70vh', width: '100%' }}
                whenCreated={setMapInstance}
            >
                <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
                <LayersControl position="topright">
                    <LayersControl.Overlay checked name="Hábitats de Animales">
                        <FeatureGroup>
                            {parksToShow.map(park => (
                                <Marker key={`park-${park['Park Code']}`} position={[park.Latitude, park.Longitude]} icon={parkIcon}>
                                    <Popup><strong>{park['Park Name']}</strong></Popup>
                                </Marker>
                            ))}
                        </FeatureGroup>
                    </LayersControl.Overlay>
                    <LayersControl.Overlay checked name="Incendios Forestales">
                        <MarkerClusterGroup>
                            {firesToShow.map((fire, index) => (
                                <Marker key={`fire-${index}`} position={[fire.lat, fire.lon]} icon={fireIcon}>
                                   <Popup><strong>{fire.name || 'Incendio'}</strong><br/>Año: {fire.year}</Popup>
                                </Marker>
                            ))}
                        </MarkerClusterGroup>
                    </LayersControl.Overlay>
                </LayersControl>
            </MapContainer>
        </div>
    );
}

export default MapComponent;