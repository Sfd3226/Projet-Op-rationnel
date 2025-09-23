package com.transfert.transfertargent.dto;

import lombok.Data;

@Data
public class TransactionRequest {
    private Long compteSourceId;
    private Long compteDestinationId;
    private Double montant;
}
