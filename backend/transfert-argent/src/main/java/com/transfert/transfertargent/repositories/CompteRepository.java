package com.transfert.transfertargent.repositories;

import com.transfert.transfertargent.models.Compte;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CompteRepository extends JpaRepository<Compte, Long> {

    Optional<Compte> findByNumeroTelephone(String numeroTelephone);
    List<Compte> findByUser_Id(Long userId);
    Optional<Compte> findByUser_Telephone(String telephone);
    Boolean existsByNumeroTelephone(String numeroTelephone);

    Page<Compte> findAll(Pageable pageable);

    List<Compte> findByNumeroTelephoneContainingOrTypeCompteContaining(String numeroTelephone, String typeCompte);

    Optional<Compte> findByNumeroTelephoneAndIdNot(String numeroTelephone, Long id);

    List<Compte> findByActive(boolean active);

    Long countByActive(boolean active);

    Long countByTypeCompte(String typeCompte);

    @Query("SELECT c FROM Compte c WHERE c.numeroTelephone LIKE %:keyword% OR c.typeCompte LIKE %:keyword% OR c.user.nom LIKE %:keyword% OR c.user.prenom LIKE %:keyword%")
    List<Compte> searchComptes(@Param("keyword") String keyword);

    @Query("SELECT c FROM Compte c WHERE c.solde < :seuil")
    List<Compte> findBySoldeLessThan(@Param("seuil") Double seuil);

    @Query("SELECT SUM(c.solde) FROM Compte c")
    Double getTotalSolde();

    @Query("SELECT AVG(c.solde) FROM Compte c WHERE c.active = true")
    Double getAverageSolde();

    @Query("SELECT MAX(c.solde) FROM Compte c WHERE c.active = true")
    Double getMaxSolde();

    @Query("SELECT c FROM Compte c WHERE c.active = true ORDER BY c.solde DESC")
    List<Compte> findAllActiveOrderBySoldeDesc();

    @Query("SELECT c FROM Compte c WHERE c.dateCreation >= :startDate AND c.dateCreation <= :endDate")
    List<Compte> findByDateCreationBetween(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(c) FROM Compte c WHERE c.dateCreation >= :startDate AND c.dateCreation <= :endDate")
    Long countByDateCreationBetween(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);

    @Query("SELECT c.typeCompte, COUNT(c) FROM Compte c GROUP BY c.typeCompte")
    List<Object[]> countComptesByType();
}