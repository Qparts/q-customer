package q.rest.customer.model.contract;

import java.util.Map;

import q.rest.customer.model.entity.CustomerVehicle;

public class PublicVehicle {
	private long id;
	private Integer vehicleYearId;
	private long customerId;
	private boolean defaultVehicle;
	private String vin;
	private Map<String,Object> vehicle;
	
	public PublicVehicle() {
		
	}
	
	public PublicVehicle(CustomerVehicle cv) {
		this.id = cv.getId();
		this.vehicleYearId = cv.getVehicleYearId();
		this.customerId = cv.getCustomerId();
		this.vin = cv.getVin();
		this.defaultVehicle = cv.isDefaultVehicle();
	}
	
	public Map<String, Object> getVehicle() {
		return vehicle;
	}

	public void setVehicle(Map<String, Object> vehicle) {
		this.vehicle = vehicle;
	}

	public boolean isDefaultVehicle() {
		return defaultVehicle;
	}

	public void setDefaultVehicle(boolean defaultVehicle) {
		this.defaultVehicle = defaultVehicle;
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
