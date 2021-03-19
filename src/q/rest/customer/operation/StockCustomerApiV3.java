package q.rest.customer.operation;

import q.rest.customer.dao.DAO;
import q.rest.customer.filter.annotation.SubscriberJwt;
import q.rest.customer.helper.AppConstants;
import q.rest.customer.helper.Helper;
import q.rest.customer.model.entity.stock.StockCustomer;
import q.rest.customer.model.entity.stock.StockSupplier;

import javax.ejb.EJB;
import javax.ws.rs.*;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/api/v3/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class StockCustomerApiV3 {

    @EJB
    private DAO dao;

    @SubscriberJwt
    @POST
    @Path("search-customer")
    public Response searchCustomer(@HeaderParam(HttpHeaders.AUTHORIZATION) String header, Map<String,String> map){
        String nameLike = "%"+map.get("query").toLowerCase() + "%";
        int id = Helper.convertToInteger(map.get("query"));
        String sql = "select b from StockCustomer b where b.companyId = :value0 and (b.id =:value1 or lower(b.name) like :value2 or b.email like :value2 or b.phone like :value2)";
        List<StockCustomer> customers = dao.getJPQLParams(StockCustomer.class, sql, Helper.getCompanyFromJWT(header), id, nameLike);
        return Response.status(200).entity(customers).build();
    }

    @SubscriberJwt
    @POST
    @Path("search-supplier")
    public Response searchSupplier(@HeaderParam(HttpHeaders.AUTHORIZATION) String header, Map<String,String> map){
        String nameLike = "%"+map.get("query").toLowerCase() + "%";
        int id = Helper.convertToInteger(map.get("query"));
        String sql = "select b from StockSupplier b where b.companyId = :value0 and (b.id = :value1 or lower(b.name) like :value2 or b.email like :value2 or b.phone like :value2)";
        List<StockSupplier> suppliers = dao.getJPQLParams(StockSupplier.class, sql, Helper.getCompanyFromJWT(header), id,  nameLike);
        return Response.status(200).entity(suppliers).build();
    }

    @SubscriberJwt
    @GET
    @Path("suppliers")
    public Response getAllSuppliers(@HeaderParam(HttpHeaders.AUTHORIZATION) String header){
        int companyId = Helper.getCompanyFromJWT(header);
        String sql = "select b from StockSupplier b where b.companyId =:value0 order by b.id";
        List<StockSupplier> suppliers = dao.getJPQLParams(StockSupplier.class, sql, companyId);
        return Response.status(200).entity(suppliers).build();
    }

    @SubscriberJwt
    @GET
    @Path("customers")
    public Response getAllCustomers(@HeaderParam(HttpHeaders.AUTHORIZATION) String header){
        int companyId = Helper.getCompanyFromJWT(header);
        String sql = "select b from StockCustomer b where b.companyId =:value0 order by b.id";
        List<StockCustomer> customers = dao.getJPQLParams(StockCustomer.class, sql, companyId);
        return Response.status(200).entity(customers).build();
    }

    @SubscriberJwt
    @GET
    @Path("customer/{id}")
    public Response getCustomer(@HeaderParam(HttpHeaders.AUTHORIZATION) String header, @PathParam(value = "id") int id){
        int companyId = Helper.getCompanyFromJWT(header);
        StockCustomer customer = dao.findTwoConditions(StockCustomer.class,"companyId", "id", companyId, id);
        return Response.status(200).entity(customer).build();
    }


    @GET
    @Path("customers/{ids}")
    @SubscriberJwt
    public Response getCustomersInBulk(@HeaderParam(HttpHeaders.AUTHORIZATION) String header, @PathParam(value = "ids") String ids) {
        String[] idsArray = ids.split(",");
        StringBuilder sql = new StringBuilder("select * from cst_stk_customer where company_id = "+ Helper.getCompanyFromJWT(header)+ " and id in (0");
        for (String s : idsArray) {
            sql.append(",").append(s);
        }
        sql.append(")");
        List<StockCustomer> customers = dao.getNative(StockCustomer.class, sql.toString());
        return Response.status(200).entity(customers).build();
    }

    @GET
    @Path("suppliers/{ids}")
    @SubscriberJwt
    public Response getSuppliersInBulk(@HeaderParam(HttpHeaders.AUTHORIZATION) String header, @PathParam(value = "ids") String ids) {
        String[] idsArray = ids.split(",");
        StringBuilder sql = new StringBuilder("select * from cst_stk_supplier where company_id = "+ Helper.getCompanyFromJWT(header)+ " and id in (0");
        for (String s : idsArray) {
            sql.append(",").append(s);
        }
        sql.append(")");
        List<StockSupplier> suppliers = dao.getNative(StockSupplier.class, sql.toString());
        return Response.status(200).entity(suppliers).build();
    }


    @SubscriberJwt
    @GET
    @Path("supplier/{id}")
    public Response getSupplier(@HeaderParam(HttpHeaders.AUTHORIZATION) String header, @PathParam(value = "id") int id){
        int companyId = Helper.getCompanyFromJWT(header);
        StockSupplier supplier = dao.findTwoConditions(StockSupplier.class,"companyId", "id", companyId, id);
        return Response.status(200).entity(supplier).build();
    }


    @SubscriberJwt
    @POST
    @Path("customer")
    public Response createCustomer(@HeaderParam(HttpHeaders.AUTHORIZATION) String header, StockCustomer customer){
        int companyId = Helper.getCompanyFromJWT(header);
        String sql = "select b from StockCustomer b where b.companyId = :value0 and b.code =:value1";
        List<StockCustomer> check = dao.getJPQLParams(StockCustomer.class, sql, companyId, customer.getCode());
        if(!check.isEmpty())
            return Response.status(409).build();
        customer.setCompanyId(Helper.getCompanyFromJWT(header));
        customer.setCreated(new Date());
        customer.setEmail(customer.getEmail().toLowerCase());
        customer.setStatus('A');
        dao.persist(customer);
        List<StockCustomer> customers = dao.getCondition(StockCustomer.class, "companyId", companyId);
        if(customers.size() == 1) {
            Map<String,Integer> map = new HashMap<>();
            map.put("customerId", customer.getId());
            Response r = this.postSecuredRequest(AppConstants.POST_DEFAULT_CUSTOMER, map, header);
            r.close();
        }
        return Response.status(200).build();
    }


    public <T> Response postSecuredRequest(String link, T t, String authHeader) {
        Invocation.Builder b = ClientBuilder.newClient().target(link).request();
        b.header(HttpHeaders.AUTHORIZATION, authHeader);
        Response r = b.post(Entity.entity(t, "application/json"));// not secured
        return r;
    }




    @SubscriberJwt
    @POST
    @Path("supplier")
    public Response createSupplier(@HeaderParam(HttpHeaders.AUTHORIZATION) String header, StockSupplier supplier){
        int companyId = Helper.getCompanyFromJWT(header);
        String sql = "select b from StockSupplier b where b.companyId = :value0 and (b.phone = :value1 or b.email =:value2)";
        List<StockSupplier> check = dao.getJPQLParams(StockSupplier.class, sql, companyId, supplier.getPhone(), supplier.getEmail());
        if(!check.isEmpty()) return Response.status(409).build();
        supplier.setCompanyId(Helper.getCompanyFromJWT(header));
        supplier.setCreated(new Date());
        supplier.setEmail(supplier.getEmail().toLowerCase());
        supplier.setStatus('A');
        dao.persist(supplier);
        return Response.status(201).build();
    }


}
