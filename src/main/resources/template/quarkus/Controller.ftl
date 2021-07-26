package [=package]

import [=servicePath]
import [=entityPath]

import java.util.*;
import org.jboss.logging.Logger;
import javax.inject.Inject;
import javax.ws.rs.*;
<#if jwt=="true">
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.jwt.JsonWebToken;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;

</#if>

<#if async == true>import reactor.core.publisher.*;</#if>

<#if jwt=="true">@RequestScoped</#if>
@Path("/")
public class [=className] {

    @Inject
    Logger log;

<#if jwt=="true">
    @Inject
    JsonWebToken jwt;
</#if>


    [=serviceName] service;

    public [=className]([=serviceName] service) {
        this.service = service;

    }
<#if jwt=="true">
    /**
    * This is a dummy Endpoint to demonstrate the behavior of JWT when an unauthenticated user accesses an endpoint.
    * @PermitAll is a JSR 250 common security annotation that indicates that the given endpoint is accessible by any caller,
    * authenticated or not.
    * We first inject the JAX-RS SecurityContext to inspect the security state of the call and use a getResponseString()
    * function to populate a response string.
    * We then check if the call is insecured by checking the request user/caller Principal against null.
    * Finally we check that the Principal and JsonWebToken have the same name since JsonWebToken does represent the current Principal.
    */
    @GET
    @Path("/permitAll")
    @Produces("text/plain")
    @PermitAll
    public String hello(@Context SecurityContext ctx) {
        return getResponseString(ctx);
    }

    private String getResponseString(SecurityContext ctx) {
        String name;
        if (ctx.getUserPrincipal() == null) {
            name = "anonymous";
        } else if (!ctx.getUserPrincipal().getName().equals(jwt.getName())) {
            throw new InternalServerErrorException("Principal and JsonWebToken names do not match");
        } else {
            name = ctx.getUserPrincipal().getName();
        }
        return String.format("hello + %s,"
        + " isHttps: %s,"
        + " authScheme: %s,"
        + " hasJWT: %s",
        name, ctx.isSecure(), ctx.getAuthenticationScheme(), hasJwt());
    }

    private boolean hasJwt() {
    return jwt.getClaimNames() != null;
    }
</#if>

    [=methods]

}