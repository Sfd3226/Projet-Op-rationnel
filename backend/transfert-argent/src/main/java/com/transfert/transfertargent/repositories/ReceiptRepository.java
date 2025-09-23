package com.transfert.transfertargent.repositories;

import com.transfert.transfertargent.models.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    Optional<Receipt> findByNumero(String numero);
    Optional<Receipt> findByTransactionId(Long transactionId);
    boolean existsByNumero(String numero);
}