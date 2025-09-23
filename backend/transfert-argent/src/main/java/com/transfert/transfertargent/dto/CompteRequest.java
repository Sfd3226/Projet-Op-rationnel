package com.transfert.transfertargent.dto;

import lombok.Data;

@Data
public class CompteRequest {
    private String numeroTelephone; // numéro du compte / téléphone
    private Double soldeInitial;    // solde au moment de la création
    private String typeCompte;      // "Courant", "Epargne", etc.
    private Long userId;            // ID de l'utilisateur lié au compte
}
