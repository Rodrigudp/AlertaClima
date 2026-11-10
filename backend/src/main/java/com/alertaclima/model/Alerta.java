package com.alertaclima.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "alertas")
public class Alerta {
    @Id
    private String id;

    private Long codigo;

    private String titulo;
    private String descricao;

    private String tipoEvento;
    private String nivelPerigo;
    private StatusAlerta status;

    private Double latitude;
    private Double longitude;

    private String endereco;
    private String urlImagem;

    private LocalDateTime dataEvento;

    private InformacoesUsuario criadoPor;
    private InformacoesUsuario validadoPor;

    private String notasValidacao;

    /** Lista de subdocumentos com cada mudança de status do alerta. */
    private List<RegistroHistorico> historico = new ArrayList<>();

    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
    private LocalDateTime arquivadoEm;

    public void registrarHistorico(StatusAlerta novoStatus, InformacoesUsuario responsavel, String observacao) {
        if (historico == null) {
            historico = new ArrayList<>();
        }
        historico.add(new RegistroHistorico(novoStatus, LocalDateTime.now(), responsavel, observacao));
    }
}
