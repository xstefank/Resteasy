package org.jboss.resteasy.test.cdi.stereotype.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/preMatch")
public class PreMatchingResource
{
   @Path("get")
   @GET
   public String get() {
      return "preMatch success";
   }
}
