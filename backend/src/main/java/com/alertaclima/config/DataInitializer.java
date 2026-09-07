package com.alertaclima.config;

import com.alertaclima.model.Alert;
import com.alertaclima.model.User;
import com.alertaclima.repository.AlertRepository;
import com.alertaclima.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Profile("seed")
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AlertRepository alertRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Limpando banco de dados para o reset...");
        alertRepository.deleteAll();
        userRepository.deleteAll();
        
        User citizen = new User();
        citizen.setName("Cidadão Exemplo");
        citizen.setEmail("cidadao@exemplo.com");
        citizen.setRole("CITIZEN");
        citizen = userRepository.save(citizen);

        User analyst = new User();
        analyst.setName("Analista Defesa Civil");
        analyst.setEmail("analista@defesacivil.gov.br");
        analyst.setRole("ANALYST");
        analyst = userRepository.save(analyst);

        String[][] events = {
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

        for (String[] ev : events) {
            Alert alert = new Alert();
            alert.setTitle(ev[0]);
            alert.setDescription(ev[1]);
            alert.setEvent_type(ev[2]);
            alert.setDanger_level(ev[3]);
            alert.setStatus(ev[4]);
            alert.setLatitude(Double.parseDouble(ev[5]));
            alert.setLongitude(Double.parseDouble(ev[6]));
            alert.setAddress(ev[7]);
            alert.setEvent_date(LocalDateTime.now());
            alert.setCreated_by(citizen.getId());
            if(ev[4].equals("CONFIRMADO") || ev[4].equals("RESOLVIDO")) {
                alert.setValidated_by(analyst.getId());
            }
            alert.setImage_url("https://via.placeholder.com/150");
            alertRepository.save(alert);
        }
        
        System.out.println("Banco de dados populado com " + events.length + " alertas fictícios!");
    }
}
