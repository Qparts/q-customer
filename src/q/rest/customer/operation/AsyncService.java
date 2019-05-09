package q.rest.customer.operation;

import q.rest.customer.dao.DAO;
import q.rest.customer.helper.AppConstants;
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
import java.util.Properties;



@Stateless
public class AsyncService {

    @EJB
    private DAO dao;


    @Asynchronous
    public void sendHtmlEmail(String email, String subject, String body) {
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
        } catch (MessagingException ex) {
            ex.printStackTrace();
        }
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
    public void sendToCusotmerNotification(String message){
        CustomerNotificationEndPoint.broadcast(message);
    }


}
