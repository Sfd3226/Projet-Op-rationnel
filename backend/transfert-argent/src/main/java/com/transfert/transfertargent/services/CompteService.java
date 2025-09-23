package com.transfert.transfertargent.services;

import com.transfert.transfertargent.models.Compte;
import com.transfert.transfertargent.models.User;
import com.transfert.transfertargent.repositories.CompteRepository;
import com.transfert.transfertargent.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CompteService {

    private final CompteRepository compteRepository;
    private final UserRepository userRepository;

    // ✅ CORRECTION: Récupérer le compte connecté
    public Compte getCompteConnecte() {
        String telephone = SecurityContextHolder.getContext().getAuthentication().getName();
        User utilisateur = userRepository.findByTelephone(telephone)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // ✅ CORRECTION: findByUser_Id retourne une List, on prend le premier
        List<Compte> comptes = compteRepository.findByUser_Id(utilisateur.getId());

        if (comptes.isEmpty()) {
            throw new RuntimeException("Compte introuvable pour l'utilisateur: " + utilisateur.getId());
        }

        // Retourne le premier compte (normalement un utilisateur n'a qu'un compte)
        return comptes.get(0);
    }

    // ✅ CORRECTION ALTERNATIVE: Si vous voulez utiliser Optional
    public Compte getCompteConnecteAlternative() {
        String telephone = SecurityContextHolder.getContext().getAuthentication().getName();
        User utilisateur = userRepository.findByTelephone(telephone)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // Utiliser findFirst si disponible dans votre repository
        Optional<Compte> compteOpt = compteRepository.findByUser_Id(utilisateur.getId())
                .stream()
                .findFirst();

        return compteOpt.orElseThrow(() -> new RuntimeException("Compte introuvable"));
    }

    // Créer un compte avec numéro de téléphone
    public Compte creerCompte(User utilisateur, Double soldeInitial, String typeCompte, String numeroTelephone) {
        Compte compte = Compte.builder()
                .user(utilisateur)
                .solde(soldeInitial)
                .typeCompte(typeCompte)
                .numeroTelephone(numeroTelephone)
                .build();
        return compteRepository.save(compte);
    }

    // Mettre à jour le solde
    public Compte mettreAJourSolde(Long compteId, Double nouveauSolde) {
        Compte compte = compteRepository.findById(compteId)
                .orElseThrow(() -> new RuntimeException("Compte introuvable"));
        compte.setSolde(nouveauSolde);
        return compteRepository.save(compte);
    }

    // Récupérer tous les comptes d'un utilisateur
    public List<Compte> comptesParUser(Long userId) {
        return compteRepository.findByUser_Id(userId);
    }

    // ✅ NOUVELLE MÉTHODE: Récupérer un compte par son ID
    public Compte getCompteById(Long compteId) {
        return compteRepository.findById(compteId)
                .orElseThrow(() -> new RuntimeException("Compte non trouvé avec l'ID: " + compteId));
    }

    // ✅ NOUVELLE MÉTHODE: Récupérer un compte par numéro de téléphone
    public Compte getCompteByNumeroTelephone(String numeroTelephone) {
        return compteRepository.findByNumeroTelephone(numeroTelephone)
                .orElseThrow(() -> new RuntimeException("Compte non trouvé avec le numéro: " + numeroTelephone));
    }
}