package com.huytpq.SecurityEx.recipe.entity;

import com.huytpq.SecurityEx.base.data.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cm_user")
public class User extends BaseEntity {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cm_user_id_seq")
    @SequenceGenerator(name = "cm_user_id_seq", sequenceName = "cm_user_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "username", unique = true)
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "retype_password")
    private String retypePassword;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(name = "google_account_id", unique = true)
    private String googleAccountId;

    @Column(name = "facebook_account_id", unique = true)
    private String facebookAccountId;

    @Column(name = "active")
    private Boolean active = true;

    @Transient
    private Role role;

}