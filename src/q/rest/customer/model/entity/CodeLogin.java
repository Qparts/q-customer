package q.rest.customer.model.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "cst_code_login")
public class CodeLogin {
    private static final long serialVersionUID = 1L;
    @Id
    @SequenceGenerator(name = "cst_code_login_id_seq_gen", sequenceName = "cst_code_login_id_seq", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cst_code_login_id_seq_gen")
    @Column(name = "id", updatable = false)
    private long id;
    @Column(name = "customer_id")
    private long customerId;
    @Column(name="code")
    private String code;
    @Column(name="created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;
    @Column(name="expire")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expire;

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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
