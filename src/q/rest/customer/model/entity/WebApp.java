package q.rest.customer.model.entity;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "cst_apps")
public class WebApp implements Serializable{
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "app_code", updatable=false)
    private int appCode;

    @Column(name = "app_name")
    private String appName;

    @Column(name = "app_secret")
    private String appSecret;

    @Column(name="active")
    private boolean active;


    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getAppCode() {
        return appCode;
    }

    public void setAppCode(int appCode) {
        this.appCode = appCode;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WebApp webApp = (WebApp) o;
        return appCode == webApp.appCode &&
                active == webApp.active &&
                Objects.equals(appName, webApp.appName) &&
                Objects.equals(appSecret, webApp.appSecret);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appCode, appName, appSecret, active);
    }
}