package com.transfert.transfertargent.controllers;

import com.transfert.transfertargent.dto.AuthenticationResponse;
import com.transfert.transfertargent.dto.AuthenticationRequest;
import com.transfert.transfertargent.dto.RegisterRequest;
import com.transfert.transfertargent.services.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200") // Autoriser Angular
public class AuthController {

    private final AuthenticationService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestPart("registerRequest") RegisterRequest request,
            @RequestPart(value = "photoProfil", required = false) MultipartFile photoProfil,
            @RequestPart(value = "photoPiece", required = false) MultipartFile photoPiece
    ) {
        request.setPhotoProfil(photoProfil);
        request.setPhotoPiece(photoPiece);
        AuthenticationResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthenticationRequest request) {
        AuthenticationResponse response = authService.authenticate(request);
        return ResponseEntity.ok(response);
    }
}