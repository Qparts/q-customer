package q.rest.customer.model.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Table(name="cst_address")
@Entity
public class CustomerAddress implements Serializable{

	private static final long serialVersionUID = 1L;
	@Id
	@SequenceGenerator(name = "cst_address_id_seq_gen", sequenceName = "cst_address_id_seq", initialValue=1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cst_address_id_seq_gen")
	@Column(name="id")
	private long id;
	@Column(name="customer_id")
	private long customerId;
	@Column(name="address_line_1")
	private String line1;
	@Column(name="address_line_2")
	private String line2;
	@Column(name="city_id")
	private int cityId;
	@Column(name="ZIP")
	private String zipCode;
	@Column(name="created")
	@Temporal(TemporalType.TIMESTAMP)
	private Date created;
	@Column(name="created_by")
	private Integer createdBy;
	@Column(name="status")
	private Character status;//A = active, I = archived
	@Column(name="latitude")
	private Double latitude;
	@Column(name="longitude")
	private Double longitude;
	@Column(name="title")
	private String title;
	@Column(name="mobile")
    private String mobile;
	@Column(name="is_default")
	private boolean isDefault;
	
	public CustomerAddress() {
		
	}

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
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
	public int getCityId() {
		return cityId;
	}
	public void setCityId(int cityId) {
		this.cityId = cityId;
	}
	public String getZipCode() {
		return zipCode;
	}
	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	public Integer getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(Integer created_by) {
		this.createdBy = created_by;
	}

	public boolean isDefault() {
		return isDefault;
	}

	public void setDefault(boolean aDefault) {
		isDefault = aDefault;
	}

	public Character getStatus() {
		return status;
	}
	public void setStatus(Character status) {
		this.status = status;
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
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CustomerAddress that = (CustomerAddress) o;
		return id == that.id &&
				customerId == that.customerId &&
				cityId == that.cityId &&
				isDefault == that.isDefault &&
				Objects.equals(line1, that.line1) &&
				Objects.equals(line2, that.line2) &&
				Objects.equals(zipCode, that.zipCode) &&
				Objects.equals(created, that.created) &&
				Objects.equals(createdBy, that.createdBy) &&
				Objects.equals(status, that.status) &&
				Objects.equals(latitude, that.latitude) &&
				Objects.equals(longitude, that.longitude) &&
				Objects.equals(title, that.title) &&
				Objects.equals(mobile, that.mobile);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, customerId, line1, line2, cityId, zipCode, created, createdBy, status, latitude, longitude, title, mobile, isDefault);
	}
}
