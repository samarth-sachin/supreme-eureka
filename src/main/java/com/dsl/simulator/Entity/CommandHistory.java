package com.dsl.simulator.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "command_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommandHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String command;

    @Column(columnDefinition = "TEXT")
    private String result;

    @Column(length = 50)
    private String status = "SUCCESS";

    @Column(name = "executed_at", updatable = false)
    private LocalDateTime executedAt;

    @PrePersist
    protected void onCreate() {
        executedAt = LocalDateTime.now();
    }
}

