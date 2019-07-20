package org.jboss.resteasy.test.cdi.stereotype.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@PathStereotype
public class PathResource
{
   @Path("/get")
   @GET
   @Produces(MediaType.TEXT_PLAIN)
   public Response test() {
      return Response.ok("PathResource").build();
   }
}
