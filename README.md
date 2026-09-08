# AlertaClima

## Problema
Eventos climáticos extremos e ocorrências de risco, como enchentes, quedas de árvores e deslizamentos, costumam acontecer de forma rápida e imprevisível. Na maioria das vezes, os órgãos de Defesa Civil demoram a agir porque ainda dependem de canais lentos, como telefone ou boletim de ocorrência presencial. O AlertaClima nasce pra mudar isso: um sistema colaborativo em que qualquer cidadão pode reportar uma situação de perigo direto do celular, enviando também sua localização pelo GPS, enquanto analistas da Defesa Civil acompanham e validam essas informações em tempo real.

## ODS relacionado
**ODS 11 – Cidades e Comunidades Sustentáveis**, meta 11.5 (reduzir mortes, pessoas afetadas e perdas causadas por desastres). O AlertaClima contribui ao acelerar o mapeamento colaborativo de riscos urbanos, dando à Defesa Civil visibilidade em tempo real sobre ocorrências.

## Tecnologias
* **Frontend:** React, Vite, TypeScript, Tailwind CSS, Leaflet, OpenStreetMap.
* **Backend:** Java 17, Spring Boot, Spring Data MongoDB, Springdoc OpenAPI (Swagger UI).
* **Testes:** JUnit 5, Mockito, MockMvc, JaCoCo.
* **Banco de Dados:** MongoDB (collection `alertas`).

## Como executar

### Pré-requisitos
| Ferramenta | Versão |
|---|---|
| JDK | 17 (LTS) |
| Maven | 3.8+ (ou via IDE) |
| Node.js | 18+ |
| MongoDB | 7.x local ou Atlas |

> Use JDK 17 no IntelliJ (**File → Project Structure → SDK**). Versões mais novas (24+) quebram a compilação do Lombok com erro `TypeTag :: UNKNOWN`.

### 1. MongoDB
```powershell
docker run -d --name alertaclima-mongo -p 27017:27017 mongo:7
```
A conexão fica em `backend/src/main/resources/application.properties` (`spring.data.mongodb.uri`). O database e a collection `alertas` são criados automaticamente.

### 2. Backend
Abra a pasta `backend` na IDE, aguarde o Maven baixar as dependências e execute `AlertaClimaApplication.java`. Sobe na porta **8080** e recria 10 alertas de exemplo no Mongo (profile `seed`, ativo por padrão — remova de `spring.profiles.active` para preservar dados entre reinicializações).

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
Roda os testes e falha (`BUILD FAILURE`) se a cobertura de linhas ficar abaixo de 70%. Relatório em `backend/target/site/jacoco/index.html`.
