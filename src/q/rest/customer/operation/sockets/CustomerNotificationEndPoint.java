package q.rest.customer.operation.sockets;

import q.rest.customer.dao.DAO;
import q.rest.customer.model.entity.AccessToken;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint("/ws/notifications/customer/{customerId}/token/{token}")
@Stateless
public class CustomerNotificationEndPoint {

//    @Inject
//    private DAO dao;
//
//    private Session session;
//    private long customerId;
//    private String token;
//
//    private static Set<CustomerNotificationEndPoint> notificationsEndPoints = new CopyOnWriteArraySet<>();
//
//    @OnMessage
//    public String onMessage(String message) {
//        return (message);
//    }
//
//
//    @OnOpen
//    public void onOpen(Session session, @PathParam("customerId") Long customerId, @PathParam("token") String token) throws IOException {
//        this.session = session;
//        this.customerId = customerId;
//        this.token = token;
//        if(this.tokenMatched()) {
//            notificationsEndPoints.add(this);
//        }
//        else {
//            session.close();
//        }
//    }
//
//
//    @OnClose
//    public void onClose(Session session, CloseReason reason){
//        notificationsEndPoints.remove(this);
//    }
//
//
//    public static void sendToCustomer(String message, long customerId) {
//        notificationsEndPoints.forEach(endpoint -> {
//            synchronized (endpoint) {
//                if (endpoint.session.isOpen()) {
//                    if(endpoint.customerId == customerId) {
//                        endpoint.session.getAsyncRemote().sendText(message);
//                    }
//                }
//            }
//        });
//    }
//
//
//    public static void broadcast(String message) {
//        notificationsEndPoints.forEach(endpoint -> {
//            synchronized (endpoint) {
//                if (endpoint.session.isOpen()) {
//                    endpoint.session.getAsyncRemote().sendText(message);
//                }
//            }
//        });
//    }
//
//
//    private boolean tokenMatched() {
//        String jpql = "select b from AccessToken b where b.customerId = :value0 and b.status = :value1 and b.token = :value2 and b.expire > :value3";
//        var l = dao.getJPQLParams(AccessToken.class, jpql, customerId, 'A', token, new Date());
//        return !l.isEmpty();
//    }
//
//    public <T> Response postNoneSecuredRequest(String link, T t) {
//        Invocation.Builder b = ClientBuilder.newClient().target(link).request();
//        Response r = b.post(Entity.entity(t, "application/json"));// not secured
//        return r;
//    }
}
