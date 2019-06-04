package org.jboss.resteasy.test.interceptor;


import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.resteasy.test.client.ClientTestBase;
import org.jboss.resteasy.test.interceptor.resource.ClientDummyMessageBodyReader;
import org.jboss.resteasy.test.interceptor.resource.ClientMessageBodyResource;
import org.jboss.resteasy.test.interceptor.resource.ClientResource;
import org.jboss.resteasy.test.interceptor.resource.CustomTestApp;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

/**
 * @tpSubChapter Interceptor
 * @tpChapter Integration tests
 * @tpTestCaseDetails Tests @Provider annotation on MessagaBodyReader (RESTEASY-2163)
 * @tpSince RESTEasy 4.0.0
 */
@RunWith(Arquillian.class)
@RunAsClient
public class ClientMessageBodyReaderRegistrationTest extends ClientTestBase {

   static Client client;

   @Deployment
   public static Archive<?> deploy() {
      WebArchive war = ShrinkWrap.create(WebArchive.class, ClientMessageBodyReaderRegistrationTest.class.getSimpleName() + ".war");
      war.addClasses(CustomTestApp.class, ClientDummyMessageBodyReader.class, ClientMessageBodyResource.class);
      return war;
   }

   @Before
   public void before() {
      client = ClientBuilder.newClient();
   }

   @After
   public void close() {
      client.close();
   }

   @Test
   public void filterRegisteredTest() throws Exception {
      WebTarget base = client.target(generateURL("/") + "testIt");
      Response response = base.request().get();
      Assert.assertEquals(200, response.getStatus());
      Assert.assertEquals(ClientDummyMessageBodyReader.MESSAGE, response.readEntity(String.class));
   }

}
