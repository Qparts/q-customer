package q.rest.customer.model.contract;

public class SocialMediaCredentialModel {
    private String email;
    private String firstName;
    private String lastName;
    private String socialMediaId;
    private String platform;
    private Integer countryId;
    private String defaultLang;


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

    public String getSocialMediaId() {
        return socialMediaId;
    }

    public void setSocialMediaId(String socialMediaId) {
        this.socialMediaId = socialMediaId;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }
}
