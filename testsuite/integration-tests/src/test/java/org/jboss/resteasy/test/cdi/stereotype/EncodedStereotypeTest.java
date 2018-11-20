package org.jboss.resteasy.test.cdi.stereotype;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.resteasy.test.cdi.stereotype.resource.EncodedResource;
import org.jboss.resteasy.test.cdi.stereotype.resource.PathResource;
import org.jboss.resteasy.test.cdi.stereotype.resource.TestApplication;
import org.jboss.resteasy.test.cdi.stereotype.resource.stereotypes.EncodedStereotype;
import org.jboss.resteasy.test.cdi.stereotype.resource.stereotypes.PathStereotype;
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
public class EncodedStereotypeTest
{
   private static Client client;
   protected static final Logger logger = LogManager.getLogger(EncodedStereotypeTest.class.getName());

   @BeforeClass
   public static void setup()
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
      WebArchive war = TestUtil.prepareArchive(EncodedStereotypeTest.class.getSimpleName());
      war.addClasses(EncodedStereotype.class);
      return TestUtil.finishContainerPrepare(war, null, EncodedResource.class);
   }

   @Test
   public void testEncodedStereotypeResource()
   {
      WebTarget base = client.target(generateURL("/encoded"));
      Response response = base.path("hello%20world").request().get();

      Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
      Assert.assertEquals("hello%20world", response.readEntity(String.class));
   }

   private String generateURL(String path)
   {
      return PortProviderUtil.generateURL(path, EncodedStereotypeTest.class.getSimpleName());
   }
}
