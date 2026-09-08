import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api';
import { MapPin, ImagePlus, X } from 'lucide-react';

export default function NewAlert() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [locating, setLocating] = useState(false);
  const [formData, setFormData] = useState({
    titulo: '',
    descricao: '',
    tipo_evento: 'Árvore caída',
    nivel_perigo: 'Moderado',
    latitude: '',
    longitude: '',
    endereco: '',
    url_imagem: '',
    criado_por: { nome: 'Cidadão Exemplo', email: 'cidadao@exemplo.com', papel: 'CITIZEN' } // Usuário padrão para o exemplo
  });
  const [imagePreview, setImagePreview] = useState<string | null>(null);

  const eventTypes = [
    'Tornado', 'Árvore caída', 'Alagamento', 
    'Enchente', 'Granizo', 'Vendaval', 'Deslizamento', 'Incêndio', 
    'Bloqueio de rodovia', 'Outro evento de risco'
  ];

  const dangerLevels = ['Baixo', 'Moderado', 'Alto', 'Crítico'];

  useEffect(() => {
    getLocation();
  }, []);

  const getLocation = () => {
    setLocating(true);
    if ('geolocation' in navigator) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          setFormData(prev => ({
            ...prev,
            latitude: position.coords.latitude.toString(),
            longitude: position.coords.longitude.toString()
          }));
          setLocating(false);
        },
        (error) => {
          console.error(error);
          setLocating(false);
        }
      );
    } else {
      setLocating(false);
    }
  };

  const handleChange = (e: any) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleImageChange = (e: any) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = () => {
      const result = reader.result as string;
      setFormData(prev => ({ ...prev, url_imagem: result }));
      setImagePreview(result);
    };
    reader.readAsDataURL(file);
  };

  const removeImage = () => {
    setFormData(prev => ({ ...prev, url_imagem: '' }));
    setImagePreview(null);
  };

  const handleSubmit = async (e: any) => {
    e.preventDefault();
    setLoading(true);
    
    try {
      const payload = {
        ...formData,
        latitude: parseFloat(formData.latitude),
        longitude: parseFloat(formData.longitude),
        data_evento: new Date()
      };

      await api.post('/alertas', payload);
      alert('Alerta registrado com sucesso.');
      navigate('/dashboard');
    } catch (error: any) {
      alert(error.response?.data?.error || 'Erro ao registrar alerta');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto bg-white p-6 rounded-lg shadow-sm border border-gray-200">
      <h1 className="text-2xl font-bold text-gray-800 mb-6">Registrar Novo Alerta</h1>
      
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Título</label>
          <input 
            required 
            type="text" 
            name="titulo"
            value={formData.titulo}
            onChange={handleChange}
            className="w-full p-2 border border-gray-300 rounded focus:ring-blue-500 focus:border-blue-500" 
            placeholder="Ex: Árvore caída na pista"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Tipo de Evento</label>
          <select 
            name="tipo_evento"
            value={formData.tipo_evento}
            onChange={handleChange}
            className="w-full p-2 border border-gray-300 rounded"
          >
            {eventTypes.map(type => <option key={type} value={type}>{type}</option>)}
          </select>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Descrição</label>
          <textarea 
            required 
            name="descricao"
            value={formData.descricao}
            onChange={handleChange}
            rows={3}
            className="w-full p-2 border border-gray-300 rounded" 
            placeholder="Descreva a situação..."
          ></textarea>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Nível de Perigo</label>
          <select 
            name="nivel_perigo"
            value={formData.nivel_perigo}
            onChange={handleChange}
            className="w-full p-2 border border-gray-300 rounded"
          >
            {dangerLevels.map(level => <option key={level} value={level}>{level}</option>)}
          </select>
        </div>

        <div className="bg-gray-50 p-4 rounded border border-gray-200">
          <div className="flex justify-between items-center mb-2">
            <label className="block text-sm font-medium text-gray-700">Localização</label>
            <button 
              type="button" 
              onClick={getLocation}
              className="text-sm text-blue-600 flex items-center gap-1 hover:text-blue-800"
            >
              <MapPin size={16} />
              {locating ? 'Buscando...' : 'Obter GPS'}
            </button>
          </div>
          
          <div className="grid grid-cols-2 gap-4 mb-2">
            <div>
              <label className="block text-xs text-gray-500">Latitude</label>
              <input 
                required 
                type="number" 
                step="any"
                name="latitude"
                value={formData.latitude}
                onChange={handleChange}
                className="w-full p-2 border border-gray-300 rounded text-sm" 
              />
            </div>
            <div>
              <label className="block text-xs text-gray-500">Longitude</label>
              <input 
                required 
                type="number" 
                step="any"
                name="longitude"
                value={formData.longitude}
                onChange={handleChange}
                className="w-full p-2 border border-gray-300 rounded text-sm" 
              />
            </div>
          </div>
          
          <div>
            <label className="block text-xs text-gray-500">Endereço / Referência</label>
            <input 
              type="text" 
              name="endereco"
              value={formData.endereco}
              onChange={handleChange}
              className="w-full p-2 border border-gray-300 rounded text-sm" 
              placeholder="Opcional"
            />
          </div>
        </div>

        <div className="bg-gray-50 p-4 rounded border border-gray-200">
          <label className="block text-sm font-medium text-gray-700 mb-2">Imagem (opcional)</label>

          {imagePreview ? (
            <div className="relative w-full h-48">
              <img src={imagePreview} alt="Prévia da imagem anexada" className="w-full h-48 object-cover rounded border border-gray-200" />
              <button
                type="button"
                onClick={removeImage}
                className="absolute top-2 right-2 bg-white/90 text-red-600 rounded-full p-1 shadow hover:bg-white"
                title="Remover imagem"
              >
                <X size={16} />
              </button>
            </div>
          ) : (
            <label
              htmlFor="image-upload"
              className="flex flex-col items-center justify-center gap-2 w-full h-32 border-2 border-dashed border-gray-300 rounded cursor-pointer text-gray-400 hover:border-blue-400 hover:text-blue-500"
            >
              <ImagePlus size={24} />
              <span className="text-sm">Clique para anexar uma foto da ocorrência</span>
            </label>
          )}
          <input
            id="image-upload"
            type="file"
            accept="image/*"
            onChange={handleImageChange}
            className="hidden"
          />
        </div>

        <button
          type="submit"
          disabled={loading}
          className="w-full bg-blue-600 text-white font-bold py-3 px-4 rounded hover:bg-blue-700 disabled:opacity-50"
        >
          {loading ? 'Registrando...' : 'Registrar Alerta'}
        </button>
      </form>
    </div>
  );
}
