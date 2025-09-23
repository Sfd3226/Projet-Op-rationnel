package com.transfert.transfertargent.controllers;

import com.transfert.transfertargent.services.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/receipts")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ReceiptController {

    private final ReceiptService receiptService;

    // ✅ TÉLÉCHARGEMENT PAR NUMÉRO DE REÇU
    @GetMapping("/{numero}/download")
    public ResponseEntity<Resource> downloadReceipt(@PathVariable String numero) {
        try {
            // Récupère le reçu
            var receipt = receiptService.getReceiptByNumero(numero);

            // Charge le fichier PDF
            Path filePath = Paths.get(receipt.getUrlFichier());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"" + receipt.getNumero() + ".pdf\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ✅ TÉLÉCHARGEMENT PAR ID DE TRANSACTION
    @GetMapping("/transaction/{transactionId}/download")
    public ResponseEntity<Resource> downloadReceiptByTransaction(@PathVariable Long transactionId) {
        try {
            var receipt = receiptService.getReceiptByTransactionId(transactionId);
            return downloadReceipt(receipt.getNumero());

        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}