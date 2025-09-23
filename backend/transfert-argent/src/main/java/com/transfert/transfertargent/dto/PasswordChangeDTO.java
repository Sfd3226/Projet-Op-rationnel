package com.transfert.transfertargent.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordChangeDTO {
    private String currentPassword;
    private String newPassword;
    private String confirmPassword;
}