package q.rest.customer.operation;

import q.rest.customer.dao.DAO;
import q.rest.customer.filter.SecuredCustomer;
import q.rest.customer.filter.SecuredUser;
import q.rest.customer.filter.ValidApp;
import q.rest.customer.model.entity.*;

import javax.ejb.EJB;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Path("/internal/api/v2")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CustomerInternalApiV2 {

    @EJB
    private DAO dao;

    @Path("test")
    @GET
    public String test(){
        return "test internal";
    }

    @Path("match-token")
    @SecuredCustomer
    @POST
    public Response matchToken(Map<String,String> map){
        try{
            String appSecret = map.get("appSecret");
            String token = map.get("token");
            Long customerId = Long.parseLong(map.get("username"));
            WebApp webApp = getWebAppFromSecret(appSecret);
            String sql = "select b from AccessToken b where b.customerId = :value0 and b.webApp = :value1 " +
                    "and b.status =:value2 and b.token =:value3 and b.expire > :value4";
            List<AccessToken> accessTokenList = dao.getJPQLParams(AccessToken.class, sql, customerId, webApp, 'A', token, new Date());
            if(accessTokenList.isEmpty()){
                throw new NotAuthorizedException("Request authorization failed");
            }
            return Response.status(200).build();
        }catch(Exception ex){
            return Response.status(401).build();// unauthorized
        }
    }

    @SecuredUser
    @POST
    @Path("customers-from-ids")
    public Response getCustomerFromIds(long[] cids) {
        try {
            String sql = "select * from cst_customer where id in (0";
            for (int i = 0; i < cids.length; i++) {
                sql = sql + "," + cids[i];
            }
            sql = sql + ")";
            List<Customer> customers = dao.getNative(Customer.class, sql);
            for(Customer customer : customers){
                this.prepareCustomer(customer);
            }
            return Response.status(200).entity(customers).build();
        } catch (Exception ex) {
            return Response.status(500).build();
        }
    }

    @SecuredUser
    @GET
    @Path("customer/{customerId}")
    public Response getCustomer(@PathParam(value = "customerId") long customerId){
        try{
            Customer customer = dao.find(Customer.class, customerId);
            if(customer == null){
                return Response.status(404).build();
            }
            this.prepareCustomer(customer);
            return Response.status(200).entity(customer).build();
        }catch (Exception ex){
            return Response.status(500).build();
        }

    }

    private void prepareCustomer(Customer customer){
        List<CustomerAddress> addresses = dao.getCondition(CustomerAddress.class, "customerId", customer.getId());
        List<CustomerVehicle> vehicles = dao.getCondition(CustomerVehicle.class, "customerId", customer.getId());
        customer.setAddresses(addresses);
        customer.setVehicles(vehicles);
    }



    @SecuredCustomer
    @Path("valid-customer/{customerId}")
    @GET
    public Response isValidCustomerOperation(@HeaderParam("Authorization") String header, @PathParam("customerId") long customerId) {
        try{
            Customer check = getCustomerFromAuthHeader(header);
            if(check.getId() != customerId) {
                return Response.status(401).build();
            }
            return Response.status(204).build(); }
        catch (Exception ex){
            return Response.status(500).build();
        }
    }

    private Customer getCustomerFromAuthHeader(String authHeader) {
        try {
            String[] values = authHeader.split("&&");
            String username = values[1].trim();
            Customer c = dao.find(Customer.class, Long.parseLong(username));
            return c;
        } catch (Exception ex) {
            return null;
        }
    }


    // retrieves app object from app secret
    private WebApp getWebAppFromSecret(String secret) throws NotAuthorizedException {
        // verify web app secret
        WebApp webApp = dao.findTwoConditions(WebApp.class, "appSecret", "active", secret, true);
        if (webApp == null) {
            throw new NotAuthorizedException("Unauthorized Access");
        }
        return webApp;
    }
}
