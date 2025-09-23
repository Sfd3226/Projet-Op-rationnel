package com.transfert.transfertargent.repositories;

import com.transfert.transfertargent.models.User;
import com.transfert.transfertargent.models.Role;
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
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByTelephone(String telephone);
    Optional<User> findByEmail(String email);
    Boolean existsByTelephone(String telephone);
    Boolean existsByEmail(String email);

    Page<User> findAll(Pageable pageable);

    List<User> findByNomContainingOrPrenomContainingOrEmailContainingOrTelephoneContaining(
            String nom, String prenom, String email, String telephone);

    Optional<User> findByTelephoneAndIdNot(String telephone, Long id);

    Optional<User> findByEmailAndIdNot(String email, Long id);

    List<User> findByRole(Role role);

    List<User> findByEnabled(boolean enabled);

    Long countByRole(Role role);

    Long countByEnabled(boolean enabled);

    @Query("SELECT u FROM User u WHERE u.nom LIKE %:keyword% OR u.prenom LIKE %:keyword% OR u.email LIKE %:keyword% OR u.telephone LIKE %:keyword%")
    List<User> searchUsers(@Param("keyword") String keyword);

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startDate AND u.createdAt <= :endDate")
    Long countByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate);

    @Query("SELECT u FROM User u ORDER BY u.createdAt DESC")
    List<User> findAllOrderByCreatedAtDesc();

    @Query("SELECT u FROM User u WHERE u.enabled = :enabled ORDER BY u.nom, u.prenom")
    List<User> findByEnabledOrderByName(@Param("enabled") boolean enabled);

    @Query("SELECT u FROM User u ORDER BY u.createdAt DESC LIMIT :limit")
    List<User> findRecentUsers(@Param("limit") int limit);
}