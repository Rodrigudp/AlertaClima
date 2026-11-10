package com.alertaclima.service;

import com.alertaclima.model.Alerta;
import com.alertaclima.model.InformacoesUsuario;
import com.alertaclima.model.Usuario;
import com.alertaclima.repository.AlertaRepository;
import com.alertaclima.util.CalculadoraDistancia;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlertaService {

    private static final String STATUS_INICIAL = "SUSPEITO";

    private final AlertaRepository alertaRepository;
    private final UsuarioService usuarioService;

    public AlertaService(AlertaRepository alertaRepository, UsuarioService usuarioService) {
        this.alertaRepository = alertaRepository;
        this.usuarioService = usuarioService;
    }

    public Alerta criarAlerta(Alerta alerta) {
        if (alerta.getStatus() == null) alerta.setStatus(STATUS_INICIAL);
        alerta.setCodigo(alertaRepository.count() + 1);
        alerta.setCriado_por(usuarioService.vincular(alerta.getCriado_por()));
        alerta.registrarHistorico(alerta.getStatus(), alerta.getCriado_por(), "Alerta registrado");
        LocalDateTime agora = LocalDateTime.now();
        alerta.setCriadoEm(agora);
        alerta.setAtualizadoEm(agora);
        return alertaRepository.save(alerta);
    }

    public List<Alerta> buscarAlertasAtivos(String tipoEvento, String status, String nivelPerigo, Double lat, Double lon, Double distancia) {
        return alertaRepository.findByArquivadoEmIsNullOrderByCriadoEmDesc().stream()
                .filter(a -> correspondeAoFiltro(tipoEvento, a.getTipo_evento()))
                .filter(a -> correspondeAoFiltro(status, a.getStatus()))
                .filter(a -> correspondeAoFiltro(nivelPerigo, a.getNivel_perigo()))
                .filter(a -> estaDentroDoRaio(a, lat, lon, distancia))
                .toList();
    }

    public List<Alerta> buscarAlertasArquivados() {
        return alertaRepository.findByArquivadoEmIsNotNullOrderByArquivadoEmDesc();
    }

    public Alerta buscarAlertaPorId(String id) {
        return alertaRepository.findById(id).orElse(null);
    }

    /** Alertas registrados por um usuário (relacionamento usuarios -> alertas). */
    public List<Alerta> buscarAlertasPorCriador(String usuarioId) {
        usuarioService.buscarPorId(usuarioId);
        return alertaRepository.buscarPorCriador(usuarioId);
    }

    /** Alertas ativos que caem dentro de alguma região monitorada pelo usuário. */
    public List<Alerta> buscarAlertasNasRegioesDoUsuario(String usuarioId) {
        Usuario usuario = usuarioService.buscarPorId(usuarioId);
        return alertaRepository.findByArquivadoEmIsNullOrderByCriadoEmDesc().stream()
                .filter(a -> usuario.monitora(a.getLatitude(), a.getLongitude()))
                .toList();
    }

    public Alerta atualizarAlerta(String id, Alerta dadosAtualizacao) {
        Alerta alerta = buscarAlertaPorId(id);
        if (alerta == null) return null;
        if (dadosAtualizacao.getNivel_perigo() != null) alerta.setNivel_perigo(dadosAtualizacao.getNivel_perigo());
        alerta.setAtualizadoEm(LocalDateTime.now());
        return alertaRepository.save(alerta);
    }

    public Alerta atualizarStatus(String id, String status, InformacoesUsuario validadoPor, String notas) {
        Alerta alerta = buscarAlertaPorId(id);
        if (alerta == null) return null;
        InformacoesUsuario validador = usuarioService.vincular(validadoPor);
        alerta.setStatus(status);
        alerta.setValidado_por(validador);
        alerta.setNotas_validacao(notas);
        alerta.registrarHistorico(status, validador, notas);
        alerta.setAtualizadoEm(LocalDateTime.now());
        return alertaRepository.save(alerta);
    }

    public boolean arquivarAlerta(String id) {
        Alerta alerta = buscarAlertaPorId(id);
        if (alerta == null) return false;
        alerta.setArquivadoEm(LocalDateTime.now());
        alertaRepository.save(alerta);
        return true;
    }

    private boolean correspondeAoFiltro(String filtro, String valor) {
        return filtro == null || filtro.isEmpty() || filtro.equals(valor);
    }

    private boolean estaDentroDoRaio(Alerta alerta, Double lat, Double lon, Double distancia) {
        if (lat == null || lon == null || distancia == null
                || alerta.getLatitude() == null || alerta.getLongitude() == null) {
            return true;
        }
        return CalculadoraDistancia.distanciaEmKm(lat, lon, alerta.getLatitude(), alerta.getLongitude()) <= distancia;
    }
}
