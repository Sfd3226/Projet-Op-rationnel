package com.transfert.transfertargent.controllers;

import com.transfert.transfertargent.models.HistoriqueTransaction;
import com.transfert.transfertargent.repositories.HistoriqueTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/historique")
@RequiredArgsConstructor
public class HistoriqueTransactionController {

    private final HistoriqueTransactionRepository historiqueTransactionRepository;

    @GetMapping
    public List<HistoriqueTransaction> getAllHistorique() {
        return historiqueTransactionRepository.findAll();
    }
}
