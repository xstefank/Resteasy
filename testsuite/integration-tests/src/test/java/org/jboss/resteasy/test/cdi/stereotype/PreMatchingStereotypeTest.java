package org.jboss.resteasy.test.cdi.stereotype;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.resteasy.test.cdi.stereotype.resource.PreMatchingResource;
import org.jboss.resteasy.test.cdi.stereotype.resource.ProviderResource;
import org.jboss.resteasy.test.cdi.stereotype.resource.TestApplication;
import org.jboss.resteasy.test.cdi.stereotype.resource.provider.Dummy;
import org.jboss.resteasy.test.cdi.stereotype.resource.provider.DummyProvider;
import org.jboss.resteasy.test.cdi.stereotype.resource.provider.PreMatchingProvider;
import org.jboss.resteasy.test.cdi.stereotype.resource.stereotypes.PreMatchingStereotype;
import org.jboss.resteasy.test.cdi.stereotype.resource.stereotypes.ProviderStereotype;
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
public class PreMatchingStereotypeTest
{
   private static Client client;
   protected static final Logger logger = LogManager.getLogger(PreMatchingStereotypeTest.class.getName());

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
      WebArchive war = TestUtil.prepareArchive(PreMatchingStereotypeTest.class.getSimpleName());
      war.addClasses(TestApplication.class, PreMatchingStereotype.class, PreMatchingProvider.class, PreMatchingResource.class);
      return war;
   }

   @Test
   public void testPathStereotypeResourceDeployed()
   {
      WebTarget base = client.target(generateURL("/something/different"));
      Response response = base.request().get();

      Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
      Assert.assertEquals("preMatch success", response.readEntity(String.class));
   }

   private String generateURL(String path)
   {
      return PortProviderUtil.generateURL(path, PreMatchingStereotypeTest.class.getSimpleName());
   }
}
