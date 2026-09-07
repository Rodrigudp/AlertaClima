import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/Layout';
import Dashboard from './pages/Dashboard';
import NewAlert from './pages/NewAlert';
import MapPage from './pages/MapPage';
import Archived from './pages/Archived';
import AlertDetails from './pages/AlertDetails';
import 'leaflet/dist/leaflet.css';

// Fix leaflet icons
import L from 'leaflet';
import icon from 'leaflet/dist/images/marker-icon.png';
import iconShadow from 'leaflet/dist/images/marker-shadow.png';

let DefaultIcon = L.icon({
    iconUrl: icon,
    shadowUrl: iconShadow,
    iconSize: [25, 41],
    iconAnchor: [12, 41]
});
L.Marker.prototype.options.icon = DefaultIcon;

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Layout />}>
          <Route index element={<Navigate to="/dashboard" />} />
          <Route path="dashboard" element={<Dashboard />} />
          <Route path="novo-alerta" element={<NewAlert />} />
          <Route path="mapa" element={<MapPage />} />
          <Route path="arquivados" element={<Archived />} />
          <Route path="alerta/:id" element={<AlertDetails />} />
        </Route>
      </Routes>
    </Router>
  );
}

export default App;
