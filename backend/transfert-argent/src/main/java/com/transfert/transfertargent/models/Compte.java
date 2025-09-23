package com.transfert.transfertargent.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "comptes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Compte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double solde;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    private String typeCompte;

    @Column(name = "numero_telephone", nullable = false, unique = true)
    private String numeroTelephone;

    @Builder.Default
    private boolean active = true;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToMany(mappedBy = "compteSource", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Transaction> transactionsEnvoyees;

    @OneToMany(mappedBy = "compteDestination", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Transaction> transactionsRecues;

    @PrePersist
    public void prePersist() {
        this.dateCreation = LocalDateTime.now();
        if (this.solde == null) {
            this.solde = 0.0;
        }
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}