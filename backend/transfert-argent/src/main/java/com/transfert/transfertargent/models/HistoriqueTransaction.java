package com.transfert.transfertargent.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "historique_transaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoriqueTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "compte_source_id")
    private Compte compteSource;

    @ManyToOne
    @JoinColumn(name = "compte_destination_id")
    private Compte compteDestination;

    private Double montant;
    private Double frais;
    private String statut;
    private LocalDateTime dateTransaction;
}
