package com.transfert.transfertargent.services;

import com.transfert.transfertargent.dto.AuthenticationResponse;
import com.transfert.transfertargent.dto.AuthenticationRequest;
import com.transfert.transfertargent.dto.RegisterRequest;
import com.transfert.transfertargent.models.Compte;
import com.transfert.transfertargent.models.Role;
import com.transfert.transfertargent.models.User;
import com.transfert.transfertargent.repositories.CompteRepository;
import com.transfert.transfertargent.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final CompteRepository compteRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private final String uploadDir = "uploads";

    // Enregistrement d'un nouvel utilisateur
    public AuthenticationResponse register(RegisterRequest request) {
        try {
            // Gestion des fichiers
            if (request.getPhotoProfil() != null && !request.getPhotoProfil().isEmpty()) {
                String profilFilename = saveFile(request.getPhotoProfil());
                request.setPhotoProfilPath(profilFilename);
            }
            if (request.getPhotoPiece() != null && !request.getPhotoPiece().isEmpty()) {
                String pieceFilename = saveFile(request.getPhotoPiece());
                request.setPhotoPiecePath(pieceFilename);
            }
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'upload des fichiers", e);
        }

        // Création de l'utilisateur
        User user = User.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .telephone(request.getTelephone())
                .password(passwordEncoder.encode(request.getPassword()))
                .pays(request.getPays())
                .numeroPiece(request.getNumeroPiece())
                .photoProfil(request.getPhotoProfilPath())
                .photoPiece(request.getPhotoPiecePath())
                .role(Role.USER)
                .build();

        userRepository.save(user);

        // Création automatique d'un compte courant
        Compte compte = Compte.builder()
                .user(user)
                .solde(0.0)
                .typeCompte("Courant")
                .numeroTelephone(user.getTelephone())
                .build();
        compteRepository.save(compte);

        // Génération du JWT
        String token = jwtService.generateToken(user);

        // ✅ CORRECTION: Retourner le response AVEC le rôle
        return AuthenticationResponse.builder()
                .token(token)
                .id(user.getId())
                .firstName(user.getPrenom())
                .lastName(user.getNom())
                .photoProfil(user.getPhotoProfil())
                .role(user.getRole().name()) // ← AJOUT IMPORTANT
                .telephone(user.getTelephone()) // ← OPTIONNEL
                .build();
    }

    // Authentification de l'utilisateur
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        User user = userRepository.findByTelephone(request.getTelephone())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Mot de passe incorrect");
        }

        // Générer le token JWT
        String token = jwtService.generateToken(user);

        // ✅ Retourner le response AVEC le rôle
        return AuthenticationResponse.builder()
                .token(token)
                .id(user.getId())
                .firstName(user.getPrenom())
                .lastName(user.getNom())
                .photoProfil(user.getPhotoProfil())
                .role(user.getRole().name()) // ← AJOUT IMPORTANT
                .telephone(user.getTelephone()) // ← OPTIONNEL
                .build();
    }

    // Méthode pour sauvegarder les fichiers
    private String saveFile(MultipartFile file) throws IOException {
        Path dirPath = Paths.get(uploadDir);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }
        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = dirPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath);
        return filename;
    }

    // ✅ OPTIONNEL: Méthode pour obtenir les infos de l'utilisateur connecté
    public AuthenticationResponse getCurrentUserInfo(String telephone) {
        User user = userRepository.findByTelephone(telephone)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        return AuthenticationResponse.builder()
                .id(user.getId())
                .firstName(user.getPrenom())
                .lastName(user.getNom())
                .photoProfil(user.getPhotoProfil())
                .role(user.getRole().name())
                .telephone(user.getTelephone())
                .build();
    }
}