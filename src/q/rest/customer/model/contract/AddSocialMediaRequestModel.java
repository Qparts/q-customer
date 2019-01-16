package q.rest.customer.model.contract;

public class AddSocialMediaRequestModel {
    private String platform;
    private long customerId;
    private String socialMediaId;
    private String email;

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public String getSocialMediaId() {
        return socialMediaId;
    }

    public void setSocialMediaId(String socialMediaId) {
        this.socialMediaId = socialMediaId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
