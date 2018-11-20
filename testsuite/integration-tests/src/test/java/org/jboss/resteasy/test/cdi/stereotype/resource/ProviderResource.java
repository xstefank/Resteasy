package org.jboss.resteasy.test.cdi.stereotype.resource;

import org.jboss.resteasy.test.cdi.stereotype.resource.provider.Dummy;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/provider")
public class ProviderResource
{
   @Path("get")
   @GET
   public Dummy getDummy() {
      return new Dummy("Dummy", 42);
   }
}
