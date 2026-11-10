import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api';
import { MapPin, Trash2, UserPlus } from 'lucide-react';

const emptyRegion = { nome: '', latitude: '', longitude: '', raioKm: '' };
const emptyUser = { nome: '', email: '', telefone: '', papel: 'CITIZEN' };

export default function Users() {
  const [users, setUsers] = useState<any[]>([]);
  const [selected, setSelected] = useState<any>(null);
  const [monitored, setMonitored] = useState<any[]>([]);
  const [created, setCreated] = useState<any[]>([]);
  const [region, setRegion] = useState(emptyRegion);
  const [newUser, setNewUser] = useState(emptyUser);
  const [showNewUser, setShowNewUser] = useState(false);

  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUsers = async (keepId?: string) => {
    try {
      const res = await api.get('/usuarios');
      setUsers(res.data);
      const next = res.data.find((u: any) => u.id === (keepId ?? selected?.id)) ?? res.data[0];
      if (next) selectUser(next);
    } catch (error) {
      console.error(error);
    }
  };

  const selectUser = async (user: any) => {
    setSelected(user);
    try {
      const [mon, cri] = await Promise.all([
        api.get(`/usuarios/${user.id}/alertas-monitorados`),
        api.get(`/usuarios/${user.id}/alertas`),
      ]);
      setMonitored(mon.data);
      setCreated(cri.data);
    } catch (error) {
      console.error(error);
    }
  };

  const errorMessage = (error: any, fallback: string) => error?.response?.data?.error ?? fallback;

  const addRegion = async (e: any) => {
    e.preventDefault();
    try {
      await api.post(`/usuarios/${selected.id}/regioes`, {
        nome: region.nome,
        latitude: Number(region.latitude),
        longitude: Number(region.longitude),
        raioKm: Number(region.raioKm),
      });
      setRegion(emptyRegion);
      fetchUsers(selected.id);
    } catch (error) {
      alert(errorMessage(error, 'Erro ao adicionar região'));
    }
  };

  const removeRegion = async (nome: string) => {
    try {
      await api.delete(`/usuarios/${selected.id}/regioes/${encodeURIComponent(nome)}`);
      fetchUsers(selected.id);
    } catch (error) {
      alert(errorMessage(error, 'Erro ao remover região'));
    }
  };

  const createUser = async (e: any) => {
    e.preventDefault();
    try {
      const res = await api.post('/usuarios', newUser);
      setNewUser(emptyUser);
      setShowNewUser(false);
      fetchUsers(res.data.id);
    } catch (error) {
      alert(errorMessage(error, 'Erro ao cadastrar usuário'));
    }
  };

  const input = 'w-full p-2 border border-gray-300 rounded text-sm';

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold text-gray-800">Usuários e regiões monitoradas</h1>
        <button
          onClick={() => setShowNewUser(!showNewUser)}
          className="flex items-center gap-2 bg-blue-600 text-white text-sm font-semibold px-4 py-2 rounded hover:bg-blue-700"
        >
          <UserPlus size={16} /> Novo usuário
        </button>
      </div>

      {showNewUser && (
        <form onSubmit={createUser} className="bg-white p-4 rounded-lg shadow-sm border border-gray-200 grid grid-cols-1 md:grid-cols-5 gap-3">
          <input required placeholder="Nome" value={newUser.nome} onChange={(e) => setNewUser({ ...newUser, nome: e.target.value })} className={input} />
          <input required type="email" placeholder="E-mail" value={newUser.email} onChange={(e) => setNewUser({ ...newUser, email: e.target.value })} className={input} />
          <input placeholder="Telefone" value={newUser.telefone} onChange={(e) => setNewUser({ ...newUser, telefone: e.target.value })} className={input} />
          <select value={newUser.papel} onChange={(e) => setNewUser({ ...newUser, papel: e.target.value })} className={input}>
            <option value="CITIZEN">Cidadão</option>
            <option value="ANALYST">Analista</option>
          </select>
          <button type="submit" className="bg-slate-800 text-white text-sm font-semibold rounded hover:bg-slate-900 py-2">Cadastrar</button>
        </form>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 divide-y">
          {users.map((u) => (
            <button
              key={u.id}
              onClick={() => selectUser(u)}
              className={`w-full text-left p-4 hover:bg-gray-50 ${selected?.id === u.id ? 'bg-blue-50' : ''}`}
            >
              <p className="font-semibold text-gray-800">{u.nome}</p>
              <p className="text-xs text-gray-500">{u.email} · {u.papel === 'ANALYST' ? 'Analista' : 'Cidadão'}</p>
              <p className="text-xs text-gray-500 mt-1">{u.regioesMonitoradas?.length ?? 0} região(ões) monitorada(s)</p>
            </button>
          ))}
          {users.length === 0 && <p className="p-4 text-sm text-gray-500">Nenhum usuário cadastrado.</p>}
        </div>

        {selected && (
          <div className="lg:col-span-2 space-y-6">
            <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
              <h2 className="text-lg font-bold text-gray-800 mb-3">Regiões de {selected.nome}</h2>
              <div className="space-y-2">
                {(selected.regioesMonitoradas || []).map((r: any) => (
                  <div key={r.nome} className="flex justify-between items-center bg-gray-50 border border-gray-100 rounded p-3">
                    <div className="flex items-center gap-2">
                      <MapPin size={16} className="text-blue-600" />
                      <div>
                        <p className="text-sm font-medium text-gray-800">{r.nome}</p>
                        <p className="text-xs text-gray-500">{r.latitude.toFixed(4)}, {r.longitude.toFixed(4)} · raio {r.raioKm} km</p>
                      </div>
                    </div>
                    <button onClick={() => removeRegion(r.nome)} className="text-red-600 hover:text-red-800" title="Remover região">
                      <Trash2 size={16} />
                    </button>
                  </div>
                ))}
                {(selected.regioesMonitoradas || []).length === 0 && <p className="text-sm text-gray-500">Nenhuma região monitorada.</p>}
              </div>

              <form onSubmit={addRegion} className="mt-4 grid grid-cols-2 md:grid-cols-5 gap-2">
                <input required placeholder="Nome da região" value={region.nome} onChange={(e) => setRegion({ ...region, nome: e.target.value })} className={`${input} col-span-2 md:col-span-1`} />
                <input required type="number" step="any" placeholder="Latitude" value={region.latitude} onChange={(e) => setRegion({ ...region, latitude: e.target.value })} className={input} />
                <input required type="number" step="any" placeholder="Longitude" value={region.longitude} onChange={(e) => setRegion({ ...region, longitude: e.target.value })} className={input} />
                <input required type="number" step="any" min="0.1" placeholder="Raio (km)" value={region.raioKm} onChange={(e) => setRegion({ ...region, raioKm: e.target.value })} className={input} />
                <button type="submit" className="bg-blue-600 text-white text-sm font-semibold rounded hover:bg-blue-700 py-2">Adicionar</button>
              </form>
            </div>

            <AlertList title="Alertas ativos nas regiões monitoradas" alerts={monitored} empty="Nenhum alerta ativo nessas regiões." />
            <AlertList title="Alertas registrados por este usuário" alerts={created} empty="Este usuário ainda não registrou alertas." />
          </div>
        )}
      </div>
    </div>
  );
}

function AlertList({ title, alerts, empty }: { title: string; alerts: any[]; empty: string }) {
  return (
    <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
      <h2 className="text-lg font-bold text-gray-800 mb-3">{title} ({alerts.length})</h2>
      {alerts.length === 0 ? (
        <p className="text-sm text-gray-500">{empty}</p>
      ) : (
        <ul className="divide-y">
          {alerts.map((a) => (
            <li key={a.id} className="py-2 flex justify-between items-center">
              <div>
                <Link to={`/alerta/${a.id}`} className="text-sm font-medium text-blue-700 hover:underline">{a.titulo}</Link>
                <p className="text-xs text-gray-500">{a.endereco} · {a.tipoEvento}</p>
              </div>
              <span className="text-xs font-semibold px-2 py-1 bg-gray-100 rounded">{a.status}</span>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
