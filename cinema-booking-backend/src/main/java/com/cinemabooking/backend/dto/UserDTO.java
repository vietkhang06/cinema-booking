package com.cinemabooking.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    public static final String COLLECTION_NAME = "users";
    
    private String uid;
    private String email;
    private String phone;
    private String name;
    private String birthDate;
    private String gender;
    private String avatarUrl;
    private String role;
    private String status;
    private String memberLevel;
    private Long createdAt;
    private Long updatedAt;
    private Boolean deleted;
}
