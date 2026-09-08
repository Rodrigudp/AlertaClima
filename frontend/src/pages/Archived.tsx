import { useState, useEffect } from 'react';
import { api } from '../api';
import { format } from 'date-fns';

export default function Archived() {
  const [alerts, setAlerts] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchAlerts();
  }, []);

  const fetchAlerts = async () => {
    try {
      const res = await api.get('/alertas/arquivados');
      setAlerts(res.data);
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-gray-800">Alertas Arquivados</h1>
      
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm text-gray-500">
            <thead className="text-xs text-gray-700 uppercase bg-gray-50">
              <tr>
                <th className="px-4 py-3">ID</th>
                <th className="px-4 py-3">Evento</th>
                <th className="px-4 py-3">Data Original</th>
                <th className="px-4 py-3">Data Arquivamento</th>
                <th className="px-4 py-3">Motivo/Status</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan={5} className="text-center py-4">Carregando...</td></tr>
              ) : alerts.length === 0 ? (
                <tr><td colSpan={5} className="text-center py-4">Nenhum alerta arquivado encontrado</td></tr>
              ) : alerts.map(alert => (
                <tr key={alert.id} className="border-b hover:bg-gray-50">
                  <td className="px-4 py-3">#{String(alert.codigo ?? '?').padStart(4, '0')}</td>
                  <td className="px-4 py-3 font-medium text-gray-900">{alert.titulo}</td>
                  <td className="px-4 py-3">{format(new Date(alert.data_evento), 'dd/MM/yyyy HH:mm')}</td>
                  <td className="px-4 py-3">{alert.arquivadoEm ? format(new Date(alert.arquivadoEm), 'dd/MM/yyyy HH:mm') : '-'}</td>
                  <td className="px-4 py-3 text-red-600 font-medium">ARQUIVADO</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
