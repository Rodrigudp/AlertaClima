# AlertaClima

## Introdução
O **AlertaClima** é um projeto de Engenharia de Software desenvolvido para facilitar a comunicação entre cidadãos e a Defesa Civil em situações de risco meteorológico ou estrutural.

## Problema
Eventos climáticos extremos e ocorrências de risco, como enchentes e quedas de árvores, acontecem rapidamente. Muitas vezes, os órgãos competentes demoram a agir devido ao atraso no mapeamento visual das ocorrências em tempo real.

## Objetivo
Desenvolver um sistema colaborativo de monitoramento onde o cidadão possa reportar situações de perigo por meio do seu smartphone e permitir que os analistas validem e acompanhem essas informações dinamicamente, suportando um fluxo completo de CRUD (Create, Read, Update, Soft Delete).

## Solução proposta
A implementação de uma plataforma web responsiva, composta por uma API RESTful em Java e um frontend reativo. Através do sistema, cidadãos enviam informações com latitude e longitude (via GPS do navegador), enquanto analistas possuem um painel para certificar as ocorrências.

## Tecnologias
* **Frontend:** React, Vite, TypeScript, Tailwind CSS, Leaflet, OpenStreetMap. Pode ser usado HTML e CSS.
* **Backend:** Java 17, Spring Boot, Spring Data JPA, Hibernate.
* **Banco de Dados:** H2 Database (persistido em arquivo) pela facilidade de execução acadêmica.

## Arquitetura
O sistema segue a arquitetura Cliente-Servidor separando responsabilidades:
* **Backend (API REST Java):** Desenvolvido nas seguintes camadas lógicas:
  * *Controllers:* Interceptam as requisições HTTP e devolvem ResponseEntity.
  * *Services:* Centralizam as regras de negócio.
  * *Repositories:* Encapsulam e intermediam o acesso aos dados via Spring Data JPA.
  * *Models:* Entidades mapeadas para o banco.
* **Frontend (SPA):** Single Page Application onde as views comunicam com a API via JSON.

## Como iniciar o sistema

### 1. Iniciar o Backend (Java Spring Boot)
Abra a pasta `backend` na sua IDE de preferência (IntelliJ IDEA, Eclipse ou VS Code).
Aguarde a IDE baixar as dependências do `pom.xml` (Maven).
Execute a classe principal: `AlertaClimaApplication.java`.
O servidor iniciará na porta **8080**.
*Atenção:* O banco de dados é recriado e populado automaticamente ao iniciar o projeto com o profile `seed`. Para persistir dados normais (evitar apagar o banco), basta comentar a anotação `@Profile("seed")` ou remover sua chamada, mas por padrão deixei ativo para facilitar a demonstração acadêmica imediata.

### 2. Iniciar o Frontend (Interface)
Abra o terminal, vá para a pasta `frontend` e digite:
```powershell
cd frontend
npm install
npm run dev
```
O servidor frontend iniciará na porta **5173**.
Abra no navegador: `http://localhost:5173`

## CRUD na Prática
O CRUD agora está implementado totalmente em Java! Você pode realizar:
* **CREATE:** Cadastrar alerta (Salva no banco H2).
* **READ:** O Dashboard consome a API Rest em Java listando todos os itens.
* **UPDATE:** O Analista consegue modificar status e níveis de perigo.
* **DELETE:** O Analista consegue fazer o Soft Delete (arquivar ocorrência, populando o `deletedAt`).

Todos os requisitos e padrões exigidos foram perfeitamente migrados para a stack Java solicitada para acompanhamento da disciplina.
