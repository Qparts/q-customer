package q.rest.customer.model.contract;

import q.rest.customer.model.entity.Customer;
import q.rest.customer.model.entity.CustomerAddress;
import q.rest.customer.model.entity.SocialMediaProfile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PublicCustomer {

    private long id;
    private String email;
    private String firstName;
    private String lastName;
    private String mobile;
    private Integer countryId;
    private String defaultLang;
    private char status;
    private List<PublicSocialMediaProfile> socialMedia;
    private List<PublicAddress> addresses;
    private List<PublicVehicle> vehicles;

    public PublicCustomer(Customer customer, List<SocialMediaProfile> profiles, List<CustomerAddress> customerAddresses, List<PublicVehicle> vehicles) {
        this.id = customer.getId();
        this.email = customer.getEmail();
        this.firstName = customer.getFirstName();
        this.lastName = customer.getLastName();
        this.countryId = customer.getCountryId();
        this.defaultLang = customer.getDefaultLang();
        this.addresses = new ArrayList<>();
        this.status = customer.getStatus();
        for(CustomerAddress ca : customerAddresses) {
            PublicAddress address = new PublicAddress(ca);
            this.addresses.add(address);
        }

        this.vehicles = vehicles;

        socialMedia = new ArrayList<>();
        for(SocialMediaProfile smp : profiles) {
            PublicSocialMediaProfile contract = new PublicSocialMediaProfile();
            contract.setPlatform(smp.getPlatform());
            contract.setSocialMediaId(smp.getSocialMediaId());
            this.socialMedia.add(contract);
        }
    }




    public List<PublicSocialMediaProfile> getSocialMedia() {
        return socialMedia;
    }




    public void setSocialMedia(List<PublicSocialMediaProfile> socialMedia) {
        this.socialMedia = socialMedia;
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
    public String getMobile() {
        return mobile;
    }
    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
    public Integer getCountryId() {
        return countryId;
    }
    public void setCountryId(Integer countryId) {
        this.countryId = countryId;
    }
    public List<PublicAddress> getAddresses() {
        return addresses;
    }
    public void setAddresses(List<PublicAddress> addresses) {
        this.addresses = addresses;
    }

    public List<PublicVehicle> getVehicles() {
        return vehicles;
    }

    public void setVehicles(List<PublicVehicle> vehicles) {
        this.vehicles = vehicles;
    }

    public char getStatus() {
        return status;
    }

    public void setStatus(char status) {
        this.status = status;
    }




    public String getDefaultLang() {
        return defaultLang;
    }

    public void setDefaultLang(String defaultLang) {
        this.defaultLang = defaultLang;
    }
}
