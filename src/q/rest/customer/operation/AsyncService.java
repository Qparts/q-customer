package q.rest.customer.operation;

import q.rest.customer.dao.DAO;
import q.rest.customer.helper.AppConstants;
import q.rest.customer.model.entity.EmailSent;
import q.rest.customer.model.entity.SmsSent;
import q.rest.customer.operation.sockets.CustomerNotificationEndPoint;
import q.rest.customer.operation.sockets.NotificationsEndPoint;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.PasswordAuthentication;
import javax.mail.Transport;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Properties;



@Stateless
public class AsyncService {

    @EJB
    private DAO dao;


    @Asynchronous
    public void sendHtmlEmail(EmailSent emailSent, String email, String subject, String body) {
        Properties properties = System.getProperties();
        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(AppConstants.EMAIL_ADDRESS, AppConstants.PASSWORD);
            }
        });
        properties.setProperty("mail.smtp.host", AppConstants.SMTP_SERVER);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.starttls.enable", "true");
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(AppConstants.EMAIL_ADDRESS));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
            message.setSubject(subject);
            message.setContent(body, "text/html; charset=utf-8");
            Transport.send(message);
            createEmailSentObject(emailSent, 'S');
        } catch (MessagingException ex) {
            createEmailSentObject(emailSent, 'F');
        }
    }

    @Asynchronous
    public void sendSms(SmsSent smsSent, String mobileFull, String text) {
        try {
            String textEncoded = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String url = AppConstants.getSMSMaxLink(mobileFull, textEncoded);
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            int responseCode = con.getResponseCode();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            createSmsSentSentObject(smsSent, 'S');
            in.close();
        } catch (Exception ignore) {

        }
    }


    private void createSmsSentSentObject(SmsSent smsSent, char status){
        smsSent.setCreated(new Date());
        smsSent.setStatus(status);
        dao.persist(smsSent);
    }


    private void createEmailSentObject(EmailSent emailSent, char status){
        emailSent.setCreated(new Date());
        emailSent.setStatus(status);
        dao.persist(emailSent);
    }

    public int getNoVinsCount() {
        String sql = "select count(b) from CustomerVehicle b where b.imageAttached =:value0";
        Number number = dao.findJPQLParams(Number.class, sql , true);
        if(number == null){
            number = 0;
        }
        return number.intValue();
    }

    @Asynchronous
    public void broadcastToNotification(String message){
        NotificationsEndPoint.broadcast(message);

    }

    @Asynchronous
    public void sendToCusotmerNotification(String message, long customerId){
        CustomerNotificationEndPoint.sendToCustomer(message, customerId);
    }


}
