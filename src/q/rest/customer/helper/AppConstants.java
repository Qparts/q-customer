package q.rest.customer.helper;

import q.rest.customer.model.contract.WireTransferEmailRequest;
import q.rest.customer.model.entity.Customer;

public class AppConstants {

//    private final static String WEBSITE_BASE_URL = SysProps.getValue("webAppBase");
//    private final static String PUBLIC_VEHICLE_SERVICE = SysProps.getValue("vehiclePublicService");
//    private final static String USER_SERVICE = SysProps.getValue("userService");
//    private final static String LOCATION_SERVICE = SysProps.getValue("locationService");
    private final static String SUBSCRIBER_SERVICE = SysProps.getValue("subscriberService").replace("/v1/", "/v2/");

    public final static String POST_DEFAULT_CUSTOMER = SUBSCRIBER_SERVICE + "default-customer";


//
//    public final static String SIGNUP_EMAIL_TEMPLATE = "email/signup.vm";
//    public final static String SIGNUP_EMAIL_TEMPLATE_QETAA = "email/signup-code-qetaa.vm";
//    public final static String PASSWORD_RESET_EMAIL_TEMPLATE = "email/password-reset.vm";
//    public final static String QUOTATION_READY_EMAIL_TEMPLATE = "email/quotation-ready.vm";
//    public final static String QUOTATION_READY_QETAA_EMAIL_TEMPLATE = "email/quotation-ready-qetaa.vm";
//    public final static String WIRE_TRANSFER_EMAIL_TEMPLATE = "email/wire-transfer.vm";
//    public final static String WIRE_TRANSFER_QUOTATION_EMAIL_TEMPLATE = "email/wire-transfer-quotation.vm";
//    public final static String WIRE_TRANSFER_QETAA_EMAIL_TEMPLATE = "email/wire-transfer-qetaa.vm";
//    public final static String WIRE_TRANSFER_QETAA_QUOTATION_EMAIL_TEMPLATE = "email/wire-transfer-quotation-qetaa.vm";
//    public final static String SHIPMENT_QETAA_EMAIL_TEMPLATE = "email/shipment-qetaa.vm";
//    public final static String SHIPMENT_EMAIL_TEMPLATE = "email/shipment.vm";
//    public final static String QUOTATION_SUBMITTED_EMAIL_TEMPLATE = "email/quotation-submitted.vm";
//    public final static String QUOTATION_SUBMITTED_QETAA_EMAIL_TEMPLATE = "email/quotation-submitted-qetaa.vm";

//    public final static String USER_MATCH_TOKEN = USER_SERVICE + "match-token";
//    public static final String USER_MATCH_TOKEN_WS = USER_SERVICE + "match-token/ws";
//
//    public final static String ACCOUNT_ACTIVATION_EMAIL_SUBJECT= "Account Activation - تفعيل الحساب";
//    public final static String ACCOUNT_SIGNUP_CODE_EMAIL_SUBJECT= "Signup Code - رمز التحقق";
//    public final static String RESET_PASSWORD_EMAIL_SUBJECT = "Reset Password - إعادة تهيئة كلمة المرور";
//    public final static String getQuotationReadyEmailSubject(long quotationId){
//        return "Quotation No. " + quotationId + " طلب التسعيرة رقم ";
//    }
//
//    public final static String getWireTransferRequestEmailSubject(WireTransferEmailRequest wire){
//        if(wire.getPurpose().equals("cart")){
//            return "Cart No. " + wire.getCartId() + " رقم الطلب:  ";
//        }
//        else{
//            return "Quotation No. " + wire.getQuotationId() + " رقم التسعيرة:  ";
//        }
//
//    }
//    public final static String getShipmentEmailSubject(long shipmentId){
//        return "Shipment No. " + shipmentId + " شحنة رقم";
//    }
//
//    public final static String getCityVariableNames(int cityId){
//        return LOCATION_SERVICE + "city/"+cityId+"/names-only";
//    }

//    public final static String EMAIL_ADDRESS = "no-reply@qetaa.com";
//    public final static String PASSWORD = "qetaa3!Cs@";
//    public final static String SMTP_SERVER = "smtp.zoho.com";
//
//    public final static String POST_GET_MODEL_YEARS_FROM_IDS = PUBLIC_VEHICLE_SERVICE + "model-year-from-ids";

//
//    public final static String getActivationLink(String code, String email) {
//        return WEBSITE_BASE_URL + "activate-email?code=" + code + "&email=" + email;
//    }
//    public final static String getQuotationReadyLink(long quotationId, String email, String code, int appCode) {
//        if(appCode == 2) {
//            return WEBSITE_BASE_URL + "setting/quotations?panel=replied&id=" + quotationId + "&code=" + code + "&email=" + email;
//        } else{
//            return "https://qetaa.com/codelg?c=replied&id=" + quotationId + "&code=" + code + "&email=" + email;
//        }
//    }
//
//    public final static String getCodeLoginLink(String email, String code, long quotationId) {
//        return "qetaa.com/codelg?c=" + code + "&q=" + quotationId + "&e=" + email;
//    }
////
//    public final static String getPasswordResetLink(String code, String email){
//        return WEBSITE_BASE_URL + "password/reset-password?code=" + code + "&email=" + email;
//    }
//
//    public final static String getSMSMaxLink(String mobile,String text){
//        return SMS_MAX_PROVIDER_HOST
//                + "user="+SMS_MAX_PROVIDER_USERNAME
//                + "&pass="+SMS_MAX_PROVIDER_PASSWORD
//                + "&to="+mobile
//                + "&message="+text
//                + "&sender="+SMS_MAX_PROVIDER_SENDER;
//    }

}
