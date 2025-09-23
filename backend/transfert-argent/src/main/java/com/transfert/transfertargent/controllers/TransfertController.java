package com.transfert.transfertargent.controllers;

import com.transfert.transfertargent.dto.TransfertRequest;
import com.transfert.transfertargent.models.Compte;
import com.transfert.transfertargent.models.Transaction;
import com.transfert.transfertargent.services.CompteService;
import com.transfert.transfertargent.services.TransfertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.*;

        import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/transfert")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class TransfertController {

    private final TransfertService transfertService;
    private final CompteService compteService;

    @PostMapping
    public ResponseEntity<?> effectuerTransfert(@RequestBody TransfertRequest request) {
        try {
            Compte compteSource = compteService.getCompteConnecte();

            Transaction transaction = transfertService.effectuerTransfert(
                    compteSource,
                    request.getTelephoneDestinataire(),
                    request.getMontant()
            );

            // ✅ RÉPONSE PROPRE SANS RÉFÉRENCES CIRCULAIRES
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Transfert effectué avec succès");
            response.put("transactionId", transaction.getId());
            response.put("montant", transaction.getMontant());
            response.put("frais", transaction.getFrais());
            response.put("statut", transaction.getStatut());
            response.put("dateTransaction", transaction.getDateTransaction());

            if (transaction.getReceipt() != null) {
                Map<String, String> receiptInfo = new HashMap<>();
                receiptInfo.put("numero", transaction.getReceipt().getNumero());
                receiptInfo.put("downloadUrl", "/api/receipts/" + transaction.getReceipt().getNumero());
                receiptInfo.put("generatedAt", transaction.getReceipt().getDateGeneration().toString());
                response.put("receipt", receiptInfo);
            }

            response.put("details", Map.of(
                    "montantTransfere", transaction.getMontant(),
                    "fraisAppliques", transaction.getFrais(),
                    "totalDebite", transaction.getMontant() + transaction.getFrais()
            ));

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Échec du transfert", "message", e.getMessage())
            );
        }
    }
}