package org.jboss.resteasy.test.cdi.stereotype.resource;

import org.jboss.resteasy.test.cdi.stereotype.resource.stereotypes.MethodPathStereotype;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/custom")
public class MethodPathResource
{
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @MethodPathStereotype
    @javax.enterprise.inject.Produces
    public Response test() {
        return Response.ok("MethodPathResource").build();
    }
    
}
