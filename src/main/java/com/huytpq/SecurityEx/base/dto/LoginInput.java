package com.huytpq.SecurityEx.base.dto;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginInput {
    private String username;
    private String password;
    private Long roleId;
    private String displayName;
    private String email;
    private String profileImage;
    private String googleAccountId;
    private String facebookAccountId;
    private String role;
}
