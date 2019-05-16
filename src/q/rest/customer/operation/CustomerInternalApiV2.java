package q.rest.customer.operation;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cliftonlabs.json_simple.JsonObject;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import q.rest.customer.dao.DAO;
import q.rest.customer.filter.SecuredCustomer;
import q.rest.customer.filter.SecuredUser;
import q.rest.customer.helper.AppConstants;
import q.rest.customer.helper.Helper;
import q.rest.customer.model.contract.WireTransferEmailRequest;
import q.rest.customer.model.entity.*;

import javax.ejb.EJB;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.StringWriter;
import java.util.*;

@Path("/internal/api/v2")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CustomerInternalApiV2 {

    @EJB
    private DAO dao;

    @EJB
    private AsyncService async;

    @Context
    private ServletContext context;

    @SecuredUser
    @GET
    @Path("newest")
    public Response getNewestCustomers(){
        try{
            String sql = "select b from Customer b where id != :value0 order by created desc";
            List<Customer> customers = dao.getJPQLParamsOffsetMax(Customer.class, sql, 0, 20, 0L);
            return Response.status(200).entity(customers).build();
        }catch (Exception ex){
            return Response.status(500).build();
        }

    }

    @SecuredUser
    @GET
    @Path("search/{query}")
    public Response searchProduct(@PathParam(value = "query") String query){
        try {
            Long id = Helper.convertToLong(query);
            String lowered = "%"+ query.trim().toLowerCase() + "%";
            String sql = "select b from Customer b where b.id = :value0 and lower(b.email) like :value1 " +
                    "or lowert(b.firstName) like :value1 or lower(b.lastName) like :value1 or b.id in (" +
                    "select c.customerId from CustomerAddress where b.mobile like :value1)";
            List<Customer> customers = dao.getJPQLParamsOffsetMax(Customer.class, sql, 0, 20, id, lowered);
            return Response.status(200).entity(customers).build();
        }catch (Exception ex){
            return Response.status(500).build();
        }
    }

    @Path("customer-vehicle/vin")
    @SecuredUser
    @PUT
    public Response updateVin(CustomerVehicle customerVehicle){
        try{
            customerVehicle.setImageAttached(false);
            customerVehicle.setVin(customerVehicle.getVin().toUpperCase().trim());
            dao.update(customerVehicle);
            async.broadcastToNotification("noVins,"+async.getNoVinsCount());
            return Response.status(201).build();
        }catch (Exception ex){
            return Response.status(500).build();
        }
    }

    @Path("customer-vehicles/no-vin")
    @SecuredUser
    @GET
    public Response getIncompleteCustomerVehicles(){
        try{
            String sql = "select b from CustomerVehicle b where b.imageAttached =:value0 order by b.created asc";
            List<CustomerVehicle> customerVehicles = dao.getJPQLParams(CustomerVehicle.class, sql, true);
            List<Map> maps = new ArrayList<>();
            for(CustomerVehicle customerVehicle : customerVehicles){
                Map<String,Object> map = new HashMap<String,Object>();
                Customer customer = dao.find(Customer.class, customerVehicle.getCustomerId());
                map.put("customer", customer);
                map.put("customerVehicle", customerVehicle);
                maps.add(map);
            }
            return Response.status(200).entity(maps).build();
        }
        catch (Exception ex){
            return Response.status(500).build();
        }

    }



    @POST
    @Path("match-token/ws")
    public Response matchTokenWs(Map<String, Object> map) {
        try {
            String token = ((String) map.get("token"));
            Long customerId = ((Number) map.get("customerId")).longValue();
            String jpql = "select b from AccessToken b where b.customerId = :value0 and b.status = :value1 and b.token = :value2 and b.expire > :value3";
            List<AccessToken> l = dao.getJPQLParams(AccessToken.class, jpql, customerId, 'A', token, new Date());
            if (!l.isEmpty()) {
                return Response.status(200).build();
            } else {
                throw new Exception();
            }
        }catch(Exception ex) {
            return Response.status(403).build();// unauthorized
        }
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
        List<EmailSent> emails = dao.getCondition(EmailSent.class, "customerId", customer.getId());
        customer.setAddresses(addresses);
        customer.setVehicles(vehicles);
        customer.setEmailsSent(emails);
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


    @SecuredCustomer
    @POST
    @Path("email/wire-transfer")
    public Response sendWireTransferEmail(WireTransferEmailRequest wire){
        try{
            Map<String,Object> vmap = new HashMap<>();
            Customer customer = dao.find(Customer.class, wire.getCustomerId());
            vmap.put("firstName", customer.getFirstName());
            vmap.put("orderLink", "https://www.q.parts");
            vmap.put("cartId", wire.getCartId());
            vmap.put("wireTransferId", wire.getWireTransferId());
            vmap.put("amount", Helper.round(wire.getAmount(), 2));
            List<Map> banks = new ArrayList<>();
            for(Map<String,Object> map : wire.getBanks()){
                String account = (String) map.get("account");
                String owner = (String) map.get("owner");
                String iban = (String) map.get("iban");
                String name = (String) map.get("name");
                String nameAr = (String) map.get("nameAr");

                Map<String,Object> bankMap = new HashMap<>();
                bankMap.put("name", name);
                bankMap.put("nameAr", nameAr);
                bankMap.put("accountNo", account);
                bankMap.put("accountName", owner);
                bankMap.put("iban", iban);
                banks.add(bankMap);
            }
            vmap.put("banks", banks);
            String body = getHtmlTemplate(AppConstants.WIRE_TRANSFER_EMAIL_TEMPLATE, vmap);

            EmailSent emailSent = new EmailSent();
            emailSent.setEmail(customer.getEmail());
            emailSent.setPurpose("Wire Transfer");
            emailSent.setCreatedBy(0);
            emailSent.setCartId(wire.getCartId());
            emailSent.setCustomerId(customer.getId());
            emailSent.setWireId(wire.getWireTransferId());
            async.sendHtmlEmail(emailSent, customer.getEmail(), AppConstants.getWireTransferRequestEmailSubject(wire.getCartId()), body);
            return Response.status(200).build();
        }catch (Exception ex){
            return Response.status(500).build();
        }
    }

    @SecuredUser
    @Path("address")
    @POST
    public Response createAddress(CustomerAddress address){
        try{
            address.setCreated(new Date());
            dao.persist(address);
            return Response.status(201).build();
        }catch (Exception ex){
            return Response.status(500).build();
        }
    }


    @SecuredUser
    @Path("email/quotation-ready")
    @POST
    public Response sendQuotationReadyEmail(Map<String,Object> map){
        try{
            long quotationId = ((Number) map.get("quotationId")).longValue();
            long customerId = ((Number) map.get("customerId")).longValue();
            Customer customer = dao.find(Customer.class, customerId);
            String code = generateCodeLogin(customerId);
            String quotationLink= AppConstants.getQuotationReadyLink(quotationId, customer.getEmail(), code);
            String firstName = customer.getFirstName();
            Map<String,Object> vmap = new HashMap<>();
            vmap.put("quotationLink", quotationLink);
            vmap.put("firstName", firstName);
            vmap.put("quotationId", quotationId);
            String body = getHtmlTemplate(AppConstants.QUOTATION_READY_EMAIL_TEMPLATE, vmap);

            EmailSent emailSent = new EmailSent();
            emailSent.setEmail(customer.getEmail());
            emailSent.setPurpose("Quotation Ready");
            emailSent.setCreatedBy(0);
            emailSent.setCustomerId(customer.getId());
            emailSent.setQuotationId(quotationId);

            async.sendHtmlEmail(emailSent, customer.getEmail(), AppConstants.getQuotationReadyEmailSubject(quotationId), body);
            String title = "Quotation #" + quotationId + " is completed! - ";
            title += " تم الإنتهاء من التعسيرة رقم " + quotationId;
            Map<String,Object> nmap= new HashMap<String, Object>();
            nmap.put("purpose", "quotationComplete");
            nmap.put("url", quotationLink);
            nmap.put("title", title);
            String objectMapper = new ObjectMapper().writeValueAsString(nmap);
            async.sendToCusotmerNotification(objectMapper);
            return Response.status(200).build();
        }catch(Exception ex){
            return Response.status(500).build();
        }
    }


    private String generateCodeLogin(long customerId) {
        CodeLogin cl = new CodeLogin();
        boolean available = false;
        String code = "";
        do {
            code = Helper.getRandomSaltString(20);
            String jpql = "select b from CodeLogin b where b.code = :value0 and b.expire >= :value1";
            List<CodeLogin> l = dao.getJPQLParams(CodeLogin.class, jpql, code, new Date());
            if (l.isEmpty()) {
                available = true;
            }
        } while (!available);
        cl.setCode(code);
        cl.setCustomerId(customerId);
        cl.setCreated(new Date());
        cl.setExpire(Helper.addMinutes(cl.getCreated(), 60 * 24 * 5));
        dao.persist(cl);
        return code;
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




    public String getHtmlTemplate(String templateName, Map<String,Object> map){
        Properties p = new Properties();
        p.setProperty("resource.loader", "webapp");
        p.setProperty("webapp.resource.loader.class", "org.apache.velocity.tools.view.WebappResourceLoader");
        p.setProperty("webapp.resource.loader.path", "/WEB-INF/velocity/");
        VelocityEngine engine = new VelocityEngine(p);
        engine.setApplicationAttribute("javax.servlet.ServletContext", context);
        engine.init();
        Template template = engine.getTemplate(templateName);
        VelocityContext velocityContext = new VelocityContext();
        map.forEach((k,v) -> velocityContext.put(k,v));
        StringWriter writer = new StringWriter();
        template.merge(velocityContext, writer);
        return writer.toString();
    }
}
