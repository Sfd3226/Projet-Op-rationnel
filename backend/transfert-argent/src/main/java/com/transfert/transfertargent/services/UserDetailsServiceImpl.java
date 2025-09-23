package com.transfert.transfertargent.services;

import com.transfert.transfertargent.models.User;  // Importer User
import com.transfert.transfertargent.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public User loadUserByUsername(String telephone) throws UsernameNotFoundException {
        // Charger l'utilisateur à partir du téléphone
        return userRepository.findByTelephone(telephone)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));
    }
}
