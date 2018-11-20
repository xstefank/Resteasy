package org.jboss.resteasy.test.cdi.stereotype.resource;

import org.jboss.resteasy.test.cdi.stereotype.resource.stereotypes.PathStereotype;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@PathStereotype
public class PathResource2
{
    
    @Path("/get")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response test() {
        return Response.ok("PathResource2").build();
    }
    
}
