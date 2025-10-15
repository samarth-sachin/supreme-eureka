package com.dsl.simulator.Entity;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "from_entity", nullable = false, length = 50)
    private String fromEntity;

    @Column(name = "to_entity", nullable = false, length = 50)
    private String toEntity;

    @Column(name = "message_text", nullable = false, columnDefinition = "TEXT")
    private String messageText;

    @Column(name = "sent_at", updatable = false)
    private LocalDateTime sentAt;

    @PrePersist
    protected void onCreate() {
        sentAt = LocalDateTime.now();
    }
}
