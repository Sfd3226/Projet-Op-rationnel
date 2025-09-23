package com.transfert.transfertargent.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String telephone;
    private String password;
}
