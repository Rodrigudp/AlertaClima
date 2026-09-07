import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api';
import { format } from 'date-fns';
import { Eye, Edit, Trash2 } from 'lucide-react';

export default function Dashboard() {
  const [alerts, setAlerts] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  
  const [filterType, setFilterType] = useState('');
  const [filterStatus, setFilterStatus] = useState('');
  const [filterDanger, setFilterDanger] = useState('');

  useEffect(() => {
    fetchAlerts();
  }, [filterType, filterStatus, filterDanger]);

  const fetchAlerts = async () => {
    setLoading(true);
    try {
      let url = '/alerts?';
      if (filterType) url += `event_type=${encodeURIComponent(filterType)}&`;
      if (filterStatus) url += `status=${encodeURIComponent(filterStatus)}&`;
      if (filterDanger) url += `danger_level=${encodeURIComponent(filterDanger)}&`;

      const res = await api.get(url);
      setAlerts(res.data);
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const deleteAlert = async (id: string) => {
    if (!confirm('Deseja realmente arquivar este alerta?')) return;
    try {
      await api.delete(`/alerts/${id}`);
      fetchAlerts();
    } catch (error) {
      alert('Erro ao arquivar alerta');
    }
  };

  const getDangerColor = (level: string) => {
    switch(level) {
      case 'Baixo': return 'bg-green-100 text-green-800';
      case 'Moderado': return 'bg-yellow-100 text-yellow-800';
      case 'Alto': return 'bg-orange-100 text-orange-800';
      case 'Crítico': return 'bg-red-100 text-red-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  const getStatusColor = (status: string) => {
    switch(status) {
      case 'SUSPEITO': return 'bg-gray-100 text-gray-600';
      case 'EM ANÁLISE': return 'bg-blue-100 text-blue-800';
      case 'CONFIRMADO': return 'bg-red-100 text-red-800';
      case 'RESOLVIDO': return 'bg-green-100 text-green-800';
      case 'FALSO ALARME': return 'bg-purple-100 text-purple-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  const stats = {
    total: alerts.length,
    suspeitos: alerts.filter(a => a.status === 'SUSPEITO').length,
    confirmados: alerts.filter(a => a.status === 'CONFIRMADO').length,
    criticos: alerts.filter(a => a.danger_level === 'Crítico').length,
  };

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-gray-800">Dashboard</h1>
      
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div className="bg-white p-4 rounded-lg shadow-sm border border-gray-200">
          <div className="text-sm text-gray-500">Alertas Ativos</div>
          <div className="text-3xl font-bold text-gray-800">{stats.total}</div>
        </div>
        <div className="bg-white p-4 rounded-lg shadow-sm border border-gray-200">
          <div className="text-sm text-gray-500">Suspeitos</div>
          <div className="text-3xl font-bold text-yellow-600">{stats.suspeitos}</div>
        </div>
        <div className="bg-white p-4 rounded-lg shadow-sm border border-gray-200">
          <div className="text-sm text-gray-500">Confirmados</div>
          <div className="text-3xl font-bold text-blue-600">{stats.confirmados}</div>
        </div>
        <div className="bg-white p-4 rounded-lg shadow-sm border border-gray-200">
          <div className="text-sm text-gray-500">Críticos</div>
          <div className="text-3xl font-bold text-red-600">{stats.criticos}</div>
        </div>
      </div>
      
      {/* Filtros */}
      <div className="bg-white p-4 rounded-lg shadow-sm border border-gray-200 flex flex-wrap gap-4">
        <div>
          <label className="block text-xs font-medium text-gray-700 mb-1">Tipo de Evento</label>
          <select value={filterType} onChange={e => setFilterType(e.target.value)} className="p-2 border border-gray-300 rounded text-sm w-48">
            <option value="">Todos</option>
            <option value="Tornado">Tornado</option>
            <option value="Árvore caída">Árvore caída</option>
            <option value="Alagamento">Alagamento</option>
            <option value="Enchente">Enchente</option>
            <option value="Granizo">Granizo</option>
            <option value="Vendaval">Vendaval</option>
            <option value="Deslizamento">Deslizamento</option>
            <option value="Incêndio">Incêndio</option>
            <option value="Bloqueio de rodovia">Bloqueio de rodovia</option>
          </select>
        </div>
        <div>
          <label className="block text-xs font-medium text-gray-700 mb-1">Status</label>
          <select value={filterStatus} onChange={e => setFilterStatus(e.target.value)} className="p-2 border border-gray-300 rounded text-sm w-48">
            <option value="">Todos</option>
            <option value="SUSPEITO">SUSPEITO</option>
            <option value="EM ANÁLISE">EM ANÁLISE</option>
            <option value="CONFIRMADO">CONFIRMADO</option>
            <option value="RESOLVIDO">RESOLVIDO</option>
            <option value="FALSO ALARME">FALSO ALARME</option>
          </select>
        </div>
        <div>
          <label className="block text-xs font-medium text-gray-700 mb-1">Nível de Perigo</label>
          <select value={filterDanger} onChange={e => setFilterDanger(e.target.value)} className="p-2 border border-gray-300 rounded text-sm w-48">
            <option value="">Todos</option>
            <option value="Baixo">Baixo</option>
            <option value="Moderado">Moderado</option>
            <option value="Alto">Alto</option>
            <option value="Crítico">Crítico</option>
          </select>
        </div>
      </div>

      <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
        <div className="p-4 border-b border-gray-200 flex justify-between items-center">
          <h2 className="text-lg font-semibold text-gray-800">Alertas Recentes</h2>
        </div>
        
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm text-gray-500">
            <thead className="text-xs text-gray-700 uppercase bg-gray-50">
              <tr>
                <th className="px-4 py-3">ID</th>
                <th className="px-4 py-3">Evento</th>
                <th className="px-4 py-3">Data/Hora</th>
                <th className="px-4 py-3">Nível</th>
                <th className="px-4 py-3">Status</th>
                <th className="px-4 py-3 text-right">Ações</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan={6} className="text-center py-4">Carregando...</td></tr>
              ) : alerts.length === 0 ? (
                <tr><td colSpan={6} className="text-center py-4">Nenhum alerta encontrado</td></tr>
              ) : alerts.map(alert => (
                <tr key={alert.id} className="border-b hover:bg-gray-50">
                  <td className="px-4 py-3">#{String(alert.code ?? '?').padStart(4, '0')}</td>
                  <td className="px-4 py-3 font-medium text-gray-900">{alert.title}</td>
                  <td className="px-4 py-3">{format(new Date(alert.event_date), 'dd/MM/yyyy HH:mm')}</td>
                  <td className="px-4 py-3">
                    <span className={`px-2 py-1 rounded-full text-xs font-medium ${getDangerColor(alert.danger_level)}`}>
                      {alert.danger_level}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <span className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(alert.status)}`}>
                      {alert.status}
                    </span>
                  </td>
                  <td className="px-4 py-3 flex justify-end space-x-2">
                    <Link to={`/alerta/${alert.id}`} className="text-blue-600 hover:text-blue-800 p-1">
                      <Eye size={18} />
                    </Link>
                    <button onClick={() => deleteAlert(alert.id)} className="text-red-600 hover:text-red-800 p-1" title="Arquivar">
                      <Trash2 size={18} />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
