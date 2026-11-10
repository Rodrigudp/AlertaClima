package com.alertaclima.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Documento da coleção "usuarios" (cidadãos e analistas da Defesa Civil). */
@Data
@Document(collection = "usuarios")
public class Usuario {
    @Id
    private String id;

    private String nome;

    @Indexed(unique = true)
    private String email;

    private String telefone;
    private PapelUsuario papel;

    /** Lista de subdocumentos: regiões que o usuário acompanha. */
    private List<RegiaoMonitorada> regioesMonitoradas = new ArrayList<>();

    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    public InformacoesUsuario resumo() {
        return new InformacoesUsuario(id, nome, email, papel);
    }

    public boolean monitora(Double lat, Double lon) {
        return regioesMonitoradas != null
                && regioesMonitoradas.stream().anyMatch(r -> r.contem(lat, lon));
    }
}
