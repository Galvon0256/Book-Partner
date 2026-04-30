package com.cg.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "stores")
public class Store {

    @Id
    @NotBlank(message = "Store ID is required")
    @Size(max = 4, message = "Store ID must be at most 4 characters")
    @Column(name = "stor_id", length = 4)
    private String storId;

    @NotBlank(message = "Store name is required")
    @Size(max = 40, message = "Store name must be at most 40 characters")
    @Column(name = "stor_name", length = 40)
    private String storName;

    @Size(max = 40, message = "Address must be at most 40 characters")
    @Column(name = "stor_address", length = 40)
    private String storAddress;

    @Size(max = 20, message = "City must be at most 20 characters")
    @Column(name = "city", length = 20)
    private String city;

    @Size(max = 2, message = "State must be at most 2 characters")
    @Column(name = "state", columnDefinition = "CHAR(2)", length = 2)
    private String state;

    @Size(max = 5, message = "Zip must be at most 5 characters")
    @Column(name = "zip", columnDefinition = "CHAR(5)", length = 5)
    private String zip;

    public String getStorId() { return storId; }
    public void setStorId(String storId) { this.storId = storId; }

    public String getStorName() { return storName; }
    public void setStorName(String storName) { this.storName = storName; }

    public String getStorAddress() { return storAddress; }
    public void setStorAddress(String storAddress) { this.storAddress = storAddress; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getZip() { return zip; }
    public void setZip(String zip) { this.zip = zip; }
}
