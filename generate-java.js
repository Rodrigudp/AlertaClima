const fs = require('fs');
const path = require('path');

const basePath = 'C:/Users/Rodrigo/.gemini/antigravity/scratch/AlertaClima/backend';

const files = {
  'pom.xml': `<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>3.1.5</version>
		<relativePath/>
	</parent>
	<groupId>com.alertaclima</groupId>
	<artifactId>backend</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>AlertaClima</name>
	<description>Sistema Colaborativo de Monitoramento de Eventos Climáticos</description>
	<properties>
		<java.version>17</java.version>
	</properties>
	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-jpa</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>
		<dependency>
			<groupId>com.h2database</groupId>
			<artifactId>h2</artifactId>
			<scope>runtime</scope>
		</dependency>
		<dependency>
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
			<optional>true</optional>
		</dependency>
	</dependencies>
	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
		</plugins>
	</build>
</project>
`,
  'src/main/resources/application.properties': `
spring.datasource.url=jdbc:h2:file:./database;DB_CLOSE_ON_EXIT=FALSE;AUTO_SERVER=TRUE
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
spring.h2.console.enabled=true
server.port=8080
`,
  'src/main/java/com/alertaclima/AlertaClimaApplication.java': `package com.alertaclima;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AlertaClimaApplication {
    public static void main(String[] args) {
        SpringApplication.run(AlertaClimaApplication.class, args);
    }
}
`,
  'src/main/java/com/alertaclima/config/CorsConfig.java': `package com.alertaclima.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
`,
  'src/main/java/com/alertaclima/model/User.java': `package com.alertaclima.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    @Column(unique = true)
    private String email;
    
    private String role; // CITIZEN ou ANALYST
    
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
`,
  'src/main/java/com/alertaclima/model/Alert.java': `package com.alertaclima.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
@Data
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private String event_type;
    private String danger_level;
    private String status;
    
    private Double latitude;
    private Double longitude;
    
    private String address;
    private String image_url;
    
    private LocalDateTime event_date;
    
    private Long created_by;
    private Long validated_by;
    
    @Column(columnDefinition = "TEXT")
    private String validation_notes;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
`,
  'src/main/java/com/alertaclima/repository/UserRepository.java': `package com.alertaclima.repository;

import com.alertaclima.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {}
`,
  'src/main/java/com/alertaclima/repository/AlertRepository.java': `package com.alertaclima.repository;

import com.alertaclima.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    
    @Query("SELECT a FROM Alert a WHERE a.deletedAt IS NULL ORDER BY a.createdAt DESC")
    List<Alert> findAllActive();
    
    @Query("SELECT a FROM Alert a WHERE a.deletedAt IS NOT NULL ORDER BY a.deletedAt DESC")
    List<Alert> findAllArchived();
}
`,
  'src/main/java/com/alertaclima/service/AlertService.java': `package com.alertaclima.service;

import com.alertaclima.model.Alert;
import com.alertaclima.repository.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AlertService {
    @Autowired
    private AlertRepository alertRepository;
    
    public Alert createAlert(Alert alert) {
        if(alert.getStatus() == null) alert.setStatus("SUSPEITO");
        return alertRepository.save(alert);
    }
    
    public List<Alert> getActiveAlerts(String eventType, String status, String dangerLevel, Double lat, Double lon, Double distance) {
        List<Alert> alerts = alertRepository.findAllActive();
        
        return alerts.stream().filter(a -> {
            boolean match = true;
            if(eventType != null && !eventType.isEmpty() && !eventType.equals(a.getEvent_type())) match = false;
            if(status != null && !status.isEmpty() && !status.equals(a.getStatus())) match = false;
            if(dangerLevel != null && !dangerLevel.isEmpty() && !dangerLevel.equals(a.getDanger_level())) match = false;
            if(lat != null && lon != null && distance != null && a.getLatitude() != null && a.getLongitude() != null) {
                double dist = getDistanceFromLatLonInKm(lat, lon, a.getLatitude(), a.getLongitude());
                if(dist > distance) match = false;
            }
            return match;
        }).collect(Collectors.toList());
    }
    
    public List<Alert> getArchivedAlerts() {
        return alertRepository.findAllArchived();
    }
    
    public Alert getAlertById(Long id) {
        return alertRepository.findById(id).orElse(null);
    }
    
    public Alert updateAlert(Long id, Alert updateData) {
        Alert alert = getAlertById(id);
        if(alert != null) {
            if(updateData.getDanger_level() != null) alert.setDanger_level(updateData.getDanger_level());
            return alertRepository.save(alert);
        }
        return null;
    }
    
    public Alert updateStatus(Long id, String status, Long validatedBy, String notes) {
        Alert alert = getAlertById(id);
        if(alert != null) {
            alert.setStatus(status);
            alert.setValidated_by(validatedBy);
            alert.setValidation_notes(notes);
            return alertRepository.save(alert);
        }
        return null;
    }
    
    public boolean archiveAlert(Long id) {
        Alert alert = getAlertById(id);
        if(alert != null) {
            alert.setDeletedAt(LocalDateTime.now());
            alertRepository.save(alert);
            return true;
        }
        return false;
    }
    
    private double getDistanceFromLatLonInKm(double lat1, double lon1, double lat2, double lon2) {
      double R = 6371; 
      double dLat = (lat2 - lat1) * (Math.PI / 180);
      double dLon = (lon2 - lon1) * (Math.PI / 180);
      double a = 
        Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(lat1 * (Math.PI / 180)) * Math.cos(lat2 * (Math.PI / 180)) * 
        Math.sin(dLon / 2) * Math.sin(dLon / 2); 
      double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)); 
      return R * c; 
    }
}
`,
  'src/main/java/com/alertaclima/controller/AlertController.java': `package com.alertaclima.controller;

import com.alertaclima.model.Alert;
import com.alertaclima.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {
    
    @Autowired
    private AlertService alertService;
    
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Alert alert) {
        Alert created = alertService.createAlert(alert);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Alerta registrado com sucesso.", "alert", created));
    }
    
    @GetMapping
    public ResponseEntity<List<Alert>> getAll(
            @RequestParam(required = false) String event_type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String danger_level,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            @RequestParam(required = false) Double distance
    ) {
        return ResponseEntity.ok(alertService.getActiveAlerts(event_type, status, danger_level, lat, lon, distance));
    }
    
    @GetMapping("/archived")
    public ResponseEntity<List<Alert>> getArchived() {
        return ResponseEntity.ok(alertService.getArchivedAlerts());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        Alert alert = alertService.getAlertById(id);
        if(alert == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Alerta não encontrado"));
        return ResponseEntity.ok(alert);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Alert alert) {
        Alert updated = alertService.updateAlert(id, alert);
        if(updated == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Alerta não encontrado"));
        return ResponseEntity.ok(updated);
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String status = (String) body.get("status");
        Long validatedBy = body.get("validated_by") != null ? Long.valueOf(body.get("validated_by").toString()) : null;
        String notes = (String) body.get("validation_notes");
        
        Alert updated = alertService.updateStatus(id, status, validatedBy, notes);
        if(updated == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Alerta não encontrado"));
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> archive(@PathVariable Long id) {
        boolean success = alertService.archiveAlert(id);
        if(!success) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Alerta não encontrado"));
        return ResponseEntity.ok(Map.of("message", "Alerta arquivado com sucesso."));
    }
}
`,
  'src/main/java/com/alertaclima/config/DataInitializer.java': `package com.alertaclima.config;

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
            {"Nuvem funil observada no horizonte", "Possível formação de tornado avistada por moradores.", "Nuvem funil / possível tornado", "Crítico", "SUSPEITO", "-25.4284", "-49.2733", "Curitiba - PR"},
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
`
};

for (const [relativePath, content] of Object.entries(files)) {
  const fullPath = path.join(basePath, relativePath);
  fs.mkdirSync(path.dirname(fullPath), { recursive: true });
  fs.writeFileSync(fullPath, content);
}

console.log('Arquivos Java gerados com sucesso.');
