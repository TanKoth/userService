package org.userservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
public class Session extends BaseModel{
    @ManyToOne
    private User user;
    private String token;
    private Date expiredAt;
    private String ipAddress;
    private String deviceInfo;

    @Enumerated(EnumType.ORDINAL)
    private SessionStatus status;
}
