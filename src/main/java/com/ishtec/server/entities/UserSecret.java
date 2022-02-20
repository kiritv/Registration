package com.ishtec.server.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.ishtec.server.model.UserInfo;

@Entity
@Table(name="user_secret")
@Getter @Setter @NoArgsConstructor
public class UserSecret {
    @Id
    @Column(name="user_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_generator")
    @SequenceGenerator(name="user_generator", sequenceName = "user_seq")
    private Long userId;

    @Column(name="email", nullable=false, unique = true)
    private String email;

    @Column(name="encrypted_password")
    private String encryptedPassword;

    @Column(name="enabled")
    private Boolean enabled;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_profile_id", referencedColumnName = "user_profile_id")
    private UserProfile userProfile;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    Set<LookupRole> userRoles;

    public UserSecret(String email, String encryptedPassword, Boolean enabled) {
        super();
        this.email = email;
        this.encryptedPassword = encryptedPassword;
        this.enabled = enabled;
        this.userRoles = new HashSet<>();
    }

    public UserInfo getUserInfo() {
        UserInfo userInfo = new UserInfo();
        if (getUserProfile() != null) {
            userInfo.setFirstName(getUserProfile().getFirstName());
            userInfo.setLastName(getUserProfile().getLastName());
        }
        userInfo.setEmail(getEmail());
        return userInfo;
    }
}
