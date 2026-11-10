# Modelo de dados (MongoDB)

Banco: `alertaclima` (MongoDB 7). A PoC usa **três coleções**, ligadas por referência e com documentos aninhados.

```
usuarios  1 ──── N  alertas      (alertas.criado_por.usuarioId  → usuarios._id)
usuarios  1 ──── N  comentarios  (comentarios.autor.usuarioId   → usuarios._id)
alertas   1 ──── N  comentarios  (comentarios.alertaId          → alertas._id)
```

## Coleção `usuarios`

Cidadãos e analistas da Defesa Civil. Contém a lista de subdocumentos `regioesMonitoradas`, usada para descobrir quais alertas interessam a cada usuário.

```json
{
  "_id": "6730f1...",
  "nome": "Cidadão Exemplo",
  "email": "cidadao@exemplo.com",
  "telefone": "44999990000",
  "papel": "CITIZEN",
  "regioesMonitoradas": [
    { "nome": "Casa - São Paulo", "latitude": -23.5505, "longitude": -46.6333, "raioKm": 15.0 },
    { "nome": "Trabalho - Curitiba", "latitude": -25.4284, "longitude": -49.2733, "raioKm": 20.0 }
  ],
  "criadoEm": "2026-11-10T12:00:00",
  "atualizadoEm": "2026-11-10T12:00:00"
}
```

| Campo | Tipo | Observação |
|---|---|---|
| `email` | string | Índice único |
| `papel` | string | `CITIZEN` ou `ANALYST` |
| `regioesMonitoradas` | array de subdocumentos | `nome`, `latitude`, `longitude`, `raioKm` (> 0) |

## Coleção `alertas`

Ocorrências reportadas pelos cidadãos. Guarda um **resumo embutido** do criador e do validador (com a referência `usuarioId`) e a lista de subdocumentos `historico` com cada mudança de status.

```json
{
  "_id": "6730f2...",
  "codigo": 1,
  "titulo": "Árvore caída na pista principal",
  "descricao": "Uma grande árvore caiu bloqueando a via nos dois sentidos.",
  "tipo_evento": "Árvore caída",
  "nivel_perigo": "Alto",
  "status": "CONFIRMADO",
  "latitude": -23.5505,
  "longitude": -46.6333,
  "endereco": "Centro, São Paulo - SP",
  "url_imagem": "http://localhost:8080/images/placeholder-alert.png",
  "data_evento": "2026-11-10T11:40:00",
  "criado_por":   { "usuarioId": "6730f1...", "nome": "Cidadão Exemplo", "email": "cidadao@exemplo.com", "papel": "CITIZEN" },
  "validado_por": { "usuarioId": "6730f3...", "nome": "Analista Defesa Civil", "email": "analista@defesacivil.gov.br", "papel": "ANALYST" },
  "notas_validacao": "Situação avaliada pela Defesa Civil",
  "historico": [
    { "status": "SUSPEITO",   "data": "2026-11-10T11:41:00", "responsavel": { "usuarioId": "6730f1...", "nome": "Cidadão Exemplo" }, "observacao": "Alerta registrado" },
    { "status": "CONFIRMADO", "data": "2026-11-10T11:55:00", "responsavel": { "usuarioId": "6730f3...", "nome": "Analista Defesa Civil" }, "observacao": "Situação avaliada pela Defesa Civil" }
  ],
  "criadoEm": "2026-11-10T11:41:00",
  "atualizadoEm": "2026-11-10T11:55:00",
  "arquivadoEm": null
}
```

| Campo | Tipo | Observação |
|---|---|---|
| `status` | string | `SUSPEITO`, `EM ANÁLISE`, `CONFIRMADO`, `RESOLVIDO`, `FALSO ALARME` |
| `criado_por`, `validado_por` | subdocumento | Resumo do usuário + referência `usuarioId` |
| `historico` | array de subdocumentos | Um item por mudança de status |
| `arquivadoEm` | data | Soft delete: preenchido quando o alerta é arquivado |

## Coleção `comentarios`

Comentários de cidadãos e analistas sobre um alerta. Fica em coleção própria (e não embutida no alerta) porque cresce sem limite e é consultada tanto por alerta quanto por autor.

```json
{
  "_id": "6730f4...",
  "alertaId": "6730f2...",
  "autor": { "usuarioId": "6730f3...", "nome": "Analista Defesa Civil", "email": "analista@defesacivil.gov.br", "papel": "ANALYST" },
  "texto": "Equipe de poda acionada, previsão de 1h para liberar a via.",
  "criadoEm": "2026-11-10T12:05:00"
}
```

| Campo | Tipo | Observação |
|---|---|---|
| `alertaId` | string | Referência a `alertas._id` (indexado) |
| `autor` | subdocumento | Resumo do usuário + referência `usuarioId` |

## Decisões de modelagem

- **Embutir ou referenciar.** Dados lidos sempre junto com o documento e de tamanho limitado (regiões do usuário, histórico do alerta, resumo do autor) ficam **embutidos**. Dados que crescem sem limite ou são consultados de forma independente (comentários) ficam em **coleção separada com referência**.
- **Resumo desnormalizado do usuário.** Alertas e comentários guardam `nome`, `email` e `papel` junto da referência `usuarioId`. Assim a listagem não precisa de uma consulta extra (sem `$lookup`). Ao criar um alerta ou comentário, o backend procura o usuário pelo `usuarioId` ou pelo e-mail e grava a referência correta.
- **Consultas que usam o relacionamento.**
  - Alertas de um usuário: `{ "criado_por.usuarioId": <id> }`
  - Comentários de um alerta: `{ "alertaId": <id> }` ordenado por `criadoEm`
  - Comentários de um usuário: `{ "autor.usuarioId": <id> }`
  - Alertas nas regiões do usuário: carrega `regioesMonitoradas` do usuário e filtra os alertas ativos pela distância (Haversine) até o centro de cada região.
