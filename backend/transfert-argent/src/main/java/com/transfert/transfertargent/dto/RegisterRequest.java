package com.transfert.transfertargent.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String password;
    private String pays;
    private String numeroPiece;
    private MultipartFile photoProfil;
    private MultipartFile photoPiece;


    private String photoProfilPath;
    private String photoPiecePath;
}
