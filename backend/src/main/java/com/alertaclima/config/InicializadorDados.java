package com.alertaclima.config;

import com.alertaclima.model.Alerta;
import com.alertaclima.model.Comentario;
import com.alertaclima.model.InformacoesUsuario;
import com.alertaclima.model.PapelUsuario;
import com.alertaclima.model.RegiaoMonitorada;
import com.alertaclima.model.StatusAlerta;
import com.alertaclima.model.Usuario;
import com.alertaclima.repository.AlertaRepository;
import com.alertaclima.repository.ComentarioRepository;
import com.alertaclima.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Popula as coleções usuarios, alertas e comentarios com dados fictícios
 * já relacionados entre si (perfil "seed").
 */
@Component
@Profile("seed")
public class InicializadorDados implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(InicializadorDados.class);

    @Autowired
    private AlertaRepository alertaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ComentarioRepository comentarioRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    public void run(String... args) throws Exception {
        log.info("Limpando banco de dados para o reset...");
        comentarioRepository.deleteAll();
        alertaRepository.deleteAll();
        usuarioRepository.deleteAll();

        Usuario cidadao = usuarioRepository.save(novoUsuario("Cidadão Exemplo", "cidadao@exemplo.com", PapelUsuario.CITIZEN,
                List.of(new RegiaoMonitorada("Casa - São Paulo", -23.5505, -46.6333, 15.0),
                        new RegiaoMonitorada("Trabalho - Curitiba", -25.4284, -49.2733, 20.0))));
        Usuario cidada = usuarioRepository.save(novoUsuario("Maria Moradora", "maria@exemplo.com", PapelUsuario.CITIZEN,
                List.of(new RegiaoMonitorada("Bairro - Rio de Janeiro", -22.9068, -43.1729, 25.0))));
        Usuario analista = usuarioRepository.save(novoUsuario("Analista Defesa Civil", "analista@defesacivil.gov.br", PapelUsuario.ANALYST,
                List.of(new RegiaoMonitorada("Região Sudeste", -22.0, -45.0, 600.0))));

        InformacoesUsuario autorCidadao = cidadao.resumo();
        InformacoesUsuario autorCidada = cidada.resumo();
        InformacoesUsuario autorAnalista = analista.resumo();

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

        List<Alerta> salvos = new ArrayList<>();
        long codigo = 1;
        for (String[] ev : eventos) {
            InformacoesUsuario criador = ev[7].endsWith("RJ") ? autorCidada : autorCidadao;
            StatusAlerta status = StatusAlerta.deValor(ev[4]);

            Alerta alerta = new Alerta();
            alerta.setCodigo(codigo++);
            alerta.setTitulo(ev[0]);
            alerta.setDescricao(ev[1]);
            alerta.setTipoEvento(ev[2]);
            alerta.setNivelPerigo(ev[3]);
            alerta.setStatus(status);
            alerta.setLatitude(Double.parseDouble(ev[5]));
            alerta.setLongitude(Double.parseDouble(ev[6]));
            alerta.setEndereco(ev[7]);
            alerta.setDataEvento(LocalDateTime.now());
            alerta.setCriadoPor(criador);
            alerta.registrarHistorico(StatusAlerta.SUSPEITO, criador, "Alerta registrado");
            if (status != StatusAlerta.SUSPEITO) {
                alerta.setValidadoPor(autorAnalista);
                alerta.registrarHistorico(status, autorAnalista, "Situação avaliada pela Defesa Civil");
            }
            alerta.setUrlImagem(baseUrl + "/images/placeholder-alert.png");
            alerta.setCriadoEm(LocalDateTime.now());
            alerta.setAtualizadoEm(LocalDateTime.now());
            salvos.add(alertaRepository.save(alerta));
        }

        comentarioRepository.save(novoComentario(salvos.get(0), autorCidada, "Passei agora e a árvore continua lá, trânsito parado."));
        comentarioRepository.save(novoComentario(salvos.get(0), autorAnalista, "Equipe de poda acionada, previsão de 1h para liberar a via."));
        comentarioRepository.save(novoComentario(salvos.get(1), autorCidadao, "A água já baixou um pouco, mas carros ainda não passam."));
        comentarioRepository.save(novoComentario(salvos.get(2), autorAnalista, "Equipe a caminho para avaliar os telhados."));

        log.info("Banco populado: 3 usuários, {} alertas e 4 comentários fictícios.", eventos.length);
    }

    private Usuario novoUsuario(String nome, String email, PapelUsuario papel, List<RegiaoMonitorada> regioes) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setPapel(papel);
        usuario.setRegioesMonitoradas(new ArrayList<>(regioes));
        usuario.setCriadoEm(LocalDateTime.now());
        usuario.setAtualizadoEm(LocalDateTime.now());
        return usuario;
    }

    private Comentario novoComentario(Alerta alerta, InformacoesUsuario autor, String texto) {
        Comentario comentario = new Comentario();
        comentario.setAlertaId(alerta.getId());
        comentario.setAutor(autor);
        comentario.setTexto(texto);
        comentario.setCriadoEm(LocalDateTime.now());
        return comentario;
    }
}
