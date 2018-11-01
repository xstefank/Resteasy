package org.jboss.resteasy.test.provider;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("provider")
public class ProviderResource
{
   
   @GET
   @Path("get")
   public Dummy getDummy() {
      return new Dummy("Dummy", 42);
   }
}
