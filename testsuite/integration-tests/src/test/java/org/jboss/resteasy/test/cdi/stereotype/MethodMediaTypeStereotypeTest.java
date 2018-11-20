package org.jboss.resteasy.test.cdi.stereotype;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.resteasy.test.cdi.stereotype.resource.MediaTypeResource;
import org.jboss.resteasy.test.cdi.stereotype.resource.MethodMediaTypeResource;
import org.jboss.resteasy.test.cdi.stereotype.resource.stereotypes.MediaTypeStereotype;
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
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RunWith(Arquillian.class)
@RunAsClient
public class MethodMediaTypeStereotypeTest
{
   private static Client client;
   protected static final Logger logger = LogManager.getLogger(MethodMediaTypeStereotypeTest.class.getName());

   @BeforeClass
   public static void setup()
   {
      client = ClientBuilder.newClient();
   }

   @AfterClass
   public static void close()
   {
      client.close();
   }

   @Deployment
   private static Archive<?> deploy()
   {
      WebArchive war = TestUtil.prepareArchive(MethodMediaTypeStereotypeTest.class.getSimpleName());
      war.addClasses(MediaTypeStereotype.class);
      return TestUtil.finishContainerPrepare(war, null, MethodMediaTypeResource.class);
   }

   @Test
   public void testProducesInStereotype()
   {
      WebTarget base = client.target(generateURL("/mediatype"));
      Response response = base.path("produces").request().get();

      Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
      Assert.assertEquals("Wrong content of the response", "{}", response.readEntity(String.class));
      Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
   }

   @Test
   public void testConsumesInStereotype()
   {
      WebTarget base = client.target(generateURL("/mediatype"));
      Response response = base.path("consumes").request().header("Content-type", MediaType.APPLICATION_XML).post(Entity.xml("<>"));

      Assert.assertEquals(Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode(), response.getStatus());
   }


   private String generateURL(String path)
   {
      return PortProviderUtil.generateURL(path, MethodMediaTypeStereotypeTest.class.getSimpleName());
   }
}
