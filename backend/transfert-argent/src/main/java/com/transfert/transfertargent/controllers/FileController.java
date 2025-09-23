// FileController.java
package com.transfert.transfertargent.controllers;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final Path uploadDir = Paths.get("uploads"); // L'emplacement de votre dossier "uploads"

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
        try {
            // 1. Construire le chemin d'accès au fichier
            Path filePath = uploadDir.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            // 2. Vérifier si le fichier existe et est lisible
            if (resource.exists() && resource.isReadable()) {
                // 3. Renvoyer le fichier en tant que réponse HTTP
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE) // Ou MediaType.IMAGE_JPEG_VALUE, etc.
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            // 4. Gérer l'exception si le fichier n'est pas trouvé ou s'il y a un problème de lecture
            return ResponseEntity.internalServerError().build();
        }
    }
}