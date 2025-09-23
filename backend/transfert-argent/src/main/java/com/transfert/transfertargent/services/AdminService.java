package com.transfert.transfertargent.services;

import com.transfert.transfertargent.models.Compte;
import com.transfert.transfertargent.models.Transaction;
import com.transfert.transfertargent.models.User;
import com.transfert.transfertargent.models.Role;
import com.transfert.transfertargent.repositories.CompteRepository;
import com.transfert.transfertargent.repositories.TransactionRepository;
import com.transfert.transfertargent.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final CompteRepository compteRepository;
    private final TransactionRepository transactionRepository;

    // ==================== GESTION UTILISATEURS ====================
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }

    @Transactional
    public User updateUserRole(Long userId, Role newRole) {
        User user = getUserById(userId);
        user.setRole(newRole);
        return userRepository.save(user);
    }

    @Transactional
    public User toggleUserStatus(Long userId) {
        User user = getUserById(userId);
        user.setEnabled(!user.isEnabled());
        return userRepository.save(user);
    }

    public List<User> searchUsers(String keyword) {
        return userRepository.searchUsers(keyword);
    }

    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }

    public List<User> getUsersByStatus(boolean enabled) {
        return userRepository.findByEnabled(enabled);
    }

    public Map<String, Long> getUsersCountByRole() {
        Map<String, Long> countMap = new HashMap<>();
        for (Role role : Role.values()) {
            Long count = userRepository.countByRole(role);
            countMap.put(role.name(), count != null ? count : 0L);
        }
        return countMap;
    }

    // ==================== GESTION COMPTES ====================
    public Page<Compte> getAllComptes(Pageable pageable) {
        return compteRepository.findAll(pageable);
    }

    public List<Compte> getAllComptes() {
        return compteRepository.findAll();
    }

    public Compte getCompteById(Long compteId) {
        return compteRepository.findById(compteId).orElseThrow(() -> new RuntimeException("Compte non trouvé"));
    }

    @Transactional
    public Compte toggleCompteStatus(Long compteId) {
        Compte compte = getCompteById(compteId);
        compte.setActive(!compte.isActive());
        return compteRepository.save(compte);
    }

    public List<Compte> getComptesByUser(Long userId) {
        return compteRepository.findByUser_Id(userId);
    }

    public List<Compte> searchComptes(String keyword) {
        return compteRepository.searchComptes(keyword);
    }

    public List<Compte> getComptesWithLowBalance(Double seuil) {
        return compteRepository.findBySoldeLessThan(seuil);
    }

    public List<Compte> getComptesByStatus(boolean active) {
        return compteRepository.findByActive(active);
    }

    public Map<String, Long> getComptesCountByType() {
        List<Object[]> results = compteRepository.countComptesByType();
        Map<String, Long> countMap = new HashMap<>();
        for (Object[] result : results) {
            countMap.put((String) result[0], (Long) result[1]);
        }
        return countMap;
    }

    // ==================== GESTION TRANSACTIONS ====================
    public Page<Transaction> getAllTransactions(Pageable pageable) {
        return transactionRepository.findAllByOrderByDateTransactionDesc(pageable);
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAllOrderByDateDesc();
    }

    @Transactional
    public Transaction effectuerDepotAdmin(Long compteId, Double montant, String motif) {
        if (montant <= 0) throw new RuntimeException("Montant doit être positif");
        Compte compte = getCompteById(compteId);
        if (!compte.isActive()) throw new RuntimeException("Compte inactif");

        Transaction transaction = Transaction.builder()
                .montant(montant).frais(0.0).compteDestination(compte).compteSource(null)
                .statut("SUCCES").dateTransaction(LocalDateTime.now()).build();

        compte.setSolde(compte.getSolde() + montant);
        compteRepository.save(compte);
        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction effectuerRetraitAdmin(Long compteId, Double montant, String motif) {
        if (montant <= 0) throw new RuntimeException("Montant doit être positif");
        Compte compte = getCompteById(compteId);
        if (!compte.isActive()) throw new RuntimeException("Compte inactif");
        if (compte.getSolde() < montant) throw new RuntimeException("Solde insuffisant");

        Transaction transaction = Transaction.builder()
                .montant(montant).frais(0.0).compteSource(compte).compteDestination(null)
                .statut("SUCCES").dateTransaction(LocalDateTime.now()).build();

        compte.setSolde(compte.getSolde() - montant);
        compteRepository.save(compte);
        return transactionRepository.save(transaction);
    }

    // ==================== STATISTIQUES ====================
    public Map<String, Object> getPlatformStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalComptes", compteRepository.count());
        stats.put("totalTransactions", transactionRepository.count());
        stats.put("totalSolde", compteRepository.getTotalSolde() != null ? compteRepository.getTotalSolde() : 0.0);
        stats.put("activeUsers", userRepository.countByEnabled(true));
        stats.put("activeComptes", compteRepository.countByActive(true));
        stats.put("timestamp", LocalDateTime.now());
        return stats;
    }

    public List<Transaction> getRecentTransactions(int limit) {
        return transactionRepository.findTopNByOrderByDateTransactionDesc(limit);
    }

    public List<Transaction> getDepotTransactions() {
        return transactionRepository.findDepotTransactions();
    }

    public List<Transaction> getRetraitTransactions() {
        return transactionRepository.findRetraitTransactions();
    }

    public Map<String, Object> getTransactionStats(LocalDateTime start, LocalDateTime end) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("count", transactionRepository.countByDateRange(start, end));
        stats.put("totalAmount", transactionRepository.getTotalAmountByDateRange(start, end));
        stats.put("totalFees", transactionRepository.getTotalFeesByDateRange(start, end));
        stats.put("period", Map.of("start", start, "end", end));
        return stats;
    }

    // ==================== METHODES UTILITAIRES ====================
    public boolean isTelephoneExists(String telephone, Long excludeUserId) {
        return excludeUserId != null ?
                userRepository.findByTelephoneAndIdNot(telephone, excludeUserId).isPresent() :
                userRepository.findByTelephone(telephone).isPresent();
    }

    public boolean isEmailExists(String email, Long excludeUserId) {
        return excludeUserId != null ?
                userRepository.findByEmailAndIdNot(email, excludeUserId).isPresent() :
                userRepository.findByEmail(email).isPresent();
    }
}