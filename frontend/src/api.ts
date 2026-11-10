import axios from 'axios';

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? '/api',
});

// Usuários de demonstração (cadastrados pelo seed do backend na coleção "usuarios").
// O backend liga o e-mail ao documento do usuário e grava a referência usuarioId.
export const USUARIOS_DEMO: Record<string, { nome: string; email: string; papel: string }> = {
  CITIZEN: { nome: 'Cidadão Exemplo', email: 'cidadao@exemplo.com', papel: 'CITIZEN' },
  ANALYST: { nome: 'Analista Defesa Civil', email: 'analista@defesacivil.gov.br', papel: 'ANALYST' },
};
