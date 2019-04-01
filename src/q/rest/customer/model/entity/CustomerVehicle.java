package q.rest.customer.model.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Table(name = "cst_vehicles")
@Entity
public class CustomerVehicle implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @SequenceGenerator(name = "cst_vehicles_id_seq_gen", sequenceName = "cst_vehicles_id_seq", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cst_vehicles_id_seq_gen")
    @Column(name = "id")
    private long id;
    @Column(name = "customer_id")
    private long customerId;
    @Column(name = "vehicle_year_id")
    private Integer vehicleYearId;
    @Column(name = "vin")
    private String vin;
    @Column(name = "created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;
    @Column(name = "created_by")
    private Integer createdBy;
    @Column(name = "status")
    private char status;
    @Column(name = "is_default")
    private boolean defaultVehicle;
    @Column(name = "image_attached")
    private boolean imageAttached;

    public boolean isImageAttached() {
        return imageAttached;
    }

    public void setImageAttached(boolean imageAttached) {
        this.imageAttached = imageAttached;
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

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public Integer getVehicleYearId() {
        return vehicleYearId;
    }

    public void setVehicleYearId(Integer vehicleYearId) {
        this.vehicleYearId = vehicleYearId;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
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

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public char getStatus() {
        return status;
    }

    public void setStatus(char status) {
        this.status = status;
    }


}
