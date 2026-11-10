package com.alertaclima.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Documento da coleção "comentarios". Relaciona-se com "alertas" por referência (alertaId)
 * e com "usuarios" pelo autor embutido (autor.usuarioId).
 */
@Data
@Document(collection = "comentarios")
public class Comentario {
    @Id
    private String id;

    @Indexed
    private String alertaId;

    private InformacoesUsuario autor;
    private String texto;
    private LocalDateTime criadoEm;
}
