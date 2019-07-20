package org.jboss.resteasy.test.cdi.stereotype.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/encoded")
@EncodedStereotype
public class EncodedResource
{
   @Path("/{param}")
   @GET
   @Produces(MediaType.TEXT_PLAIN)
   public Response test(@PathParam("param") String param) {
      return Response.ok(param).build();
   }
}
