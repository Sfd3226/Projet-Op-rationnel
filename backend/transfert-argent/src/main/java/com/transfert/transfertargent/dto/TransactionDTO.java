package com.transfert.transfertargent.dto;

import com.transfert.transfertargent.models.Transaction;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TransactionDTO {
    private Long id;
    private Double montant;
    private Double frais;
    private String statut;
    private LocalDateTime dateTransaction;
    private String compteSourceNumero;
    private String compteDestinationNumero;
    private String type; // "ENVOI" ou "RECEPTION"

    // Constructeur depuis l'entité Transaction
    public TransactionDTO(Transaction transaction, String userTelephone) {
        this.id = transaction.getId();
        this.montant = transaction.getMontant();
        this.frais = transaction.getFrais();
        this.statut = transaction.getStatut();
        this.dateTransaction = transaction.getDateTransaction();
        this.compteSourceNumero = transaction.getCompteSource().getNumeroTelephone();
        this.compteDestinationNumero = transaction.getCompteDestination().getNumeroTelephone();

        // Déterminer automatiquement le type
        this.type = transaction.getCompteSource().getNumeroTelephone().equals(userTelephone)
                ? "ENVOI" : "RECEPTION";
    }
}