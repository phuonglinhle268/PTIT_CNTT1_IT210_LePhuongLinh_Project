package org.example.java_web_project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_profiles")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_profile_id")
    private Integer userProfileId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String fullname;

    private String phone;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String address;

    private String avatar;

    public enum Gender { MALE, FEMALE, OTHER }
}
