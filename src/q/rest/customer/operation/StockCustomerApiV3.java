package q.rest.customer.operation;

import q.rest.customer.dao.DAO;
import q.rest.customer.filter.annotation.InternalApp;
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
        String sql = "select b from StockCustomer b " +
                " where b.companyId = :value0 " +
                " and (" +
                " b.id =:value1 " +
                " or lower(b.name) like :value2 " +
                " or b.email like :value2 " +
                " or b.phone like :value2 " +
                " or b.code = :value1)";
        List<StockCustomer> customers = dao.getJPQLParams(StockCustomer.class, sql, Helper.getCompanyFromJWT(header), id, nameLike);
        return Response.status(200).entity(customers).build();
    }

    @SubscriberJwt
    @POST
    @Path("search-customer-ids")
    public Response searchCustomerIds(@HeaderParam(HttpHeaders.AUTHORIZATION) String header, Map<String,String> map){
        String nameLike = "%"+map.get("query").toLowerCase() + "%";
        int id = Helper.convertToInteger(map.get("query"));
        String sql = "select b.id from StockCustomer b " +
                " where b.companyId = :value0" +
                " and (" +
                " b.id =:value1" +
                " or lower(b.name) like :value2" +
                " or b.email like :value2" +
                " or b.phone like :value2" +
                " or b.code =:value1)";
        List<Integer> customerIds = dao.getJPQLParams(Integer.class, sql, Helper.getCompanyFromJWT(header), id, nameLike);
        Map<String,Object> newMap = new HashMap<>();
        newMap.put("customerIds", customerIds);
        return Response.status(200).entity(newMap).build();
    }

    @SubscriberJwt
    @POST
    @Path("search-supplier")
    public Response searchSupplier(@HeaderParam(HttpHeaders.AUTHORIZATION) String header, Map<String,String> map){
        String nameLike = "%"+map.get("query").toLowerCase() + "%";
        int id = Helper.convertToInteger(map.get("query"));
        //to search code
        String sql = "select b from StockSupplier b " +
                " where b.companyId = :value0 " +
                " and (" +
                " b.id = :value1 " +
                " or lower(b.name) like :value2 " +
                " or b.email like :value2 " +
                " or b.phone like :value2 " +
                " or b.code =:value1)";
        List<StockSupplier> suppliers = dao.getJPQLParams(StockSupplier.class, sql, Helper.getCompanyFromJWT(header), id,  nameLike);
        return Response.status(200).entity(suppliers).build();
    }

    @SubscriberJwt
    @POST
    @Path("search-supplier-ids")
    public Response searchSupplierIds(@HeaderParam(HttpHeaders.AUTHORIZATION) String header, Map<String,String> map){
        String nameLike = "%"+map.get("query").toLowerCase() + "%";
        int id = Helper.convertToInteger(map.get("query"));
        String sql = "select b.id from StockSupplier b " +
                " where b.companyId = :value0" +
                " and (" +
                " b.id = :value1" +
                " or lower(b.name) like :value2" +
                " or b.email like :value2" +
                " or b.phone like :value2" +
                " or b.code = :value1)";
        List<Integer> suppliersIds = dao.getJPQLParams(Integer.class, sql, Helper.getCompanyFromJWT(header), id,  nameLike);
        Map<String,Object> newMap = new HashMap<>();
        newMap.put("supplierIds", suppliersIds);
        return Response.status(200).entity(newMap).build();
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
    @PUT
    @Path("customer")
    public Response updateCustomer(@HeaderParam(HttpHeaders.AUTHORIZATION) String header, StockCustomer customer){
        int companyId = Helper.getCompanyFromJWT(header);
        String sql = "select b from StockCustomer b where b.companyId = :value0 and b.id = :value1";
        StockCustomer original = dao.findJPQLParams(StockCustomer.class, sql, companyId, customer.getId());
        if(original == null)
            return Response.status(409).build();

        original.setName(customer.getName());
        original.setEmail(customer.getEmail());
        original.setPhone(customer.getPhone());
        original.setAddress(customer.getAddress());
        original.setNote(customer.getNote());
        dao.update(original);
        return Response.status(200).entity(original).build();
    }


    @SubscriberJwt
    @PUT
    @Path("supplier")
    public Response updateSupplier(@HeaderParam(HttpHeaders.AUTHORIZATION) String header, StockSupplier supplier){
        int companyId = Helper.getCompanyFromJWT(header);
        String sql = "select b from StockSupplier b where b.companyId = :value0 and b.id = :value1";
        StockSupplier original = dao.findJPQLParams(StockSupplier.class, sql, companyId, supplier.getId());
        if(original == null)
            return Response.status(409).build();

        original.setName(supplier.getName());
        original.setEmail(supplier.getEmail());
        original.setPhone(supplier.getPhone());
        original.setAddress(supplier.getAddress());
        original.setNote(supplier.getNote());
        dao.update(original);
        return Response.status(200).entity(original).build();
    }

    @SubscriberJwt
    @POST
    @Path("customer")
    public Response createCustomer(@HeaderParam(HttpHeaders.AUTHORIZATION) String header, StockCustomer customer){
        int companyId = Helper.getCompanyFromJWT(header);
        String sql = "select b from StockCustomer b where b.companyId = :value0 and b.phone = :value1";
        List<StockCustomer> check = dao.getJPQLParams(StockCustomer.class, sql, companyId, customer.getPhone());
        if(!check.isEmpty())
            return Response.status(409).build();

        sql = "select b from StockCustomer b where b.companyId = :value0 order by id desc";
        List<StockCustomer> lastList = dao.getJPQLParamsOffsetMax(StockCustomer.class, sql, 0, 1, companyId);
        int code = 1000;
        if(!lastList.isEmpty())
            code = lastList.get(0).getCode() + 1;
        customer.setCode(code);
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

    @SubscriberJwt
    @POST
    @Path("supplier")
    public Response createSupplier(@HeaderParam(HttpHeaders.AUTHORIZATION) String header, StockSupplier supplier){
        int companyId = Helper.getCompanyFromJWT(header);
        String sql = "select b from StockSupplier b where b.companyId = :value0 and b.phone = :value1";
        List<StockSupplier> check = dao.getJPQLParams(StockSupplier.class, sql, companyId, supplier.getPhone());
        if(!check.isEmpty()) return Response.status(409).build();
        sql = "select b from StockSupplier b where b.companyId = :value0 order by id desc";
        List<StockSupplier> lastList = dao.getJPQLParamsOffsetMax(StockSupplier.class, sql, 0, 1, companyId);
        int code = 1001;
        if(!lastList.isEmpty())
            code = lastList.get(0).getCode() + 1;
        supplier.setCompanyId(Helper.getCompanyFromJWT(header));
        supplier.setCreated(new Date());
        supplier.setEmail(supplier.getEmail().toLowerCase());
        supplier.setStatus('A');
        supplier.setCode(code);
        dao.persist(supplier);
        return Response.status(201).build();
    }

    @InternalApp
    @POST
    @Path("default-cash-customer")
    public Response createDefaultCustomer(Map<String,Integer> map){
        int companyId = map.get("companyId");
        int countryId = map.get("countryId");
        StockCustomer customer  = new StockCustomer();
        customer.setStatus('A');
        customer.setCreated(new Date());
        customer.setCompanyId(companyId);
        customer.setCode(1000);
        customer.setCountryId(countryId);
        customer.setName("Cash Customer");
        dao.persist(customer);
        Map<String,Integer> mp2 = new HashMap<>();
        mp2.put("customerId", customer.getId());
        return Response.status(200).entity(mp2).build();
    }




    public <T> Response postSecuredRequest(String link, T t, String authHeader) {
        Invocation.Builder b = ClientBuilder.newClient().target(link).request();
        b.header(HttpHeaders.AUTHORIZATION, authHeader);
        Response r = b.post(Entity.entity(t, "application/json"));// not secured
        return r;
    }



}
