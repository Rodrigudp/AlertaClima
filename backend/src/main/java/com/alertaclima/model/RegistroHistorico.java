package com.alertaclima.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Subdocumento de Alerta: cada mudança de status fica registrada aqui. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroHistorico {
    private StatusAlerta status;
    private LocalDateTime data;
    private InformacoesUsuario responsavel;
    private String observacao;
}
