package org.jboss.resteasy.test.interceptor.resource;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class ClientResponseFilterImpl implements ClientResponseFilter
{

   @Override
   public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException
   {
      responseContext.setStatus(456);
   }
}
