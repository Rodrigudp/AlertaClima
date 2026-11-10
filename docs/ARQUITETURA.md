# Arquitetura

```
┌──────────────────────────┐      HTTP/JSON       ┌───────────────────────────────────────────┐      ┌───────────┐
│ Frontend (React + Vite)  │ ───────────────────▶ │ Backend (Spring Boot)                     │ ───▶ │  MongoDB  │
│ Dashboard, Mapa, Novo    │                      │ Controller → Service → Repository         │      │ usuarios  │
│ alerta, Detalhes,        │ ◀─────────────────── │ (Spring Data MongoDB)                     │ ◀─── │ alertas   │
│ Usuários                 │                      │                                           │      │ comentarios│
└──────────────────────────┘                      └───────────────────────────────────────────┘      └───────────┘
```

## Backend em camadas

| Pacote | Responsabilidade | Classes |
|---|---|---|
| `controller` | Endpoints REST, conversão HTTP ↔ objetos | `AlertaController`, `UsuarioController`, `ComentarioController` |
| `service` | Regras de negócio e validações | `AlertaService`, `UsuarioService`, `ComentarioService` |
| `repository` | Acesso ao MongoDB (Spring Data) | `AlertaRepository`, `UsuarioRepository`, `ComentarioRepository` |
| `model` | Documentos e subdocumentos | `Alerta`, `Usuario`, `Comentario`, `InformacoesUsuario`, `RegiaoMonitorada`, `RegistroHistorico`, `PapelUsuario`, `StatusAlerta` |
| `dto` | Corpo de requisição que não é um documento | `AtualizacaoStatusRequest` |
| `exception` | Erros de domínio e tradução para HTTP (404/400) | `RecursoNaoEncontradoException`, `RegraNegocioException`, `TratadorDeErros` |
| `util` | Cálculo geográfico reutilizado | `CalculadoraDistancia` |
| `config` | CORS, OpenAPI, dados de exemplo | `CorsConfig`, `OpenApiConfiguration`, `InicializadorDados` |

### Aplicação de orientação a objetos

- **Encapsulamento de comportamento no modelo:** `Usuario.monitora(lat, lon)`, `RegiaoMonitorada.contem(lat, lon)` e `ehValida()`, `Alerta.registrarHistorico(...)`, `Usuario.resumo()`. A regra fica junto do dado que ela usa.
- **Injeção de dependência por construtor** em todos os services e controllers: as dependências são `final` e explícitas, o que facilita os testes com mocks.
- **Responsabilidade única:** o cálculo de distância saiu do `AlertaService` para `CalculadoraDistancia`, e é reutilizado pelo filtro por raio e pelas regiões monitoradas.
- **Tratamento de erros centralizado:** services lançam exceções de domínio e o `TratadorDeErros` (`@RestControllerAdvice`) as converte em respostas `404`/`400` no formato `{ "error": ... }`, inclusive para JSON malformado e valores de enum inválidos.
- **Enums** `PapelUsuario` e `StatusAlerta` tipam os valores fechados no próprio modelo; `StatusAlerta` também converte o rótulo usado na API (`EM ANÁLISE`).

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/alertas` | Criar alerta (vincula `criadoPor` ao usuário e inicia o histórico) |
| GET | `/api/alertas` | Listar ativos (filtros `tipoEvento`, `status`, `nivelPerigo`, `lat`, `lon`, `distance`) |
| GET | `/api/alertas/arquivados` | Listar arquivados |
| GET | `/api/alertas/{id}` | Detalhar alerta |
| PUT | `/api/alertas/{id}` | Atualizar nível de perigo |
| PATCH | `/api/alertas/{id}/status` | Validar/alterar status (acrescenta item ao histórico) |
| DELETE | `/api/alertas/{id}` | Arquivar (soft delete) |
| GET | `/api/alertas/{id}/comentarios` | Comentários do alerta |
| POST | `/api/alertas/{id}/comentarios` | Comentar no alerta |
| DELETE | `/api/comentarios/{id}` | Remover comentário |
| POST | `/api/usuarios` | Cadastrar usuário |
| GET | `/api/usuarios?papel=` | Listar usuários |
| GET / PUT / DELETE | `/api/usuarios/{id}` | Detalhar, atualizar, remover usuário |
| POST | `/api/usuarios/{id}/regioes` | Adicionar região monitorada |
| DELETE | `/api/usuarios/{id}/regioes/{nome}` | Remover região monitorada |
| GET | `/api/usuarios/{id}/alertas` | Alertas ativos criados pelo usuário |
| GET | `/api/usuarios/{id}/alertas-monitorados` | Alertas ativos dentro das regiões do usuário |
| GET | `/api/usuarios/{id}/comentarios` | Comentários feitos pelo usuário |

A documentação interativa (Swagger UI) fica em `http://localhost:8080/docs`.

## Evolução entre as entregas

| | 1ª entrega (`v1.0-entrega1`) | 2ª entrega (`v2.0-entrega2`) |
|---|---|---|
| Coleções | `alertas` | `usuarios`, `alertas`, `comentarios` |
| Relacionamentos | — | usuário → alertas, usuário → comentários, alerta → comentários |
| Documentos complexos | resumo do usuário embutido | + `regioesMonitoradas[]` e `historico[]` |
| Funcionalidades | CRUD de alertas, filtros, mapa, validação | + usuários, regiões monitoradas, alertas por região, comentários, histórico de status |
| Qualidade | — | injeção por construtor, erros centralizados, enums para papel/status, nomes de campo padronizados em camelCase, cálculo geográfico isolado |
| Testes | 30 testes, 99% de linhas | 104 testes (incluindo repositórios contra MongoDB embarcado), 100% de linhas e 96% de branches, CI no GitHub Actions |
