# AlertaClima

## Introdução
O **AlertaClima** é um projeto de Engenharia de Software desenvolvido para facilitar a comunicação entre cidadãos e a Defesa Civil em situações de risco meteorológico ou estrutural.

## Problema
Eventos climáticos extremos e ocorrências de risco, como enchentes e quedas de árvores, acontecem rapidamente. Muitas vezes, os órgãos competentes demoram a agir devido ao atraso no mapeamento visual das ocorrências em tempo real.

## Objetivo
Desenvolver um sistema colaborativo de monitoramento onde o cidadão possa reportar situações de perigo por meio do seu smartphone e permitir que os analistas validem e acompanhem essas informações dinamicamente, suportando um fluxo completo de CRUD (Create, Read, Update, Soft Delete).

## Público / Contexto
Cidadãos que testemunham eventos de risco (árvore caída, alagamento, deslizamento, etc.) em sua região e equipes de Defesa Civil municipais/estaduais responsáveis por validar e priorizar a resposta a essas ocorrências.

## ODS relacionado
**ODS 11 – Cidades e Comunidades Sustentáveis**, da Agenda 2030 da ONU, especificamente a meta 11.5 (reduzir mortes, número de pessoas afetadas e perdas causadas por desastres, incluindo os relacionados à água). O AlertaClima contribui diretamente para essa meta ao acelerar o mapeamento colaborativo de riscos urbanos, dando à Defesa Civil visibilidade em tempo real sobre ocorrências que, sem o sistema, dependeriam de canais mais lentos (telefone, boletim de ocorrência presencial etc.).

## Solução proposta
A implementação de uma plataforma web responsiva, composta por uma API RESTful em Java e um frontend reativo. Através do sistema, cidadãos enviam informações com latitude e longitude (via GPS do navegador), enquanto analistas possuem um painel para certificar as ocorrências.

## Tecnologias
* **Frontend:** React, Vite, TypeScript, Tailwind CSS, Leaflet, OpenStreetMap. Pode ser usado HTML e CSS.
* **Backend:** Java 17, Spring Boot, Spring Data MongoDB.
* **Banco de Dados:** MongoDB, numa única collection `alerts` (dados do usuário que criou/validou o alerta ficam embutidos no próprio documento).

## Arquitetura
O sistema segue a arquitetura Cliente-Servidor separando responsabilidades:
* **Backend (API REST Java):** Desenvolvido nas seguintes camadas lógicas:
  * *Controllers:* Interceptam as requisições HTTP e devolvem ResponseEntity.
  * *Services:* Centralizam as regras de negócio.
  * *Repositories:* Encapsulam e intermediam o acesso aos dados via Spring Data MongoDB.
  * *Models:* Documentos mapeados para a collection `alerts` no MongoDB.
* **Frontend (SPA):** Single Page Application onde as views comunicam com a API via JSON.

## Como iniciar o sistema

### 0. Ter um MongoDB rodando
É preciso ter uma instância do MongoDB acessível localmente (por padrão em `mongodb://localhost:27017`). A forma mais simples é rodar via Docker:
```powershell
docker run -d --name alertaclima-mongo -p 27017:27017 mongo:7
```
A connection string fica configurada em `backend/src/main/resources/application.properties` (`spring.data.mongodb.uri`). O database e a collection `alerts` são criados automaticamente no primeiro registro.

### 1. Iniciar o Backend (Java Spring Boot)
Abra a pasta `backend` na sua IDE de preferência (IntelliJ IDEA, Eclipse ou VS Code).
Aguarde a IDE baixar as dependências do `pom.xml` (Maven).
Execute a classe principal: `AlertaClimaApplication.java`.
O servidor iniciará na porta **8080**.
*Atenção:* A collection é limpa e populada automaticamente ao iniciar o projeto com o profile `seed`. Para persistir dados normais (evitar apagar a collection), basta comentar a anotação `@Profile("seed")` ou remover sua chamada, mas por padrão deixei ativo para facilitar a demonstração acadêmica imediata.

### 2. Iniciar o Frontend (Interface)
Abra o terminal, vá para a pasta `frontend` e digite:
```powershell
cd frontend
npm install
npm run dev
```
O servidor frontend iniciará na porta **5173**.
Abra no navegador: `http://localhost:5173`

## Testes automatizados e cobertura
O backend possui testes unitários (JUnit 5 + Mockito, cobrindo `AlertService`, `AlertController`, `DataInitializer` e o modelo `Alert`) com cobertura medida via JaCoCo. Para rodar:
```powershell
cd backend
mvn verify
```
O comando roda todos os testes e falha (`BUILD FAILURE`) se a cobertura de linhas ficar abaixo de 70% — o relatório HTML fica em `backend/target/site/jacoco/index.html`.

## CRUD na Prática
O CRUD agora está implementado totalmente em Java! Você pode realizar:
* **CREATE:** Cadastrar alerta (Salva na collection `alerts` do MongoDB).
* **READ:** O Dashboard consome a API Rest em Java listando todos os itens.
* **UPDATE:** O Analista consegue modificar status e níveis de perigo.
* **DELETE:** O Analista consegue fazer o Soft Delete (arquivar ocorrência, populando o `deletedAt`).

Todos os requisitos e padrões exigidos foram perfeitamente migrados para a stack Java solicitada para acompanhamento da disciplina.
