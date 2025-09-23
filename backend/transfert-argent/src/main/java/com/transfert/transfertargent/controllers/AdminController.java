package com.transfert.transfertargent.controllers;

import com.transfert.transfertargent.models.Compte;
import com.transfert.transfertargent.models.Transaction;
import com.transfert.transfertargent.models.User;
import com.transfert.transfertargent.models.Role;
import com.transfert.transfertargent.services.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    // ==================== UTILISATEURS ====================
    @GetMapping("/users")
    public ResponseEntity<Page<User>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(adminService.getAllUsers(pageable));
    }

    @GetMapping("/test")
    public ResponseEntity<String> testAdmin() {
        return ResponseEntity.ok("Admin access works!");
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.getUserById(userId));
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<User> updateUserRole(@PathVariable Long userId, @RequestParam Role newRole) {
        return ResponseEntity.ok(adminService.updateUserRole(userId, newRole));
    }

    @PutMapping("/users/{userId}/status")
    public ResponseEntity<User> toggleUserStatus(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.toggleUserStatus(userId));
    }

    // ==================== COMPTES ====================
    @GetMapping("/comptes")
    public ResponseEntity<Page<Compte>> getAllComptes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("dateCreation").descending());
        return ResponseEntity.ok(adminService.getAllComptes(pageable));
    }

    @GetMapping("/comptes/{compteId}")
    public ResponseEntity<Compte> getCompteById(@PathVariable Long compteId) {
        return ResponseEntity.ok(adminService.getCompteById(compteId));
    }

    @PutMapping("/comptes/{compteId}/status")
    public ResponseEntity<Compte> toggleCompteStatus(@PathVariable Long compteId) {
        return ResponseEntity.ok(adminService.toggleCompteStatus(compteId));
    }

    // ==================== TRANSACTIONS ====================
    @GetMapping("/transactions")
    public ResponseEntity<Page<Transaction>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dateTransaction") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(adminService.getAllTransactions(pageable));
    }

    // ==================== OPERATIONS ====================
    @PostMapping("/comptes/{compteId}/depot")
    public ResponseEntity<Transaction> effectuerDepot(
            @PathVariable Long compteId,
            @RequestParam Double montant,
            @RequestParam(required = false) String motif) {

        return ResponseEntity.ok(adminService.effectuerDepotAdmin(compteId, montant, motif));
    }

    @PostMapping("/comptes/{compteId}/retrait")
    public ResponseEntity<Transaction> effectuerRetrait(
            @PathVariable Long compteId,
            @RequestParam Double montant,
            @RequestParam(required = false) String motif) {

        return ResponseEntity.ok(adminService.effectuerRetraitAdmin(compteId, montant, motif));
    }

    // ==================== STATISTIQUES ====================
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getPlatformStatistics() {
        return ResponseEntity.ok(adminService.getPlatformStatistics());
    }

    @GetMapping("/transactions/recent")
    public ResponseEntity<List<Transaction>> getRecentTransactions(
            @RequestParam(defaultValue = "10") int limit) {

        return ResponseEntity.ok(adminService.getRecentTransactions(limit));
    }

    @GetMapping("/transactions/stats")
    public ResponseEntity<Map<String, Object>> getTransactionStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        return ResponseEntity.ok(adminService.getTransactionStats(start, end));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}