package com.transfert.transfertargent.repositories;

import com.transfert.transfertargent.models.HistoriqueTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoriqueTransactionRepository extends JpaRepository<HistoriqueTransaction, Long> {

    // List<HistoriqueTransaction> findByCompteSourceId(Long compteId);
}
