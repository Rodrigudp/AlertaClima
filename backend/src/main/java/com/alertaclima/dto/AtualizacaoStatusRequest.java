package com.alertaclima.dto;

import com.alertaclima.model.InformacoesUsuario;
import com.alertaclima.model.StatusAlerta;

/** Corpo do PATCH /api/alertas/{id}/status enviado pelo analista. */
public record AtualizacaoStatusRequest(StatusAlerta status, InformacoesUsuario validadoPor, String notasValidacao) {
}
