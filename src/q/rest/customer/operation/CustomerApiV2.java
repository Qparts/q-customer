package q.rest.customer.operation;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import q.rest.customer.dao.DAO;
import q.rest.customer.filter.Secured;
import q.rest.customer.filter.SecuredCustomer;
import q.rest.customer.filter.SecuredUser;
import q.rest.customer.filter.ValidApp;
import q.rest.customer.helper.AppConstants;
import q.rest.customer.helper.Helper;
import q.rest.customer.model.contract.*;
import q.rest.customer.model.entity.*;

import javax.ejb.EJB;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.*;
import java.io.StringWriter;
import java.util.*;

@Path("/api/v2/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CustomerApiV2 {

    @Context
    private ServletContext context;

    @EJB
    private DAO dao;

    @EJB
    private AsyncService async;



    @GET
    @Path("test")
    @Produces(MediaType.TEXT_HTML)
    public Response testQuotationReadyHtml(){
        Map<String,Object> vmap = new HashMap<>();
        vmap.put("firstName", "Fareed");
        vmap.put("quotationLink", "http://somelink.com");
        vmap.put("quotationId", 50001);
        String body = this.getHtmlTemplate(AppConstants.QUOTATION_READY_EMAIL_TEMPLATE, vmap);
        return Response.status(200).entity(body).build();
    }

    @GET
    @Path("test2")
    @Produces(MediaType.TEXT_HTML)
    public Response testWireTransferHtml(){
        Map<String,Object> vmap = new HashMap<>();
        vmap.put("firstName", "Fareed");
        vmap.put("orderLink", "http://somelink.com");
        vmap.put("cartId", 50001);
        vmap.put("amount", 2234);
        vmap.put("wireTransferId", 5123123);

        Map<String,String> bankMap1 =new HashMap<>();
        bankMap1.put("name", "Rajehi");
        bankMap1.put("nameAr", "الراجحي");
        bankMap1.put("iban", "Rajehi IBAN");
        bankMap1.put("accountNo", "Account Number");
        bankMap1.put("accountName", "Account Name");


        Map<String, String> bankMap2 = new HashMap<>();
        bankMap2.put("name", "Ahli");
        bankMap2.put("nameAr", "الأهلي");
        bankMap2.put("iban", "Ahli IBAN");
        bankMap2.put("accountNo", "Account Number");
        bankMap2.put("accountName", "Account Name");

        List<Map> banks = new ArrayList<>();
        banks.add(bankMap1);
        banks.add(bankMap2);
        vmap.put("banks", banks);

        String body = this.getHtmlTemplate(AppConstants.WIRE_TRANSFER_QETAA_EMAIL_TEMPLATE, vmap);
        return Response.status(200).entity(body).build();
    }

    @GET
    @Path("test3")
    @Produces(MediaType.TEXT_HTML)
    public Response testSignupHtml(){
        Map<String,Object> vmap = new HashMap<>();
        vmap.put("activationLink", "http://somelink.com");
        String body = this.getHtmlTemplate(AppConstants.SIGNUP_EMAIL_TEMPLATE, vmap);
        return Response.status(200).entity(body).build();
    }



    @GET
    @Path("test4")
    @Produces(MediaType.TEXT_HTML)
    public Response testPasswordResetHtml(){
        Map<String,Object> vmap = new HashMap<>();
        vmap.put("passwordResetLink", "http://somelink.com");
        vmap.put("firstName", "Fareed");
        String body = this.getHtmlTemplate(AppConstants.PASSWORD_RESET_EMAIL_TEMPLATE, vmap);
        return Response.status(200).entity(body).build();
    }




    @GET
    @Path("test5")
    @Produces(MediaType.TEXT_HTML)
    public Response testQetaaSignupHtml() {
        Map<String, Object> vmap = new HashMap<>();
        vmap.put("activationCode", "1234");
        String body = this.getHtmlTemplate(AppConstants.SIGNUP_EMAIL_TEMPLATE_QETAA, vmap);
        return Response.status(200).entity(body).build();
    }

    @GET
    @Path("test6")
    @Produces(MediaType.TEXT_HTML)
    public Response testShipmentHtml(){
        Map<String,Object> vmap = new HashMap<>();
        vmap.put("trackReference", "12312312312312");
        vmap.put("trackLink", "http://somelink.com");
        vmap.put("trackable", true);
        vmap.put("firstName", "Fareed");
        vmap.put("cartNumber", "50012312");
        vmap.put("courierName", "SMSA");
        vmap.put("courierNameAr", "سمسا");
        vmap.put("shipmentId", "53289473");
        vmap.put("line1", "Some address");
        vmap.put("line2", "Some address line2");
        vmap.put("cityName", "Khobar");
        vmap.put("cityNameAr", "الخبر");
        vmap.put("regionName", "Easter Province");
        vmap.put("regionNameAr", "المنطقة الشرقية");
        vmap.put("countryName", "Saudi Arabia");
        vmap.put("countryNameAr", "السعودية");
        String body = this.getHtmlTemplate(AppConstants.SHIPMENT_EMAIL_TEMPLATE, vmap);
        return Response.status(200).entity(body).build();
    }

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
    @PUT
    @Path("reset-password/sms")
    public Response resetPassword(@HeaderParam("Authorization") String header, Map<String, String> map) {
        try {
            WebApp webApp = getWebAppFromAuthHeader(header);
            String mobile = Helper.getFullMobile(map.get("mobile"), "966");
            String password = Helper.cypher(map.get("password"));
            Customer customer = dao.findTwoConditions(Customer.class, "mobile", "appCode", mobile, webApp.getAppCode());
            customer.setPassword(password);
            dao.update(customer);
            return getSuccessResponseWithLogin(header, customer, webApp);
        } catch (Exception ex) {
            return Response.status(500).build();
        }
    }


    @ValidApp
    @POST
    @Path("reset-password/sms")
    public Response resetPasswordSms(@HeaderParam("Authorization") String header, Map<String,String> map){
        try {
            String mobile = map.get("mobile");
            String mobileFull = Helper.getFullMobile(mobile, "966");
            WebApp webApp = getWebAppFromAuthHeader(header);
            List<Customer> list = dao.getTwoConditions(Customer.class, "mobile", "appCode", mobileFull, webApp.getAppCode());
            if (list.isEmpty() || list.size() > 1) {
                return Response.status(404).build();
            } else {
                int code = Helper.getRandomInteger(1000, 9999);
                String smsContent =  "رمز التحقق:" + code;
                SmsSent smsSent = this.getSmsSent(mobile, smsContent, "Qetaa reset password", null);
                this.async.sendSms(smsSent, mobile, smsContent);
                return Response.status(200).entity(code).build();
            }

        } catch (Exception ex) {
            return Response.status(500).build();
        }
    }

    private SmsSent getSmsSent(String mobile, String content, String purpose, Long customerId){
        SmsSent smsSent = new SmsSent();
        smsSent.setMobile(mobile);
        smsSent.setSmsContent(content);
        smsSent.setPurpose(purpose);
        smsSent.setCreatedBy(0);
        smsSent.setCustomerId(customerId);
        return smsSent;
    }

    @Secured
    @POST
    @Path("email/quotation-submitted")
    public Response sendEmailQuotationSubmitted(@HeaderParam("Authorization") String authHeader, Map<String, Object> map){
        try{
            long customerId = ( (Number) map.get("customerId")).longValue();
            long quotationId= ( (Number) map.get("quotationId")).longValue();
            Customer customer = dao.find(Customer.class, customerId);
            Map<String,Object> vmap = new HashMap<>();
            vmap.put("firstName", customer.getFirstName());
            vmap.put("quotationId", quotationId);
            String body;
            //this is q-parts
            if(customer.getAppCode() == 2){
                body = getHtmlTemplate(AppConstants.QUOTATION_SUBMITTED_EMAIL_TEMPLATE, vmap);
            }
            //this is qetaa.com
            else{
                body = getHtmlTemplate(AppConstants.QUOTATION_SUBMITTED_QETAA_EMAIL_TEMPLATE, vmap);
            }
            EmailSent emailSent = new EmailSent();
            emailSent.setEmail(customer.getEmail());
            emailSent.setPurpose("Quotation Submitted");
            emailSent.setAppCode(customer.getAppCode());
            emailSent.setCreatedBy(0);
            emailSent.setQuotationId(quotationId);
            emailSent.setCustomerId(customer.getId());
            async.sendHtmlEmail(emailSent, customer.getEmail(), AppConstants.getQuotationReadyEmailSubject(quotationId), body);
            return Response.status(201).build();
        }catch (Exception ex){
            return Response.status(500).build();
        }
    }


    @ValidApp
    @POST
    @Path("request-signup-code")
    public Response requestSMS(@HeaderParam("Authorization") String authHeader, Map<String, String> map) {
        try {
            WebApp webApp;
            try {
                Integer appCode = Integer.parseInt(map.get("appCode"));
                webApp = dao.find(WebApp.class, appCode);
            }catch (Exception ex){
                webApp = this.getWebAppFromAuthHeader(authHeader);
            }
            String email = map.get("email");
            String countryCode = map.get("countryCode");
            String mobile = Helper.getFullMobile(map.get("mobile"), countryCode);
            String sql = "select b from Customer b where (b.mobile =:value0 or b.email =:value1) and b.appCode = :value2";
            List<Customer> check = dao.getJPQLParams(Customer.class, sql, mobile, email, webApp.getAppCode());
            if(!check.isEmpty()){
                return Response.status(409).build();
            }
            int code = Helper.getRandomInteger(1000, 9999);
            if(countryCode.equals("966")){
                String smsContent =  "رمز التحقق:" + code;
                SmsSent smsSent = this.getSmsSent(mobile, smsContent, "Qetaa Signup code", null);
                smsSent.setMobile(mobile);
                smsSent.setSmsContent(smsContent);
                smsSent.setCreatedBy(0);
                this.async.sendSms(smsSent, mobile, smsContent);
            }
            else{
                //create body
                Map<String, Object> vmap = new HashMap<>();
                vmap.put("activationCode", code);
                String body = this.getHtmlTemplate(AppConstants.SIGNUP_EMAIL_TEMPLATE_QETAA, vmap);
                EmailSent emailSent = new EmailSent();
                emailSent.setEmail(email);
                emailSent.setPurpose("Qetaa Signup code");
                emailSent.setCreatedBy(0);
                emailSent.setCustomerId(null);
                emailSent.setAppCode(webApp.getAppCode());
                async.sendHtmlEmail(emailSent, email, AppConstants.ACCOUNT_SIGNUP_CODE_EMAIL_SUBJECT, body);
            }

            return Response.status(200).entity(code).build();


        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(500).build();
        }
    }





    @ValidApp
    @POST
    @Path("signup/qetaa")
    public Response signupQetaa(@HeaderParam("Authorization") String header, QetaaRegisterModel registerModel){
        try{
            WebApp webApp = getWebAppFromAuthHeader(header);
            Customer customer = new Customer();
            customer.setEmail(registerModel.getEmail().toLowerCase());
            customer.setCountryId(registerModel.getCountryId());
            customer.setCreated(new Date());
            customer.setDefaultLang("ar");
            customer.setCreatedBy(0);
            customer.setFirstName(registerModel.getFirstName());
            customer.setLastName(registerModel.getLastName());
            if(registerModel.getType() != 'F'){
                customer.setPassword(Helper.cypher(registerModel.getPassword()));
            }
            customer.setSmsActive((registerModel.getCountryId() == 1));
            customer.setNewsletterActive(true);
            customer.setStatus('A');//active
            customer.setMobile(Helper.getFullMobile(registerModel.getMobile(), registerModel.getCountryCode()));
            customer.setAppCode(webApp.getAppCode());
            dao.persist(customer);

            if(registerModel.getType() == 'F'){
                createSocialMediaLink(customer, registerModel.getFacebookId(), "facebook", registerModel.getContactEmail(), webApp.getAppCode());
            }
            LoginObject map = this.getLoginObject(header, customer, webApp);
            return Response.status(200).entity(map).build();
        }catch (Exception ex){
            return Response.status(500).build();
        }
    }




    @ValidApp
    @POST
    @Path("signup")
    public Response signup(@HeaderParam("Authorization")String authHeader, SignupRequestModel signupModel){
        try{
            WebApp webApp = getWebAppFromAuthHeader(authHeader);
            Customer check = dao.findTwoConditions(Customer.class, "email", "appCode", signupModel.getEmail(), webApp.getAppCode());
            if(null != check){
                return Response.status(409).entity("email already exists").build();
            }
            Customer customer = new Customer();
            customer.setEmail(signupModel.getEmail().toLowerCase());
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
            customer.setAppCode(webApp.getAppCode());
            dao.persist(customer);

            String code = this.createVerificationObject(customer.getEmail(), customer.getId());
            String activationLink = AppConstants.getActivationLink(code, customer.getEmail());
            Map<String,Object> vmap = new HashMap<>();
            vmap.put("activationLink", activationLink);
            String body = getHtmlTemplate(AppConstants.SIGNUP_EMAIL_TEMPLATE, vmap);
            EmailSent emailSent = new EmailSent();
            emailSent.setEmail(customer.getEmail());
            emailSent.setPurpose("Signup Activation");
            emailSent.setCreatedBy(0);
            emailSent.setAppCode(webApp.getAppCode());
            emailSent.setCustomerId(customer.getId());
            async.sendHtmlEmail(emailSent, customer.getEmail(), AppConstants.ACCOUNT_ACTIVATION_EMAIL_SUBJECT, body);
            //send back login object
            LoginObject loginObject = this.getLoginObject(authHeader, customer, webApp);
            return Response.status(202).entity(loginObject).build();
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
    @Path("code-login")
    public Response codeLogin(@HeaderParam("Authorization") String header, CredentialsModel cred){
        try{
            WebApp webApp = getWebAppFromAuthHeader(header);
            Customer customer = dao.findTwoConditions(Customer.class, "email", "appCode", cred.getEmail(), webApp.getAppCode());
            if(customer == null){
                return getResourceNotFoundResponse("Invalid credentials");
            }
            String sql = "select b from CodeLogin b where b.customerId = :value0 and b.code = :value1 " +
                    " and b.expire > :value2 ";
            CodeLogin codeLogin = dao.findJPQLParams(CodeLogin.class, sql , customer.getId(), cred.getCode(), new Date());
            if(codeLogin == null){
                return Response.status(403).entity("Invalid credentials").build();
            }
            return getSuccessResponseWithLogin(header, customer , getWebAppFromAuthHeader(header));
        }catch (Exception ex){
            return Response.status(500).build();
        }
    }


    //for qetaa only
    @ValidApp
    @POST
    @Path("login/facebook")
    public Response facebookLogin(@HeaderParam("Authorization") String header, QetaaRegisterModel registerModel){
        try{
            WebApp webApp = getWebAppFromAuthHeader(header);
            // already authenticated in facebook
            Customer customer = getCustomerFromSocialMedia("facebook", registerModel.getFacebookId(), webApp.getAppCode());
            // get customer from facebook
            if(customer == null ){
                return Response.status(404).build();
            }
            LoginObject loginObject = getLoginObject(header, customer, webApp);
            return Response.status(200).entity(loginObject).build();
        }catch (Exception ex){
            ex.printStackTrace();
            return Response.status(500).build();
        }
    }

    @ValidApp
    @POST
    @Path("login")
    public Response login(@HeaderParam("Authorization") String authHeader, CredentialsModel cModel){
        try {
            WebApp webApp = this.getWebAppFromAuthHeader(authHeader);
            String email = cModel.getEmail().trim().toLowerCase();
            String password= Helper.cypher(cModel.getPassword());
            String sql = "select b from Customer b where (b.email = :value0 or b.mobile =:value1) and b.password = :value2 and b.appCode =:value3";
            Customer customer = dao.findJPQLParams(Customer.class, sql, email, Helper.getFullMobile(email, "966") , password, webApp.getAppCode());
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
            WebApp webApp = getWebAppFromAuthHeader(authHeader);
            String email = cModel.getEmail();
            Customer customer = dao.findCondition(Customer.class, "email", email);
            if(customer != null){
                String code = createPasswordResetObject(customer.getId());
                String activationLink = AppConstants.getPasswordResetLink(code, customer.getEmail());
                Map<String,Object> vmap = new HashMap<>();
                vmap.put("passwordResetLink", activationLink);
                vmap.put("firstName", customer.getFirstName());
                String body = getHtmlTemplate(AppConstants.PASSWORD_RESET_EMAIL_TEMPLATE , vmap);
                EmailSent emailSent = new EmailSent();
                emailSent.setEmail(customer.getEmail());
                emailSent.setPurpose("Reset Password");
                emailSent.setCreatedBy(0);
                emailSent.setAppCode(webApp.getAppCode());
                emailSent.setCustomerId(customer.getId());
                async.sendHtmlEmail(emailSent, email, AppConstants.RESET_PASSWORD_EMAIL_SUBJECT, body);
            }
            return Response.status(200).build();
        } catch (Exception ex){
            return getServerErrorResponse();
        }
    }


    //needs visit for merged dashboard
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

    //needs visit for merged dashboard
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

    //not for qetaa .. for q only
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
            if(this.socialMediaExists(smModel.getSocialMediaId(), smModel.getPlatform(), webApp.getAppCode())){
                Customer customer = getCustomerFromSocialMedia(smModel.getPlatform(), smModel.getSocialMediaId(), webApp.getAppCode());
                return getSuccessResponseWithLogin(authHeader, customer, webApp);
            }

            //check if email exists!
            Customer check = dao.findTwoConditions(Customer.class, "email" , "appCode", smModel.getEmail(), webApp.getAppCode());
            if(check != null){
                this.createSocialMediaLink(check, smModel.getSocialMediaId(), smModel.getPlatform(), smModel.getEmail(), webApp.getAppCode());
                return this.getSuccessResponseWithLogin(authHeader, check, webApp);
            }

            //email doesn't exist! sign him/her up and create sm link.
            Customer customer = new Customer();
            customer.setEmail(smModel.getEmail().toLowerCase());
            customer.setFirstName(smModel.getFirstName());
            customer.setLastName(smModel.getLastName());
            customer.setSmsActive(false);
            customer.setNewsletterActive(true);
            customer.setDefaultLang(smModel.getDefaultLang());
            customer.setCreated(new Date());
            customer.setCreatedBy(0);
            customer.setCountryId(smModel.getCountryId());
            customer.setStatus('V');
            customer.setAppCode(webApp.getAppCode());
            dao.persist(customer);
            createSocialMediaLink(customer, smModel.getSocialMediaId(), smModel.getPlatform(), smModel.getEmail(), webApp.getAppCode());
            return this.getSuccessResponseWithLogin(authHeader, customer, webApp);
        }catch(Exception ex){
            ex.printStackTrace();
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
    @Path("vehicle-if-available")
    public Response getOrCustomerVehicle(@HeaderParam("Authorization") String header, PublicVehicle pvModel){
        try{
            if(!customerFound(pvModel.getCustomerId())) {
                return Response.status(404).build();
            }
            if(!validCustomerOperation(pvModel.getCustomerId(), header)) {
                return Response.status(401).build();
            }

            String jpql = "select b from CustomerVehicle b where b.customerId = :value0 and b.vehicleYearId = :value1 and b.vin =:value2";
            List<CustomerVehicle> customerVehicles = dao.getJPQLParams(CustomerVehicle.class, jpql, pvModel.getCustomerId(), pvModel.getVehicleYearId(), pvModel.getVin().toUpperCase().trim());
            if(!customerVehicles.isEmpty()){
                return Response.status(409).entity(customerVehicles.get(0).getId()).build();
            }

            CustomerVehicle customerVehicle = createVehicle(pvModel);
            makeVehicleDefault(customerVehicle.getCustomerId(), customerVehicle.getId());
            return Response.status(200).entity(customerVehicle.getId()).build();
        }catch (Exception ex){
            ex.printStackTrace();
            return Response.status(500).build();
        }
    }

    private CustomerVehicle createVehicle(PublicVehicle pv){
        CustomerVehicle customerVehicle = new CustomerVehicle();
        customerVehicle.setStatus('A');
        customerVehicle.setCreated(new Date());
        customerVehicle.setDefaultVehicle(false);
        customerVehicle.setCreatedBy(0);
        customerVehicle.setCustomerId(pv.getCustomerId());
        customerVehicle.setVehicleYearId(pv.getVehicleYearId());
        if(pv.getVin() != null){
            customerVehicle.setVin(pv.getVin().toUpperCase().trim());
        }
        else{
            customerVehicle.setVin(null);
        }

        if(pv.getImageAttached() == null){
            customerVehicle.setImageAttached(false);
        }
        else{
            customerVehicle.setImageAttached(pv.getImageAttached());
        }
        dao.persist(customerVehicle);
        if(customerVehicle.isImageAttached()){
            async.broadcastToNotification("noVins,"+async.getNoVinsCount());
        }
        return customerVehicle;
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
            CustomerVehicle cv = createVehicle(pvModel);
            if(cv.isImageAttached()){
                async.broadcastToNotification("noVins," + async.getNoVinsCount());
            }
            if(pvModel.isDefaultVehicle()){
                makeVehicleDefault(cv.getCustomerId(), cv.getId());
            }
            pvModel.setId(cv.getId());
            pvModel.setVehicle(this.getVehicleFromId(header, pvModel.getVehicleYearId()));
            return Response.status(200).entity(pvModel).build();
        }catch(Exception ex){
            ex.printStackTrace();
            return getServerErrorResponse();
        }
    }

    private void makeVehicleDefault(long customerId, long vehicleId){
        try {
            List<CustomerVehicle> cvs = dao.getTwoConditions(CustomerVehicle.class, "customerId", "defaultVehicle", customerId, true);
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
            List<CustomerAddress> cvs = dao.getTwoConditions(CustomerAddress.class, "customerId", "defaultAddress", customerId, true);
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
    @Path("vehicle/{vehicleId}")
    public Response archiveVehicle(@HeaderParam("Authorization") String header, @PathParam(value="vehicleId") long vehicleId){
        try{
            CustomerVehicle cv = dao.find(CustomerVehicle.class, vehicleId);
            if(!customerFound(cv.getCustomerId())) {
                return Response.status(404).build();
            }

            if(!validCustomerOperation(cv.getCustomerId(), header)) {
                return Response.status(401).build();
            }

            cv.setStatus('X');
            dao.update(cv);
            return Response.status(201).build();

        }catch (Exception ex){
            return getServerErrorResponse();
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
            WebApp webApp = getWebAppFromAuthHeader(header);
            //check if this customer exists
            if (!customerFound(smModel.getCustomerId())) {
                return Response.status(404).build();
            }


            if (!validCustomerOperation(smModel.getCustomerId(), header)) {
                return Response.status(401).build();
            }

            //check if this social media exists
            if (socialMediaExists(smModel.getSocialMediaId(), smModel.getPlatform(), webApp.getAppCode())) {
                return Response.status(409).build();
            }

            //check if a customer is registered with this email
            Customer check = dao.findTwoConditions(Customer.class, "email", "appCode", smModel.getEmail(), webApp.getAppCode());
            if (check != null) {
                return Response.status(409).build();
            }

            Customer customer = dao.find(Customer.class, smModel.getCustomerId());
            createSocialMediaLink(customer, smModel.getSocialMediaId(), smModel.getPlatform(), smModel.getEmail(), webApp.getAppCode());
            return Response.status(201).build();
        }catch(Exception ex){
            return getServerErrorResponse();
        }
    }

    //meeds visit
    @SecuredCustomer
    @PUT
    @Path("password")
    public Response updatePassword(@HeaderParam("Authorization") String header, PasswordUpdateModel pwModel){
        try{

            WebApp webApp = getWebAppFromAuthHeader(header);
            if(pwModel.getNewPassword() == null || pwModel.getOldPassword() == null || pwModel.getNewPassword().equals("") || pwModel.getOldPassword().equals("")){
                return getBadRequestResponse();
            }

            if(!customerFound(pwModel.getCustomerId())) {
                return Response.status(401).build();
            }

            if(!validCustomerOperation(pwModel.getCustomerId(), header)) {
                return Response.status(401).build();
            }

            String sql = "select b from Customer b where b.id = :value0 and b.password = :value1 and b.appCode = :value2";
            Customer customer = dao.findJPQLParams(Customer.class, sql, pwModel.getCustomerId(), Helper.cypher(pwModel.getOldPassword()), webApp.getAppCode() );
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


    private void createSocialMediaLink(Customer c, String socialMediaId, String platform, String email, int appCode) throws Exception{
        SocialMediaProfile sm = new SocialMediaProfile();
        sm.setCustomerId(c.getId());
        sm.setPlatform(platform);
        sm.setSocialMediaId(socialMediaId);
        sm.setSocialMediaEmail(email);
        sm.setAppCode(appCode);
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


    private LoginObject getLoginObject(String authHeader, Customer customer, WebApp webApp) {
        AccessToken token = this.issueToken(customer, webApp, 60 * 24 * 7);// 60 minutes
        List<CustomerAddress> addresses = dao.getTwoConditions(CustomerAddress.class, "customerId", "status", customer.getId(), 'A');
        List<SocialMediaProfile> smps = dao.getCondition(SocialMediaProfile.class, "customerId", customer.getId());
        PublicCustomer pc = new PublicCustomer(customer, smps, addresses, getCustomerPublicVehicles(authHeader, customer.getId()));
        LoginObject loginObject = new LoginObject();
        loginObject.setCustomer(pc);
        loginObject.setToken(token.getToken());
        loginObject.setTokenExpire(token.getExpire().getTime());
        return loginObject;
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


    private Customer getCustomerFromSocialMedia(String platform, String socialMediaId, int appCode) {
        String sql = "select b from SocialMediaProfile b where b.platform = :value0 and b.socialMediaId = :value1 and b.appCode = :value2";
        SocialMediaProfile sm = dao.findJPQLParams(SocialMediaProfile.class, sql , platform, socialMediaId, appCode);
        if(sm == null) {
            return null;
        }
        return dao.find(Customer.class, sm.getCustomerId());
    }


    private boolean socialMediaExists(String socialMediaId, String platform, int appCode) {
        String sql = "select b from SocialMediaProfile b where b.platform = :value0 and b.socialMediaId = :value1 and b.appCode = :value2";
        SocialMediaProfile sm = dao.findJPQLParams(SocialMediaProfile.class, sql , platform, socialMediaId, appCode);
        if(sm != null) {
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
        LoginObject loginObject = getLoginObject(authHeader, customer, webApp);
        return Response.status(200).entity(loginObject).build();
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

    private long getCustomerIdFromHeader(String header){
        try{
            String[] values = header.split("&&");
            String customerId = values[3].trim();
            return Long.parseLong(customerId);
        } catch (Exception ex ){
            return 0;
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
