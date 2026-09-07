import { Outlet, Link, useLocation } from 'react-router-dom';
import { Home, PlusCircle, Map as MapIcon, Archive } from 'lucide-react';
import { useState } from 'react';

export default function Layout() {
  const location = useLocation();
  const [role, setRole] = useState<'CITIZEN' | 'ANALYST'>('ANALYST'); // Toggleable for demo

  const navItems = [
    { path: '/dashboard', label: 'Dashboard', icon: Home },
    { path: '/novo-alerta', label: 'Novo Alerta', icon: PlusCircle },
    { path: '/mapa', label: 'Mapa de Alertas', icon: MapIcon },
  ];

  if (role === 'ANALYST') {
    navItems.push({ path: '/arquivados', label: 'Alertas Arquivados', icon: Archive });
  }

  return (
    <div className="flex h-screen bg-gray-100">
      <aside className="w-64 bg-slate-800 text-white flex flex-col hidden md:flex">
        <div className="p-4 text-xl font-bold border-b border-slate-700">AlertaClima</div>
        <div className="p-4 border-b border-slate-700">
          <p className="text-sm text-slate-400 mb-2">Simular Perfil:</p>
          <select 
            value={role} 
            onChange={(e) => setRole(e.target.value as any)}
            className="w-full bg-slate-700 text-white p-2 rounded"
          >
            <option value="CITIZEN">Cidadão</option>
            <option value="ANALYST">Analista Defesa Civil</option>
          </select>
        </div>
        <nav className="flex-1 p-4 space-y-2">
          {navItems.map((item) => (
            <Link
              key={item.path}
              to={item.path}
              className={`flex items-center space-x-3 p-3 rounded transition-colors ${
                location.pathname.startsWith(item.path) ? 'bg-blue-600' : 'hover:bg-slate-700'
              }`}
            >
              <item.icon size={20} />
              <span>{item.label}</span>
            </Link>
          ))}
        </nav>
      </aside>
      
      {/* Mobile nav for demo purposes */}
      
      <main className="flex-1 flex flex-col overflow-hidden">
        <header className="bg-white shadow-sm p-4 md:hidden flex justify-between items-center">
          <div className="text-xl font-bold">AlertaClima</div>
          <select 
            value={role} 
            onChange={(e) => setRole(e.target.value as any)}
            className="bg-slate-100 p-2 rounded"
          >
            <option value="CITIZEN">Cidadão</option>
            <option value="ANALYST">Analista</option>
          </select>
        </header>
        <div className="flex-1 overflow-y-auto p-4 md:p-6">
          <Outlet context={{ role }} />
        </div>
        
        {/* Mobile bottom nav */}
        <nav className="md:hidden bg-white border-t flex justify-around p-2">
          {navItems.map((item) => (
            <Link
              key={item.path}
              to={item.path}
              className={`flex flex-col items-center p-2 ${
                location.pathname.startsWith(item.path) ? 'text-blue-600' : 'text-gray-500'
              }`}
            >
              <item.icon size={24} />
              <span className="text-xs mt-1">{item.label}</span>
            </Link>
          ))}
        </nav>
      </main>
    </div>
  );
}
