package com.alertaclima.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "alertas")
public class Alerta {
    @Id
    private String id;

    private Long codigo;

    private String titulo;
    private String descricao;

    private String tipo_evento;
    private String nivel_perigo;
    private String status;

    private Double latitude;
    private Double longitude;

    private String endereco;
    private String url_imagem;

    private LocalDateTime data_evento;

    private InformacoesUsuario criado_por;
    private InformacoesUsuario validado_por;

    private String notas_validacao;

    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
    private LocalDateTime arquivadoEm;
}
