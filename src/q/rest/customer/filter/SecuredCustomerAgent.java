package q.rest.customer.filter;

import q.rest.customer.dao.DAO;
import q.rest.customer.model.entity.AccessToken;
import q.rest.customer.model.entity.WebApp;

import javax.annotation.Priority;
import javax.ejb.EJB;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SecuredCustomer
@Provider
@Priority(Priorities.AUTHENTICATION)
public class SecuredCustomerAgent implements ContainerRequestFilter {

    @EJB
    private DAO dao;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        try{
            String header = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
            if(header == null || !header.startsWith("Bearer") || header.startsWith("Bearer null")){
                throw new Exception();
            }
            String[] values = header.split("&&");
            String tokenPart = values[0];
            String token = tokenPart.substring("Bearer".length()).trim();
            String username = values[1].trim();
            String appSecret = values[2].trim();
            String type = values[3].trim();
            matchToken(token, username, appSecret, type);
        }
        catch (Exception ex){
            requestContext.abortWith(Response.status(401).entity("Unauthorized Access").build());
        }
    }

    private void matchToken(String token, String username, String appSecret, String type) throws NotAuthorizedException{
        if(!type.equals("C")){
            throw new NotAuthorizedException("Request authorization failed");
        }
        WebApp webApp = getWebAppFromSecret(appSecret);
        String sql = "select b from AccessToken b where b.customerId = :value0 and b.webApp = :value1 " +
                "and b.status =:value2 and b.token =:value3 and b.expire > :value4";
        List<AccessToken> accessTokenList = dao.getJPQLParams(AccessToken.class, sql, Long.parseLong(username), webApp, 'A', token, new Date());
        if(accessTokenList.isEmpty()){
           throw new NotAuthorizedException("Request authorization failed");
        }
    }

    // retrieves app object from app secret
    private WebApp getWebAppFromSecret(String secret) throws NotAuthorizedException{
        // verify web app secret
        WebApp webApp = dao.findTwoConditions(WebApp.class, "appSecret", "active", secret, true);
        if (webApp == null) {
            throw new NotAuthorizedException("Unauthorized Access");
        }
        return webApp;
    }

}
