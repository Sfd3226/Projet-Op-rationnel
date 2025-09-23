package com.transfert.transfertargent.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String prenom;
    private String nom;
    private String email;
    private String telephone;
    private String pays;
    private String photoProfil;
    private String role;
}