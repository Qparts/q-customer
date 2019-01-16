package q.rest.customer.model.contract;

import java.util.Map;

import q.rest.customer.model.entity.CustomerVehicle;

public class PublicVehicle {
	private long id;
	private Integer vehicleYearId;
	private long customerId;
	private boolean isDefault;
	private String vin;
	private Map<String,Object> vehicle;
	
	public PublicVehicle() {
		
	}
	
	public PublicVehicle(CustomerVehicle cv) {
		this.id = cv.getId();
		this.vehicleYearId = cv.getVehicleYearId();
		this.customerId = cv.getCustomerId();
		this.vin = cv.getVin();
		this.isDefault = cv.isDefault();
	}
	
	public Map<String, Object> getVehicle() {
		return vehicle;
	}

	public void setVehicle(Map<String, Object> vehicle) {
		this.vehicle = vehicle;
	}

	public boolean isDefault() {
		return isDefault;
	}

	public void setDefault(boolean aDefault) {
		isDefault = aDefault;
	}

	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public Integer getVehicleYearId() {
		return vehicleYearId;
	}
	public void setVehicleYearId(Integer vehicleId) {
		this.vehicleYearId = vehicleId;
	}
	public long getCustomerId() {
		return customerId;
	}
	public void setCustomerId(long customerId) {
		this.customerId = customerId;
	}
	public String getVin() {
		return vin;
	}
	public void setVin(String vin) {
		this.vin = vin;
	}
	
	
}
