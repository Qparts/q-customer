package q.rest.customer.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "cst_customer")
public class Customer implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @SequenceGenerator(name = "cst_customer_id_seq_gen", sequenceName = "cst_customer_id_seq", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cst_customer_id_seq_gen")
    @Column(name = "id", updatable = false)
    private long id;
    @Column(name = "email")
    private String email;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    @Column(name = "status")
    private char status;//N= not verified, V = verified
    @Column(name = "created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;
    @Column(name = "password")
    @JsonIgnore
    private String password;//hashed password
    @Column(name = "created_by")
    private int createdBy;
    @Column(name = "country_id")
    private Integer countryId;
    @Column(name = "default_lang")
    private String defaultLang;
    @Column(name = "sms_active")
    private boolean smsActive;
    @Column(name="newsletter_active")
    private boolean newsletterActive;

    @Transient
    private List<CustomerVehicle> vehicles;
    @Transient
    private List<CustomerAddress> addresses;

    public boolean isSmsActive() {
        return smsActive;
    }

    public void setSmsActive(boolean smsActive) {
        this.smsActive = smsActive;
    }

    public boolean isNewsletterActive() {
        return newsletterActive;
    }

    public void setNewsletterActive(boolean newsletterActive) {
        this.newsletterActive = newsletterActive;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public char getStatus() {
        return status;
    }

    public void setStatus(char status) {
        this.status = status;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public Integer getCountryId() {
        return countryId;
    }

    public void setCountryId(Integer countryId) {
        this.countryId = countryId;
    }

    public String getDefaultLang() {
        return defaultLang;
    }

    public void setDefaultLang(String defaultLang) {
        this.defaultLang = defaultLang;
    }

    public List<CustomerVehicle> getVehicles() {
        return vehicles;
    }

    public void setVehicles(List<CustomerVehicle> vehicles) {
        this.vehicles = vehicles;
    }

    public List<CustomerAddress> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<CustomerAddress> addresses) {
        this.addresses = addresses;
    }
}
