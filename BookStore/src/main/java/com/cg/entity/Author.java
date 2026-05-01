package com.cg.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "authors")
public class Author {

    @Id
    @Column(name = "au_id", length = 11, nullable = false)
    @NotBlank(message = "Author ID is required")
    @Pattern(
        regexp = "^[0-9]{3}-[0-9]{2}-[0-9]{4}$",
        message = "Author ID must be in format: 999-99-9999"
    )
    private String auId;

    @Column(name = "au_lname", length = 40, nullable = false)
    @NotBlank(message = "Last name is required")
    @Size(max = 40, message = "Last name must not exceed 40 characters")
    private String auLname;

    @Column(name = "au_fname", length = 20, nullable = false)
    @NotBlank(message = "First name is required")
    @Size(max = 20, message = "First name must not exceed 20 characters")
    private String auFname;

    @Column(name = "phone", length = 10, nullable = false)
    @NotBlank(message = "Phone is required")
    @Size(max = 10, message = "Phone must not exceed 10 characters")
    private String phone;

    @Column(name = "address", length = 40)
    private String address;

    @Column(name = "city", length = 20)
    private String city;

    @Column(name = "state", length = 50)
    @NotBlank(message = "State is required")
    @Size(max = 50, message = "State name too long")
    private String state;

    @Column(name = "zip", length = 6)
    @Pattern(
    regexp = "^[1-9][0-9]{5}$",
    message = "PIN code must be exactly 6 digits and cannot start with 0"
)
    private String zip;

    @Column(name = "contract", nullable = false)
    @NotNull(message = "Contract field is required (use 0 or 1)")
    @Min(value = 0, message = "Contract must be 0 or 1")
    @Max(value = 1, message = "Contract must be 0 or 1")
    private Integer contract;
    @JsonIgnore
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TitleAuthor> titleAuthors;

    public Author() {
        // Default constructor required by JPA
    }

    public Author(String auId, String auLname, String auFname, String phone,
                  String address, String city, String state, String zip, Integer contract) {
        this.auId = auId;
        this.auLname = auLname;
        this.auFname = auFname;
        this.phone = phone;
        this.address = address;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.contract = contract;
    }

    public String getAuId() { return auId; }
    public void setAuId(String auId) { this.auId = auId; }

    public String getAuLname() { return auLname; }
    public void setAuLname(String auLname) { this.auLname = auLname; }

    public String getAuFname() { return auFname; }
    public void setAuFname(String auFname) { this.auFname = auFname; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getZip() { return zip; }
    public void setZip(String zip) { this.zip = zip; }

    public Integer getContract() { return contract; }
    public void setContract(Integer contract) { this.contract = contract; }

    public List<TitleAuthor> getTitleAuthors() { return titleAuthors; }
    public void setTitleAuthors(List<TitleAuthor> titleAuthors) { this.titleAuthors = titleAuthors; }

    @Override
    public String toString() {
        return "Author{" +
                "auId='" + auId + '\'' +
                ", auFname='" + auFname + '\'' +
                ", auLname='" + auLname + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                '}';
    }
}
