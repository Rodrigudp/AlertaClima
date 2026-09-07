import { useState, useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import { api } from '../api';
import { format } from 'date-fns';

export default function MapPage() {
  const [alerts, setAlerts] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [distanceFilter, setDistanceFilter] = useState('');
  const [timeFilter, setTimeFilter] = useState('');
  const [userLat, setUserLat] = useState<number | null>(null);
  const [userLon, setUserLon] = useState<number | null>(null);

  useEffect(() => {
    // Get user location for distance filter
    if ('geolocation' in navigator) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          setUserLat(position.coords.latitude);
          setUserLon(position.coords.longitude);
        }
      );
    }
    fetchAlerts();
  }, []);

  const fetchAlerts = async () => {
    setLoading(true);
    try {
      let url = '/alerts?';
      if (distanceFilter && userLat && userLon) {
        url += `lat=${userLat}&lon=${userLon}&distance=${distanceFilter}&`;
      }
      if (timeFilter) {
        const now = new Date();
        const past = new Date(now.getTime() - parseInt(timeFilter) * 60000);
        url += `start_date=${past.toISOString()}&end_date=${now.toISOString()}`;
      }
      
      const res = await api.get(url);
      setAlerts(res.data);
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAlerts();
  }, [distanceFilter, timeFilter]);

  const center: [number, number] = userLat && userLon 
    ? [userLat, userLon] 
    : [-23.5505, -46.6333]; // Default SP

  return (
    <div className="h-full flex flex-col space-y-4">
      <h1 className="text-2xl font-bold text-gray-800">Mapa de Alertas</h1>
      
      <div className="bg-white p-4 rounded-lg shadow-sm border border-gray-200 flex flex-wrap gap-4 items-end">
        <div>
          <label className="block text-xs font-medium text-gray-700 mb-1">Distância (da sua localização)</label>
          <select 
            value={distanceFilter}
            onChange={(e) => setDistanceFilter(e.target.value)}
            className="p-2 border border-gray-300 rounded text-sm w-48"
            disabled={!userLat}
          >
            <option value="">Qualquer distância</option>
            <option value="5">Até 5 km</option>
            <option value="10">Até 10 km</option>
            <option value="25">Até 25 km</option>
            <option value="50">Até 50 km</option>
            <option value="100">Até 100 km</option>
          </select>
          {!userLat && <p className="text-[10px] text-red-500">Permita GPS para filtrar</p>}
        </div>

        <div>
          <label className="block text-xs font-medium text-gray-700 mb-1">Período</label>
          <select 
            value={timeFilter}
            onChange={(e) => setTimeFilter(e.target.value)}
            className="p-2 border border-gray-300 rounded text-sm w-48"
          >
            <option value="">Qualquer período</option>
            <option value="30">Últimos 30 minutos</option>
            <option value="60">Últimos 60 minutos</option>
            <option value="180">Últimas 3 horas</option>
            <option value="360">Últimas 6 horas</option>
            <option value="1440">Últimas 24 horas</option>
          </select>
        </div>
      </div>

      <div className="flex-1 bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden min-h-[400px]">
        {loading && <div className="absolute inset-0 bg-white/50 z-50 flex items-center justify-center">Carregando mapa...</div>}
        <MapContainer center={center} zoom={5} className="h-full w-full">
          <TileLayer
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />
          
          {alerts.map(alert => (
            <Marker key={alert.id} position={[alert.latitude, alert.longitude]}>
              <Popup>
                <div className="space-y-1">
                  <h3 className="font-bold text-sm">{alert.title}</h3>
                  <p className="text-xs text-gray-600">{alert.event_type}</p>
                  <p className="text-xs font-medium text-red-600">Perigo: {alert.danger_level}</p>
                  <p className="text-xs font-medium text-blue-600">Status: {alert.status}</p>
                  <p className="text-xs text-gray-500">{format(new Date(alert.event_date), 'dd/MM/yyyy HH:mm')}</p>
                </div>
              </Popup>
            </Marker>
          ))}
        </MapContainer>
      </div>
    </div>
  );
}
