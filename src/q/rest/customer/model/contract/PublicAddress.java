package q.rest.customer.model.contract;

import q.rest.customer.model.entity.CustomerAddress;

public class PublicAddress {
	private long id;
	private long customerId;
	private String line1;
	private String line2;
	private Integer cityId;
	private String zipCode;
	private String title;
	private Double latitude;
	private Double longitude;
	private String mobile;
	private boolean isDefault;
	
	public PublicAddress() {
		
	}
	
	public PublicAddress(CustomerAddress customerAddress) {
		this.id = customerAddress.getId();
		this.customerId = customerAddress.getCustomerId();
		this.line1 = customerAddress.getLine1();
		this.line2 = customerAddress.getLine2();
		this.cityId = customerAddress.getCityId();
		this.zipCode = customerAddress.getZipCode();
		this.title = customerAddress.getTitle();
		this.latitude = customerAddress.getLatitude();
		this.longitude = customerAddress.getLongitude();
		this.mobile = customerAddress.getMobile();
		this.isDefault =customerAddress.isDefault();
	}


	public boolean isDefault() {
		return isDefault;
	}

	public void setDefault(boolean aDefault) {
		isDefault = aDefault;
	}

	public Double getLatitude() {
		return latitude;
	}
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}
	public Double getLongitude() {
		return longitude;
	}
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}
	public long getId() {
		return id;
	}
	public void setId(long addressId) {
		this.id = addressId;
	}
	public long getCustomerId() {
		return customerId;
	}
	public void setCustomerId(long customerId) {
		this.customerId = customerId;
	}
	public String getLine1() {
		return line1;
	}
	public void setLine1(String line1) {
		this.line1 = line1;
	}
	public String getLine2() {
		return line2;
	}
	public void setLine2(String line2) {
		this.line2 = line2;
	}
	public Integer getCityId() {
		return cityId;
	}
	public void setCityId(Integer cityId) {
		this.cityId = cityId;
	}
	public String getZipCode() {
		return zipCode;
	}
	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

	
	
}
