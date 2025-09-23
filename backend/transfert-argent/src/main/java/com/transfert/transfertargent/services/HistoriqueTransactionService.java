package com.transfert.transfertargent.services;

import com.transfert.transfertargent.models.Compte;
import com.transfert.transfertargent.models.HistoriqueTransaction;
import com.transfert.transfertargent.repositories.HistoriqueTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class HistoriqueTransactionService {

    private final HistoriqueTransactionRepository historiqueTransactionRepository;

    /**
     * Enregistre une transaction dans l'historique
     *
     * @param compteSource      Le compte source
     * @param compteDestination Le compte destination
     * @param montant           Le montant transféré
     * @param frais             Les frais appliqués
     * @param statut            Statut de la transaction ("SUCCES", "ECHOUEE", etc.)
     */
    public void enregistrer(Compte compteSource, Compte compteDestination, Double montant, Double frais, String statut) {
        HistoriqueTransaction historique = HistoriqueTransaction.builder()
                .compteSource(compteSource)
                .compteDestination(compteDestination)
                .montant(montant)
                .frais(frais)
                .statut(statut)
                .dateTransaction(LocalDateTime.now())
                .build();

        historiqueTransactionRepository.save(historique);
    }
}
