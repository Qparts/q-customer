package q.rest.customer.model.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Table(name = "cst_email_verification")
@Entity
public class EmailVerification implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @SequenceGenerator(name = "cst_email_verification_id_seq_gen", sequenceName = "cst_email_verification_id_seq", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cst_email_verification_id_seq_gen")
    @Column(name = "id")
    private long id;
    @Column(name = "customer_id")
    private long customerId;
    @Column(name = "email")
    private String email;
    @Column(name = "created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;
    @Column(name = "expire")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expire;
    @Column(name = "token")
    private String token;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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

}
