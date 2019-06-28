package q.rest.customer.model.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="cst_social_media")
public class SocialMediaProfile implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name = "cst_social_media_id_seq_gen", sequenceName = "cst_social_media_id_seq", initialValue=1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cst_social_media_id_seq_gen")
	@Column(name = "id", updatable=false)
	private long id;
	@Column(name="platform")
	private String platform;
	@Column(name="social_media_id")
	private String socialMediaId;
	@Column(name="social_media_email")
	private String socialMediaEmail;
	@Column(name="customer_id")
	private long customerId;
	@Column(name="app_code")
	private int appCode;

	public int getAppCode() {
		return appCode;
	}

	public void setAppCode(int appCode) {
		this.appCode = appCode;
	}

	public long getId(){
		return id;
	}

	public void setId(long id){
		this.id = id;
	}

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

	public long getCustomerId() {
	    return customerId;
	}

	public void setCustomerId(long customerId) {
		this.customerId = customerId;
	}

	public String getSocialMediaEmail() {
		return socialMediaEmail;
	}

	public void setSocialMediaEmail(String socialMediaEmail) {
		this.socialMediaEmail = socialMediaEmail;
	}
}
