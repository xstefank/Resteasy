package org.jboss.resteasy.test.provider;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.spi.HttpResponseCodes;
import org.jboss.resteasy.utils.PortProviderUtil;
import org.jboss.resteasy.utils.TestUtil;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

@RunWith(Arquillian.class)
@RunAsClient
public class DummyProviderTest
{

   static ResteasyClient client;

   @Before
   public void init() {
      client = (ResteasyClient)ClientBuilder.newClient();
   }

   @After
   public void after() throws Exception {
      client.close();
   }

   @Deployment
   public static Archive<?> deploy() {
      WebArchive war = TestUtil.prepareArchive(DummyProviderTest.class.getSimpleName());
      war.addClass(Dummy.class);
      war.addClass(DummyProvider.class);
      return TestUtil.finishContainerPrepare(war, null, ProviderResource.class);
   }
   
   @Test
   public void test() throws Exception {
      Response response = client.target(generateURL("/provider/get")).request().get();
      Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
      Assert.assertEquals("Dummy provider foo", response.readEntity(String.class));
      response.close();
   }

   private String generateURL(String path) {
      return PortProviderUtil.generateURL(path, DummyProviderTest.class.getSimpleName());
   }
}
