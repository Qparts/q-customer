package q.rest.customer.operation;

import q.rest.customer.dao.DAO;
import q.rest.customer.filter.SecuredCustomer;
import q.rest.customer.filter.SecuredUser;
import q.rest.customer.filter.ValidApp;
import q.rest.customer.helper.AppConstants;
import q.rest.customer.helper.Helper;
import q.rest.customer.model.contract.*;
import q.rest.customer.model.entity.*;

import javax.ejb.EJB;
import javax.ws.rs.*;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

@Path("/api/v2/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CustomerApiV2 {

    @EJB
    private DAO dao;

    @EJB
    private AsyncService async;


    @ValidApp
    @POST
    @Path("logout")
    public Response logout(@HeaderParam("Authorization") String header, long customerId ){
        try{
            WebApp app = this.getWebAppFromAuthHeader(header);
            deactivateOldTokens(customerId, app);
            return Response.status(200).build();
        }catch (Exception ex){
            return getServerErrorResponse();
        }
    }

    @ValidApp
    @POST
    @Path("signup")
    public Response signup(@HeaderParam("Authorization")String authHeader, SignupRequestModel signupModel){
        try{
            Customer check = dao.findCondition(Customer.class, "email", signupModel.getEmail());

            if(null != check){
                return Response.status(409).entity("email already exists").build();
            }

            Customer customer = new Customer();
            customer.setEmail(signupModel.getEmail());
            customer.setCountryId(signupModel.getCountryId());
            customer.setCreated(new Date());
            customer.setCreatedBy(0);
            customer.setDefaultLang(signupModel.getDefaultLang());
            customer.setFirstName(signupModel.getFirstName());
            customer.setLastName(signupModel.getLastName());
            customer.setPassword(Helper.cypher(signupModel.getPassword()));
            customer.setSmsActive(false);
            customer.setNewsletterActive(true);
            customer.setStatus('I');//inactive
            dao.persist(customer);

            String code = this.createVerificationObject(customer.getEmail(), customer.getId());
            String body = Helper.prepareHtmlActivationEmail(AppConstants.getActivationLink(code, customer.getEmail()));
            async.sendHtmlEmail(customer.getEmail(), AppConstants.ACCOUNT_ACTIVATION_EMAIL_SUBJECT, body);
            //send back login object
            Map<String,Object> map = this.getLoginObject(authHeader, customer, this.getWebAppFromAuthHeader(authHeader));
            return Response.status(202).entity(map).build();
        }catch(Exception ex){
            return getServerErrorResponse();
        }
    }

    @ValidApp
    @POST
    @Path("account-verify")
    public Response verifyAccount(@HeaderParam("Authorization") String authHeader, AccountVerifyRequestModel avrModel){
        try{
            WebApp webApp = this.getWebAppFromAuthHeader(authHeader);
            String sql = "select b from EmailVerification b where b.token = :value0 " +
                    " and b.email = :value1";
            EmailVerification ev = dao.findJPQLParams(EmailVerification.class, sql, avrModel.getCode(), avrModel.getEmail());
            if(ev == null){
                return getResourceNotFoundResponse();
            }
            if(ev.getExpire().before(new Date())){
                dao.delete(ev);
                //delete verification
                return Response.status(410).entity("Resource gone and no longer available").build();
            }
            Customer c = dao.find(Customer.class, ev.getCustomerId());
            c.setStatus('V');//verified
            dao.update(c);
            dao.delete(ev);
            return getSuccessResponseWithLogin(authHeader, c , webApp);
        }catch(Exception ex){
            return getServerErrorResponse();
        }
    }

    @ValidApp
    @POST
    @Path("login")
    public Response login(@HeaderParam("Authorization") String authHeader, CredentialsModel cModel){
        try {
            WebApp webApp = this.getWebAppFromAuthHeader(authHeader);
            Customer customer = dao.findTwoConditions(Customer.class, "email", "password", cModel.getEmail(), Helper.cypher(cModel.getPassword()));
            if (customer == null) {
                return getResourceNotFoundResponse("Invalid credentials");
            }
            return getSuccessResponseWithLogin(authHeader, customer, webApp);
        }catch(Exception ex){
            return getServerErrorResponse();
        }
    }


    @ValidApp
    @POST
    @Path("reset-password")
    public Response resetPassword(@HeaderParam("Authorization") String authHeader, CredentialsModel cModel){
        try{
            String email = cModel.getEmail();
            Customer customer = dao.findCondition(Customer.class, "email", email);
            if(customer != null){
                String code = createPasswordResetObject(customer.getId());
                String body = Helper.prepareHtmlResetPasswordEmail(AppConstants.getActivationLink(code, email), customer.getFirstName());
                async.sendHtmlEmail(email, AppConstants.RESET_PASSWORD_EMAIL_SUBJECT, body);
            }
            return Response.status(200).build();
        } catch (Exception ex){
            return getServerErrorResponse();
        }
    }

    @ValidApp
    @GET
    @Path("reset-password/token/{token-value}")
    public Response checkResetPassword(@HeaderParam("Authorization") String authHeader, @PathParam("token-value") String tokenValue){
        try{
            //check if this token value exists!
            String sql = "select b from PasswordReset b where b.token =:value0 and b.status = :value1";
            PasswordReset passwordReset = dao.findJPQLParams(PasswordReset.class, sql, tokenValue, 'A');
            //if it does not exist
            if(null == passwordReset){
                return getResourceNotFoundResponse();
            }
            //if it is expired
            if(passwordReset.getExpire().before(new Date())){
                return Response.status(410).entity("Resource gone and no longer available").build();
            }
            //valid token
            return Response.status(201).build();
        }catch(Exception ex){
            return getServerErrorResponse();
        }
    }

    @ValidApp
    @PUT
    @Path("reset-password")
    public Response resetPasswordUpdate(@HeaderParam("Authorization") String authHeader, ResetPasswordRequestModel rpModel){
        try{
            rpModel.getToken();
            String sql = "select b from PasswordReset b where b.token = :value0 and b.expire > :value1 and b.status = :value2";
            PasswordReset pw = dao.findJPQLParams(PasswordReset.class, sql , rpModel.getToken(), new Date(), 'A');
            if(null == pw){
                return getResourceNotFoundResponse("Invalid token or email");
            }
            //token matched! update password
            Customer customer = dao.find(Customer.class, pw.getCustomerId());
            customer.setPassword(Helper.cypher(rpModel.getPassword()));
            if(customer.getStatus() == 'I'){
                String sql2 = "select b from EmailVerification b where b.customerId = :value0 and b.expire > :value1";
                EmailVerification ev = dao.findJPQLParams(EmailVerification.class, sql2, customer.getId(), new Date());
                dao.delete(ev);
                customer.setStatus('V');
            }
            dao.update(customer);
            sql = "select b from PasswordReset b where b.customerId =:value0 and b.status = :value1";
            List<PasswordReset> passwordResets = dao.getJPQLParams(PasswordReset.class, sql, pw.getCustomerId(), 'A');
            for(PasswordReset pr : passwordResets){
                pr.setStatus('I');
                dao.update(pr);
            }
            return getSuccessResponseWithLogin(authHeader, customer, getWebAppFromAuthHeader(authHeader));
        }catch(Exception ex){
            return getServerErrorResponse();
        }
    }

    @ValidApp
    @POST
    @Path("social-media-auth")
    public Response login(@HeaderParam("Authorization") String authHeader, SocialMediaCredentialModel smModel){
        try{
            if(smModel.getEmail() == null || smModel.getEmail().equals("")
                    || smModel.getSocialMediaId() == null || smModel.getSocialMediaId() == ""
                    || smModel.getPlatform() == null || smModel.getPlatform() == ""){
                return getBadRequestResponse("Incomplete information");
            }

            WebApp webApp = this.getWebAppFromAuthHeader(authHeader);
            if(this.socialMediaExists(smModel.getSocialMediaId(), smModel.getPlatform())){
                Customer customer = getCustomerFromSocialMedia(smModel.getPlatform(), smModel.getSocialMediaId());
                return getSuccessResponseWithLogin(authHeader, customer, webApp);
            }

            //check if email exists!
            Customer check = dao.findCondition(Customer.class, "email" , smModel.getEmail());
            if(check != null){
                this.createSocialMediaLink(check, smModel.getSocialMediaId(), smModel.getPlatform(), smModel.getEmail());
                return this.getSuccessResponseWithLogin(authHeader, check, webApp);
            }

            //email doesn't exist! sign him/her up and create sm link.
            Customer customer = new Customer();
            customer.setEmail(smModel.getEmail());
            customer.setFirstName(smModel.getFirstName());
            customer.setLastName(smModel.getLastName());
            customer.setSmsActive(false);
            customer.setNewsletterActive(true);
            customer.setDefaultLang(smModel.getDefaultLang());
            customer.setCreated(new Date());
            customer.setCreatedBy(0);
            customer.setCountryId(smModel.getCountryId());
            customer.setStatus('V');
            dao.persist(customer);
            createSocialMediaLink(customer, smModel.getSocialMediaId(), smModel.getPlatform(), smModel.getEmail());
            return this.getSuccessResponseWithLogin(authHeader, customer, webApp);
        }catch(Exception ex){
            return getServerErrorResponse();
        }
    }

    @SecuredCustomer
    @PUT
    @Path("customer")
    public Response updateCustomerInfo(@HeaderParam("Authorization") String header, PublicCustomer pc){
        try{
            if(!customerFound(pc.getId())) {
                return Response.status(404).build();
            }
            if(!validCustomerOperation(pc.getId(), header)) {
                return Response.status(401).build();
            }
            Customer c = dao.find(Customer.class, pc.getId());
            c.setFirstName(pc.getFirstName());
            c.setLastName(pc.getLastName());
            c.setDefaultLang(pc.getDefaultLang());
            dao.update(c);
            return Response.status(201).build();
        }catch (Exception ex){
            return getServerErrorResponse();
        }
    }


    @SecuredCustomer
    @POST
    @Path("vehicle")
    public Response addCustomerVehicle(@HeaderParam("Authorization") String header, PublicVehicle pvModel){
        try{
            if(!customerFound(pvModel.getCustomerId())) {
                return Response.status(404).build();
            }
            if(!validCustomerOperation(pvModel.getCustomerId(), header)) {
                return Response.status(401).build();
            }

            CustomerVehicle cv = new CustomerVehicle();
            cv.setCreated(new Date());
            cv.setStatus('A');
            cv.setCreatedBy(0);
            cv.setCustomerId(pvModel.getCustomerId());
            cv.setVehicleYearId(pvModel.getVehicleYearId());
            cv.setVin(pvModel.getVin());
            dao.persist(cv);
            if(pvModel.isDefaultVehicle()){
                makeVehicleDefault(cv.getCustomerId(), cv.getId());
            }
            pvModel.setId(cv.getId());
            pvModel.setVehicle(this.getVehicleFromId(header, pvModel.getVehicleYearId()));
            return Response.status(200).entity(pvModel).build();

        }catch(Exception ex){
            return getServerErrorResponse();
        }
    }

    private void makeVehicleDefault(long customerId, long vehicleId){
        try {
            List<CustomerVehicle> cvs = dao.getTwoConditions(CustomerVehicle.class, "customerId", "isDefault", customerId, true);
            for (CustomerVehicle customerVehicle : cvs) {
                customerVehicle.setDefaultVehicle(false);
                dao.update(customerVehicle);
            }
            CustomerVehicle cv = dao.find(CustomerVehicle.class, vehicleId);
            cv.setDefaultVehicle(true);
            dao.update(cv);
        }catch(Exception ignore){

        }
    }



    private void makeAddressDefault(long customerId, long vehicleId){
        try {
            List<CustomerAddress> cvs = dao.getTwoConditions(CustomerAddress.class, "customerId", "isDefault", customerId, true);
            for (CustomerAddress address : cvs) {
                address.setDefaultAddress(false);
                dao.update(address);
            }
            CustomerAddress cv = dao.find(CustomerAddress.class, vehicleId);
            cv.setDefaultAddress(true);
            dao.update(cv);
        }catch(Exception ignore){

        }
    }


    @SecuredCustomer
    @DELETE
    @Path("address/{addressId}")
    public Response archiveAddress(@HeaderParam("Authorization") String header, @PathParam(value = "addressId") long addressId){
        try{
            CustomerAddress address = dao.find(CustomerAddress.class, addressId);
            if(!customerFound(address.getCustomerId())) {
                return Response.status(404).build();
            }

            if(!validCustomerOperation(address.getCustomerId(), header)) {
                return Response.status(401).build();
            }

            address.setStatus('X');
            dao.update(address);
            return Response.status(201).build();

        }catch(Exception ex){
            return getServerErrorResponse();
        }
    }

    @SecuredCustomer
    @POST
    @Path("address")
    public Response addAddress(@HeaderParam("Authorization") String header, PublicAddress publicAddress){
        try{
            if(!customerFound(publicAddress.getCustomerId())) {
                return Response.status(404).build();
            }

            if(!validCustomerOperation(publicAddress.getCustomerId(), header)) {
                return Response.status(401).build();
            }

            CustomerAddress address= new CustomerAddress();
            address.setCityId(publicAddress.getCityId());
            address.setCreated(new Date());
            address.setCreatedBy(0);
            address.setCustomerId(publicAddress.getCustomerId());
            address.setLatitude(publicAddress.getLatitude());
            address.setLine1(publicAddress.getLine1());
            address.setLine2(publicAddress.getLine2());
            address.setLongitude(publicAddress.getLongitude());
            address.setStatus('A');
            address.setTitle(publicAddress.getTitle());
            address.setZipCode(publicAddress.getZipCode());
            address.setMobile(publicAddress.getMobile());
            dao.persist(address);
            if(address.isDefaultAddress()){
                this.makeAddressDefault(address.getCustomerId(), address.getId());
            }
            publicAddress.setId(address.getId());
            return Response.status(200).entity(publicAddress).build();
        } catch(Exception ex){
            return getServerErrorResponse();
        }

    }


    @SecuredCustomer
    @POST
    @Path("social-media")
    public Response addSocialMedia(@HeaderParam("Authorization") String header, AddSocialMediaRequestModel smModel){
        try {
            //check if this customer exists
            if (!customerFound(smModel.getCustomerId())) {
                return Response.status(404).build();
            }


            if (!validCustomerOperation(smModel.getCustomerId(), header)) {
                return Response.status(401).build();
            }

            //check if this social media exists
            if (socialMediaExists(smModel.getSocialMediaId(), smModel.getPlatform())) {
                return Response.status(409).build();
            }

            //check if a customer is registered with this email
            Customer check = dao.findCondition(Customer.class, "email", smModel.getEmail());
            if (check != null) {
                return Response.status(409).build();
            }

            Customer customer = dao.find(Customer.class, smModel.getCustomerId());
            createSocialMediaLink(customer, smModel.getSocialMediaId(), smModel.getPlatform(), smModel.getEmail());
            return Response.status(201).build();
        }catch(Exception ex){
            return getServerErrorResponse();
        }
    }

    @SecuredCustomer
    @PUT
    @Path("password")
    public Response updatePassword(@HeaderParam("Authorization") String header, PasswordUpdateModel pwModel){
        try{
            if(pwModel.getNewPassword() == null || pwModel.getOldPassword() == null || pwModel.getNewPassword().equals("") || pwModel.getOldPassword().equals("")){
                return getBadRequestResponse();
            }

            if(!customerFound(pwModel.getCustomerId())) {
                return Response.status(401).build();
            }

            if(!validCustomerOperation(pwModel.getCustomerId(), header)) {
                return Response.status(401).build();
            }

            String sql = "select b from Customer b where b.id = :value0 and b.password = :value1";
            Customer customer = dao.findJPQLParams(Customer.class, sql, pwModel.getCustomerId(), Helper.cypher(pwModel.getOldPassword()));
            if(customer == null){
                Map<String,String> mapz = new HashMap<>();
                mapz.put("result", "old password did not match");
                return Response.status(401).entity(mapz).build();
            }
            customer.setPassword(Helper.cypher(pwModel.getNewPassword()));
            dao.update(customer);
            return Response.status(201).build();
        }catch(Exception ex){
            return getServerErrorResponse();
        }
    }


    private void createSocialMediaLink(Customer c, String socialMediaId, String platform, String email) throws Exception{
        SocialMediaProfile sm = new SocialMediaProfile();
        sm.setCustomerId(c.getId());
        sm.setPlatform(platform);
        sm.setSocialMediaId(socialMediaId);
        sm.setSocialMediaEmail(email);
        dao.persist(sm);
    }

    private String createVerificationObject(String email, Long customerId) {
        String code = "";
        boolean available = false;
        do {
            code = Helper.getRandomSaltString(20);
            String sql = "select b from EmailVerification b where b.token = :value0 and b.expire >= :value1";
            List<EmailVerification> l = dao.getJPQLParams(EmailVerification.class, sql, code, new Date());
            if (l.isEmpty()) {
                available = true;
            }
        } while (!available);

        EmailVerification cev = new EmailVerification();
        cev.setToken(code);
        cev.setCreated(new Date());
        cev.setCustomerId(customerId);
        cev.setExpire(Helper.addMinutes(cev.getCreated(), 60*24*14));
        cev.setEmail(email);
        dao.persist(cev);
        return code;
    }


    private String createPasswordResetObject(long customerId) {
        String code;
        boolean available = false;
        do {
            code = Helper.getRandomSaltString(20);
            String sql = "select b from PasswordReset b where b.token = :value0 and b.expire >= :value1 and b.status = :value2";
            List<PasswordReset> passwordResets = dao.getJPQLParams(PasswordReset.class, sql, code, new Date(), 'A');
            if (passwordResets.isEmpty()) {
                available = true;
            }
        } while (!available);
        //create object
        PasswordReset passwordReset = new PasswordReset();
        passwordReset.setCreated(new Date());
        passwordReset.setCustomerId(customerId);
        passwordReset.setExpire(Helper.addMinutes(passwordReset.getCreated(), 60*24*2));
        passwordReset.setStatus('A');
        passwordReset.setToken(code);
        dao.persist(passwordReset);
        //return code
        return code;
    }


    private Map<String, Object> getLoginObject(String authHeader, Customer customer, WebApp webApp) {
        AccessToken token = this.issueToken(customer, webApp, 60*4);// 60 minutes
        List<CustomerAddress> addresses = dao.getTwoConditions(CustomerAddress.class, "customerId", "status", customer.getId(), 'A');
        List<SocialMediaProfile> smps = dao.getCondition(SocialMediaProfile.class, "customerId", customer.getId());
        PublicCustomer pc = new PublicCustomer(customer, smps, addresses, getCustomerPublicVehicles(authHeader, customer.getId()));
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("token", token.getToken());
        map.put("tokenExpire", token.getExpire().getTime());
        map.put("customer", pc);
        return map;
    }

    private List<PublicVehicle> getCustomerPublicVehicles(String authHeader, long customerId){
        String jpql = "select b.vehicleYearId from CustomerVehicle b where b.customerId = :value0 and b.status = :value1";
        List<Integer> modelYearIds = dao.getJPQLParams(Integer.class, jpql, customerId, 'A');
        Response r = this.postSecuredRequest(AppConstants.POST_GET_MODEL_YEARS_FROM_IDS, modelYearIds, authHeader);
        List<PublicVehicle> pvs = new ArrayList<>();
        if(r.getStatus() == 200) {
            List<Map<String,Object>> list = r.readEntity(new GenericType<List<Map<String,Object>>>(){});
            List<CustomerVehicle> vehicles = dao.getTwoConditions(CustomerVehicle.class, "customerId", "status", customerId, 'A');
            for(CustomerVehicle cv : vehicles) {
                PublicVehicle pv = new PublicVehicle(cv);
                for(Map<String,Object> map : list) {
                    if(((Integer)map.get("id")).intValue() == cv.getVehicleYearId()) {
                        pv.setVehicle(map);
                        break;
                    }
                }
                pvs.add(pv);
            }
        }
        return pvs;
    }


    private Customer getCustomerFromSocialMedia(String platform, String socialMediaId) {
        SocialMediaProfile sm = dao.findTwoConditions(SocialMediaProfile.class, "platform", "socialMediaId", platform, socialMediaId);
        if(sm == null) {
            return null;
        }
        return dao.find(Customer.class, sm.getCustomerId());
    }


    private boolean socialMediaExists(String socialMediaId, String platform) {
        SocialMediaProfile smCheck = dao.findTwoConditions(SocialMediaProfile.class, "platform", "socialMediaId", platform, socialMediaId);
        if(smCheck != null) {
            return true;
        }
        return false;
    }

    private boolean customerFound(long customerId) {
        Customer c = dao.find(Customer.class, customerId);
        if(c == null) {
            return false;
        }
        return true;
    }

    private boolean validCustomerOperation(long customerId, String authHeader) {
        Customer check = getCustomerFromAuthHeader(authHeader);
        if(check.getId() != customerId) {
            return false;
        }
        return true;
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

    private AccessToken issueToken(Customer customer, WebApp appCode, int expireMinutes) {
        deactivateOldTokens(customer.getId(), appCode);
        Date tokenTime = new Date();
        AccessToken accessToken = new AccessToken(customer.getId(), tokenTime);
        accessToken.setAppCode(appCode);
        accessToken.setExpire(Helper.addMinutes(tokenTime, expireMinutes));
        accessToken.setStatus('A');
        accessToken.setToken(Helper.getSecuredRandom());
        dao.persist(accessToken);
        return accessToken;
    }

    private void deactivateOldTokens(long customerId, WebApp app) {
        String sql = "select b from AccessToken b where b.customerId = :value0 and b.webApp =:value1 and b.status = :value2";
        List<AccessToken> tokens = dao.getJPQLParams(AccessToken.class, sql, customerId, app, 'A');
        for (AccessToken t : tokens) {
            t.setStatus('K');// kill old token
            dao.update(t);
        }
    }

    private static Response getServerErrorResponse(){
        return Response.status(500).entity("Server Error").build();
    }

    private static Response getBadRequestResponse(){
        return Response.status(500).entity("Bad request").build();
    }

    private static Response getBadRequestResponse(String msg){
        return Response.status(500).entity(msg).build();
    }

    private static Response getResourceNotFoundResponse(){
        return Response.status(404).entity("Resource not found").build();
    }

    private static Response getResourceNotFoundResponse(String message){
        return Response.status(404).entity(message).build();
    }

    private Response getSuccessResponseWithLogin(String authHeader, Customer customer, WebApp webApp){
        Map map = getLoginObject(authHeader, customer, webApp);
        return Response.status(200).entity(map).build();
    }

    private WebApp getWebAppFromAuthHeader(String authHeader) {
        try {
            String[] values = authHeader.split("&&");
            String appSecret = values[2].trim();
            // Validate app secret
            return getWebAppFromSecret(appSecret);
        } catch (Exception ex) {
            return null;
        }
    }

    // retrieves app object from app secret
    private WebApp getWebAppFromSecret(String secret) throws Exception {
        // verify web app secret
        WebApp webApp = dao.findTwoConditions(WebApp.class, "appSecret", "active", secret, true);
        if (webApp == null) {
            throw new Exception();
        }
        return webApp;
    }

    private Map<String,Object> getVehicleFromId(String authHeader, int modelYearId){
        List<Integer> modelYearIds = new ArrayList<>();
        modelYearIds.add(modelYearId);
        Response r = this.postSecuredRequest(AppConstants.POST_GET_MODEL_YEARS_FROM_IDS, modelYearIds, authHeader);
        System.out.println("status from vehicle service " + r.getStatus());
        if(r.getStatus() == 200) {
            List<Map<String,Object>> list = r.readEntity(new GenericType<List<Map<String,Object>>>(){});
            return list.get(0);
        }
        return null;
    }


    public <T> Response postSecuredRequest(String link, T t, String authHeader) {
        Builder b = ClientBuilder.newClient().target(link).request();
        b.header(HttpHeaders.AUTHORIZATION, authHeader);
        Response r = b.post(Entity.entity(t, "application/json"));// not secured
        return r;
    }

}
