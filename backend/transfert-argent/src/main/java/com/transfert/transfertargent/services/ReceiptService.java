package com.transfert.transfertargent.services;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.transfert.transfertargent.models.Receipt;
import com.transfert.transfertargent.models.Transaction;
import com.transfert.transfertargent.repositories.ReceiptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReceiptService {

    private final ReceiptRepository receiptRepository;
    private static final String RECEIPTS_DIR = "receipts/";

    /**
     * Génère un reçu PDF pour une transaction
     * @param transaction La transaction pour laquelle générer le reçu
     * @return Le reçu généré
     */
    public Receipt generateReceipt(Transaction transaction) {
        try {
            // ✅ VÉRIFICATION SI UN REÇU EXISTE DÉJÀ (NOUVEAU)
            Optional<Receipt> existingReceipt = receiptRepository.findByTransactionId(transaction.getId());
            if (existingReceipt.isPresent()) {
                System.out.println("📄 Reçu existant trouvé pour la transaction " + transaction.getId() + ", retour du reçu existant");
                return existingReceipt.get();
            }

            // ✅ Validation de la transaction
            validateTransaction(transaction);

            // ✅ Création du répertoire si inexistant
            createReceiptsDirectory();

            // ✅ Génération numéro unique
            String numero = generateNumeroUnique();

            // ✅ Création du PDF
            String filename = numero + ".pdf";
            String filePath = RECEIPTS_DIR + filename;

            createPdfReceipt(transaction, filePath);

            // ✅ Enregistrement du reçu en base
            Receipt receipt = saveReceiptToDatabase(transaction, numero, filePath);

            System.out.println("✅ Nouveau reçu généré avec succès: " + numero);
            return receipt;

        } catch (IllegalArgumentException e) {
            // ✅ Erreur de validation métier
            System.err.println("❌ Erreur validation reçu: " + e.getMessage());
            throw new RuntimeException("Erreur validation: " + e.getMessage());

        } catch (DocumentException e) {
            // ✅ Erreur de génération PDF
            System.err.println("❌ Erreur génération PDF: " + e.getMessage());
            throw new RuntimeException("Erreur création PDF: " + e.getMessage());

        } catch (Exception e) {
            // ✅ Erreur générale
            System.err.println("❌ Erreur génération reçu: " + e.getMessage());
            throw new RuntimeException("Erreur génération reçu: " + e.getMessage());
        }
    }

    /**
     * Récupère ou génère un reçu PDF pour une transaction
     */
    public Receipt getOrGenerateReceipt(Transaction transaction) {
        // ✅ Vérifie si un reçu existe déjà
        Optional<Receipt> existingReceipt = receiptRepository.findByTransactionId(transaction.getId());
        if (existingReceipt.isPresent()) {
            System.out.println("📄 Reçu existant trouvé, retour: " + existingReceipt.get().getNumero());
            return existingReceipt.get();
        }

        // ✅ Génère un nouveau reçu si aucun n'existe
        System.out.println("🆕 Aucun reçu existant, génération d'un nouveau");
        return generateReceipt(transaction);
    }

    /**
     * Valide la transaction avant génération du reçu
     */
    private void validateTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction ne peut pas être null");
        }

        if (!"SUCCES".equals(transaction.getStatut())) {
            throw new IllegalArgumentException("Reçu ne peut être généré que pour les transactions SUCCES");
        }

        if (transaction.getCompteSource() == null || transaction.getCompteDestination() == null) {
            throw new IllegalArgumentException("Transaction doit avoir un expéditeur et un destinataire");
        }

        if (transaction.getMontant() == null || transaction.getMontant() <= 0) {
            throw new IllegalArgumentException("Montant de transaction invalide");
        }
    }

    /**
     * Crée le répertoire de stockage des reçus
     */
    private void createReceiptsDirectory() throws Exception {
        Path dirPath = Paths.get(RECEIPTS_DIR);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        // ✅ Vérification des permissions d'écriture
        if (!Files.isWritable(dirPath)) {
            throw new Exception("Permissions insuffisantes pour écrire dans le dossier receipts/");
        }
    }

    /**
     * Génère un numéro de reçu unique
     */
    private String generateNumeroUnique() {
        String numero;
        int attempts = 0;
        final int MAX_ATTEMPTS = 10;

        do {
            if (attempts++ >= MAX_ATTEMPTS) {
                throw new RuntimeException("Impossible de générer un numéro de reçu unique");
            }

            numero = "RC" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                    + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        } while (receiptRepository.existsByNumero(numero));

        return numero;
    }

    /**
     * Crée le PDF du reçu
     */
    private void createPdfReceipt(Transaction transaction, String filePath) throws Exception {
        Document document = new Document();
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(filePath);
            PdfWriter.getInstance(document, fos);
            document.open();

            // ✅ Titre du document
            addTitle(document);

            // ✅ Informations de la transaction
            addTransactionDetails(document, transaction);

            // ✅ Informations des parties
            addPartiesInfo(document, transaction);

            // ✅ Footer
            addFooter(document);

        } finally {
            // ✅ Fermeture sécurisée des ressources
            if (document.isOpen()) {
                document.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }

    /**
     * Ajoute le titre au PDF
     */
    private void addTitle(Document document) throws DocumentException {
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.BLUE);
        Paragraph title = new Paragraph("REÇU DE TRANSFERT D'ARGENT", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);
    }

    /**
     * Ajoute les détails de la transaction au PDF
     */
    private void addTransactionDetails(Document document, Transaction transaction) throws DocumentException {
        Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 12);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(20);

        addTableRow(table, "Numéro Transaction:", transaction.getId().toString(), boldFont, normalFont);
        addTableRow(table, "Date Transaction:",
                transaction.getDateTransaction().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")),
                boldFont, normalFont);
        addTableRow(table, "Montant Transféré:", String.format("%,.2f FCFA", transaction.getMontant()), boldFont, normalFont);
        addTableRow(table, "Frais (1%):", String.format("%,.2f FCFA", transaction.getFrais()), boldFont, normalFont);
        addTableRow(table, "Total Débité:", String.format("%,.2f FCFA", transaction.getMontant() + transaction.getFrais()), boldFont, normalFont);
        addTableRow(table, "Statut:", transaction.getStatut(), boldFont, normalFont);

        document.add(table);
    }

    /**
     * Ajoute les informations des parties au PDF
     */
    private void addPartiesInfo(Document document, Transaction transaction) throws DocumentException {
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.DARK_GRAY);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 12);

        // ✅ Section Expéditeur
        Paragraph expediteurHeader = new Paragraph("EXPÉDITEUR", headerFont);
        expediteurHeader.setSpacingAfter(10);
        document.add(expediteurHeader);

        PdfPTable expediteurTable = new PdfPTable(2);
        expediteurTable.setWidthPercentage(100);
        expediteurTable.setSpacingAfter(20);

        addTableRow(expediteurTable, "Nom:",
                transaction.getCompteSource().getUser().getPrenom() + " " +
                        transaction.getCompteSource().getUser().getNom(),
                normalFont, normalFont);
        addTableRow(expediteurTable, "Téléphone:",
                transaction.getCompteSource().getNumeroTelephone(),
                normalFont, normalFont);

        document.add(expediteurTable);

        // ✅ Section Destinataire
        Paragraph destinataireHeader = new Paragraph("DESTINATAIRE", headerFont);
        destinataireHeader.setSpacingAfter(10);
        document.add(destinataireHeader);

        PdfPTable destinataireTable = new PdfPTable(2);
        destinataireTable.setWidthPercentage(100);

        addTableRow(destinataireTable, "Nom:",
                transaction.getCompteDestination().getUser().getPrenom() + " " +
                        transaction.getCompteDestination().getUser().getNom(),
                normalFont, normalFont);
        addTableRow(destinataireTable, "Téléphone:",
                transaction.getCompteDestination().getNumeroTelephone(),
                normalFont, normalFont);

        document.add(destinataireTable);
    }

    /**
     * Ajoute le footer au PDF
     */
    private void addFooter(Document document) throws DocumentException {
        Font footerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC);
        Font smallFont = new Font(Font.FontFamily.HELVETICA, 8);

        Paragraph footer = new Paragraph("\n\n", footerFont);
        footer.add(new Chunk("Ce reçu est une preuve légale de votre transaction. ", footerFont));
        footer.add(new Chunk("Conservez-le pour vos archives.", footerFont));
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        Paragraph generatedInfo = new Paragraph(
                "Généré le " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")),
                smallFont
        );
        generatedInfo.setAlignment(Element.ALIGN_CENTER);
        document.add(generatedInfo);
    }

    /**
     * Ajoute une ligne au tableau PDF
     */
    private void addTableRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(PdfPCell.NO_BORDER);
        labelCell.setPadding(5);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(PdfPCell.NO_BORDER);
        valueCell.setPadding(5);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    /**
     * Sauvegarde le reçu en base de données
     */
    private Receipt saveReceiptToDatabase(Transaction transaction, String numero, String filePath) {
        Receipt receipt = Receipt.builder()
                .numero(numero)
                .urlFichier(filePath)
                .transaction(transaction)
                .build();

        Receipt receiptSauvegarde = receiptRepository.save(receipt);

        // ✅ Mise à jour de la transaction avec le reçu
        transaction.setReceipt(receiptSauvegarde);

        return receiptSauvegarde;
    }

    /**
     * Récupère un reçu par son numéro
     */
    public Receipt getReceiptByNumero(String numero) {
        return receiptRepository.findByNumero(numero)
                .orElseThrow(() -> new RuntimeException("Reçu non trouvé avec le numéro: " + numero));
    }

    /**
     * Récupère un reçu par l'ID de transaction
     */
    public Receipt getReceiptByTransactionId(Long transactionId) {
        return receiptRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Aucun reçu trouvé pour la transaction ID: " + transactionId));
    }

    /**
     * Vérifie si un reçu existe pour une transaction
     */
    public boolean receiptExistsForTransaction(Long transactionId) {
        return receiptRepository.findByTransactionId(transactionId).isPresent();
    }
}