package com.cg.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "publishers")
public class Publisher {

    @Id
    @NotBlank(message = "Publisher ID is required")
    @Size(max = 4, message = "Publisher ID must be at most 4 characters")
    @Column(name = "pub_id")
    private String pubId;

    @NotBlank(message = "Publisher name is required")
    @Size(max = 40, message = "Publisher name must be at most 40 characters")
    @Column(name = "pub_name")
    private String pubName;

    @Size(max = 20, message = "City must be at most 20 characters")
    @Column(name = "city")
    private String city;

    @Size(max = 2, message = "State must be 2 characters")
    @Column(name = "state")
    private String state;

    @Size(max = 30, message = "Country must be at most 30 characters")
    @Column(name = "country", length = 30)
    private String country;

    // ── Getters & Setters ─────────────────────────────

    public String getPubId() {
        return pubId;
    }

    public void setPubId(String pubId) {
        this.pubId = pubId;
    }

    public String getPubName() {
        return pubName;
    }

    public void setPubName(String pubName) {
        this.pubName = pubName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
