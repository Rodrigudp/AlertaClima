# AlertaClima

[![CI](https://github.com/Rodrigudp/AlertaClima/actions/workflows/ci.yml/badge.svg)](https://github.com/Rodrigudp/AlertaClima/actions/workflows/ci.yml)

Sistema colaborativo de alertas climáticos e de risco urbano: cidadãos reportam ocorrências pelo celular, analistas da Defesa Civil validam, e cada usuário acompanha os alertas das regiões que monitora.

## Problema
Eventos climáticos extremos e ocorrências de risco, como enchentes, quedas de árvores e deslizamentos, costumam acontecer de forma rápida e imprevisível. Na maioria das vezes, os órgãos de Defesa Civil demoram a agir porque ainda dependem de canais lentos, como telefone ou boletim de ocorrência presencial. O AlertaClima nasce pra mudar isso: um sistema colaborativo em que qualquer cidadão pode reportar uma situação de perigo direto do celular, enviando também sua localização pelo GPS, enquanto analistas da Defesa Civil acompanham e validam essas informações em tempo real.

## ODS relacionado
**ODS 11 – Cidades e Comunidades Sustentáveis**, meta 11.5 (reduzir mortes, pessoas afetadas e perdas causadas por desastres). O AlertaClima contribui ao acelerar o mapeamento colaborativo de riscos urbanos, dando à Defesa Civil visibilidade em tempo real sobre ocorrências.

## Tecnologias
* **Frontend:** React, Vite, TypeScript, Tailwind CSS, Leaflet, OpenStreetMap.
* **Backend:** Java 17, Spring Boot, Spring Data MongoDB, Springdoc OpenAPI (Swagger UI).
* **Testes:** JUnit 5, Mockito, MockMvc, MongoDB embarcado (Flapdoodle), JaCoCo.
* **Banco de Dados:** MongoDB, com as coleções `usuarios`, `alertas` e `comentarios`.
* **CI:** GitHub Actions (testes, cobertura e build do frontend a cada push).

## Funcionalidades
* Registro de alertas com localização por GPS, foto e nível de perigo.
* Dashboard e mapa com filtros por tipo, status, perigo e raio em km.
* Validação pelo analista, com **histórico de status** de cada alerta.
* **Comentários** de cidadãos e analistas em cada alerta.
* **Usuários com regiões monitoradas** (centro + raio) e listagem dos alertas ativos dentro dessas regiões.
* Arquivamento (soft delete) e consulta de alertas arquivados.

## Estrutura do banco
| Coleção | Conteúdo | Relacionamento / aninhamento |
|---|---|---|
| `usuarios` | Cidadãos e analistas | Lista de subdocumentos `regioesMonitoradas` |
| `alertas` | Ocorrências reportadas | `criadoPor.usuarioId` → `usuarios`; lista de subdocumentos `historico` |
| `comentarios` | Comentários sobre alertas | `alertaId` → `alertas`; `autor.usuarioId` → `usuarios` |

Exemplos de documentos e decisões de modelagem: [docs/BANCO-DE-DADOS.md](docs/BANCO-DE-DADOS.md). Camadas, classes e endpoints: [docs/ARQUITETURA.md](docs/ARQUITETURA.md).

## Como executar

### Pré-requisitos
| Ferramenta | Versão |
|---|---|
| JDK | 17 (LTS) |
| Maven | 3.8+ (ou via IDE) |
| Node.js | 18+ |
| MongoDB | 7.x local ou Atlas |

> O código é Java 17. O build foi verificado com JDK 17 (CI) e JDK 24 (local).

### 1. MongoDB
```powershell
docker run -d --name alertaclima-mongo -p 27017:27017 mongo:7
```
A conexão fica em `backend/src/main/resources/application.properties` (`spring.data.mongodb.uri`). O database e as coleções são criados automaticamente.

### 2. Backend
Abra a pasta `backend` na IDE, aguarde o Maven baixar as dependências e execute `AlertaClimaApplication.java`. Sobe na porta **8080**.

Para carregar os dados de exemplo (3 usuários com regiões monitoradas, 10 alertas com histórico e 4 comentários), ative o profile `seed`: no IntelliJ, em **Run → Edit Configurations → Active profiles**, ou pela linha de comando com `mvn spring-boot:run -Dspring-boot.run.profiles=seed`. **Atenção:** o `seed` apaga e recria as três coleções a cada inicialização. Sem ele, os dados são preservados.

Com o backend rodando, a documentação interativa da API fica em `http://localhost:8080/docs` (Swagger UI).

### 3. Frontend
```powershell
cd frontend
npm install
npm run dev
```
Acesse `http://localhost:5173`.

### Testes
```powershell
cd backend
mvn verify
```
Roda os 104 testes automatizados (JUnit 5 + Mockito + MockMvc, e testes de repositório com `@DataMongoTest` contra um MongoDB embarcado, baixado automaticamente na primeira execução, sem precisar de Docker) e mede a cobertura com JaCoCo. O build falha (`BUILD FAILURE`) se a cobertura de linhas ficar abaixo de 70%. Relatório em `backend/target/site/jacoco/index.html`.

Resultado na 2ª entrega: **100% de linhas** e **96% de branches**.

O mesmo comando roda no GitHub Actions a cada push (aba **Actions**). Cada execução mostra a cobertura no resumo e disponibiliza o relatório HTML para download no artefato `relatorio-jacoco`.

## Entregas
| Entrega | Versão |
|---|---|
| 1ª entrega | tag `v1.0-entrega1` |
| 2ª entrega | tag `v2.0-entrega2` |

## Metodologia
Kanban no GitHub Projects: **[Quadro AlertaClima - AEP](https://github.com/users/Rodrigudp/projects/2/views/2)**.

Cada tarefa é uma issue do repositório, com etiqueta da entrega (`entrega-1`, `entrega-2`) e da área (`backend`, `frontend`, `testes`, `docs`), e passa pelas colunas Todo → In Progress → Done. As issues concluídas ficam fechadas, o que deixa o histórico do trabalho visível também na aba **Issues**.

## Equipe
* Rodrigo Antonio Del Padre Filho
* Sergio Guilherme Reis Pedroso
* André Tavares
