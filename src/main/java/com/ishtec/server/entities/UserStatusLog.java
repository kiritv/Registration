package com.ishtec.server.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
@Table(name = "user_status_log")
@Getter @Setter @NoArgsConstructor
public class UserStatusLog {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_status_log_generator")
    @SequenceGenerator(name="user_status_log_generator", sequenceName = "user_status_log_seq")
    @Column(name="user_status_log_id")
    private long userStatusLogId;

    @OneToOne(targetEntity = UserSecret.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private UserSecret user;

    @OneToOne(targetEntity = LookupStatus.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "status_id")
    private LookupStatus status;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "insert_date")
    private Date insertDate;

    public UserStatusLog(UserSecret user, LookupStatus status) {
        super();
        this.user = user;
        this.status = status;
    }
}
