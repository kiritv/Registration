package com.ishtec.server.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="user_profile")
@Getter @Setter @NoArgsConstructor
public class UserProfile {
    @Id
    @Column(name="user_profile_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_profile_generator")
    @SequenceGenerator(name="user_profile_generator", sequenceName = "user_profile_seq")
    private Long userProfileId;

    @OneToOne(mappedBy = "userProfile")
    private UserSecret userSecret;

    @Column(name="first_name")
    private String firstName;

    @Column(name="last_name")
    private String lastName;

    public UserProfile(UserSecret userSecret, String firstName, String lastName) {
        this.userSecret = userSecret;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
