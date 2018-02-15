package org.jboss.resteasy.test.stereotype.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/mediatype")
@MediaTypeStereotype
public class MediaTypeResource
{

   @GET
   @Path("/produces")
   public Response produces()
   {
      return Response.ok("{}").build();
   }

}
