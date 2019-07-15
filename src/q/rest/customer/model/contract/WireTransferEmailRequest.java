package q.rest.customer.model.contract;

import java.util.List;
import java.util.Map;

public class WireTransferEmailRequest {

    private long customerId;
    private long cartId;
    private long quotationId;
    private String purpose;
    private long wireTransferId;
    private double amount;
    private List<Map<String,Object>> banks;


    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public long getCartId() {
        return cartId;
    }

    public void setCartId(long cartId) {
        this.cartId = cartId;
    }

    public long getWireTransferId() {
        return wireTransferId;
    }

    public void setWireTransferId(long wireTransferId) {
        this.wireTransferId = wireTransferId;
    }

    public List<Map<String,Object>> getBanks() {
        return banks;
    }

    public void setBanks(List<Map<String,Object>> banks) {
        this.banks = banks;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public long getQuotationId() {
        return quotationId;
    }

    public void setQuotationId(long quotationId) {
        this.quotationId = quotationId;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
}
