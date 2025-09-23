package com.transfert.transfertargent.repositories;

import com.transfert.transfertargent.models.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM Transaction t WHERE t.compteSource.numeroTelephone = :telephone OR t.compteDestination.numeroTelephone = :telephone")
    List<Transaction> findByCompteSourceNumeroTelephoneOrCompteDestinationNumeroTelephone(@Param("telephone") String telephone);

    @Query("SELECT t FROM Transaction t WHERE t.compteSource.numeroTelephone = :numeroTelephone")
    List<Transaction> findByCompteSource_NumeroTelephone(@Param("numeroTelephone") String numeroTelephone);

    @Query("SELECT t FROM Transaction t WHERE t.compteDestination.numeroTelephone = :numeroTelephone")
    List<Transaction> findByCompteDestination_NumeroTelephone(@Param("numeroTelephone") String numeroTelephone);

    @Query("SELECT t FROM Transaction t WHERE t.compteSource.id = :compteId OR t.compteDestination.id = :compteId")
    List<Transaction> findByCompteSourceIdOrCompteDestinationId(@Param("compteId") Long compteId);

    @Query("SELECT t FROM Transaction t WHERE t.statut = :statut")
    List<Transaction> findByStatut(@Param("statut") String statut);

    @Query("SELECT t FROM Transaction t WHERE t.dateTransaction BETWEEN :start AND :end")
    List<Transaction> findByDateTransactionBetween(@Param("start") LocalDateTime start,
                                                   @Param("end") LocalDateTime end);

    // ✅ METHODES POUR L'ADMIN
    @Query("SELECT t FROM Transaction t ORDER BY t.dateTransaction DESC")
    List<Transaction> findAllOrderByDateDesc();

    @Query(value = "SELECT t FROM Transaction t ORDER BY t.dateTransaction DESC",
            countQuery = "SELECT COUNT(t) FROM Transaction t")
    Page<Transaction> findAllTransactionsWithPagination(Pageable pageable);

    @Query("SELECT t FROM Transaction t ORDER BY t.dateTransaction DESC")
    Page<Transaction> findAllByOrderByDateTransactionDesc(Pageable pageable);

    @Query(value = "SELECT t FROM Transaction t ORDER BY t.dateTransaction DESC LIMIT :limit",
            nativeQuery = true)
    List<Transaction> findTopNByOrderByDateTransactionDesc(@Param("limit") int limit);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.dateTransaction BETWEEN :start AND :end")
    Long countByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT SUM(t.montant) FROM Transaction t WHERE t.dateTransaction BETWEEN :start AND :end")
    Double getTotalAmountByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT SUM(t.frais) FROM Transaction t WHERE t.dateTransaction BETWEEN :start AND :end")
    Double getTotalFeesByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT t FROM Transaction t WHERE t.compteSource IS NULL AND t.compteDestination IS NOT NULL")
    List<Transaction> findDepotTransactions();

    @Query("SELECT t FROM Transaction t WHERE t.compteSource IS NOT NULL AND t.compteDestination IS NULL")
    List<Transaction> findRetraitTransactions();

    @Query("SELECT FUNCTION('DATE', t.dateTransaction), COUNT(t), SUM(t.montant) " +
            "FROM Transaction t WHERE t.dateTransaction BETWEEN :start AND :end " +
            "GROUP BY FUNCTION('DATE', t.dateTransaction) " +
            "ORDER BY FUNCTION('DATE', t.dateTransaction) DESC")
    List<Object[]> getDailyStats(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // ✅ NOUVELLES MÉTHODES DE PAGINATION
    Page<Transaction> findAll(Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.dateTransaction BETWEEN :start AND :end")
    Page<Transaction> findByDateTransactionBetweenWithPagination(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.statut = :statut")
    Page<Transaction> findByStatutWithPagination(@Param("statut") String statut, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.compteSource.id = :compteId OR t.compteDestination.id = :compteId")
    Page<Transaction> findByCompteIdWithPagination(@Param("compteId") Long compteId, Pageable pageable);
}