package com.ishtec.server.entities;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="lookup_role")
@Getter @Setter @NoArgsConstructor
public class LookupRole {
    @Id
    @Column(name="role_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "role_generator")
    @SequenceGenerator(name="role_generator", sequenceName = "role_seq")
    private Long roleId;

    @Column(name="role_name", unique = true)
    private String roleName;

    @ManyToMany(mappedBy = "userRoles")
    Set<UserSecret> userSecrets;

    public LookupRole(String roleName) {
        super();
        this.roleName = roleName;
    }
}
