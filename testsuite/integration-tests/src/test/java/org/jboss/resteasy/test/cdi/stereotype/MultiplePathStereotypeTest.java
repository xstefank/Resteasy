package org.jboss.resteasy.test.cdi.stereotype;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.test.cdi.stereotype.resource.PathResource;
import org.jboss.resteasy.test.cdi.stereotype.resource.PathResource2;
import org.jboss.resteasy.test.cdi.stereotype.resource.TestApplication;
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
public class MultiplePathStereotypeTest
{
   private static Client client;
   protected static final Logger logger = LogManager.getLogger(MultiplePathStereotypeTest.class.getName());

   @ArquillianResource
   private Deployer deployer;

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

   @Deployment(name = "deployA")
   private static Archive<?> deployA()
   {
      WebArchive war = TestUtil.prepareArchive("deployA");
      war.addClasses(PathStereotype.class);
      return TestUtil.finishContainerPrepare(war, null, PathResource.class);
   }

   @Deployment(name = "deployB", managed = false)
   private static Archive<?> deployB()
   {
      WebArchive war = TestUtil.prepareArchive("deployB");
      war.addClasses(TestApplication.class);
      war.addClasses(PathStereotype.class);
      return TestUtil.finishContainerPrepare(war, null, PathResource2.class);
   }

   @Test
   @OperateOnDeployment("deployA")
   public void testPathStereotypeResourceDeployed()
   {
      deployer.deploy("deployB");

      WebTarget baseDeployA = client.target(PortProviderUtil.generateURL("/custom", "deployA"));
      Response responseA = baseDeployA.path("get").request().get();

      Assert.assertEquals(Response.Status.OK.getStatusCode(), responseA.getStatus());
      Assert.assertEquals("PathResource", responseA.readEntity(String.class));

      WebTarget baseDeployB = client.target(PortProviderUtil.generateURL("/custom", "deployB"));
      Response responseB = baseDeployB.path("get").request().get();

      Assert.assertEquals(Response.Status.OK.getStatusCode(), responseB.getStatus());
      Assert.assertEquals("PathResource2", responseB.readEntity(String.class));

      deployer.undeploy("deployB");
   }

}
