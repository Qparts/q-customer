package q.rest.customer.helper;

public class AppConstants {

    private final static String WEBSITE_BASE_URL = SysProps.getValue("webAppBase");
    private final static String PUBLIC_VEHICLE_SERVICE = SysProps.getValue("vehiclePublicService");
    private final static String USER_SERVICE = SysProps.getValue("userService");

    public final static String USER_MATCH_TOKEN = USER_SERVICE + "match-token";

    public final static String ACCOUNT_ACTIVATION_EMAIL_SUBJECT= "Account Activation - تفعيل الحساب";
    public final static String RESET_PASSWORD_EMAIL_SUBJECT = "Reset Password - إعادة تهيئة كلمة المرور";


    public final static String EMAIL_ADDRESS = "no-reply@qetaa.com";
    public final static String PASSWORD = "qetaa3!Cs@";
    public final static String SMTP_SERVER = "smtp.zoho.com";

    public final static String POST_GET_MODEL_YEARS_FROM_IDS = PUBLIC_VEHICLE_SERVICE + "model-year-from-ids";


    public final static String getActivationLink(String code, String email) {
        return WEBSITE_BASE_URL + "activate-email?code=" + code + "&email=" + email;
    }

    public final static String getPasswordResetLink(String code, String email){
        return WEBSITE_BASE_URL + "password/reset-password?code=" + code + "&email=" + email;
    }
}
