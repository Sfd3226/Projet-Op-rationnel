package com.transfert.transfertargent.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double montant;
    private Double frais;

    // ✅ On ignore certaines propriétés pour éviter la récursion infinie
    @ManyToOne
    @JoinColumn(name = "compte_source_id")
    @JsonIgnoreProperties({"transactionsEnvoyees", "transactionsRecues", "user"})
    private Compte compteSource;

    @ManyToOne
    @JoinColumn(name = "compte_destination_id")
    @JsonIgnoreProperties({"transactionsEnvoyees", "transactionsRecues", "user"})
    private Compte compteDestination;

    private String statut;
    private LocalDateTime dateTransaction;

    // ✅ Évite la boucle Transaction ↔ Receipt
    @OneToOne(mappedBy = "transaction", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("transaction")
    private Receipt receipt;

    @PrePersist
    public void prePersist() {
        this.dateTransaction = LocalDateTime.now();
        if (this.statut == null) {
            this.statut = "SUCCES";
        }
    }
}
