package com.transfert.transfertargent.services;

import com.transfert.transfertargent.dto.UserProfileDTO;
import com.transfert.transfertargent.dto.PasswordChangeDTO;
import com.transfert.transfertargent.dto.CompteInfoDTO;
import com.transfert.transfertargent.models.User;
import com.transfert.transfertargent.models.Compte;
import com.transfert.transfertargent.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ✅ Méthode existante
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

    // ✅ NOUVEAU: Récupérer le profil utilisateur avec ses comptes
    @Transactional(readOnly = true)
    public UserProfileDTO getUserProfile(String telephone) {
        User user = userRepository.findByTelephone(telephone)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec le téléphone: " + telephone));

        return mapToProfileDTO(user);
    }

    // ✅ NOUVEAU: Mettre à jour le profil utilisateur
    @Transactional
    public UserProfileDTO updateUserProfile(String telephone, UserProfileDTO profileDTO) {
        User user = userRepository.findByTelephone(telephone)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec le téléphone: " + telephone));

        // Vérifier si le nouveau téléphone est déjà utilisé par un autre utilisateur
        if (!telephone.equals(profileDTO.getTelephone())) {
            userRepository.findByTelephone(profileDTO.getTelephone())
                    .ifPresent(existingUser -> {
                        throw new RuntimeException("Le numéro de téléphone est déjà utilisé par un autre utilisateur");
                    });
        }

        // Vérifier si le nouvel email est déjà utilisé par un autre utilisateur
        if (!user.getEmail().equals(profileDTO.getEmail())) {
            userRepository.findByEmail(profileDTO.getEmail())
                    .ifPresent(existingUser -> {
                        throw new RuntimeException("L'email est déjà utilisé par un autre utilisateur");
                    });
        }

        // Mettre à jour uniquement les champs autorisés
        user.setPrenom(profileDTO.getPrenom());
        user.setNom(profileDTO.getNom());
        user.setEmail(profileDTO.getEmail());
        user.setTelephone(profileDTO.getTelephone());
        user.setPays(profileDTO.getPays());
        user.setPhotoProfil(profileDTO.getPhotoProfil());

        User updatedUser = userRepository.save(user);
        return mapToProfileDTO(updatedUser);
    }

    // ✅ NOUVEAU: Changer le mot de passe
    @Transactional
    public void changePassword(String telephone, PasswordChangeDTO passwordDTO) {
        User user = userRepository.findByTelephone(telephone)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec le téléphone: " + telephone));

        // Vérifier le mot de passe actuel
        if (!passwordEncoder.matches(passwordDTO.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Le mot de passe actuel est incorrect");
        }

        // Vérifier la confirmation du nouveau mot de passe
        if (!passwordDTO.getNewPassword().equals(passwordDTO.getConfirmPassword())) {
            throw new RuntimeException("La confirmation du nouveau mot de passe ne correspond pas");
        }

        // Vérifier que le nouveau mot de passe est différent de l'ancien
        if (passwordEncoder.matches(passwordDTO.getNewPassword(), user.getPassword())) {
            throw new RuntimeException("Le nouveau mot de passe doit être différent de l'actuel");
        }

        // Mettre à jour le mot de passe
        user.setPassword(passwordEncoder.encode(passwordDTO.getNewPassword()));
        userRepository.save(user);
    }

    // ✅ NOUVEAU: Vérifier si un email existe déjà
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    // ✅ NOUVEAU: Vérifier si un téléphone existe déjà
    @Transactional(readOnly = true)
    public boolean telephoneExists(String telephone) {
        return userRepository.findByTelephone(telephone).isPresent();
    }

    // ✅ NOUVEAU: Méthode pour trouver un utilisateur par email (utile pour les vérifications)
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'email: " + email));
    }

    // ✅ Méthode helper: Mapping User → UserProfileDTO
    private UserProfileDTO mapToProfileDTO(User user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setPrenom(user.getPrenom());
        dto.setNom(user.getNom());
        dto.setEmail(user.getEmail());
        dto.setTelephone(user.getTelephone());
        dto.setPays(user.getPays());
        dto.setPhotoProfil(user.getPhotoProfil());

        // Mapper les comptes
        List<CompteInfoDTO> comptesDTO = user.getComptes().stream()
                .map(this::mapToCompteInfoDTO)
                .collect(Collectors.toList());
        dto.setComptes(comptesDTO);

        return dto;
    }

    // ✅ Méthode helper: Mapping Compte → CompteInfoDTO
    private CompteInfoDTO mapToCompteInfoDTO(Compte compte) {
        CompteInfoDTO dto = new CompteInfoDTO();
        dto.setId(compte.getId());
        dto.setSolde(compte.getSolde());
        dto.setTypeCompte(compte.getTypeCompte());
        dto.setNumeroTelephone(compte.getNumeroTelephone());
        dto.setDateCreation(compte.getDateCreation());
        return dto;
    }

    // ✅ NOUVEAU: Mettre à jour uniquement la photo de profil
    @Transactional
    public UserProfileDTO updateProfilePicture(String telephone, String photoUrl) {
        User user = userRepository.findByTelephone(telephone)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec le téléphone: " + telephone));

        user.setPhotoProfil(photoUrl);
        User updatedUser = userRepository.save(user);
        return mapToProfileDTO(updatedUser);
    }

    // ✅ NOUVEAU: Récupérer le numéro de téléphone par user ID
    @Transactional(readOnly = true)
    public String getTelephoneById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + userId));
        return user.getTelephone();
    }
}