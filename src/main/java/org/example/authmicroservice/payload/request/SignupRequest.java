package org.example.authmicroservice.payload.request;

import lombok.Data;
import java.util.Set;

@Data
public class SignupRequest {
    private String username;
    private String email;
    private String password;
    private Set<String> role; // Roles as strings, e.g., ["user", "admin"]
}