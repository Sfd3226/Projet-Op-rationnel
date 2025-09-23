package com.transfert.transfertargent.controllers;

import com.transfert.transfertargent.dto.TransactionDTO;
import com.transfert.transfertargent.services.JwtService;
import com.transfert.transfertargent.services.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map; // ✅ IMPORT AJOUTÉ

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class TransactionController {

    private final TransactionService transactionService;
    private final JwtService jwtService;

    @GetMapping("/historique")
    public ResponseEntity<List<TransactionDTO>> getHistoriqueComplet(HttpServletRequest request) {
        try {
            String telephone = extractTelephoneFromRequest(request);
            List<TransactionDTO> dtos = transactionService.getHistoriqueComplet(telephone);
            return ResponseEntity.ok(dtos);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/envoyees")
    public ResponseEntity<List<TransactionDTO>> getTransactionsEnvoyees(HttpServletRequest request) {
        try {
            String telephone = extractTelephoneFromRequest(request);
            List<TransactionDTO> dtos = transactionService.getTransactionsEnvoyees(telephone);
            return ResponseEntity.ok(dtos);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/recues")
    public ResponseEntity<List<TransactionDTO>> getTransactionsRecues(HttpServletRequest request) {
        try {
            String telephone = extractTelephoneFromRequest(request);
            List<TransactionDTO> dtos = transactionService.getTransactionsRecues(telephone);
            return ResponseEntity.ok(dtos);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ ENDPOINT POUR TÉLÉCHARGER UN REÇU (PDF)
    @GetMapping("/{id}/receipt")
    public ResponseEntity<byte[]> downloadReceipt(@PathVariable Long id, HttpServletRequest request) {
        try {
            String telephone = extractTelephoneFromRequest(request);
            byte[] pdfBytes = transactionService.generateReceiptPdf(id, telephone);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename("receipt-" + id + ".pdf")
                    .build());
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    // ✅ ENDPOINT ADMIN POUR ANNULER UNE TRANSACTION
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/{id}/annuler")
    public ResponseEntity<String> annulerTransaction(@PathVariable Long id) {
        try {
            transactionService.annulerTransaction(id);
            return ResponseEntity.ok("Transaction annulée avec succès");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur interne");
        }
    }

    // ✅ ENDPOINT POUR OBtenir une transaction spécifique
    @GetMapping("/{id}")
    public ResponseEntity<TransactionDTO> getTransaction(@PathVariable Long id, HttpServletRequest request) {
        try {
            String telephone = extractTelephoneFromRequest(request);
            TransactionDTO dto = transactionService.getTransactionById(id, telephone);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // ✅ ENDPOINT POUR OBTENIR LES INFORMATIONS DU REÇU
    @GetMapping("/{id}/receipt-info")
    public ResponseEntity<Map<String, Object>> getReceiptInfo(@PathVariable Long id, HttpServletRequest request) {
        try {
            String telephone = extractTelephoneFromRequest(request);

            boolean hasReceipt = transactionService.hasReceipt(id, telephone);
            String receiptNumero = transactionService.getReceiptNumero(id, telephone);

            return ResponseEntity.ok(Map.of(
                    "hasReceipt", hasReceipt,
                    "receiptNumero", receiptNumero
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // ✅ MÉTHODE POUR EXTRAIRE LE TÉLÉPHONE DU TOKEN
    private String extractTelephoneFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            return jwtService.extractTelephone(token);
        }
        throw new RuntimeException("Token JWT manquant ou invalide");
    }
}