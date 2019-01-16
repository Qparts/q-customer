package q.rest.customer.model.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Table(name="cst_password_reset")
@Entity
public class PasswordReset implements Serializable {

    @Id
    @SequenceGenerator(name = "cst_password_reset_id_seq_gen", sequenceName = "cst_password_reset_id_seq", initialValue=1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cst_password_reset_id_seq_gen")
    @Column(name="id")
    private long id;
    @Column(name="customer_id")
    private long customerId;

    @Column(name="token")
    private String token;
    @Column(name="created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;
    @Column(name="expire")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expire;
    @Column(name="status")
    private char status;

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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getExpire() {
        return expire;
    }

    public void setExpire(Date expire) {
        this.expire = expire;
    }

    public char getStatus() {
        return status;
    }

    public void setStatus(char status) {
        this.status = status;
    }
}
