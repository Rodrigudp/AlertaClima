package com.alertaclima.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Resumo do usuário embutido nos documentos de alertas e comentários.
 * O campo usuarioId é a referência para o documento completo na coleção "usuarios";
 * nome, email e papel ficam duplicados (desnormalizados) para evitar uma consulta extra na listagem.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InformacoesUsuario {
    private String usuarioId;
    private String nome;
    private String email;
    private String papel; // CITIZEN ou ANALYST
}
