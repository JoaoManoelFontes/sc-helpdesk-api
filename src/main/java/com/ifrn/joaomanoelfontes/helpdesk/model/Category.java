package com.ifrn.joaomanoelfontes.helpdesk.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "support_sector_id", nullable = false)
    private SupportSector supportSector;

    @OneToMany(mappedBy = "category")
    private List<Ticket> tickets = new ArrayList<>();

    public Category() {
    }

    public Category(String name, String description, SupportSector supportSector) {
        this.name = name;
        this.description = description;
        this.supportSector = supportSector;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public SupportSector getSupportSector() { return supportSector; }
    public void setSupportSector(SupportSector supportSector) { this.supportSector = supportSector; }
    public List<Ticket> getTickets() { return tickets; }
}
