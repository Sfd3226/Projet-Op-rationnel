package com.transfert.transfertargent.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "receipts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Receipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String numero; // Numéro unique du reçu (ex: RC20240115104530)

    private String urlFichier; // Chemin du fichier PDF

    private LocalDateTime dateGeneration;

    @OneToOne
    @JoinColumn(name = "transaction_id", unique = true)
    @JsonIgnoreProperties("receipt") // ✅ évite boucle JSON
    private Transaction transaction;

    @PrePersist
    public void prePersist() {
        this.dateGeneration = LocalDateTime.now();
    }
}
