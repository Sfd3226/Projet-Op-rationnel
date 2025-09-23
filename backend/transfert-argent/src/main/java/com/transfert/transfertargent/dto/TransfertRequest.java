package com.transfert.transfertargent.dto;

import lombok.Data;

@Data
public class TransfertRequest {
    private String telephoneDestinataire;
    private Double montant;
}