package com.ifrn.joaomanoelfontes.helpdesk.model;

import com.ifrn.joaomanoelfontes.helpdesk.model.enums.UserType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, unique = true, length = 160)
    private String email;

    @Column(nullable = false, length = 30)
    private String registration;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "support_sector_id")
    private SupportSector supportSector;

    public User() {
    }

    public User(String name, String email, String registration, UserType type, SupportSector supportSector) {
        this.name = name;
        this.email = email;
        this.registration = registration;
        this.type = type;
        this.supportSector = supportSector;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRegistration() { return registration; }
    public void setRegistration(String registration) { this.registration = registration; }
    public UserType getType() { return type; }
    public void setType(UserType type) { this.type = type; }
    public SupportSector getSupportSector() { return supportSector; }
    public void setSupportSector(SupportSector supportSector) { this.supportSector = supportSector; }
}
