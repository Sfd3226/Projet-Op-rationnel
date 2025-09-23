package com.transfert.transfertargent.controllers;

import com.transfert.transfertargent.dto.CompteRequest;
import com.transfert.transfertargent.models.Compte;
import com.transfert.transfertargent.models.User;
import com.transfert.transfertargent.services.CompteService;
import com.transfert.transfertargent.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comptes")
@RequiredArgsConstructor
public class CompteController {

    private final CompteService compteService;
    private final UserService userService;

    /**
     * Créer un compte pour un utilisateur donné
     */
    @PostMapping("/creer")
    public Compte creerCompte(@RequestBody CompteRequest compteRequest) {
        User utilisateur = userService.getUserById(compteRequest.getUserId());
        return compteService.creerCompte(
                utilisateur,
                compteRequest.getSoldeInitial(),
                compteRequest.getTypeCompte(),
                compteRequest.getNumeroTelephone()
        );
    }

    /**
     * Consulter le solde du compte connecté
     */
    @GetMapping("/solde")
    public Double consulterSolde() {
        Compte compte = compteService.getCompteConnecte();
        return compte.getSolde();
    }

    /**
     * Dépôt sur le compte connecté
     */
    @PostMapping("/depot")
    public Compte depot(@RequestParam Double montant) {
        Compte compte = compteService.getCompteConnecte();
        compte.setSolde(compte.getSolde() + montant);
        return compteService.mettreAJourSolde(compte.getId(), compte.getSolde());
    }

    /**
     * Retrait sur le compte connecté
     */
    @PostMapping("/retrait")
    public Compte retrait(@RequestParam Double montant) {
        Compte compte = compteService.getCompteConnecte();
        if (compte.getSolde() < montant) {
            throw new RuntimeException("Solde insuffisant pour le retrait");
        }
        compte.setSolde(compte.getSolde() - montant);
        return compteService.mettreAJourSolde(compte.getId(), compte.getSolde());
    }

    /**
     * Récupérer tous les comptes d'un utilisateur
     */
    @GetMapping("/user/{userId}")
    public List<Compte> comptesParUser(@PathVariable Long userId) {
        return compteService.comptesParUser(userId);
    }
}
