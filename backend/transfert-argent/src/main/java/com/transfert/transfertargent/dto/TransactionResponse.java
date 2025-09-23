package com.transfert.transfertargent.dto;

import com.transfert.transfertargent.models.Transaction;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TransactionResponse {
    private Long id;
    private Double montant;
    private Double frais;
    private String type; // "ENVOI" ou "RECEPTION"
    private String autrePartie; // téléphone de l'autre partie
    private String statut;
    private LocalDateTime dateTransaction;
    private String nomAutrePartie; // nom de l'autre partie (optionnel)

    public TransactionResponse(Transaction transaction, String numeroTelephoneConnecte) {
        this.id = transaction.getId();
        this.montant = transaction.getMontant();
        this.frais = transaction.getFrais();
        this.statut = transaction.getStatut();
        this.dateTransaction = transaction.getDateTransaction();

        // Déterminer le type de transaction
        if (transaction.getCompteSource().getNumeroTelephone().equals(numeroTelephoneConnecte)) {
            this.type = "ENVOI";
            this.autrePartie = transaction.getCompteDestination().getNumeroTelephone();
            this.nomAutrePartie = transaction.getCompteDestination().getUser().getPrenom() + " " +
                    transaction.getCompteDestination().getUser().getNom();
        } else {
            this.type = "RECEPTION";
            this.autrePartie = transaction.getCompteSource().getNumeroTelephone();
            this.nomAutrePartie = transaction.getCompteSource().getUser().getPrenom() + " " +
                    transaction.getCompteSource().getUser().getNom();
        }
    }
}