package org.jboss.resteasy.test.cdi.stereotype;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.resteasy.test.cdi.stereotype.resource.MethodPathResource;
import org.jboss.resteasy.test.cdi.stereotype.resource.stereotypes.MethodPathStereotype;
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
public class MethodPathStereotypeTest
{
   private static Client client;
   protected static final Logger logger = LogManager.getLogger(MethodPathStereotypeTest.class.getName());

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
      WebArchive war = TestUtil.prepareArchive(MethodPathStereotypeTest.class.getSimpleName());
      war.addClasses(MethodPathStereotype.class);
      return TestUtil.finishContainerPrepare(war, null, MethodPathResource.class);
   }

   @Test
   public void testPathStereotypeResourceDeployed()
   {
      WebTarget base = client.target(generateURL("/custom"));
      Response response = base.path("get").request().get();

      Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
   }

   private String generateURL(String path)
   {
      return PortProviderUtil.generateURL(path, MethodPathStereotypeTest.class.getSimpleName());
   }
}
