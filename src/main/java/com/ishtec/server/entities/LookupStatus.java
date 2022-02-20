package com.ishtec.server.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="lookup_status")
@Getter @Setter @NoArgsConstructor
public class LookupStatus {
    @Id
    @Column(name="status_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "status_generator")
    @SequenceGenerator(name="status_generator", sequenceName = "status_seq")
    private Long statusId;

    @Column(name="status_name", unique = true)
    private String statusName;

    public LookupStatus(String statusName) {
        super();
        this.statusName = statusName;
    }
}
