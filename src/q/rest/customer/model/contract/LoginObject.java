package q.rest.customer.model.contract;

import java.io.Serializable;

public class LoginObject implements Serializable {
    private PublicCustomer customer;
    private String token;
    private long tokenExpire;



    public PublicCustomer getCustomer() {
        return customer;
    }

    public void setCustomer(PublicCustomer customer) {
        this.customer = customer;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getTokenExpire() {
        return tokenExpire;
    }

    public void setTokenExpire(long tokenExpire) {
        this.tokenExpire = tokenExpire;
    }
}
