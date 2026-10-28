package com.ifrn.joaomanoelfontes.helpdesk.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "support_sectors")
public class SupportSector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, unique = true, length = 20)
    private String acronym;

    @OneToMany(mappedBy = "supportSector")
    private List<Category> categories = new ArrayList<>();

    @OneToMany(mappedBy = "supportSector")
    private List<User> technicians = new ArrayList<>();

    public SupportSector() {
    }

    public SupportSector(String name, String acronym) {
        this.name = name;
        this.acronym = acronym;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAcronym() { return acronym; }
    public void setAcronym(String acronym) { this.acronym = acronym; }
    public List<Category> getCategories() { return categories; }
    public List<User> getTechnicians() { return technicians; }
}
