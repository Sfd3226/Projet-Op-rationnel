package com.transfert.transfertargent.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private String prenom;
    private String nom;
    private String email;
    private String telephone;
    private String pays;
    private String photoProfil;
    private List<CompteInfoDTO> comptes;

    // Méthodes pratiques
    public String getFullName() {
        return prenom + " " + nom;
    }
}