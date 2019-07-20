package org.jboss.resteasy.test.cdi.stereotype;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.resteasy.test.cdi.stereotype.resource.MethodPathResource;
import org.jboss.resteasy.test.cdi.stereotype.resource.PathResource;
import org.jboss.resteasy.test.cdi.stereotype.resource.PathStereotype;
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
public class PathStereotypeTest
{
   private static final String DEPLOY_CLASS = "deployClass";
   private static final String DEPLOY_METHOD = "deployMethod";

   private static Client client;

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

   @Deployment(name = DEPLOY_CLASS)
   private static Archive<?> deploy()
   {
      WebArchive war = TestUtil.prepareArchive(DEPLOY_CLASS);
      war.addClasses(PathStereotype.class);
      return TestUtil.finishContainerPrepare(war, null, PathResource.class);
   }

   @Deployment(name = DEPLOY_METHOD)
   private static Archive<?> deployMethod()
   {
      WebArchive war = TestUtil.prepareArchive(DEPLOY_METHOD);
      war.addClasses(PathStereotype.class);
      return TestUtil.finishContainerPrepare(war, null, MethodPathResource.class);
   }

   @Test
   @OperateOnDeployment(DEPLOY_CLASS)
   public void testPathStereotypeResourceClassDeployed()
   {
      WebTarget base = client.target(PortProviderUtil.generateURL("/custom", DEPLOY_CLASS));
      Response response = base.path("get").request().get();

      Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
   }

   @Test
   @OperateOnDeployment(DEPLOY_METHOD)
   public void testPathStereotypeResourceMethodDeployed()
   {
      WebTarget base = client.target(PortProviderUtil.generateURL("/method", DEPLOY_METHOD));
      Response response = base.path("custom").request().get();

      Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
   }
}
