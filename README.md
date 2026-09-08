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
* **Frontend:** React, Vite, TypeScript, Tailwind CSS, Leaflet, OpenStreetMap.
* **Backend:** Java 17, Spring Boot, Spring Data MongoDB, Springdoc OpenAPI (Swagger UI).
* **Testes:** JUnit 5, Mockito, MockMvc, JaCoCo.
* **Banco de Dados:** MongoDB, numa única collection `alerts` (dados do usuário que criou/validou o alerta ficam embutidos no próprio documento).

## Pré-requisitos

| Ferramenta | Versão recomendada |
|---|---|
| **JDK** | **17 (LTS)** — versão testada e recomendada para o projeto |
| Maven | 3.8+ (ou via IDE) |
| Node.js | 18+ (apenas para o frontend) |
| MongoDB | 7.x local ou Atlas |

### JDK 17

O projeto foi configurado para **Java 17** (`java.version` no `pom.xml`). Use essa versão no IntelliJ (**File → Project Structure → SDK**) para evitar incompatibilidades com dependências como o **Lombok** — em testes com JDK mais recente (ex.: 24 ou 26), a compilação pode falhar com erro `TypeTag :: UNKNOWN`.

Confirme a versão ativa:
```powershell
java -version
```

Instalação no Windows (opcional):
```powershell
winget install Microsoft.OpenJDK.17
```

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

Abra a pasta `backend` na IDE (IntelliJ IDEA recomendado), aguarde o Maven baixar as dependências e execute `AlertaClimaApplication.java`.

O servidor sobe na porta **8080**. Na inicialização, o sistema recria automaticamente **10 alertas de exemplo** no MongoDB e o terminal exibe os acessos da API, da documentação Swagger e do JSON OpenAPI.

> Para preservar dados entre reinicializações, remova `seed` de `spring.profiles.active` em `backend/src/main/resources/application.properties`.

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

O backend possui testes unitários e de controller (`AlertService`, `AlertController`, `DataInitializer`, `CorsConfig`, `OpenApiConfiguration`, `StartupAccessLogger` e modelo `Alert`), com cobertura medida via **JaCoCo**.

### Como executar

```powershell
cd backend
mvn verify
```

O comando roda todos os testes e **falha** (`BUILD FAILURE`) se a cobertura de linhas ficar abaixo de **70%**.

Relatório HTML:
```text
backend/target/site/jacoco/index.html
```

### Resultados obtidos (última execução)

| Métrica | Resultado | Mínimo exigido (AEP) |
|---|---|---|
| **Linhas** | **99%** (104 de 105) | 70% |
| Instruções | 99% | — |
| Branches | 83% | — |

| Pacote | Cobertura de linhas |
|---|---|
| `com.alertaclima.controller` | 100% |
| `com.alertaclima.config` | 100% |
| `com.alertaclima.service` | 98% (1 linha não coberta) |

> Para reproduzir: execute `mvn verify` na pasta `backend` e abra o `index.html` do JaCoCo.

## Documentação da API (Swagger)

Com o backend em execução, a documentação interativa da API está disponível em:

- Interface Swagger UI: `http://localhost:8080/docs`
- Contrato OpenAPI (JSON): `http://localhost:8080/v3/api-docs`

Na interface é possível visualizar os endpoints, os schemas dos modelos e executar requisições de teste diretamente pelo navegador.

Ao iniciar o backend, o terminal também exibe esses endereços automaticamente.

## CRUD na Prática
O CRUD agora está implementado totalmente em Java! Você pode realizar:
* **CREATE:** Cadastrar alerta (Salva na collection `alerts` do MongoDB).
* **READ:** O Dashboard consome a API Rest em Java listando todos os itens.
* **UPDATE:** O Analista consegue modificar status e níveis de perigo.
* **DELETE:** O Analista consegue fazer o Soft Delete (arquivar ocorrência, populando o `deletedAt`).

Todos os requisitos e padrões exigidos foram perfeitamente migrados para a stack Java solicitada para acompanhamento da disciplina.
