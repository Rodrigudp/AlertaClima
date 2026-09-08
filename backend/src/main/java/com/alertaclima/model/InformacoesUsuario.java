package com.alertaclima.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InformacoesUsuario {
    private String id;
    private String nome;
    private String email;
    private String papel; // CITIZEN ou ANALYST
}
