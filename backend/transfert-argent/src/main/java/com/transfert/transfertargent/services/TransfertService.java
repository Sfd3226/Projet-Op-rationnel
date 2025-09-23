package com.transfert.transfertargent.services;

import com.transfert.transfertargent.models.Compte;
import com.transfert.transfertargent.models.Transaction;
import com.transfert.transfertargent.repositories.CompteRepository;
import com.transfert.transfertargent.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransfertService {

    private final CompteRepository compteRepository;
    private final TransactionRepository transactionRepository;
    private final ReceiptService receiptService;

    @Transactional
    public Transaction effectuerTransfert(Compte compteSource, String telephoneDestinataire, Double montant) {
        // 1. Calcul des frais (1%)
        Double frais = montant * 0.01;
        Double totalDebit = montant + frais;

        // 2. Vérification solde suffisant
        if (compteSource.getSolde() < totalDebit) {
            throw new RuntimeException("Solde insuffisant. Montant: " + montant + " + Frais: " + frais + " = " + totalDebit);
        }

        // 3. Trouver le compte destinataire
        Compte compteDestinataire = compteRepository.findByNumeroTelephone(telephoneDestinataire)
                .orElseThrow(() -> new RuntimeException("Destinataire avec le numéro " + telephoneDestinataire + " introuvable"));

        // 4. Vérifier qu'on ne transfert pas à soi-même
        if (compteSource.getNumeroTelephone().equals(telephoneDestinataire)) {
            throw new RuntimeException("Impossible de transférer à votre propre compte");
        }

        // 5. Mise à jour des soldes
        compteSource.setSolde(compteSource.getSolde() - totalDebit);
        compteDestinataire.setSolde(compteDestinataire.getSolde() + montant);

        compteRepository.save(compteSource);
        compteRepository.save(compteDestinataire);

        // 6. Enregistrement de la transaction
        Transaction transaction = Transaction.builder()
                .montant(montant)
                .frais(frais)
                .compteSource(compteSource)
                .compteDestination(compteDestinataire)
                .statut("SUCCES")
                .build();

        Transaction transactionSauvegardee = transactionRepository.save(transaction);

        // ✅ Génération du reçu
        try {
            receiptService.generateReceipt(transactionSauvegardee);
            // ✅ Recharger la transaction pour avoir le reçu
            return transactionRepository.findById(transactionSauvegardee.getId())
                    .orElse(transactionSauvegardee);
        } catch (Exception e) {
            // ✅ En cas d'erreur de génération, on retourne quand même la transaction
            System.err.println("Erreur génération reçu: " + e.getMessage());
            return transactionSauvegardee;
        }
    }
}