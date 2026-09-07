package com.alertaclima.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "alerts")
public class Alert {
    @Id
    private String id;

    private Long code;

    private String title;
    private String description;

    private String event_type;
    private String danger_level;
    private String status;

    private Double latitude;
    private Double longitude;

    private String address;
    private String image_url;

    private LocalDateTime event_date;

    private UserInfo created_by;
    private UserInfo validated_by;

    private String validation_notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
