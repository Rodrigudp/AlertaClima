import { useState, useEffect } from 'react';
import { useParams, useNavigate, useOutletContext } from 'react-router-dom';
import { api } from '../api';
import { format } from 'date-fns';

export default function AlertDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { role } = useOutletContext<{role: string}>();
  
  const [alertData, setAlertData] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  
  // Update state
  const [status, setStatus] = useState('');
  const [dangerLevel, setDangerLevel] = useState('');
  const [notes, setNotes] = useState('');

  const statuses = ['SUSPEITO', 'EM ANÁLISE', 'CONFIRMADO', 'RESOLVIDO', 'FALSO ALARME'];
  const dangerLevels = ['Baixo', 'Moderado', 'Alto', 'Crítico'];

  useEffect(() => {
    fetchAlert();
  }, [id]);

  const fetchAlert = async () => {
    try {
      const res = await api.get(`/alertas/${id}`);
      setAlertData(res.data);
      setStatus(res.data.status);
      setDangerLevel(res.data.nivel_perigo);
      setNotes(res.data.notas_validacao || '');
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const handleUpdate = async (e: any) => {
    e.preventDefault();
    try {
      await api.patch(`/alertas/${id}/status`, {
        status,
        notas_validacao: notes,
        validado_por: { nome: 'Analista Defesa Civil', email: 'analista@defesacivil.gov.br', papel: 'ANALYST' } // Analista de exemplo
      });

      await api.put(`/alertas/${id}`, {
        nivel_perigo: dangerLevel
      });
      
      alert('Alerta atualizado com sucesso');
      fetchAlert();
    } catch (error) {
      alert('Erro ao atualizar alerta');
    }
  };

  const archiveAlert = async () => {
    if (!confirm('Arquivar este alerta?')) return;
    try {
      await api.delete(`/alertas/${id}`);
      alert('Alerta arquivado com sucesso.');
      navigate('/dashboard');
    } catch (error) {
      alert('Erro ao arquivar alerta');
    }
  };

  if (loading) return <div>Carregando...</div>;
  if (!alertData) return <div>Alerta não encontrado.</div>;

  const isAnalyst = role === 'ANALYST';

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
        <div className="flex justify-between items-start mb-6">
          <div>
            <h1 className="text-2xl font-bold text-gray-800">{alertData.titulo}</h1>
            <p className="text-gray-500 text-sm mt-1">Registrado em {format(new Date(alertData.criadoEm), 'dd/MM/yyyy HH:mm')}</p>
          </div>
          <div className="text-right">
            <span className="inline-block px-3 py-1 bg-gray-100 text-gray-800 font-bold rounded-full mb-1 block text-center">
              {alertData.status}
            </span>
            <span className="inline-block px-3 py-1 bg-red-100 text-red-800 font-bold rounded-full block text-center">
              Nível: {alertData.nivel_perigo}
            </span>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          <div className="space-y-4">
            <div>
              <h3 className="text-sm font-semibold text-gray-700 uppercase">Descrição do Evento</h3>
              <p className="mt-1 text-gray-800 bg-gray-50 p-3 rounded border border-gray-100">
                {alertData.descricao}
              </p>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <h3 className="text-xs font-semibold text-gray-500 uppercase">Tipo</h3>
                <p className="font-medium">{alertData.tipo_evento}</p>
              </div>
              <div>
                <h3 className="text-xs font-semibold text-gray-500 uppercase">Data da Ocorrência</h3>
                <p className="font-medium">{format(new Date(alertData.data_evento), 'dd/MM/yyyy HH:mm')}</p>
              </div>
              <div>
                <h3 className="text-xs font-semibold text-gray-500 uppercase">Localização (Lat/Lon)</h3>
                <p className="font-medium text-sm">{alertData.latitude.toFixed(4)}, {alertData.longitude.toFixed(4)}</p>
              </div>
              <div>
                <h3 className="text-xs font-semibold text-gray-500 uppercase">Endereço</h3>
                <p className="font-medium text-sm">{alertData.endereco || 'Não informado'}</p>
              </div>
            </div>
          </div>

          <div>
            <h3 className="text-sm font-semibold text-gray-700 uppercase mb-2">Imagem Anexada</h3>
            {alertData.url_imagem ? (
              <img src={alertData.url_imagem} alt="Evidência" className="w-full h-48 object-cover rounded border border-gray-200" />
            ) : (
              <div className="w-full h-48 bg-gray-100 flex items-center justify-center rounded border border-gray-200 text-gray-400">
                Nenhuma imagem
              </div>
            )}
          </div>
        </div>
      </div>

      {isAnalyst && (
        <div className="bg-slate-50 p-6 rounded-lg shadow-sm border border-slate-200">
          <h2 className="text-xl font-bold text-slate-800 mb-4 flex items-center gap-2">
            Área do Analista
          </h2>
          
          <form onSubmit={handleUpdate} className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Atualizar Status</label>
                <select 
                  value={status}
                  onChange={(e) => setStatus(e.target.value)}
                  className="w-full p-2 border border-slate-300 rounded focus:ring-blue-500"
                >
                  {statuses.map(s => <option key={s} value={s}>{s}</option>)}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Nível de Perigo Oficial</label>
                <select 
                  value={dangerLevel}
                  onChange={(e) => setDangerLevel(e.target.value)}
                  className="w-full p-2 border border-slate-300 rounded focus:ring-blue-500"
                >
                  {dangerLevels.map(l => <option key={l} value={l}>{l}</option>)}
                </select>
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Notas da Análise</label>
              <textarea 
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
                rows={3}
                placeholder="Insira notas técnicas, justificativas de mudança de status..."
                className="w-full p-2 border border-slate-300 rounded"
              ></textarea>
            </div>

            <div className="flex justify-between items-center pt-2">
              <button 
                type="button" 
                onClick={archiveAlert}
                className="text-red-600 font-medium hover:text-red-800 px-4 py-2 border border-red-200 rounded hover:bg-red-50"
              >
                Arquivar
              </button>
              
              <button 
                type="submit" 
                className="bg-slate-800 text-white font-bold py-2 px-6 rounded hover:bg-slate-900"
              >
                Salvar Avaliação
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
}
