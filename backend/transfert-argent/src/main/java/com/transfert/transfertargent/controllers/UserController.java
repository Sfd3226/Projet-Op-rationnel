package com.transfert.transfertargent.controllers;

import com.transfert.transfertargent.dto.UserProfileDTO;
import com.transfert.transfertargent.dto.PasswordChangeDTO;
import com.transfert.transfertargent.services.UserService;
import com.transfert.transfertargent.services.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

    // ✅ GET /api/users/profile - Récupérer le profil complet
    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getProfile(HttpServletRequest request) {
        try {
            String telephone = extractTelephoneFromRequest(request);
            UserProfileDTO profile = userService.getUserProfile(telephone);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(null);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ✅ PUT /api/users/profile - Mettre à jour le profil
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @RequestBody UserProfileDTO profileDTO,
            HttpServletRequest request) {
        try {
            String telephone = extractTelephoneFromRequest(request);
            UserProfileDTO updatedProfile = userService.updateUserProfile(telephone, profileDTO);
            return ResponseEntity.ok(updatedProfile);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erreur lors de la mise à jour du profil");
        }
    }

    // ✅ PUT /api/users/password - Changer le mot de passe
    @PutMapping("/password")
    public ResponseEntity<String> changePassword(
            @RequestBody PasswordChangeDTO passwordDTO,
            HttpServletRequest request) {
        try {
            String telephone = extractTelephoneFromRequest(request);
            userService.changePassword(telephone, passwordDTO);
            return ResponseEntity.ok("Mot de passe modifié avec succès");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erreur lors du changement de mot de passe");
        }
    }

    // ✅ GET /api/users/check-email - Vérifier si un email existe
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmailExists(@RequestParam String email) {
        try {
            boolean exists = userService.emailExists(email);
            return ResponseEntity.ok(exists);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ✅ GET /api/users/check-telephone - Vérifier si un téléphone existe
    @GetMapping("/check-telephone")
    public ResponseEntity<Boolean> checkTelephoneExists(@RequestParam String telephone) {
        try {
            boolean exists = userService.telephoneExists(telephone);
            return ResponseEntity.ok(exists);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ✅ GET /api/users/{id}/telephone - Récupérer le téléphone par ID
    @GetMapping("/{id}/telephone")
    public ResponseEntity<String> getTelephoneById(@PathVariable Long id) {
        try {
            String telephone = userService.getTelephoneById(id);
            return ResponseEntity.ok(telephone);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ✅ Méthode utilitaire pour extraire le téléphone du token JWT
    private String extractTelephoneFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            return jwtService.extractTelephone(token);
        }
        throw new RuntimeException("Token JWT manquant ou invalide");
    }
}