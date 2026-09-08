package com.alertaclima.config;

import com.alertaclima.model.Alerta;
import com.alertaclima.model.InformacoesUsuario;
import com.alertaclima.repository.AlertaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Profile("seed")
public class InicializadorDados implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(InicializadorDados.class);

    @Autowired
    private AlertaRepository alertaRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    public void run(String... args) throws Exception {
        log.info("Limpando banco de dados para o reset...");
        alertaRepository.deleteAll();

        InformacoesUsuario cidadao = new InformacoesUsuario(null, "Cidadão Exemplo", "cidadao@exemplo.com", "CITIZEN");
        InformacoesUsuario analista = new InformacoesUsuario(null, "Analista Defesa Civil", "analista@defesacivil.gov.br", "ANALYST");

        String[][] eventos = {
            {"Árvore caída na pista principal", "Uma grande árvore caiu bloqueando a via nos dois sentidos.", "Árvore caída", "Alto", "CONFIRMADO", "-23.5505", "-46.6333", "Centro, São Paulo - SP"},
            {"Alagamento severo", "Rua completamente alagada, veículos impossibilitados de passar.", "Alagamento", "Alto", "EM ANÁLISE", "-23.5615", "-46.6553", "Av Paulista, São Paulo - SP"},
            {"Vendaval forte destelhando casas", "Vento muito forte destelhando casas e quebrando galhos.", "Vendaval", "Moderado", "SUSPEITO", "-22.9068", "-43.1729", "Rio de Janeiro - RJ"},
            {"Chuva de Granizo intenso", "Granizo quebrando vidros de carros e janelas.", "Granizo", "Moderado", "CONFIRMADO", "-19.9167", "-43.9345", "Belo Horizonte - MG"},
            {"Tornado avistado no horizonte", "Tornado em formação ou já tocando o solo.", "Tornado", "Crítico", "SUSPEITO", "-25.4284", "-49.2733", "Curitiba - PR"},
            {"Risco de Deslizamento de encosta", "Encosta cedendo lentamente devido às fortes chuvas recentes.", "Deslizamento", "Crítico", "EM ANÁLISE", "-27.5954", "-48.5480", "Florianópolis - SC"},
            {"Enchente atingindo o bairro", "Nível do rio subiu e a água já atinge as casas.", "Enchente", "Alto", "RESOLVIDO", "-30.0346", "-51.2177", "Porto Alegre - RS"},
            {"Queda de poste de energia", "Poste tombou sobre um veículo, fiação exposta.", "Outro evento de risco", "Crítico", "CONFIRMADO", "-15.7942", "-47.8822", "Brasília - DF"},
            {"Bloqueio de rodovia por terra", "Queda de barreira bloqueando a BR.", "Bloqueio de rodovia", "Alto", "SUSPEITO", "-20.3155", "-40.3128", "Vitória - ES"},
            {"Tempestade severa com raios", "Muitos raios e chuva torrencial causando picos de energia.", "Outro evento de risco", "Baixo", "RESOLVIDO", "-12.9714", "-38.5014", "Salvador - BA"}
        };

        long codigo = 1;
        for (String[] ev : eventos) {
            Alerta alerta = new Alerta();
            alerta.setCodigo(codigo++);
            alerta.setTitulo(ev[0]);
            alerta.setDescricao(ev[1]);
            alerta.setTipo_evento(ev[2]);
            alerta.setNivel_perigo(ev[3]);
            alerta.setStatus(ev[4]);
            alerta.setLatitude(Double.parseDouble(ev[5]));
            alerta.setLongitude(Double.parseDouble(ev[6]));
            alerta.setEndereco(ev[7]);
            alerta.setData_evento(LocalDateTime.now());
            alerta.setCriado_por(cidadao);
            if(ev[4].equals("CONFIRMADO") || ev[4].equals("RESOLVIDO")) {
                alerta.setValidado_por(analista);
            }
            alerta.setUrl_imagem(baseUrl + "/images/placeholder-alert.png");
            alerta.setCriadoEm(LocalDateTime.now());
            alerta.setAtualizadoEm(LocalDateTime.now());
            alertaRepository.save(alerta);
        }

        log.info("Banco de dados populado com {} alertas ficticios!", eventos.length);
    }
}
