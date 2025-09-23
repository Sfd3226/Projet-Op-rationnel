package com.transfert.transfertargent.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompteInfoDTO {
    private Long id;
    private Double solde;
    private String typeCompte;
    private String numeroTelephone;
    private LocalDateTime dateCreation;

    // Méthode utilitaire pour l'affichage
    public String getSoldeFormatted() {
        return String.format("%,.2f FCFA", solde);
    }

    public String getDateCreationFormatted() {
        return dateCreation.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}