package com.example.bankcards.dto;
import com.example.bankcards.entity.Role;
import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private Role role;
}