package com.transfert.transfertargent.services;

import com.transfert.transfertargent.models.Transaction;
import org.springframework.stereotype.Service;

@Service
public class PdfGenerationService {

    public byte[] generateReceiptPdf(Transaction transaction) {

        try {
            // Utilisez une bibliothèque comme iText, Apache PDFBox, etc.
            return "Simulation PDF content".getBytes();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }
}