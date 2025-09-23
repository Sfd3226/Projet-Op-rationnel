package com.transfert.transfertargent.dto;

import lombok.Data;

@Data
public class AuthenticationRequest {
    private String telephone;
    private String password;
}
