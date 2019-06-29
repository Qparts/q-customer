package q.rest.customer.model.contract;

import javax.persistence.*;
import java.io.Serializable;

public class PublicSocialMediaProfile implements Serializable{

	private String platform;
	private String socialMediaId;

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public String getSocialMediaId() {
		return socialMediaId;
	}

	public void setSocialMediaId(String socialMediaId) {
		this.socialMediaId = socialMediaId;
	}
}
