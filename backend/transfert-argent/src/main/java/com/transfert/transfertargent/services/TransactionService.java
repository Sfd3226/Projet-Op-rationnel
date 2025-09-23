package com.transfert.transfertargent.services;

import com.transfert.transfertargent.dto.TransactionDTO;
import com.transfert.transfertargent.models.Compte;
import com.transfert.transfertargent.models.Transaction;
import com.transfert.transfertargent.models.Receipt;
import com.transfert.transfertargent.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final ReceiptService receiptService;

    // ✅ HISTORIQUE COMPLET
    public List<TransactionDTO> getHistoriqueComplet(String telephone) {
        List<Transaction> transactions = transactionRepository.findByCompteSourceNumeroTelephoneOrCompteDestinationNumeroTelephone(telephone);
        return transactions.stream()
                .map(transaction -> new TransactionDTO(transaction, telephone))
                .collect(Collectors.toList());
    }

    // ✅ TRANSACTIONS ENVOYÉES
    public List<TransactionDTO> getTransactionsEnvoyees(String telephone) {
        List<Transaction> transactions = transactionRepository.findByCompteSource_NumeroTelephone(telephone);
        return transactions.stream()
                .map(transaction -> new TransactionDTO(transaction, telephone))
                .collect(Collectors.toList());
    }

    // ✅ TRANSACTIONS REÇUES
    public List<TransactionDTO> getTransactionsRecues(String telephone) {
        List<Transaction> transactions = transactionRepository.findByCompteDestination_NumeroTelephone(telephone);
        return transactions.stream()
                .map(transaction -> new TransactionDTO(transaction, telephone))
                .collect(Collectors.toList());
    }

    // ✅ GÉNÉRATION DE REÇU PDF (VERSION CORRIGÉE)
    public byte[] generateReceiptPdf(Long transactionId, String telephone) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction non trouvée avec l'ID: " + transactionId));

        // VÉRIFICATION DES PERMISSIONS
        if (!transaction.getCompteSource().getNumeroTelephone().equals(telephone) &&
                !transaction.getCompteDestination().getNumeroTelephone().equals(telephone)) {
            throw new RuntimeException("Accès non autorisé à cette transaction");
        }

        try {
            // ✅ UTILISEZ LA NOUVELLE MÉTHODE QUI GÈRE LES REÇUS EXISTANTS
            Receipt receipt = receiptService.getOrGenerateReceipt(transaction);

            // Lire le fichier PDF généré
            Path filePath = Paths.get(receipt.getUrlFichier());
            return Files.readAllBytes(filePath);

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF: " + e.getMessage());
        }
    }

    // ✅ OBTENIR UNE TRANSACTION SPÉCIFIQUE
    public TransactionDTO getTransactionById(Long id, String telephone) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction non trouvée avec l'ID: " + id));

        // VÉRIFICATION DES PERMISSIONS
        if (!transaction.getCompteSource().getNumeroTelephone().equals(telephone) &&
                !transaction.getCompteDestination().getNumeroTelephone().equals(telephone)) {
            throw new RuntimeException("Accès non autorisé à cette transaction");
        }

        return new TransactionDTO(transaction, telephone);
    }

    // ✅ FILTRER LES TRANSACTIONS PAR DATE (CORRIGÉ POUR LocalDateTime)
    public List<TransactionDTO> getTransactionsByDateRange(String telephone, LocalDateTime startDate, LocalDateTime endDate) {
        // ✅ IMPLÉMENTATION SIMPLIFIÉE - Filtrage en mémoire
        List<Transaction> allTransactions = transactionRepository.findByCompteSourceNumeroTelephoneOrCompteDestinationNumeroTelephone(telephone);

        return allTransactions.stream()
                .filter(transaction -> {
                    LocalDateTime transactionDate = transaction.getDateTransaction();
                    return (startDate == null || transactionDate.isAfter(startDate)) &&
                            (endDate == null || transactionDate.isBefore(endDate));
                })
                .map(transaction -> new TransactionDTO(transaction, telephone))
                .collect(Collectors.toList());
    }

    // ✅ STATISTIQUES DES TRANSACTIONS
    public java.util.Map<String, Object> getStatistiques(String telephone) {
        List<Transaction> transactions = transactionRepository.findByCompteSourceNumeroTelephoneOrCompteDestinationNumeroTelephone(telephone);

        double totalEnvoye = transactions.stream()
                .filter(t -> t.getCompteSource().getNumeroTelephone().equals(telephone))
                .mapToDouble(Transaction::getMontant)
                .sum();

        double totalRecu = transactions.stream()
                .filter(t -> t.getCompteDestination().getNumeroTelephone().equals(telephone))
                .mapToDouble(Transaction::getMontant)
                .sum();

        double totalFrais = transactions.stream()
                .filter(t -> t.getCompteSource().getNumeroTelephone().equals(telephone))
                .mapToDouble(Transaction::getFrais)
                .sum();

        long nombreTransactions = transactions.size();

        return java.util.Map.of(
                "totalEnvoye", totalEnvoye,
                "totalRecu", totalRecu,
                "totalFrais", totalFrais,
                "nombreTransactions", nombreTransactions
        );
    }

    // ✅ MÉTHODE UTILITAIRE POUR OBTENIR LE NOM À PARTIR D'UN NUMÉRO
    public String getNomFromNumero(String numeroTelephone) {
        // Implémentez cette méthode si vous avez un mapping numéro→nom
        // Pour l'instant, retournez le numéro lui-même
        return numeroTelephone;
    }

    // ✅ MÉTHODE POUR TRANSFORMER DTO EN AFFICHAGE LISIBLE
    public java.util.Map<String, String> getDisplayInfo(Transaction transaction, String telephone) {
        boolean isEnvoi = transaction.getCompteSource().getNumeroTelephone().equals(telephone);

        return java.util.Map.of(
                "type", isEnvoi ? "Envoyé à" : "Reçu de",
                "partie", isEnvoi ?
                        transaction.getCompteDestination().getNumeroTelephone() :
                        transaction.getCompteSource().getNumeroTelephone()
        );
    }

    // ✅ ANNULATION D’UNE TRANSACTION (ADMIN)
    public void annulerTransaction(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction non trouvée"));

        if ("ANNULE".equals(transaction.getStatut())) {
            throw new RuntimeException("La transaction est déjà annulée");
        }

        Compte source = transaction.getCompteSource();
        Compte destination = transaction.getCompteDestination();

        // Remboursement et mise à jour du solde
        source.setSolde(source.getSolde() + transaction.getMontant());
        destination.setSolde(destination.getSolde() - transaction.getMontant());

        transaction.setStatut("ANNULE");

        transactionRepository.save(transaction);
        // Si nécessaire : sauvegarder les comptes via CompteRepository
        // compteRepository.save(source);
        // compteRepository.save(destination);
    }

    // ✅ MÉTHODE POUR OBTENIR LE NUMÉRO DE REÇU D'UNE TRANSACTION
    public String getReceiptNumero(Long transactionId, String telephone) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction non trouvée"));

        // VÉRIFICATION DES PERMISSIONS
        if (!transaction.getCompteSource().getNumeroTelephone().equals(telephone) &&
                !transaction.getCompteDestination().getNumeroTelephone().equals(telephone)) {
            throw new RuntimeException("Accès non autorisé");
        }

        if (transaction.getReceipt() != null) {
            return transaction.getReceipt().getNumero();
        }

        return null;
    }

    // ✅ MÉTHODE POUR VÉRIFIER SI UN REÇU EXISTE
    public boolean hasReceipt(Long transactionId, String telephone) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction non trouvée"));

        // VÉRIFICATION DES PERMISSIONS
        if (!transaction.getCompteSource().getNumeroTelephone().equals(telephone) &&
                !transaction.getCompteDestination().getNumeroTelephone().equals(telephone)) {
            throw new RuntimeException("Accès non autorisé");
        }

        return transaction.getReceipt() != null;
    }
}