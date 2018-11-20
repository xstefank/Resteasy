package org.jboss.resteasy.test.cdi.stereotype;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.resteasy.test.cdi.stereotype.resource.ProviderResource;
import org.jboss.resteasy.test.cdi.stereotype.resource.TestApplication;
import org.jboss.resteasy.test.cdi.stereotype.resource.provider.ConstrainedProvider;
import org.jboss.resteasy.test.cdi.stereotype.resource.provider.Dummy;
import org.jboss.resteasy.test.cdi.stereotype.resource.stereotypes.ConstrainedToStereotype;
import org.jboss.resteasy.utils.PortProviderUtil;
import org.jboss.resteasy.utils.TestUtil;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

@RunWith(Arquillian.class)
@RunAsClient
public class ConstrainedToStereotypeTest
{
   private static Client client;
   protected static final Logger logger = LogManager.getLogger(ConstrainedToStereotypeTest.class.getName());

   @BeforeClass
   public static void setup() throws Exception
   {
      client = ClientBuilder.newClient();
   }

   @AfterClass
   public static void close()
   {
      if (client != null)
      {
         client.close();
      }
   }

   @Deployment
   private static Archive<?> deploy()
   {
      WebArchive war = TestUtil.prepareArchive(ConstrainedToStereotypeTest.class.getSimpleName());
      war.addClasses(TestApplication.class, ConstrainedToStereotype.class, Dummy.class, ConstrainedProvider.class, ProviderResource.class);
      return war;
   }

   @Test
   public void testPathStereotypeResourceDeployed()
   {
      WebTarget base = client.target(generateURL("/provider"));
      Response response = base.path("get").request().get();

      Assert.assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
      Assert.assertTrue(response.readEntity(String.class).contains(
            "Could not find MessageBodyWriter for response object of type: org.jboss.resteasy.test.cdi.stereotype.resource.provider.Dummy of media type: application/octet-stream"));
   }

   private String generateURL(String path)
   {
      return PortProviderUtil.generateURL(path, ConstrainedToStereotypeTest.class.getSimpleName());
   }
}
