package com.ishtec.server.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.CreationTimestamp;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="user_token")
@Getter @Setter @NoArgsConstructor
public class UserToken {
    @Id
    @Column(name="user_token_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_token_generator")
    @SequenceGenerator(name="user_token_generator", sequenceName = "user_token_seq")
    private Long userTokenId;

    @Column(name="email", nullable=false)
    private String email;

    @Column(name="token", nullable=false)
    private String token;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "insert_date")
    private Date insertDate;
    
    public UserToken(String email, String token) {
    	this.email = email;
    	this.token = token;
    }
}