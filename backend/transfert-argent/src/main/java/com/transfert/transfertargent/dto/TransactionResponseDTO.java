package com.transfert.transfertargent.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TransactionResponseDTO {
    private Long id;
    private Double montant;
    private Double frais;
    private String statut;
    private LocalDateTime dateTransaction;
    private String type; // "ENVOI" ou "RECEPTION"
    private String autrePartieNumero;
    private String autrePartieNom;

    // Pour les réponses détaillées
    private String compteSourceNumero;
    private String compteDestinationNumero;
}