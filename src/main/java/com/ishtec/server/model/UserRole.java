package com.ishtec.server.model;

import com.ishtec.server.types.ROLE_TYPE;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRole {
    private String email;
    private ROLE_TYPE roleName;
}
